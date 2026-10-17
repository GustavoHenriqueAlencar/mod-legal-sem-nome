package com.occultum.client.renderer;

import com.occultum.client.model.ToadFamiliarModel;
import com.occultum.common.entity.ToadFamiliarEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class ToadFamiliarRenderer extends MobRenderer<ToadFamiliarEntity, ToadFamiliarModel> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("occultum", "textures/entity/toad_familiar.png");

    public ToadFamiliarRenderer(EntityRendererProvider.Context context) {
        super(context, new ToadFamiliarModel(context.bakeLayer(ToadFamiliarModel.LAYER_LOCATION)), 0.3F);
    }

    @Override
    public ResourceLocation getTextureLocation(ToadFamiliarEntity entity) {
        return TEXTURE;
    }
}
