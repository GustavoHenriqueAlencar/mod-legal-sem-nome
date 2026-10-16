package com.occultum.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.occultum.client.model.WitchesCauldronModel;
import com.occultum.common.machine.cauldron.WitchesCauldronBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class WitchesCauldronRenderer implements BlockEntityRenderer<WitchesCauldronBlockEntity> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("occultum", "textures/block/witches_cauldron.png");

    private final WitchesCauldronModel model;

    public WitchesCauldronRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new WitchesCauldronModel(context.bakeLayer(WitchesCauldronModel.LAYER_LOCATION));
    }

    @Override
    public void render(WitchesCauldronBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        VertexConsumer buffer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        int color = blockEntity.getWaterMb() > 0 ? (0xFF000000 | blockEntity.getColorRgb()) : 0xFFFFFFFF;
        this.model.renderToBuffer(poseStack, buffer, packedLight, packedOverlay, color);
        poseStack.popPose();
    }
}
