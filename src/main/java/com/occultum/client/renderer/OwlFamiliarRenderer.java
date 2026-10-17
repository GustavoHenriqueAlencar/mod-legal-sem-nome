package com.occultum.client.renderer;

import com.occultum.client.model.OwlFamiliarModel;
import com.occultum.common.entity.OwlFamiliarEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class OwlFamiliarRenderer extends MobRenderer<OwlFamiliarEntity, OwlFamiliarModel> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("occultum", "textures/entity/owl_familiar.png");

    public OwlFamiliarRenderer(EntityRendererProvider.Context context) {
        super(context, new OwlFamiliarModel(context.bakeLayer(OwlFamiliarModel.LAYER_LOCATION)), 0.35F);
    }

    @Override
    public ResourceLocation getTextureLocation(OwlFamiliarEntity entity) {
        return TEXTURE;
    }
}
