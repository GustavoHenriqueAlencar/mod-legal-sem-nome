package com.occultum.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.occultum.client.model.AltarModel;
import com.occultum.common.altar.AltarBlock;
import com.occultum.common.altar.AltarBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class AltarRenderer implements BlockEntityRenderer<AltarBlockEntity> {
    private static final ResourceLocation CLOTH_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("occultum", "textures/block/altar_cloth.png");

    private final AltarModel model;

    public AltarRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new AltarModel(context.bakeLayer(AltarModel.LAYER_LOCATION));
    }

    @Override
    public void render(AltarBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        // Renderiza o manto cerimonial apenas se o bloco de altar estiver formado
        if (blockEntity.getBlockState().getValue(AltarBlock.FORMED)) {
            poseStack.pushPose();
            poseStack.translate(0.5D, 0.0D, 0.5D);
            VertexConsumer buffer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(CLOTH_TEXTURE));
            this.model.renderToBuffer(poseStack, buffer, packedLight, packedOverlay, 0xFFFFFFFF);
            poseStack.popPose();
        }
    }
}
