package com.occultum.common.botany.tree;

import com.occultum.common.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Supplier;

public class MysticLeavesBlock extends LeavesBlock {
    private final Supplier<? extends Item> saplingSupplier;
    private final boolean isRowan;

    public MysticLeavesBlock(Properties properties, Supplier<? extends Item> saplingSupplier, boolean isRowan) {
        super(properties);
        this.saplingSupplier = saplingSupplier;
        this.isRowan = isRowan;
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide && !player.isCreative()) {
            // Chance de 1 em 20 (5%) de muda
            if (level.random.nextFloat() < 0.05F) {
                popResource(level, pos, new ItemStack(saplingSupplier.get()));
            }

            // Folhas de Sorveira: 1 em 200 (0.5%) de Bagas de Sorveira (Rowan Berries)
            if (isRowan && level.random.nextFloat() < 0.005F) {
                popResource(level, pos, new ItemStack(ModItems.ROWAN_BERRIES.get()));
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }
}
