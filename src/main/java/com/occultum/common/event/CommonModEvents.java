package com.occultum.common.event;

import com.occultum.OccultumMod;
import com.occultum.common.entity.EntEntity;
import com.occultum.common.entity.MandrakeEntity;
import com.occultum.common.entity.OwlFamiliarEntity;
import com.occultum.common.entity.ToadFamiliarEntity;
import com.occultum.common.entity.TreefydEntity;
import com.occultum.common.registry.ModEntityTypes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

@EventBusSubscriber(modid = OccultumMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class CommonModEvents {

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntityTypes.MANDRAKE.get(), MandrakeEntity.createAttributes().build());
        event.put(ModEntityTypes.ENT.get(), EntEntity.createAttributes().build());
        event.put(ModEntityTypes.TREEFYD.get(), TreefydEntity.createAttributes().build());
        event.put(ModEntityTypes.TOAD_FAMILIAR.get(), ToadFamiliarEntity.createAttributes().build());
        event.put(ModEntityTypes.OWL_FAMILIAR.get(), OwlFamiliarEntity.createAttributes().build());
    }
}
