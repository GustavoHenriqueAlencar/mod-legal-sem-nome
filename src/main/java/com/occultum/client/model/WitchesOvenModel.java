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
 * Modelo geométrico do Forno da Bruxa (Witches' Oven).
 *
 * NOTA PARA O ARTISTA 3D:
 * Substitua createBodyLayer() pela malha exportada do Blockbench (.bbmodel).
 */
public class WitchesOvenModel extends Model {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("occultum", "witches_oven"), "main");

    private final ModelPart root;

    public WitchesOvenModel(ModelPart root) {
        super(RenderType::entityCutoutNoCull);
        this.root = root;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition part = mesh.getRoot();
        part.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-7.0F, 0.0F, -7.0F, 14.0F, 12.0F, 14.0F)
                        .texOffs(0, 26).addBox(-2.0F, 12.0F, 2.0F, 4.0F, 4.0F, 4.0F),
                PartPose.ZERO);
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        root.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
