package com.occultum.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

/**
 * Modelo geométrico do Altar Cerimonial.
 *
 * NOTA PARA O ARTISTA 3D:
 * Este modelo contém a estrutura base do Altar com o manto cerimonial.
 * Ao exportar seu modelo customizado do Blockbench:
 * 1. Substitua o método createBodyLayer() pela definição da malha exportada.
 * 2. Atualize os nomes das partes e mapeamentos de textura conforme seu arquivo .bbmodel.
 */
public class AltarModel extends Model {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("occultum", "altar"), "main");

    private final ModelPart root;

    public AltarModel(ModelPart root) {
        super(RenderType::entityCutoutNoCull);
        this.root = root;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition partDefinition = mesh.getRoot();

        // Placeholder limpo: base de pedra do altar e manto cerimonial vermelho
        partDefinition.addOrReplaceChild("base",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-8.0F, 0.0F, -8.0F, 16.0F, 12.0F, 16.0F),
                PartPose.ZERO);

        partDefinition.addOrReplaceChild("cloth",
                CubeListBuilder.create()
                        .texOffs(0, 28)
                        .addBox(-8.5F, 11.5F, -8.5F, 17.0F, 2.0F, 17.0F),
                PartPose.ZERO);

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        root.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
