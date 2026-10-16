package com.occultum.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.occultum.client.model.SpinningWheelModel;
import com.occultum.common.machine.spinningwheel.SpinningWheelBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class SpinningWheelRenderer implements BlockEntityRenderer<SpinningWheelBlockEntity> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("occultum", "textures/block/spinning_wheel.png");

    private final SpinningWheelModel model;

    public SpinningWheelRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new SpinningWheelModel(context.bakeLayer(SpinningWheelModel.LAYER_LOCATION));
    }

    @Override
    public void render(SpinningWheelBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        VertexConsumer buffer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        this.model.renderToBuffer(poseStack, buffer, packedLight, packedOverlay, 0xFFFFFFFF);
        poseStack.popPose();
    }
}
