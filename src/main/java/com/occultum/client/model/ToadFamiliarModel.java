package com.occultum.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.occultum.common.entity.ToadFamiliarEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;

/**
 * Modelo geométrico do Sapo Familiar (Toad Familiar).
 */
public class ToadFamiliarModel extends EntityModel<ToadFamiliarEntity> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("occultum", "toad_familiar"), "main");

    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart head;

    public ToadFamiliarModel(ModelPart root) {
        this.root = root;
        this.body = root.getChild("body");
        this.head = body.getChild("head");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition part = mesh.getRoot();

        PartDefinition body = part.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-3.5F, -5.0F, -4.0F, 7.0F, 5.0F, 8.0F),
                PartPose.offset(0.0F, 24.0F, 0.0F));

        body.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 13).addBox(-3.0F, -7.0F, -6.0F, 6.0F, 4.0F, 5.0F)
                        .texOffs(22, 13).addBox(-2.5F, -8.5F, -5.0F, 2.0F, 2.0F, 2.0F)
                        .texOffs(22, 17).addBox(0.5F, -8.5F, -5.0F, 2.0F, 2.0F, 2.0F),
                PartPose.ZERO);

        return LayerDefinition.create(mesh, 32, 32);
    }

    @Override
    public void setupAnim(ToadFamiliarEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.head.yRot = netHeadYaw * ((float) Math.PI / 180F);
        this.head.xRot = headPitch * ((float) Math.PI / 180F);
        this.body.y = 24.0F - Math.abs((float) Math.sin(limbSwing * 1.2F)) * 3.0F * limbSwingAmount;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        root.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
