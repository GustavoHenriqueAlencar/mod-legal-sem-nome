package com.occultum.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.occultum.client.model.CrystalBallModel;
import com.occultum.common.divination.CrystalBallBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class CrystalBallRenderer implements BlockEntityRenderer<CrystalBallBlockEntity> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("occultum", "textures/block/crystal_ball.png");

    private final CrystalBallModel model;

    public CrystalBallRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new CrystalBallModel(context.bakeLayer(CrystalBallModel.LAYER_LOCATION));
    }

    @Override
    public void render(CrystalBallBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        VertexConsumer buffer = bufferSource.getBuffer(RenderType.entityTranslucent(TEXTURE));
        this.model.renderToBuffer(poseStack, buffer, packedLight, packedOverlay, 0xDDFFFFFF);
        poseStack.popPose();
    }
}
