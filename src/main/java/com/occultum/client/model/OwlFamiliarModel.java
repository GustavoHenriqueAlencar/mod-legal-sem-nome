package com.occultum.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.occultum.common.entity.OwlFamiliarEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;

/**
 * Modelo geométrico da Coruja Familiar (Owl Familiar).
 */
public class OwlFamiliarModel extends EntityModel<OwlFamiliarEntity> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("occultum", "owl_familiar"), "main");

    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart leftWing;
    private final ModelPart rightWing;

    public OwlFamiliarModel(ModelPart root) {
        this.root = root;
        this.body = root.getChild("body");
        this.head = body.getChild("head");
        this.leftWing = body.getChild("left_wing");
        this.rightWing = body.getChild("right_wing");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition part = mesh.getRoot();

        PartDefinition body = part.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-3.0F, -9.0F, -3.0F, 6.0F, 8.0F, 6.0F),
                PartPose.offset(0.0F, 24.0F, 0.0F));

        body.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 14).addBox(-3.5F, -14.0F, -3.5F, 7.0F, 5.0F, 6.0F)
                        .texOffs(20, 14).addBox(-1.0F, -11.5F, -4.5F, 2.0F, 2.0F, 1.0F),
                PartPose.ZERO);

        body.addOrReplaceChild("left_wing",
                CubeListBuilder.create()
                        .texOffs(24, 0).addBox(3.0F, -8.5F, -2.5F, 1.0F, 7.0F, 5.0F),
                PartPose.ZERO);

        body.addOrReplaceChild("right_wing",
                CubeListBuilder.create()
                        .texOffs(24, 0).addBox(-4.0F, -8.5F, -2.5F, 1.0F, 7.0F, 5.0F),
                PartPose.ZERO);

        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public void setupAnim(OwlFamiliarEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.head.yRot = netHeadYaw * ((float) Math.PI / 180F);
        this.head.xRot = headPitch * ((float) Math.PI / 180F);
        float flap = !entity.onGround() ? (float) Math.sin(ageInTicks * 0.6F) * 0.45F : 0.0F;
        this.leftWing.zRot = -flap;
        this.rightWing.zRot = flap;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        root.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
