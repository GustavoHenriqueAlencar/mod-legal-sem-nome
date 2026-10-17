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
 * Modelo geométrico dos Fetiches (Scarecrow, Trent Effigy, Witch's Ladder).
 */
public class FetishModel extends Model {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("occultum", "fetish"), "main");

    private final ModelPart root;

    public FetishModel(ModelPart root) {
        super(RenderType::entityCutoutNoCull);
        this.root = root;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition part = mesh.getRoot();
        part.addOrReplaceChild("totem",
                CubeListBuilder.create()
                        // Poste central
                        .texOffs(0, 0).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 14.0F, 4.0F)
                        // Braços cruzados
                        .texOffs(16, 0).addBox(-7.0F, 8.0F, -1.5F, 14.0F, 3.0F, 3.0F)
                        // Cabeça do espantalho/efígie
                        .texOffs(0, 18).addBox(-3.5F, 11.0F, -3.5F, 7.0F, 5.0F, 7.0F),
                PartPose.ZERO);
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        root.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
