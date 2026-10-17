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
 * Modelos Geométricos Desacoplados para os BlockEntities da Sprint 4 (`BloodCrucible` e `MysticMirror`).
 */
public class Sprint4BlockModels {

    public static class BloodCrucibleModel extends Model {
        public static final ModelLayerLocation LAYER_LOCATION =
                new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("occultum", "blood_crucible"), "main");
        private final ModelPart root;

        public BloodCrucibleModel(ModelPart root) {
            super(RenderType::entityCutoutNoCull);
            this.root = root;
        }

        public static LayerDefinition createBodyLayer() {
            MeshDefinition mesh = new MeshDefinition();
            PartDefinition part = mesh.getRoot();
            part.addOrReplaceChild("base",
                    CubeListBuilder.create().texOffs(0, 0).addBox(-6.0F, 0.0F, -6.0F, 12.0F, 12.0F, 12.0F),
                    PartPose.ZERO);
            return LayerDefinition.create(mesh, 64, 32);
        }

        @Override
        public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
            root.render(poseStack, buffer, packedLight, packedOverlay, color);
        }
    }

    public static class MysticMirrorModel extends Model {
        public static final ModelLayerLocation LAYER_LOCATION =
                new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("occultum", "mystic_mirror"), "main");
        private final ModelPart root;

        public MysticMirrorModel(ModelPart root) {
            super(RenderType::entityTranslucent);
            this.root = root;
        }

        public static LayerDefinition createBodyLayer() {
            MeshDefinition mesh = new MeshDefinition();
            PartDefinition part = mesh.getRoot();
            part.addOrReplaceChild("frame",
                    CubeListBuilder.create().texOffs(0, 0).addBox(-7.0F, 0.0F, 6.0F, 14.0F, 30.0F, 2.0F),
                    PartPose.ZERO);
            return LayerDefinition.create(mesh, 64, 64);
        }

        @Override
        public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
            root.render(poseStack, buffer, packedLight, packedOverlay, color);
        }
    }
}
