package com.occultum.common.poppet;

import com.occultum.common.fetish.FetishBlockEntity;
import com.occultum.common.poppet.PoppetItem.PoppetType;
import com.occultum.common.registry.ModDataComponentTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * Gerenciador hierárquico global de Poppets (Inventário do Jogador -> Prateleiras de Poppets carregadas
 * no Overworld, Nether, End e Mundo Espiritual) conforme Seção 3 e Seção 4.1 de 06_POPPETS_E_FETISHES.md.
 */
public final class PoppetHandler {

    private PoppetHandler() {}

    public record FoundPoppet(ItemStack stack, @Nullable PoppetShelfBlockEntity shelf, int slot, ServerLevel level, BlockPos pos) {
        public void consumeDurability(int points) {
            boolean enhanced = FetishBlockEntity.hasPoppetEnhancementNearby(level, pos);
            int actualPoints = enhanced ? Math.max(1, (int) (points * 0.66D)) : points;
            int nextDmg = stack.getDamageValue() + actualPoints;
            if (nextDmg >= stack.getMaxDamage()) {
                stack.shrink(1);
                if (shelf != null) {
                    shelf.setItem(slot, ItemStack.EMPTY);
                    shelf.sync();
                }
                level.playSound(null, pos, SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 1.0F, 1.0F);
            } else {
                stack.setDamageValue(nextDmg);
                if (shelf != null) {
                    shelf.sync();
                }
            }
        }
    }

    public static void consumePoppetDurability(ItemStack stack, int points, ServerLevel level, BlockPos pos) {
        boolean enhanced = FetishBlockEntity.hasPoppetEnhancementNearby(level, pos);
        int actualPoints = enhanced ? Math.max(1, (int) (points * 0.66D)) : points;
        int nextDmg = stack.getDamageValue() + actualPoints;
        if (nextDmg >= stack.getMaxDamage()) {
            stack.shrink(1);
            level.playSound(null, pos, SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 1.0F, 1.0F);
        } else {
            stack.setDamageValue(nextDmg);
        }
    }

    /**
     * Busca hierárquica global interdimensional:
     * 1. Inventário do Jogador.
     * 2. Todas as Prateleiras de Poppets carregadas no servidor (qualquer dimensão).
     */
    @Nullable
    public static FoundPoppet findBoundPoppet(@Nullable MinecraftServer server, Player player, PoppetType type) {
        if (!(player.level() instanceof ServerLevel playerLevel)) return null;

        // 1. Inventário do Jogador
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack s = player.getInventory().getItem(i);
            if (s.getItem() instanceof PoppetItem pi && pi.getPoppetType() == type && PoppetItem.isBoundTo(s, player)) {
                return new FoundPoppet(s, null, i, playerLevel, player.blockPosition());
            }
        }

        // 2. Prateleiras de Poppets em todas as dimensões carregadas do servidor
        if (server != null) {
            for (ServerLevel dimLevel : server.getAllLevels()) {
                Set<BlockPos> shelves = PoppetShelfBlockEntity.getShelvesInDimension(dimLevel.dimension());
                for (BlockPos pos : shelves) {
                    if (dimLevel.getBlockEntity(pos) instanceof PoppetShelfBlockEntity shelf) {
                        for (int slot = 0; slot < shelf.getContainerSize(); slot++) {
                            ItemStack s = shelf.getItem(slot);
                            if (s.getItem() instanceof PoppetItem pi && pi.getPoppetType() == type && PoppetItem.isBoundTo(s, player)) {
                                return new FoundPoppet(s, shelf, slot, dimLevel, pos);
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * Intercepta danos recebidos pelo jogador (Terra, Água, Fogo, Fome, Vampírico e Morte).
     * Retorna true se o dano foi integralmente cancelado por um Poppet.
     */
    public static boolean onPlayerIncomingDamage(Player player, DamageSource source, float amount) {
        if (player.level().isClientSide || !(player.level() instanceof ServerLevel serverLevel)) {
            return false;
        }
        MinecraftServer server = serverLevel.getServer();

        // 9. Poppet Vampírico: Redireciona 100% do dano do portador para a vítima do 2º Taglock (25 pts por coração = 12.5 pts por ponto)
        FoundPoppet vampiric = findBoundPoppet(server, player, PoppetType.VAMPIRIC);
        if (vampiric != null) {
            String victimSig = vampiric.stack().get(ModDataComponentTypes.SECONDARY_DNA_SIGNATURE.get());
            if (victimSig != null && !victimSig.isEmpty() && !victimSig.equals(player.getUUID().toString())) {
                ItemStack tempTag = TaglockKitItem.createFilledTaglock(victimSig, "");
                LivingEntity victim = TaglockKitItem.resolveTarget(server, tempTag);
                if (victim != null && victim.isAlive()) {
                    victim.hurt(serverLevel.damageSources().magic(), amount);
                    int durabilityCost = Math.max(5, Math.round(amount * 12.5F));
                    vampiric.consumeDurability(durabilityCost);
                    serverLevel.playSound(null, player.blockPosition(), SoundEvents.SOUL_ESCAPE.value(), SoundSource.PLAYERS, 1.0F, 1.0F);
                    return true;
                }
            }
        }

        // 1. Poppet de Proteção da Terra (Queda livre ou bigorna) -> 1000 pts
        if (source.is(DamageTypeTags.IS_FALL) || source.is(DamageTypes.FALLING_ANVIL) || source.is(DamageTypes.FALLING_BLOCK)) {
            FoundPoppet earth = findBoundPoppet(server, player, PoppetType.EARTH_PROTECTION);
            if (earth != null) {
                player.resetFallDistance();
                earth.consumeDurability(1000);
                return true;
            }
        }

        // 2. Poppet de Proteção da Água (Afogamento) -> Restaura 100% ar + 15s Respiração Aquática, 1000 pts
        if (source.is(DamageTypeTags.IS_DROWNING)) {
            FoundPoppet water = findBoundPoppet(server, player, PoppetType.WATER_PROTECTION);
            if (water != null) {
                player.setAirSupply(player.getMaxAirSupply());
                player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 300, 0));
                water.consumeDurability(1000);
                return true;
            }
        }

        // 3. Poppet de Proteção do Fogo (Fogo e Lava) -> Extingue chamas + 30s Resistência ao Fogo, 1000 pts
        if (source.is(DamageTypeTags.IS_FIRE)) {
            FoundPoppet fire = findBoundPoppet(server, player, PoppetType.FIRE_PROTECTION);
            if (fire != null) {
                player.clearFire();
                player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 600, 0));
                fire.consumeDurability(1000);
                return true;
            }
        }

        // 4. Poppet de Proteção da Fome (Inanição) -> Restaura 6 fome + 6 saturação, 1000 pts
        if (source.is(DamageTypes.STARVE)) {
            FoundPoppet hunger = findBoundPoppet(server, player, PoppetType.HUNGER_PROTECTION);
            if (hunger != null) {
                player.getFoodData().eat(6, 0.5F);
                hunger.consumeDurability(1000);
                return true;
            }
        }

        // 6. Poppet de Proteção da Morte (Dano Fatal que reduziria a vida <= 0) -> Restaura 10 vida + 5s Regen II e Resist II, 1000 pts
        if (player.getHealth() - amount <= 0.0F) {
            FoundPoppet death = findBoundPoppet(server, player, PoppetType.DEATH_PROTECTION);
            if (death != null) {
                player.setHealth(10.0F);
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 1));
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100, 1));
                death.consumeDurability(1000);
                serverLevel.playSound(null, player.blockPosition(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.0F, 1.0F);
                return true;
            }
        }

        return false;
    }

    /**
     * Verifica periodicamente se alguma ferramenta em mãos (Poppet #5) ou peça de armadura equipada (Poppet #11)
     * atingiu >= 90% de dano e restaura instantaneamente para 100% de durabilidade, consumindo o Poppet (1000 pts).
     */
    public static void checkEquipmentProtectionPoppets(Player player) {
        if (player.level().isClientSide || !(player.level() instanceof ServerLevel serverLevel)) return;
        MinecraftServer server = serverLevel.getServer();

        // 5. Poppet de Proteção de Ferramenta (mão principal e secundária)
        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND}) {
            ItemStack held = player.getItemBySlot(slot);
            if (!held.isEmpty() && held.isDamageableItem() && !(held.getItem() instanceof PoppetItem)) {
                if (held.getDamageValue() >= held.getMaxDamage() * 0.90D) {
                    FoundPoppet toolPoppet = findBoundPoppet(server, player, PoppetType.TOOL_PROTECTION);
                    if (toolPoppet != null) {
                        held.setDamageValue(0);
                        toolPoppet.consumeDurability(1000);
                        serverLevel.playSound(null, player.blockPosition(), SoundEvents.ANVIL_USE, SoundSource.PLAYERS, 0.8F, 1.2F);
                    }
                }
            }
        }

        // 11. Poppet de Proteção de Armadura (4 slots de armadura)
        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            ItemStack armor = player.getItemBySlot(slot);
            if (!armor.isEmpty() && armor.isDamageableItem()) {
                if (armor.getDamageValue() >= armor.getMaxDamage() * 0.90D) {
                    FoundPoppet armorPoppet = findBoundPoppet(server, player, PoppetType.ARMOR_PROTECTION);
                    if (armorPoppet != null) {
                        armor.setDamageValue(0);
                        armorPoppet.consumeDurability(1000);
                        serverLevel.playSound(null, player.blockPosition(), SoundEvents.ANVIL_USE, SoundSource.PLAYERS, 0.8F, 1.3F);
                    }
                }
            }
        }
    }

    /**
     * 7. Poppet Anti-Vodu (Voodoo Protection):
     * Intercepta ataques de Poppet de Vodu ou maldições direcionadas (50 a 100 pts por golpe).
     * Se o atacante possuir um Gato Familiar ativo, há 35% de chance de penetrar a proteção (Seção 4.2.1 de 13_BRASEIRO).
     */
    public static boolean tryInterceptVoodooOrCurse(@Nullable MinecraftServer server, Player victim, int durabilityDamage, boolean attackerHasCatFamiliar) {
        FoundPoppet antiVoodoo = findBoundPoppet(server, victim, PoppetType.VOODOO_PROTECTION);
        if (antiVoodoo != null) {
            antiVoodoo.consumeDurability(durabilityDamage);
            antiVoodoo.level().playSound(null, antiVoodoo.pos(), SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 1.0F, 1.1F);
            if (attackerHasCatFamiliar && victim.level().random.nextFloat() < 0.35F) {
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * 10. Poppet de Proteção de Poppets (Poppet Protection) vs. Ritual #56 (Curse Poppets).
     */
    public static void degradeTargetPoppets(@Nullable MinecraftServer server, Player victim, int damageAmount) {
        FoundPoppet guard = findBoundPoppet(server, victim, PoppetType.POPPET_PROTECTION);
        if (guard != null) {
            guard.consumeDurability(100);
            return;
        }
        for (int i = 0; i < victim.getInventory().getContainerSize(); i++) {
            ItemStack s = victim.getInventory().getItem(i);
            if (s.getItem() instanceof PoppetItem && PoppetItem.isBoundTo(s, victim)) {
                int next = s.getDamageValue() + damageAmount;
                if (next >= s.getMaxDamage()) {
                    victim.getInventory().setItem(i, ItemStack.EMPTY);
                } else {
                    s.setDamageValue(next);
                }
            }
        }
    }
}
