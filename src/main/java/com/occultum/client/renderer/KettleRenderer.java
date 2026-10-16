package com.occultum.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.occultum.client.model.KettleModel;
import com.occultum.common.machine.kettle.KettleBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class KettleRenderer implements BlockEntityRenderer<KettleBlockEntity> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("occultum", "textures/block/kettle.png");

    private final KettleModel model;

    public KettleRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new KettleModel(context.bakeLayer(KettleModel.LAYER_LOCATION));
    }

    @Override
    public void render(KettleBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        VertexConsumer buffer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        int color = blockEntity.hasWater() ? (0xFF000000 | blockEntity.getLiquidColor()) : 0xFFFFFFFF;
        this.model.renderToBuffer(poseStack, buffer, packedLight, packedOverlay, color);
        poseStack.popPose();
    }
}
