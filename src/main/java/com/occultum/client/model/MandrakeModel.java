package com.occultum.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.occultum.common.entity.MandrakeEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;

/**
 * Modelo geométrico da Criatura Mandrágora Desperta.
 *
 * NOTA PARA O ARTISTA 3D:
 * 1. Cole aqui a estrutura de cubos e animações exportada do Blockbench.
 * 2. As partes principais (cabeça com folhas, corpo tuberário e perninhas) estão mapeadas abaixo.
 */
public class MandrakeModel extends EntityModel<MandrakeEntity> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("occultum", "mandrake"), "main");

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart body;

    public MandrakeModel(ModelPart root) {
        this.root = root;
        this.body = root.getChild("body");
        this.head = body.getChild("head");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition partDefinition = mesh.getRoot();

        PartDefinition body = partDefinition.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-3.0F, -6.0F, -3.0F, 6.0F, 6.0F, 6.0F),
                PartPose.offset(0.0F, 24.0F, 0.0F));

        body.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 12)
                        .addBox(-4.0F, -12.0F, -4.0F, 8.0F, 6.0F, 8.0F)
                        // Folhagem no topo da cabeça
                        .texOffs(24, 0)
                        .addBox(-2.0F, -16.0F, -2.0F, 4.0F, 4.0F, 4.0F),
                PartPose.ZERO);

        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public void setupAnim(MandrakeEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.head.yRot = netHeadYaw * ((float) Math.PI / 180F);
        this.head.xRot = headPitch * ((float) Math.PI / 180F);
        // Animação de caminhada trêmula
        this.body.zRot = (float) Math.sin(limbSwing * 1.5F) * 0.15F * limbSwingAmount;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        root.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
