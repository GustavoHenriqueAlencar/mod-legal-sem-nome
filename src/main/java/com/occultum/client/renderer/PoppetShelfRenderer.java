package com.occultum.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.occultum.client.model.PoppetShelfModel;
import com.occultum.common.poppet.PoppetShelfBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class PoppetShelfRenderer implements BlockEntityRenderer<PoppetShelfBlockEntity> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("occultum", "textures/block/poppet_shelf.png");

    private final PoppetShelfModel model;

    public PoppetShelfRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new PoppetShelfModel(context.bakeLayer(PoppetShelfModel.LAYER_LOCATION));
    }

    @Override
    public void render(PoppetShelfBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        VertexConsumer buffer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        this.model.renderToBuffer(poseStack, buffer, packedLight, packedOverlay, 0xFFFFFFFF);
        poseStack.popPose();

        // Renderizar os 9 slots da matriz 3x3 sobre a prateleira
        for (int i = 0; i < blockEntity.getContainerSize(); i++) {
            ItemStack stack = blockEntity.getItem(i);
            if (!stack.isEmpty()) {
                int row = i / 3;
                int col = i % 3;
                poseStack.pushPose();
                poseStack.translate(0.25D + col * 0.25D, 0.35D + row * 0.18D, 0.25D + row * 0.22D);
                poseStack.scale(0.32F, 0.32F, 0.32F);
                Minecraft.getInstance().getItemRenderer().renderStatic(
                        stack, ItemDisplayContext.FIXED, packedLight, packedOverlay,
                        poseStack, bufferSource, blockEntity.getLevel(), i
                );
                poseStack.popPose();
            }
        }
    }
}
