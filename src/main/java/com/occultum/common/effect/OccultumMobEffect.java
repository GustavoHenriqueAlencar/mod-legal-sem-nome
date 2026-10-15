package com.occultum.common.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class OccultumMobEffect extends MobEffect {
    private final boolean curableWithMilk;

    public OccultumMobEffect(MobEffectCategory category, int color, boolean curableWithMilk) {
        super(category, color);
        this.curableWithMilk = curableWithMilk;
    }

    public boolean isCurableWithMilk() {
        return curableWithMilk;
    }

    @Override
    public boolean applyEffectTick(LivingEntity livingEntity, int amplifier) {
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }
}
