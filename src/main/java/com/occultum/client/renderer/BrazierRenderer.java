package com.occultum.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.occultum.client.model.BrazierModel;
import com.occultum.common.machine.brazier.BrazierBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class BrazierRenderer implements BlockEntityRenderer<BrazierBlockEntity> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("occultum", "textures/block/brazier.png");

    private final BrazierModel model;

    public BrazierRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new BrazierModel(context.bakeLayer(BrazierModel.LAYER_LOCATION));
    }

    @Override
    public void render(BrazierBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        VertexConsumer buffer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        this.model.renderToBuffer(poseStack, buffer, packedLight, packedOverlay, 0xFFFFFFFF);
        poseStack.popPose();

        // Render up to 3 inserted ingredients inside the brazier bowl
        for (int i = 0; i < 3; i++) {
            ItemStack item = blockEntity.getSlot(i);
            if (!item.isEmpty()) {
                poseStack.pushPose();
                double angle = i * (Math.PI * 2.0 / 3.0);
                poseStack.translate(0.5D + Math.cos(angle) * 0.14D, 0.88D, 0.5D + Math.sin(angle) * 0.14D);
                poseStack.scale(0.35F, 0.35F, 0.35F);
                Minecraft.getInstance().getItemRenderer().renderStatic(
                        item, ItemDisplayContext.FIXED, packedLight, packedOverlay,
                        poseStack, bufferSource, blockEntity.getLevel(), i
                );
                poseStack.popPose();
            }
        }
    }
}
