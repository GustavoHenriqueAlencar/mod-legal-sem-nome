package com.occultum.common.altar;

import com.occultum.common.registry.ModBlocks;
import com.occultum.common.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.WallSkullBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class AltarArtifactEvaluator {

    public record Modifiers(int capMultiplier, int rechargeMultiplier, int rangeMultiplier, int upgradeLevel) {}

    public static Modifiers evaluate(Level level, List<BlockPos> altarBaseBlocks) {
        // Categoria A: Crânios
        int skullBonus = 0; // 1 = Esqueleto, 2 = Wither, 3 = Jogador

        // Categoria B: Luz
        int lightBonus = 0; // 1 = Tocha, 2 = Candelabro

        // Categoria C: Cálice
        int chaliceBonus = 0; // 1 = Vazio, 2 = Cheio

        // Categoria D: Athame
        boolean hasAthame = false;

        // Categoria E: Ramo Místico
        boolean hasMysticBranch = false;

        // Categoria F: Pentáculo de Koboldita
        boolean hasKobolditePentacle = false;

        // Categoria G: Ovo do Infinito
        boolean hasInfiniteEgg = false;

        for (BlockPos basePos : altarBaseBlocks) {
            BlockPos topPos = basePos.above();
            BlockState state = level.getBlockState(topPos);

            // Verificação de Grassper que pode estar segurando um item de artefato
            BlockEntity be = level.getBlockEntity(topPos);
            ItemStack heldItem = ItemStack.EMPTY;
            if (be instanceof GrassperBlockEntity grassper) {
                heldItem = grassper.getHeldItem();
            }

            // Categoria G: Ovo do Infinito
            if (state.is(ModBlocks.INFINITE_EGG_BLOCK.get()) || heldItem.is(ModItems.INFINITE_EGG.get())) {
                hasInfiniteEgg = true;
            }

            // Categoria F: Pentáculo de Koboldita
            if (state.is(ModBlocks.KOBOLDITE_PENTACLE_BLOCK.get()) || heldItem.is(ModItems.KOBOLDITE_PENTACLE.get())) {
                hasKobolditePentacle = true;
            }

            // Categoria E: Ramo Místico
            if (heldItem.is(ModItems.MYSTIC_BRANCH.get())) {
                hasMysticBranch = true;
            }

            // Categoria D: Athame
            if (heldItem.is(ModItems.ATHAME.get())) {
                hasAthame = true;
            }

            // Categoria C: Cálice
            if (state.is(ModBlocks.CHALICE_FILLED.get()) || heldItem.is(ModItems.CHALICE_FILLED.get())) {
                chaliceBonus = Math.max(chaliceBonus, 2);
            } else if (state.is(ModBlocks.CHALICE.get()) || heldItem.is(ModItems.CHALICE.get())) {
                chaliceBonus = Math.max(chaliceBonus, 1);
            }

            // Categoria B: Fontes de Luz Cerimonial
            if (state.is(ModBlocks.CANDELABRA.get())) {
                lightBonus = Math.max(lightBonus, 2);
            } else if (state.is(Blocks.TORCH) || state.is(Blocks.WALL_TORCH) || state.is(Blocks.SOUL_TORCH) || state.is(Blocks.SOUL_WALL_TORCH)) {
                lightBonus = Math.max(lightBonus, 1);
            }

            // Categoria A: Crânios e Cabeças
            if (state.is(Blocks.PLAYER_HEAD) || state.is(Blocks.PLAYER_WALL_HEAD)) {
                skullBonus = Math.max(skullBonus, 3);
            } else if (state.is(Blocks.WITHER_SKELETON_SKULL) || state.is(Blocks.WITHER_SKELETON_WALL_SKULL)) {
                skullBonus = Math.max(skullBonus, 2);
            } else if (state.is(Blocks.SKELETON_SKULL) || state.is(Blocks.SKELETON_WALL_SKULL)) {
                skullBonus = Math.max(skullBonus, 1);
            }
        }

        // 1. Inicialização
        int mRec = 1;
        int mCap = 1;
        int mAlc = 1;
        int nAprim = 0;

        // 2. Bônus Aditivos
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

        // 3. Multiplicador de Pentáculo de Koboldita
        if (hasKobolditePentacle) {
            mRec *= 2;
        }

        // 4. Multiplicador de Ovo do Infinito
        if (hasInfiniteEgg) {
            mRec *= 10;
            mCap *= 10;
        }

        return new Modifiers(mCap, mRec, mAlc, nAprim);
    }
}
