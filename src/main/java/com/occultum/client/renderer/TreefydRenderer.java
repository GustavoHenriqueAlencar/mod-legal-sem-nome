package com.occultum.client.renderer;

import com.occultum.client.model.TreefydModel;
import com.occultum.common.entity.TreefydEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class TreefydRenderer extends MobRenderer<TreefydEntity, TreefydModel> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("occultum", "textures/entity/treefyd.png");

    public TreefydRenderer(EntityRendererProvider.Context context) {
        super(context, new TreefydModel(context.bakeLayer(TreefydModel.LAYER_LOCATION)), 0.6F);
    }

    @Override
    public ResourceLocation getTextureLocation(TreefydEntity entity) {
        return TEXTURE;
    }
}
