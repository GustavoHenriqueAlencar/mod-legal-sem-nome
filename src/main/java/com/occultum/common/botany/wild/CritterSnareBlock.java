package com.occultum.common.botany.wild;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class CritterSnareBlock extends BaseEntityBlock {
    public static final MapCodec<CritterSnareBlock> CODEC = simpleCodec(CritterSnareBlock::new);
    private static final VoxelShape SHAPE = box(1.0D, 0.0D, 1.0D, 15.0D, 4.0D, 15.0D);

    public CritterSnareBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CritterSnareBlockEntity(pos, state);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof CritterSnareBlockEntity snare && !snare.hasCritter()) {
                if (entity instanceof Bat) {
                    snare.setCapturedCritter("bat");
                    entity.discard();
                    level.playSound(null, pos, SoundEvents.BAT_TAKEOFF, SoundSource.BLOCKS, 1.0F, 1.0F);
                } else if (entity instanceof Frog) {
                    snare.setCapturedCritter("frog");
                    entity.discard();
                    level.playSound(null, pos, SoundEvents.FROG_AMBIENT, SoundSource.BLOCKS, 1.0F, 1.0F);
                } else if (entity instanceof Silverfish) {
                    snare.setCapturedCritter("silverfish");
                    entity.discard();
                    level.playSound(null, pos, SoundEvents.SILVERFISH_AMBIENT, SoundSource.BLOCKS, 1.0F, 1.0F);
                }
            }
        }
        super.entityInside(state, level, pos, entity);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof CritterSnareBlockEntity snare) {
            if (snare.hasCritter()) {
                if (!level.isClientSide) {
                    String critter = snare.getCapturedCritter();
                    player.displayClientMessage(Component.literal("§aCriatura libertada: " + critter), true);
                    if ("bat".equals(critter)) {
                        Bat bat = EntityType.BAT.create(level);
                        if (bat != null) {
                            bat.moveTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 0, 0);
                            level.addFreshEntity(bat);
                        }
                    } else if ("frog".equals(critter)) {
                        Frog frog = EntityType.FROG.create(level);
                        if (frog != null) {
                            frog.moveTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 0, 0);
                            level.addFreshEntity(frog);
                        }
                    } else if ("silverfish".equals(critter)) {
                        Silverfish sf = EntityType.SILVERFISH.create(level);
                        if (sf != null) {
                            sf.moveTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 0, 0);
                            level.addFreshEntity(sf);
                        }
                    }
                    snare.setCapturedCritter("");
                }
                return InteractionResult.SUCCESS;
            } else {
                if (!level.isClientSide) {
                    player.displayClientMessage(Component.literal("§7Armadilha de Criaturas vazia."), true);
                }
            }
        }
        return super.useWithoutItem(state, level, pos, player, hitResult);
    }
}
