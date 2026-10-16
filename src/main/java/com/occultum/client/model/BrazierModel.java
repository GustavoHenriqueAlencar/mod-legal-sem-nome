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
 * Modelo geométrico do Braseiro Ritualístico (Brazier).
 *
 * NOTA PARA O ARTISTA 3D:
 * Substitua createBodyLayer() pela malha exportada do Blockbench (.bbmodel).
 */
public class BrazierModel extends Model {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("occultum", "brazier"), "main");

    private final ModelPart root;

    public BrazierModel(ModelPart root) {
        super(RenderType::entityCutoutNoCull);
        this.root = root;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition part = mesh.getRoot();
        part.addOrReplaceChild("stand",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.0F, 0.0F, -4.0F, 8.0F, 9.0F, 8.0F)
                        .texOffs(0, 17).addBox(-6.0F, 9.0F, -6.0F, 12.0F, 5.0F, 12.0F),
                PartPose.ZERO);
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        root.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
