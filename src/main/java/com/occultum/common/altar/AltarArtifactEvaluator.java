package com.occultum.common.altar;

import com.occultum.common.registry.ModBlocks;
import com.occultum.common.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class AltarArtifactEvaluator {

    public record Modifiers(int capMultiplier, int rechargeMultiplier, int rangeMultiplier, int upgradeLevel) {}

    public static Modifiers evaluate(Level level, List<BlockPos> altarBaseBlocks) {
        // Bônus aditivos por categoria (apenas o maior de cada categoria conta)
        int skullBonus = 0;   // Esqueleto (+1 rec, +1 cap), Wither (+2 rec, +2 cap), Jogador (+3 rec, +3 cap)
        int lightBonus = 0;   // Tocha (+1 rec), Candelabro (+2 rec)
        int chaliceBonus = 0; // Vazio (+1 cap), Cheio com Sopa de Redstone (+2 cap)
        boolean hasAthame = false;            // +1 em M_alc
        boolean hasMysticBranch = false;      // +1 em N_aprim (exclusivamente mystic_branch)
        boolean hasKobolditePentacle = false; // M_rec = M_rec * 2
        boolean hasInfiniteEgg = false;       // M_rec = M_rec * 10 e M_cap = M_cap * 10

        for (BlockPos basePos : altarBaseBlocks) {
            BlockPos topPos = basePos.above();
            BlockState state = level.getBlockState(topPos);

            BlockEntity be = level.getBlockEntity(topPos);
            ItemStack heldItem = ItemStack.EMPTY;
            if (be instanceof GrassperBlockEntity grassper) {
                heldItem = grassper.getHeldItem();
            }

            // Verifica também itens posicionados em molduras ou pousados sobre o bloco do altar
            AABB topBox = new AABB(topPos);
            List<ItemFrame> frames = level.getEntitiesOfClass(ItemFrame.class, topBox);
            List<ItemEntity> droppedItems = level.getEntitiesOfClass(ItemEntity.class, topBox);

            // Categoria G: Ovo do Infinito
            if (state.is(ModBlocks.INFINITE_EGG_BLOCK.get()) || matchesItem(heldItem, frames, droppedItems, ModItems.INFINITE_EGG.get())) {
                hasInfiniteEgg = true;
            }

            // Categoria F: Pentáculo de Koboldita
            if (state.is(ModBlocks.KOBOLDITE_PENTACLE_BLOCK.get()) || matchesItem(heldItem, frames, droppedItems, ModItems.KOBOLDITE_PENTACLE.get())) {
                hasKobolditePentacle = true;
            }

            // Categoria E: Ramo Místico (mystic_branch, e NÃO mutating_sprig)
            if (matchesItem(heldItem, frames, droppedItems, ModItems.MYSTIC_BRANCH.get())) {
                hasMysticBranch = true;
            }

            // Categoria D: Athame sobre o altar
            if (matchesItem(heldItem, frames, droppedItems, ModItems.ATHAME.get())) {
                hasAthame = true;
            }

            // Categoria C: Cálice (Vazio: +1 cap, Cheio com Sopa de Redstone: +2 cap)
            if (state.is(ModBlocks.CHALICE_FILLED.get()) || matchesItem(heldItem, frames, droppedItems, ModItems.CHALICE_FILLED.get())) {
                chaliceBonus = Math.max(chaliceBonus, 2);
            } else if (state.is(ModBlocks.CHALICE.get()) || matchesItem(heldItem, frames, droppedItems, ModItems.CHALICE.get())) {
                chaliceBonus = Math.max(chaliceBonus, 1);
            }

            // Categoria B: Luz (Tocha: +1 rec, Candelabro: +2 rec)
            if (state.is(ModBlocks.CANDELABRA.get()) || matchesItem(heldItem, frames, droppedItems, ModItems.CANDELABRA.get())) {
                lightBonus = Math.max(lightBonus, 2);
            } else if (state.is(Blocks.TORCH) || state.is(Blocks.WALL_TORCH) || state.is(Blocks.SOUL_TORCH) || state.is(Blocks.SOUL_WALL_TORCH)) {
                lightBonus = Math.max(lightBonus, 1);
            }

            // Categoria A: Cabeças (Esqueleto: +1/+1, Wither: +2/+2, Jogador: +3/+3)
            if (state.is(Blocks.PLAYER_HEAD) || state.is(Blocks.PLAYER_WALL_HEAD)) {
                skullBonus = Math.max(skullBonus, 3);
            } else if (state.is(Blocks.WITHER_SKELETON_SKULL) || state.is(Blocks.WITHER_SKELETON_WALL_SKULL)) {
                skullBonus = Math.max(skullBonus, 2);
            } else if (state.is(Blocks.SKELETON_SKULL) || state.is(Blocks.SKELETON_WALL_SKULL)) {
                skullBonus = Math.max(skullBonus, 1);
            }
        }

        // 1. Inicialização: M_rec = 1, M_cap = 1, M_alc = 1, N_aprim = 0
        int mRec = 1;
        int mCap = 1;
        int mAlc = 1;
        int nAprim = 0;

        // 2. Soma dos bônus aditivos por categoria (apenas o maior de cada categoria)
        mRec += skullBonus;
        mCap += skullBonus;

        mRec += lightBonus;

        mCap += chaliceBonus;

        if (hasAthame) {
            mAlc += 1;
        }

        if (hasMysticBranch) {
            nAprim += 1;
        }

        // 3. Multiplicadores finais aplicados após as somas
        if (hasKobolditePentacle) {
            mRec = mRec * 2;
        }

        if (hasInfiniteEgg) {
            mRec = mRec * 10;
            mCap = mCap * 10;
        }

        return new Modifiers(mCap, mRec, mAlc, nAprim);
    }

    private static boolean matchesItem(ItemStack heldItem, List<ItemFrame> frames, List<ItemEntity> droppedItems, net.minecraft.world.item.Item target) {
        if (!heldItem.isEmpty() && heldItem.is(target)) {
            return true;
        }
        for (ItemFrame frame : frames) {
            if (frame.getItem().is(target)) {
                return true;
            }
        }
        for (ItemEntity itemEntity : droppedItems) {
            if (itemEntity.getItem().is(target)) {
                return true;
            }
        }
        return false;
    }
}
