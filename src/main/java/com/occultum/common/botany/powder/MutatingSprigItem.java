package com.occultum.common.botany.powder;

import com.occultum.common.altar.GrassperBlockEntity;
import com.occultum.common.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.TrappedChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class MutatingSprigItem extends Item {

    public MutatingSprigItem(Properties properties) {
        super(properties.durability(15));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState targetState = level.getBlockState(pos);
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        boolean success = false;

        // 1. Grama <-> Vitória-Régia Direta (Seção 6.2.1)
        if (targetState.is(Blocks.GRASS_BLOCK)) {
            level.setBlock(pos, Blocks.LILY_PAD.defaultBlockState(), 3);
            success = true;
        } else if (targetState.is(Blocks.LILY_PAD)) {
            level.setBlock(pos, Blocks.GRASS_BLOCK.defaultBlockState(), 3);
            success = true;
        }

        // 2. Transmutação de Argila Fluvial (Seção 6.2.2)
        else if (targetState.is(Blocks.DIRT) && level.getFluidState(pos.above()).is(Fluids.WATER)) {
            level.setBlock(pos, Blocks.CLAY.defaultBlockState(), 3);
            for (Direction dir : Direction.Plane.HORIZONTAL) {
                BlockPos nPos = pos.relative(dir);
                if (level.getBlockState(nPos).is(Blocks.DIRT)) {
                    level.setBlock(nPos, Blocks.CLAY.defaultBlockState(), 3);
                }
            }
            success = true;
        }

        // 3. Baú Sanguessuga (Leech Chest) (Seção 6.2.3)
        // Centro: Trapped Chest vazio. 4 vizinhos horizontais: Vinhas. 4 vizinhos Y-1: Água.
        else if (targetState.is(Blocks.TRAPPED_CHEST)) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof TrappedChestBlockEntity chest && chest.isEmpty()) {
                boolean vines = true;
                boolean waters = true;
                for (Direction dir : Direction.Plane.HORIZONTAL) {
                    if (!level.getBlockState(pos.relative(dir)).is(Blocks.VINE)) vines = false;
                    if (!level.getFluidState(pos.relative(dir).below()).is(Fluids.WATER)) waters = false;
                }
                if (vines && waters) {
                    for (Direction dir : Direction.Plane.HORIZONTAL) {
                        level.removeBlock(pos.relative(dir), false);
                    }
                    level.setBlock(pos, ModBlocks.LEECH_CHEST.get().defaultBlockState(), 3);
                    success = true;
                }
            }
        }

        // 4. Preensor de Grama (Grassper) (Seção 6.2.4)
        // Centro: Baú Comum vazio. 4 vizinhos: Grama. Y-1: Água.
        else if (targetState.is(Blocks.CHEST)) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof ChestBlockEntity chest && chest.isEmpty()) {
                boolean grass = true;
                for (Direction dir : Direction.Plane.HORIZONTAL) {
                    if (!level.getBlockState(pos.relative(dir)).is(Blocks.SHORT_GRASS)) grass = false;
                }
                boolean waterBelow = level.getFluidState(pos.below()).is(Fluids.WATER);
                if (grass && waterBelow) {
                    level.removeBlock(pos, false);
                    level.removeBlock(pos.below(), false);
                    for (Direction dir : Direction.Plane.HORIZONTAL) {
                        level.setBlock(pos.relative(dir), ModBlocks.GRASSPER.get().defaultBlockState(), 3);
                    }
                    success = true;
                }
            }
        }

        // 5. Rosa de Sangue (Blood Rose) (Seção 6.2.5)
        // Centro: Baú Sanguessuga vazio. 4 vizinhos: Papoulas. Y-1: Água.
        else if (targetState.is(ModBlocks.LEECH_CHEST.get())) {
            boolean poppies = true;
            for (Direction dir : Direction.Plane.HORIZONTAL) {
                if (!level.getBlockState(pos.relative(dir)).is(Blocks.POPPY)) poppies = false;
            }
            boolean waterBelow = level.getFluidState(pos.below()).is(Fluids.WATER);
            if (poppies && waterBelow) {
                level.removeBlock(pos, false);
                for (Direction dir : Direction.Plane.HORIZONTAL) {
                    level.setBlock(pos.relative(dir), ModBlocks.BLOOD_ROSE.get().defaultBlockState(), 3);
                }
                success = true;
            }
        }

        // 6. Armadilha de Criaturas (Critter Snare) (Seção 6.2.6)
        // Centro: Cobweb. 4 vizinhos: Mudas de Amieiro. Y-1: Água. Mob: 1 Zumbi em 1 bloco.
        else if (targetState.is(Blocks.COBWEB)) {
            boolean saplings = true;
            for (Direction dir : Direction.Plane.HORIZONTAL) {
                if (!level.getBlockState(pos.relative(dir)).is(ModBlocks.ALDER_SAPLING.get())) saplings = false;
            }
            boolean waterBelow = level.getFluidState(pos.below()).is(Fluids.WATER);
            List<Zombie> zombies = level.getEntitiesOfClass(Zombie.class, new AABB(pos).inflate(1.5));

            if (saplings && waterBelow && !zombies.isEmpty()) {
                zombies.get(0).discard(); // Sacrifício do Zumbi
                level.removeBlock(pos, false);
                for (Direction dir : Direction.Plane.HORIZONTAL) {
                    level.setBlock(pos.relative(dir), ModBlocks.CRITTER_SNARE.get().defaultBlockState(), 3);
                }
                success = true;
            }
        }

        // 7. Losna / Absinto Selvagem (Wormwood) (Seção 6.2.7)
        // Centro: Trigo maduro estágio 7. 4 vizinhos: Algodão Fantasmagórico. Diagonais Y-1: Água.
        else if (targetState.is(Blocks.WHEAT) && targetState.hasProperty(CropBlock.AGE) && targetState.getValue(CropBlock.AGE) == 7) {
            boolean cotton = true;
            for (Direction dir : Direction.Plane.HORIZONTAL) {
                if (!level.getBlockState(pos.relative(dir)).is(ModBlocks.WISPY_COTTON.get())) cotton = false;
            }
            if (cotton) {
                for (Direction dir : Direction.Plane.HORIZONTAL) {
                    level.removeBlock(pos.relative(dir), false);
                }
                level.setBlock(pos, ModBlocks.WORMWOOD_CROP.get().defaultBlockState(), 3);
                success = true;
            }
        }

        // 8. Espinhos do Vazio (Seção 6.2.8)
        // Centro: Cogumelo Gigante Vermelho. 4 vizinhos: Musgo Espanhol. 4 diagonais com Grasspers segurando Ender Pearl.
        else if (targetState.is(Blocks.RED_MUSHROOM_BLOCK)) {
            boolean moss = true;
            for (Direction dir : Direction.Plane.HORIZONTAL) {
                if (!level.getBlockState(pos.relative(dir)).is(ModBlocks.SPANISH_MOSS.get())) moss = false;
            }

            BlockPos[] diags = new BlockPos[]{pos.north().east(), pos.north().west(), pos.south().east(), pos.south().west()};
            boolean grasspersWithPearls = true;
            for (BlockPos dPos : diags) {
                BlockEntity dBe = level.getBlockEntity(dPos);
                if (dBe instanceof GrassperBlockEntity g && g.getHeldItem().is(Items.ENDER_PEARL)) {
                    // ok
                } else {
                    grasspersWithPearls = false;
                }
            }

            if (moss && grasspersWithPearls) {
                for (BlockPos dPos : diags) {
                    if (level.getBlockEntity(dPos) instanceof GrassperBlockEntity g) {
                        g.setHeldItem(ItemStack.EMPTY);
                    }
                }
                for (Direction dir : Direction.Plane.HORIZONTAL) {
                    level.removeBlock(pos.relative(dir), false);
                }
                for (int dy = 0; dy < 16; dy++) {
                    BlockPos up = pos.above(dy);
                    if (level.getBlockState(up).is(Blocks.RED_MUSHROOM_BLOCK)) {
                        level.setBlock(up, ModBlocks.VOID_BRAMBLE.get().defaultBlockState(), 3);
                    }
                }
                success = true;
            }
        }

        if (success) {
            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.WITCH, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 20, 0.4, 0.4, 0.4, 0.05);
            }
            level.playSound(null, pos, SoundEvents.SLIME_BLOCK_PLACE, SoundSource.PLAYERS, 1.2F, 1.0F);

            if (player != null && !player.isCreative()) {
                stack.hurtAndBreak(1, player, net.minecraft.world.entity.EquipmentSlot.MAINHAND);
            }
            return InteractionResult.SUCCESS;
        }

        return super.useOn(context);
    }
}
