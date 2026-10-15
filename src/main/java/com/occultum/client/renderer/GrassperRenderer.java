package com.occultum.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.occultum.common.altar.GrassperBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class GrassperRenderer implements BlockEntityRenderer<GrassperBlockEntity> {

    public GrassperRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(GrassperBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        ItemStack held = blockEntity.getHeldItem();
        if (!held.isEmpty()) {
            poseStack.pushPose();
            // Centraliza o item sobre o topo do preensor de grama
            poseStack.translate(0.5D, 0.9D, 0.5D);
            poseStack.scale(0.65F, 0.65F, 0.65F);

            // Rotação sutil
            long time = blockEntity.getLevel() != null ? blockEntity.getLevel().getGameTime() : 0;
            float rotation = (time + partialTick) * 2.0F;
            poseStack.mulPose(Axis.YP.rotationDegrees(rotation));

            Minecraft.getInstance().getItemRenderer().renderStatic(
                    held,
                    ItemDisplayContext.FIXED,
                    packedLight,
                    packedOverlay,
                    poseStack,
                    bufferSource,
                    blockEntity.getLevel(),
                    0
            );

            poseStack.popPose();
        }
    }
}
