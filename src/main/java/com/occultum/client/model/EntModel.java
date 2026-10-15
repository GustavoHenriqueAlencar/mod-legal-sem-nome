package com.occultum.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.occultum.common.entity.EntEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;

/**
 * Modelo geométrico do Guardião da Floresta (Ent).
 *
 * NOTA PARA O ARTISTA 3D:
 * O Ent possui uma silhueta alta e robusta (tronco maciço, braços de galhos pesados e raízes/pernas).
 * Substitua createBodyLayer() pelos blocos esculpidos no Blockbench.
 */
public class EntModel extends EntityModel<EntEntity> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("occultum", "ent"), "main");

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart rightArm;
    private final ModelPart leftArm;

    public EntModel(ModelPart root) {
        this.root = root;
        this.head = root.getChild("head");
        this.rightArm = root.getChild("right_arm");
        this.leftArm = root.getChild("left_arm");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition partDefinition = mesh.getRoot();

        // Tronco imponente de madeira
        partDefinition.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-8.0F, -28.0F, -6.0F, 16.0F, 28.0F, 12.0F),
                PartPose.offset(0.0F, 12.0F, 0.0F));

        // Cabeça vegetal com folhagem
        partDefinition.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(56, 0)
                        .addBox(-6.0F, -40.0F, -6.0F, 12.0F, 12.0F, 12.0F),
                PartPose.offset(0.0F, 12.0F, 0.0F));

        // Braços de galhos pesados
        partDefinition.addOrReplaceChild("right_arm",
                CubeListBuilder.create()
                        .texOffs(56, 24)
                        .addBox(-5.0F, 0.0F, -3.0F, 5.0F, 26.0F, 6.0F),
                PartPose.offset(-8.0F, -14.0F, 0.0F));

        partDefinition.addOrReplaceChild("left_arm",
                CubeListBuilder.create()
                        .texOffs(78, 24)
                        .addBox(0.0F, 0.0F, -3.0F, 5.0F, 26.0F, 6.0F),
                PartPose.offset(8.0F, -14.0F, 0.0F));

        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public void setupAnim(EntEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.head.yRot = netHeadYaw * ((float) Math.PI / 180F);
        this.head.xRot = headPitch * ((float) Math.PI / 180F);
        // Passos pesados e balanço de galhos
        this.rightArm.xRot = (float) Math.cos(limbSwing * 0.6662F + Math.PI) * 1.4F * limbSwingAmount;
        this.leftArm.xRot = (float) Math.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        root.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
