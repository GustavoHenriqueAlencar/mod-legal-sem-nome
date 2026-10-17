package com.occultum.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.occultum.client.model.OccultumHumanoidModel;
import com.occultum.client.model.SpiritModel;
import com.occultum.client.model.Sprint4BlockModels;
import com.occultum.common.entity.*;
import com.occultum.common.mirror.MysticMirrorBlockEntity;
import com.occultum.common.vampire.BloodCrucibleBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.BatRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;
import java.util.UUID;

/**
 * Renderizadores Desacoplados da Sprint 4 para Entidades e BlockEntities:
 * - `ElleRenderer`, `LilithRenderer`, `AttackBatRenderer`, `VampireServantRenderer`,
 *   `WolfmanRenderer`, `HornedHuntsmanRenderer`, `SpiritRenderer`, `CorpseRenderer` (usa a skin do jogador adormecido),
 *   `NightmareRenderer`, `LordOfTormentRenderer`, `ReflectionDemonRenderer` (usa a skin do jogador clonado
 *   ou a textura de Homem-Lobo se clonou um lobisomem), `FairestFollowerRenderer`,
 *   `BloodCrucibleRenderer` e `MysticMirrorRenderer`.
 */
public final class Sprint4Renderers {

    private Sprint4Renderers() {}

    public static class ElleRenderer extends MobRenderer<ElleEntity, OccultumHumanoidModel<ElleEntity>> {
        private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("occultum", "textures/entity/elle.png");
        public ElleRenderer(EntityRendererProvider.Context ctx) {
            super(ctx, new OccultumHumanoidModel<>(ctx.bakeLayer(OccultumHumanoidModel.ELLE_LAYER)), 0.5F);
        }
        @Override
        public ResourceLocation getTextureLocation(ElleEntity entity) {
            return TEXTURE;
        }
    }

    public static class LilithRenderer extends MobRenderer<LilithEntity, OccultumHumanoidModel<LilithEntity>> {
        private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("occultum", "textures/entity/lilith.png");
        public LilithRenderer(EntityRendererProvider.Context ctx) {
            super(ctx, new OccultumHumanoidModel<>(ctx.bakeLayer(OccultumHumanoidModel.LILITH_LAYER)), 0.6F);
        }
        @Override
        public ResourceLocation getTextureLocation(LilithEntity entity) {
            return TEXTURE;
        }
    }

    public static class AttackBatRenderer extends MobRenderer<AttackBatEntity, OccultumHumanoidModel<AttackBatEntity>> {
        private static final ResourceLocation TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/bat.png");
        public AttackBatRenderer(EntityRendererProvider.Context ctx) {
            super(ctx, new OccultumHumanoidModel<>(ctx.bakeLayer(OccultumHumanoidModel.VAMPIRE_SERVANT_LAYER)), 0.25F);
        }
        @Override
        public ResourceLocation getTextureLocation(AttackBatEntity entity) {
            return TEXTURE;
        }
    }

    public static class VampireServantRenderer extends MobRenderer<VampireServantEntity, OccultumHumanoidModel<VampireServantEntity>> {
        private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("occultum", "textures/entity/vampire_servant.png");
        public VampireServantRenderer(EntityRendererProvider.Context ctx) {
            super(ctx, new OccultumHumanoidModel<>(ctx.bakeLayer(OccultumHumanoidModel.VAMPIRE_SERVANT_LAYER)), 0.5F);
        }
        @Override
        public ResourceLocation getTextureLocation(VampireServantEntity entity) {
            return TEXTURE;
        }
    }

    public static class WolfmanRenderer extends MobRenderer<WolfmanEntity, OccultumHumanoidModel<WolfmanEntity>> {
        public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("occultum", "textures/entity/wolfman.png");
        public WolfmanRenderer(EntityRendererProvider.Context ctx) {
            super(ctx, new OccultumHumanoidModel<>(ctx.bakeLayer(OccultumHumanoidModel.WOLFMAN_LAYER)), 0.65F);
        }
        @Override
        public ResourceLocation getTextureLocation(WolfmanEntity entity) {
            return TEXTURE;
        }
    }

    public static class HornedHuntsmanRenderer extends MobRenderer<HornedHuntsmanEntity, OccultumHumanoidModel<HornedHuntsmanEntity>> {
        private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("occultum", "textures/entity/horned_huntsman.png");
        public HornedHuntsmanRenderer(EntityRendererProvider.Context ctx) {
            super(ctx, new OccultumHumanoidModel<>(ctx.bakeLayer(OccultumHumanoidModel.HORNED_HUNTSMAN_LAYER)), 0.8F);
        }
        @Override
        public ResourceLocation getTextureLocation(HornedHuntsmanEntity entity) {
            return TEXTURE;
        }
    }

    public static class SpiritRenderer extends MobRenderer<SpiritEntity, SpiritModel> {
        private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("occultum", "textures/entity/spirit.png");
        public SpiritRenderer(EntityRendererProvider.Context ctx) {
            super(ctx, new SpiritModel(ctx.bakeLayer(SpiritModel.LAYER_LOCATION)), 0.15F);
        }
        @Override
        public ResourceLocation getTextureLocation(SpiritEntity entity) {
            return TEXTURE;
        }
    }

    public static class CorpseRenderer extends MobRenderer<CorpseEntity, OccultumHumanoidModel<CorpseEntity>> {
        public CorpseRenderer(EntityRendererProvider.Context ctx) {
            super(ctx, new OccultumHumanoidModel<>(ctx.bakeLayer(OccultumHumanoidModel.CORPSE_LAYER)), 0.4F);
        }

        @Override
        protected void setupRotations(CorpseEntity entity, PoseStack poseStack, float bob, float yBodyRot, float partialTick, float scale) {
            super.setupRotations(entity, poseStack, bob, yBodyRot, partialTick, scale);
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            poseStack.translate(0.0F, -1.0F, 0.0F);
        }

        @Override
        public ResourceLocation getTextureLocation(CorpseEntity entity) {
            UUID owner = entity.getOwnerUUID();
            if (owner != null) {
                return resolvePlayerSkin(owner);
            }
            return DefaultPlayerSkin.getDefaultTexture();
        }
    }

    public static class NightmareRenderer extends MobRenderer<NightmareEntity, OccultumHumanoidModel<NightmareEntity>> {
        private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("occultum", "textures/entity/nightmare.png");
        public NightmareRenderer(EntityRendererProvider.Context ctx) {
            super(ctx, new OccultumHumanoidModel<>(ctx.bakeLayer(OccultumHumanoidModel.NIGHTMARE_LAYER)), 0.55F);
        }
        @Override
        public ResourceLocation getTextureLocation(NightmareEntity entity) {
            return TEXTURE;
        }
    }

    public static class LordOfTormentRenderer extends MobRenderer<LordOfTormentEntity, OccultumHumanoidModel<LordOfTormentEntity>> {
        private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("occultum", "textures/entity/lord_of_torment.png");
        public LordOfTormentRenderer(EntityRendererProvider.Context ctx) {
            super(ctx, new OccultumHumanoidModel<>(ctx.bakeLayer(OccultumHumanoidModel.LORD_OF_TORMENT_LAYER)), 0.75F);
        }
        @Override
        public ResourceLocation getTextureLocation(LordOfTormentEntity entity) {
            return TEXTURE;
        }
    }

    public static class ReflectionDemonRenderer extends MobRenderer<ReflectionDemonEntity, OccultumHumanoidModel<ReflectionDemonEntity>> {
        private static final ResourceLocation DEFAULT_TEX = ResourceLocation.fromNamespaceAndPath("occultum", "textures/entity/reflection_demon.png");
        public ReflectionDemonRenderer(EntityRendererProvider.Context ctx) {
            super(ctx, new OccultumHumanoidModel<>(ctx.bakeLayer(OccultumHumanoidModel.REFLECTION_DEMON_LAYER)), 0.5F);
        }
        @Override
        public ResourceLocation getTextureLocation(ReflectionDemonEntity entity) {
            if (entity.isMimickedWolfman()) {
                return WolfmanRenderer.TEXTURE;
            }
            Optional<UUID> mimicked = entity.getMimickedUuid();
            if (mimicked.isPresent()) {
                return resolvePlayerSkin(mimicked.get());
            }
            return DEFAULT_TEX;
        }
    }

    public static class FairestFollowerRenderer extends MobRenderer<FairestFollowerEntity, OccultumHumanoidModel<FairestFollowerEntity>> {
        private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("occultum", "textures/entity/fairest_follower.png");
        public FairestFollowerRenderer(EntityRendererProvider.Context ctx) {
            super(ctx, new OccultumHumanoidModel<>(ctx.bakeLayer(OccultumHumanoidModel.FAIREST_FOLLOWER_LAYER)), 0.5F);
        }
        @Override
        public ResourceLocation getTextureLocation(FairestFollowerEntity entity) {
            return TEXTURE;
        }
    }

    public static class BloodCrucibleRenderer implements BlockEntityRenderer<BloodCrucibleBlockEntity> {
        private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("occultum", "textures/block/blood_crucible.png");
        private final Sprint4BlockModels.BloodCrucibleModel model;

        public BloodCrucibleRenderer(BlockEntityRendererProvider.Context context) {
            this.model = new Sprint4BlockModels.BloodCrucibleModel(context.bakeLayer(Sprint4BlockModels.BloodCrucibleModel.LAYER_LOCATION));
        }

        @Override
        public void render(BloodCrucibleBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                           MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
            poseStack.pushPose();
            poseStack.translate(0.5D, 0.0D, 0.5D);
            VertexConsumer buffer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
            this.model.renderToBuffer(poseStack, buffer, packedLight, packedOverlay, 0xFFFFFFFF);
            poseStack.popPose();
        }
    }

    public static class MysticMirrorRenderer implements BlockEntityRenderer<MysticMirrorBlockEntity> {
        private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("occultum", "textures/block/mystic_mirror.png");
        private final Sprint4BlockModels.MysticMirrorModel model;

        public MysticMirrorRenderer(BlockEntityRendererProvider.Context context) {
            this.model = new Sprint4BlockModels.MysticMirrorModel(context.bakeLayer(Sprint4BlockModels.MysticMirrorModel.LAYER_LOCATION));
        }

        @Override
        public void render(MysticMirrorBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                           MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
            poseStack.pushPose();
            poseStack.translate(0.5D, 0.0D, 0.5D);
            VertexConsumer buffer = bufferSource.getBuffer(RenderType.entityTranslucent(TEXTURE));
            int color = blockEntity.getOracleFaceTicks() > 0 ? 0xEE7777FF : 0xDDFFFFFF;
            this.model.renderToBuffer(poseStack, buffer, packedLight, packedOverlay, color);
            poseStack.popPose();
        }
    }

    private static ResourceLocation resolvePlayerSkin(UUID uuid) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.getConnection() != null) {
            PlayerInfo info = mc.getConnection().getPlayerInfo(uuid);
            if (info != null) {
                return info.getSkin().texture();
            }
        }
        return DefaultPlayerSkin.get(uuid).texture();
    }
}
