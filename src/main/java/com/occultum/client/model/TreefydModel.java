package com.occultum.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.occultum.common.entity.TreefydEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;

/**
 * Modelo geométrico do Guardião Vegetal Bípede (Treefyd).
 *
 * NOTA PARA O ARTISTA 3D:
 * O Treefyd é uma planta carnívora bípede com chicote vegetal.
 * Substitua createBodyLayer() pela malha do Blockbench.
 */
public class TreefydModel extends EntityModel<TreefydEntity> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("occultum", "treefyd"), "main");

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

    public TreefydModel(ModelPart root) {
        this.root = root;
        this.head = root.getChild("head");
        this.rightLeg = root.getChild("right_leg");
        this.leftLeg = root.getChild("left_leg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition partDefinition = mesh.getRoot();

        // Tronco central
        partDefinition.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-4.0F, -18.0F, -4.0F, 8.0F, 18.0F, 8.0F),
                PartPose.offset(0.0F, 12.0F, 0.0F));

        // Cabeça com mandíbula/corola
        partDefinition.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(32, 0)
                        .addBox(-5.0F, -26.0F, -5.0F, 10.0F, 8.0F, 10.0F),
                PartPose.offset(0.0F, 12.0F, 0.0F));

        // Duas pernas de raiz para locomoção bípede
        partDefinition.addOrReplaceChild("right_leg",
                CubeListBuilder.create()
                        .texOffs(0, 26)
                        .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                PartPose.offset(-3.0F, 12.0F, 0.0F));

        partDefinition.addOrReplaceChild("left_leg",
                CubeListBuilder.create()
                        .texOffs(16, 26)
                        .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                PartPose.offset(3.0F, 12.0F, 0.0F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(TreefydEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.head.yRot = netHeadYaw * ((float) Math.PI / 180F);
        this.head.xRot = headPitch * ((float) Math.PI / 180F);

        // Marcha bípede de raízes
        this.rightLeg.xRot = (float) Math.cos(limbSwing * 0.6662F) * 1.0F * limbSwingAmount;
        this.leftLeg.xRot = (float) Math.cos(limbSwing * 0.6662F + Math.PI) * 1.0F * limbSwingAmount;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        root.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
