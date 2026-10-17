package com.occultum.common.spirit;

import com.occultum.common.dimension.OccultumDimensions;
import com.occultum.common.entity.CorpseEntity;
import com.occultum.common.entity.NightmareEntity;
import com.occultum.common.network.OccultumNetwork;
import com.occultum.common.registry.ModAttachmentTypes;
import com.occultum.common.registry.ModBlocks;
import com.occultum.common.registry.ModEntityTypes;
import com.occultum.common.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Gerenciador Completo do Mundo Espiritual e Sonhos (`09_MUNDO_ESPIRITUAL_E_SONHOS.md`):
 * - Projeção Astral por Poção do Sono (base P_0 = 0.998) ou Maçã Adormecida (base P_0 = 1.0).
 * - Geração do Corpo Físico Adormecido (`CorpseEntity`) no Overworld (skin, nome e HP do jogador).
 * - Separação estrita de inventário:
 *   * Ida (Overworld -> Spirit World): permite apenas `Icy Needle` e `Mutandis`.
 *   * Volta segura (Spirit World -> Overworld): permite apenas `Wispy Cotton`, `Disturbed Cotton`,
 *     `Mellifluous Hunger`, `Icy Needle`, `Brew of Flowing Spirit` e `Subdued Spirit`.
 * - Varredura cúbica de raio 8 blocos (17x17x17) para cálculo exato de Bom Sonho vs. Pesadelo (Seção 4.1)
 *   e verificação de Pesadelo Demoníaco (Seção 4.2).
 * - Retorno por Morte no Sonho, Despertar Seguro com `Icy Needle` e Manifestação Fantasma pelo Portal Espiritual 4x4.
 */
public final class SpiritWorldManager {

    private static final String NBT_PHYSICAL_INVENTORY = "OccultumPhysicalInventory";
    private static final String NBT_SPIRIT_INVENTORY = "OccultumSpiritInventory";

    private SpiritWorldManager() {}

    public static boolean isAllowedToEnterSpiritWorld(ItemStack stack) {
        return stack.is(ModItems.ICY_NEEDLE.get()) || stack.is(ModItems.MUTANDIS.get());
    }

    public static boolean isAllowedToReturnToOverworld(ItemStack stack) {
        return stack.is(ModItems.WISPY_COTTON.get())
                || stack.is(ModItems.DISTURBED_COTTON.get())
                || stack.is(ModItems.MELLIFLUOUS_HUNGER.get())
                || stack.is(ModItems.ICY_NEEDLE.get())
                || stack.is(ModItems.BREW_OF_FLOWING_SPIRIT.get())
                || stack.is(ModItems.SUBDUED_SPIRIT.get());
    }

    /**
     * Seção 4.1: Varredura em caixa cúbica de raio 8 blocos (17x17x17) centrada no corpo do jogador:
     * P_mod = P_0 - Delta_apanhador - Delta_espirito - Delta_algodao + Delta_coracao + Delta_fogo
     */
    public record NightmareEvaluation(double modifiedChance, boolean canBeDemonic, double demonicChance) {}

    public static NightmareEvaluation evaluateDreamEnvironment(ServerLevel level, BlockPos center, double baseChance) {
        int nightmareWeavers = 0;
        int flowingSpiritSources = 0;
        int wispyCottons = 0;
        int demonHearts = 0;
        int fires = 0;

        for (BlockPos p : BlockPos.betweenClosed(center.offset(-8, -8, -8), center.offset(8, 8, 8))) {
            BlockState st = level.getBlockState(p);
            if (st.is(ModBlocks.DREAM_WEAVER_NIGHTMARES.get())) {
                nightmareWeavers++;
            } else if (st.is(ModBlocks.FLOWING_SPIRIT_BLOCK.get()) && st.getFluidState().isSource()) {
                flowingSpiritSources++;
            } else if (st.is(ModBlocks.WISPY_COTTON.get())) {
                wispyCottons++;
            } else if (st.is(ModBlocks.BLOOD_CRUCIBLE.get()) || st.is(ModBlocks.BRAZIER.get())) {
                demonHearts++;
            } else if (st.is(Blocks.FIRE) || st.is(Blocks.SOUL_FIRE) || st.is(Blocks.CAMPFIRE)) {
                fires++;
            }
        }
        // Contabiliza também Corações de Demônio posicionados em molduras ou no chão dentro do raio 8
        for (net.minecraft.world.entity.Entity ent : level.getEntities(null, new net.minecraft.world.phys.AABB(center).inflate(8.0D))) {
            if (ent instanceof net.minecraft.world.entity.decoration.ItemFrame frame && frame.getItem().is(ModItems.DEMON_HEART.get())) {
                demonHearts++;
            } else if (ent instanceof net.minecraft.world.entity.item.ItemEntity itemEnt && itemEnt.getItem().is(ModItems.DEMON_HEART.get())) {
                demonHearts += itemEnt.getItem().getCount();
            }
        }

        int cappedWeavers = Math.min(1, nightmareWeavers);
        int cappedSpirit = Math.min(3, flowingSpiritSources);
        int cappedCotton = Math.min(2, wispyCottons);
        int cappedHearts = Math.min(2, demonHearts);
        int cappedFires = Math.min(3, fires);

        double pMod = baseChance
                - (cappedWeavers * 0.50D)
                - (cappedSpirit * 0.10D)
                - (cappedCotton * 0.10D)
                + (cappedHearts * 0.35D)
                + (cappedFires * 0.10D);

        boolean canBeDemonic = cappedWeavers >= 1 && cappedSpirit >= 1 && cappedHearts >= 1;
        double pDem = (cappedHearts * 0.35D) + (cappedFires * 0.10D);

        return new NightmareEvaluation(Math.max(0.0D, Math.min(1.0D, pMod)), canBeDemonic, pDem);
    }

    /**
     * Seção 3: Inicia a Projeção Astral do Overworld para o Mundo Espiritual.
     * @param forcedApple true se disparado pela Maçã Adormecida (100% Pesadelo Forçado), false se Poção do Sono (99.8% base).
     */
    public static void enterSpiritWorld(ServerPlayer player, boolean forcedApple) {
        ServerLevel currentLevel = player.serverLevel();
        if (currentLevel.dimension() == OccultumDimensions.SPIRIT_WORLD_LEVEL) {
            return;
        }
        ServerLevel spiritLevel = currentLevel.getServer().getLevel(OccultumDimensions.SPIRIT_WORLD_LEVEL);
        if (spiritLevel == null) return;

        BlockPos bodyPos = player.blockPosition();
        float bodyHealth = player.getHealth();

        // 1. Materializar o Corpo Físico Adormecido (CorpseEntity) no Overworld
        CorpseEntity corpse = ModEntityTypes.CORPSE.get().create(currentLevel);
        if (corpse != null) {
            corpse.moveTo(player.getX(), player.getY(), player.getZ(), player.getYRot(), 0.0F);
            corpse.setOwner(player);
            corpse.setHealth(bodyHealth);
            currentLevel.addFreshEntity(corpse);
        }

        // Salvar coordenadas e HP do corpo nos dados persistentes
        String returnData = bodyPos.getX() + "," + bodyPos.getY() + "," + bodyPos.getZ() + "," + bodyHealth;
        player.setData(ModAttachmentTypes.CORPSE_RETURN_DATA.get(), returnData);
        player.setData(ModAttachmentTypes.SPIRIT_GHOST_FORM.get(), false);

        // 2. Separação Estrita de Inventário (Congela Overworld e carrega Espiritual, mantendo Icy Needle e Mutandis)
        swapToSpiritInventory(player);

        // 3. Calcular Matemática de Bom Sonho vs. Pesadelo (Raio de 8 blocos)
        double baseChance = forcedApple ? 1.0D : 0.998D;
        NightmareEvaluation eval = forcedApple
                ? new NightmareEvaluation(1.0D, false, 0.0D)
                : evaluateDreamEnvironment(currentLevel, bodyPos, baseChance);

        // Bônus de proteção temporária se derrotou o Nightmare recentemente (últimos 24.000 ticks)
        long lastTriumph = player.getData(ModAttachmentTypes.NIGHTMARE_TRIUMPH_TIME.get());
        boolean recentTriumph = !forcedApple && lastTriumph > 0L && (currentLevel.getGameTime() - lastTriumph) < 24000L;
        double finalChance = recentTriumph ? Math.max(0.0D, eval.modifiedChance() - 0.50D) : eval.modifiedChance();

        boolean isNightmare = forcedApple || (currentLevel.random.nextDouble() < finalChance);
        player.setData(ModAttachmentTypes.SPIRIT_IS_NIGHTMARE.get(), isNightmare);

        // 4. Teleportar para a coordenada horizontal espelhada no Mundo Espiritual
        int targetY = spiritLevel.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, bodyPos.getX(), bodyPos.getZ());
        if (targetY < spiritLevel.getMinBuildHeight() + 5) {
            targetY = 70;
            spiritLevel.setBlock(new BlockPos(bodyPos.getX(), targetY - 1, bodyPos.getZ()), Blocks.STONE.defaultBlockState(), 3);
        }
        player.teleportTo(spiritLevel, bodyPos.getX() + 0.5D, targetY + 1.0D, bodyPos.getZ() + 0.5D, player.getYRot(), player.getXRot());
        OccultumNetwork.syncToClient(player);

        // 5. Se for Pesadelo (ou Pesadelo Demoníaco), invoca o predador correspondente
        if (isNightmare) {
            if (eval.canBeDemonic() && spiritLevel.random.nextDouble() < eval.demonicChance()) {
                // Pesadelo Demoníaco (Seção 4.2): tempestade de fogo demoníaco + Demônio Real
                for (int i = 0; i < 12; i++) {
                    BlockPos fp = player.blockPosition().offset(spiritLevel.random.nextInt(11) - 5, 0, spiritLevel.random.nextInt(11) - 5);
                    if (spiritLevel.getBlockState(fp).isAir()) {
                        spiritLevel.setBlock(fp, Blocks.FIRE.defaultBlockState(), 3);
                    }
                }
                WitherSkeleton realDemon = EntityType.WITHER_SKELETON.create(spiritLevel);
                if (realDemon != null) {
                    realDemon.setCustomName(Component.translatable("entity.occultum.demonic_nightmare"));
                    realDemon.moveTo(player.getX() + 4.0D, player.getY(), player.getZ() + 4.0D, 0.0F, 0.0F);
                    realDemon.setTarget(player);
                    spiritLevel.addFreshEntity(realDemon);
                }
                player.displayClientMessage(Component.translatable("message.occultum.spirit.demonic_nightmare").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD), false);
            } else {
                NightmareEntity nightmare = ModEntityTypes.NIGHTMARE.get().create(spiritLevel);
                if (nightmare != null) {
                    nightmare.moveTo(player.getX() + 5.0D, player.getY(), player.getZ() + 5.0D, 0.0F, 0.0F);
                    nightmare.setVictimUUID(player.getUUID());
                    nightmare.setTarget(player);
                    spiritLevel.addFreshEntity(nightmare);
                }
                player.displayClientMessage(Component.translatable("message.occultum.spirit.nightmare_started").withStyle(ChatFormatting.RED), false);
            }
        } else {
            player.displayClientMessage(Component.translatable("message.occultum.spirit.good_dream").withStyle(ChatFormatting.AQUA), false);
        }
    }

    /**
     * Seção 7.1 e 7.2: Retorno do Mundo Espiritual para o Corpo Físico no Overworld.
     * @param diedInDream true se o jogador morreu no sonho (perde itens espirituais não autorizados),
     *                    false se acordou voluntariamente com a Agulha Gélida (salva inventário espiritual e traz lista branca).
     */
    public static void returnFromSpiritWorld(ServerPlayer player, boolean diedInDream) {
        ServerLevel overworld = player.server.overworld();
        String data = player.getData(ModAttachmentTypes.CORPSE_RETURN_DATA.get());

        BlockPos returnPos = overworld.getSharedSpawnPos();
        float restoredHealth = player.getMaxHealth();

        if (data != null && !data.isEmpty()) {
            String[] parts = data.split(",");
            if (parts.length >= 4) {
                try {
                    returnPos = new BlockPos(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
                    restoredHealth = Float.parseFloat(parts[3]);
                } catch (NumberFormatException ignored) {}
            }
        }

        // Se estava em Forma Fantasma no Overworld e usou a Agulha Gélida: Seção 7.3.2 -> puxa de volta para o Mundo Espiritual!
        if (player.getData(ModAttachmentTypes.SPIRIT_GHOST_FORM.get()) && player.level().dimension() == Level.OVERWORLD && !diedInDream) {
            ServerLevel spiritLevel = player.server.getLevel(OccultumDimensions.SPIRIT_WORLD_LEVEL);
            if (spiritLevel != null) {
                player.setData(ModAttachmentTypes.SPIRIT_GHOST_FORM.get(), false);
                int y = spiritLevel.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, returnPos.getX(), returnPos.getZ());
                player.teleportTo(spiritLevel, returnPos.getX() + 0.5D, Math.max(64, y + 1.0D), returnPos.getZ() + 0.5D, player.getYRot(), player.getXRot());
                OccultumNetwork.syncToClient(player);
                return;
            }
        }

        // Restaurar inventário do Overworld + transferir itens da lista branca
        restorePhysicalInventory(player, !diedInDream);

        // Remover o Corpo Físico Adormecido (CorpseEntity) no Overworld
        for (CorpseEntity corpse : overworld.getEntitiesOfClass(CorpseEntity.class, new AABB(returnPos).inflate(32.0D))) {
            if (player.getUUID().equals(corpse.getOwnerUUID())) {
                corpse.discard();
            }
        }

        player.setData(ModAttachmentTypes.SPIRIT_GHOST_FORM.get(), false);
        player.setData(ModAttachmentTypes.CORPSE_RETURN_DATA.get(), "");

        if (player.level().dimension() != Level.OVERWORLD) {
            player.teleportTo(overworld, returnPos.getX() + 0.5D, returnPos.getY() + 0.2D, returnPos.getZ() + 0.5D, player.getYRot(), player.getXRot());
        } else {
            player.teleportTo(returnPos.getX() + 0.5D, returnPos.getY() + 0.2D, returnPos.getZ() + 0.5D);
        }

        player.setHealth(Math.max(1.0F, restoredHealth));
        OccultumNetwork.syncToClient(player);
        overworld.playSound(null, returnPos, SoundEvents.PLAYER_BREATH, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    /**
     * Seção 7.3: Travessia pelo Portal Espiritual (4x4) para Manifestação Fantasma no Overworld.
     */
    public static void manifestAsGhostInOverworld(ServerPlayer player) {
        ServerLevel overworld = player.server.overworld();
        BlockPos pos = player.blockPosition();
        int targetY = overworld.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos.getX(), pos.getZ());

        // Em Forma Fantasma, mantém apenas Agulhas Gélidas e Fetiches Vinculados
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack st = player.getInventory().getItem(i);
            if (!st.isEmpty() && !isAllowedInGhostForm(st)) {
                player.drop(st.copy(), false);
                player.getInventory().setItem(i, ItemStack.EMPTY);
            }
        }

        player.setData(ModAttachmentTypes.SPIRIT_GHOST_FORM.get(), true);
        player.teleportTo(overworld, pos.getX() + 0.5D, Math.max(64, targetY + 1.0D), pos.getZ() + 0.5D, player.getYRot(), player.getXRot());
        player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 12000, 0, false, false));
        OccultumNetwork.syncToClient(player);
        player.displayClientMessage(Component.translatable("message.occultum.spirit.ghost_manifested").withStyle(ChatFormatting.AQUA), false);
    }

    private static boolean isAllowedInGhostForm(ItemStack stack) {
        return stack.is(ModItems.ICY_NEEDLE.get())
                || stack.is(ModItems.SCARECROW_FETISH.get())
                || stack.is(ModItems.TRENT_EFFIGY.get())
                || stack.is(ModItems.WITCHS_LADDER.get());
    }

    private static void swapToSpiritInventory(ServerPlayer player) {
        HolderLookup.Provider registries = player.registryAccess();
        CompoundTag persistent = player.getPersistentData();

        List<ItemStack> crossingItems = new ArrayList<>();
        ListTag physicalTag = new ListTag();

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty()) {
                if (isAllowedToEnterSpiritWorld(stack)) {
                    crossingItems.add(stack.copy());
                } else {
                    CompoundTag slotTag = new CompoundTag();
                    slotTag.putInt("Slot", i);
                    slotTag.put("Item", stack.save(registries));
                    physicalTag.add(slotTag);
                }
                player.getInventory().setItem(i, ItemStack.EMPTY);
            }
        }
        persistent.put(NBT_PHYSICAL_INVENTORY, physicalTag);

        // Carregar inventário espiritual anterior
        if (persistent.contains(NBT_SPIRIT_INVENTORY, Tag.TAG_LIST)) {
            ListTag spiritTag = persistent.getList(NBT_SPIRIT_INVENTORY, Tag.TAG_COMPOUND);
            for (int i = 0; i < spiritTag.size(); i++) {
                CompoundTag slotTag = spiritTag.getCompound(i);
                int slot = slotTag.getInt("Slot");
                ItemStack.parse(registries, slotTag.getCompound("Item")).ifPresent(st -> {
                    if (slot >= 0 && slot < player.getInventory().getContainerSize()) {
                        player.getInventory().setItem(slot, st);
                    }
                });
            }
        }

        // Adicionar itens que atravessaram (Icy Needle e Mutandis)
        for (ItemStack cross : crossingItems) {
            if (!player.getInventory().add(cross)) {
                player.drop(cross, false);
            }
        }
    }

    private static void restorePhysicalInventory(ServerPlayer player, boolean preserveSpiritInventory) {
        HolderLookup.Provider registries = player.registryAccess();
        CompoundTag persistent = player.getPersistentData();

        List<ItemStack> returningAllowed = new ArrayList<>();
        ListTag spiritTag = new ListTag();

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty()) {
                if (preserveSpiritInventory && isAllowedToReturnToOverworld(stack)) {
                    returningAllowed.add(stack.copy());
                } else if (preserveSpiritInventory) {
                    CompoundTag slotTag = new CompoundTag();
                    slotTag.putInt("Slot", i);
                    slotTag.put("Item", stack.save(registries));
                    spiritTag.add(slotTag);
                }
                player.getInventory().setItem(i, ItemStack.EMPTY);
            }
        }

        if (preserveSpiritInventory) {
            persistent.put(NBT_SPIRIT_INVENTORY, spiritTag);
        } else {
            persistent.remove(NBT_SPIRIT_INVENTORY);
        }

        // Restaurar inventário do Overworld
        if (persistent.contains(NBT_PHYSICAL_INVENTORY, Tag.TAG_LIST)) {
            ListTag physicalTag = persistent.getList(NBT_PHYSICAL_INVENTORY, Tag.TAG_COMPOUND);
            for (int i = 0; i < physicalTag.size(); i++) {
                CompoundTag slotTag = physicalTag.getCompound(i);
                int slot = slotTag.getInt("Slot");
                ItemStack.parse(registries, slotTag.getCompound("Item")).ifPresent(st -> {
                    if (slot >= 0 && slot < player.getInventory().getContainerSize()) {
                        player.getInventory().setItem(slot, st);
                    }
                });
            }
            persistent.remove(NBT_PHYSICAL_INVENTORY);
        }

        // Adicionar os itens da lista branca trazidos do Mundo Espiritual
        for (ItemStack allowed : returningAllowed) {
            if (!player.getInventory().add(allowed)) {
                player.drop(allowed, false);
            }
        }
    }
}
