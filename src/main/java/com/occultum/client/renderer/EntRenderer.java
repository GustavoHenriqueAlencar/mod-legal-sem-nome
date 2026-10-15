package com.occultum.client.renderer;

import com.occultum.client.model.EntModel;
import com.occultum.common.entity.EntEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class EntRenderer extends MobRenderer<EntEntity, EntModel> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("occultum", "textures/entity/ent.png");

    public EntRenderer(EntityRendererProvider.Context context) {
        super(context, new EntModel(context.bakeLayer(EntModel.LAYER_LOCATION)), 1.2F);
    }

    @Override
    public ResourceLocation getTextureLocation(EntEntity entity) {
        return TEXTURE;
    }
}
