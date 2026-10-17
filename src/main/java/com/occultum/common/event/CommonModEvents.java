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
        event.put(ModEntityTypes.ELLE.get(), com.occultum.common.entity.ElleEntity.createAttributes().build());
        event.put(ModEntityTypes.LILITH.get(), com.occultum.common.entity.LilithEntity.createAttributes().build());
        event.put(ModEntityTypes.ATTACK_BAT.get(), com.occultum.common.entity.AttackBatEntity.createAttributes().build());
        event.put(ModEntityTypes.VAMPIRE_SERVANT.get(), com.occultum.common.entity.VampireServantEntity.createAttributes().build());
        event.put(ModEntityTypes.WOLFMAN.get(), com.occultum.common.entity.WolfmanEntity.createAttributes().build());
        event.put(ModEntityTypes.HORNED_HUNTSMAN.get(), com.occultum.common.entity.HornedHuntsmanEntity.createAttributes().build());
        event.put(ModEntityTypes.SPIRIT.get(), com.occultum.common.entity.SpiritEntity.createAttributes().build());
        event.put(ModEntityTypes.CORPSE.get(), com.occultum.common.entity.CorpseEntity.createAttributes().build());
        event.put(ModEntityTypes.NIGHTMARE.get(), com.occultum.common.entity.NightmareEntity.createAttributes().build());
        event.put(ModEntityTypes.LORD_OF_TORMENT.get(), com.occultum.common.entity.LordOfTormentEntity.createAttributes().build());
        event.put(ModEntityTypes.REFLECTION_DEMON.get(), com.occultum.common.entity.ReflectionDemonEntity.createAttributes().build());
        event.put(ModEntityTypes.FAIREST_FOLLOWER.get(), com.occultum.common.entity.FairestFollowerEntity.createAttributes().build());
    }
}
