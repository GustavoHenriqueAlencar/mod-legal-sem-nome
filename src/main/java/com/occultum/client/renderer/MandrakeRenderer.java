package com.occultum.client.renderer;

import com.occultum.client.model.MandrakeModel;
import com.occultum.common.entity.MandrakeEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class MandrakeRenderer extends MobRenderer<MandrakeEntity, MandrakeModel> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("occultum", "textures/entity/mandrake.png");

    public MandrakeRenderer(EntityRendererProvider.Context context) {
        super(context, new MandrakeModel(context.bakeLayer(MandrakeModel.LAYER_LOCATION)), 0.3F);
    }

    @Override
    public ResourceLocation getTextureLocation(MandrakeEntity entity) {
        return TEXTURE;
    }
}
