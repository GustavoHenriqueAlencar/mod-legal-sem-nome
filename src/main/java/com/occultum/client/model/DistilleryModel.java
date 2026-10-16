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
 * Modelo geométrico da Destilaria Alquímica (Distillery).
 *
 * NOTA PARA O ARTISTA 3D:
 * Substitua createBodyLayer() pela malha exportada do Blockbench (.bbmodel).
 */
public class DistilleryModel extends Model {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("occultum", "distillery"), "main");

    private final ModelPart root;

    public DistilleryModel(ModelPart root) {
        super(RenderType::entityCutoutNoCull);
        this.root = root;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition part = mesh.getRoot();
        part.addOrReplaceChild("alembic",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-6.0F, 0.0F, -6.0F, 12.0F, 8.0F, 12.0F)
                        .texOffs(0, 20).addBox(-3.0F, 8.0F, -3.0F, 6.0F, 7.0F, 6.0F),
                PartPose.ZERO);
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        root.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
