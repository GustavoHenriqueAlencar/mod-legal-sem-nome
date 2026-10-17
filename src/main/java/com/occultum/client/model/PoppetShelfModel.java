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
 * Modelo geométrico da Prateleira de Poppets (Poppet Shelf).
 */
public class PoppetShelfModel extends Model {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("occultum", "poppet_shelf"), "main");

    private final ModelPart root;

    public PoppetShelfModel(ModelPart root) {
        super(RenderType::entityCutoutNoCull);
        this.root = root;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition part = mesh.getRoot();
        part.addOrReplaceChild("shelf",
                CubeListBuilder.create()
                        // Base da prateleira
                        .texOffs(0, 0).addBox(-8.0F, 0.0F, -8.0F, 16.0F, 4.0F, 16.0F)
                        // Degraus internos para os 9 Poppets
                        .texOffs(0, 20).addBox(-7.0F, 4.0F, -7.0F, 14.0F, 3.0F, 5.0F)
                        .texOffs(0, 28).addBox(-7.0F, 7.0F, -7.0F, 14.0F, 3.0F, 2.5F),
                PartPose.ZERO);
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        root.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
