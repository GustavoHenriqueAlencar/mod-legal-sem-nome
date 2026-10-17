package com.occultum.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

/**
 * Modelo Humanoide/Bípede Desacoplado para Blockbench (Elle, Lilith, VampireServant, Wolfman,
 * HornedHuntsman, Nightmare, LordOfTorment, ReflectionDemon, FairestFollower).
 */
public class OccultumHumanoidModel<T extends Entity> extends EntityModel<T> {

    public static final ModelLayerLocation ELLE_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("occultum", "elle"), "main");
    public static final ModelLayerLocation LILITH_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("occultum", "lilith"), "main");
    public static final ModelLayerLocation VAMPIRE_SERVANT_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("occultum", "vampire_servant"), "main");
    public static final ModelLayerLocation WOLFMAN_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("occultum", "wolfman"), "main");
    public static final ModelLayerLocation HORNED_HUNTSMAN_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("occultum", "horned_huntsman"), "main");
    public static final ModelLayerLocation NIGHTMARE_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("occultum", "nightmare"), "main");
    public static final ModelLayerLocation LORD_OF_TORMENT_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("occultum", "lord_of_torment"), "main");
    public static final ModelLayerLocation REFLECTION_DEMON_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("occultum", "reflection_demon"), "main");
    public static final ModelLayerLocation FAIREST_FOLLOWER_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("occultum", "fairest_follower"), "main");
    public static final ModelLayerLocation CORPSE_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("occultum", "corpse"), "main");

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

    public OccultumHumanoidModel(ModelPart root) {
        this.root = root;
        this.head = root.getChild("head");
        this.body = root.getChild("body");
        this.rightArm = root.getChild("right_arm");
        this.leftArm = root.getChild("left_arm");
        this.rightLeg = root.getChild("right_leg");
        this.leftLeg = root.getChild("left_leg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition part = mesh.getRoot();

        part.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        part.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        part.addOrReplaceChild("right_arm",
                CubeListBuilder.create().texOffs(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                PartPose.offset(-5.0F, 2.0F, 0.0F));
        part.addOrReplaceChild("left_arm",
                CubeListBuilder.create().texOffs(40, 16).mirror().addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                PartPose.offset(5.0F, 2.0F, 0.0F));
        part.addOrReplaceChild("right_leg",
                CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                PartPose.offset(-1.9F, 12.0F, 0.0F));
        part.addOrReplaceChild("left_leg",
                CubeListBuilder.create().texOffs(0, 16).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                PartPose.offset(1.9F, 12.0F, 0.0F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.head.yRot = netHeadYaw * ((float) Math.PI / 180F);
        this.head.xRot = headPitch * ((float) Math.PI / 180F);
        this.rightArm.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount;
        this.leftArm.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
        this.rightLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
        this.leftLeg.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        root.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
