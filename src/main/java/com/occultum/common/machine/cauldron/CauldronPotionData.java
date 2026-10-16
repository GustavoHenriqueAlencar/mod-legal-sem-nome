package com.occultum.common.machine.cauldron;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.ArrayList;
import java.util.List;

public record CauldronPotionData(
        List<EffectEntry> effects,
        DispersalType dispersal,
        int extentBonus,
        int lingerBonus,
        boolean disableBlockEffects,
        boolean disableEntityEffects,
        boolean noParticles,
        int drinkSpeedReduction,
        int colorRgb
) {
    public enum DispersalType {
        DRINKABLE,
        SPLASH,
        GAS,
        LIQUID,
        TRIGGER;

        public static final Codec<DispersalType> CODEC = Codec.STRING.xmap(DispersalType::valueOf, DispersalType::name);
        public static final StreamCodec<ByteBuf, DispersalType> STREAM_CODEC = ByteBufCodecs.STRING_UTF8.map(DispersalType::valueOf, DispersalType::name);
    }

    public record EffectEntry(
            String effectId,
            int power,
            int durationTier,
            boolean inverted
    ) {
        public static final Codec<EffectEntry> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                Codec.STRING.fieldOf("effect_id").forGetter(EffectEntry::effectId),
                Codec.INT.fieldOf("power").forGetter(EffectEntry::power),
                Codec.INT.fieldOf("duration_tier").forGetter(EffectEntry::durationTier),
                Codec.BOOL.fieldOf("inverted").forGetter(EffectEntry::inverted)
        ).apply(inst, EffectEntry::new));

        public static final StreamCodec<ByteBuf, EffectEntry> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8, EffectEntry::effectId,
                ByteBufCodecs.VAR_INT, EffectEntry::power,
                ByteBufCodecs.VAR_INT, EffectEntry::durationTier,
                ByteBufCodecs.BOOL, EffectEntry::inverted,
                EffectEntry::new
        );
    }

    public static final CauldronPotionData EMPTY = new CauldronPotionData(
            List.of(), DispersalType.DRINKABLE, 0, 0, false, false, false, 0, 0x5A2CA0
    );

    public static final Codec<CauldronPotionData> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            EffectEntry.CODEC.listOf().fieldOf("effects").forGetter(CauldronPotionData::effects),
            DispersalType.CODEC.fieldOf("dispersal").forGetter(CauldronPotionData::dispersal),
            Codec.INT.fieldOf("extent_bonus").forGetter(CauldronPotionData::extentBonus),
            Codec.INT.fieldOf("linger_bonus").forGetter(CauldronPotionData::lingerBonus),
            Codec.BOOL.fieldOf("disable_block").forGetter(CauldronPotionData::disableBlockEffects),
            Codec.BOOL.fieldOf("disable_entity").forGetter(CauldronPotionData::disableEntityEffects),
            Codec.BOOL.fieldOf("no_particles").forGetter(CauldronPotionData::noParticles),
            Codec.INT.fieldOf("drink_speed_reduction").forGetter(CauldronPotionData::drinkSpeedReduction),
            Codec.INT.fieldOf("color_rgb").forGetter(CauldronPotionData::colorRgb)
    ).apply(inst, CauldronPotionData::new));

    public static final StreamCodec<ByteBuf, CauldronPotionData> STREAM_CODEC = StreamCodec.of(
            (buf, val) -> {
                ByteBufCodecs.VAR_INT.encode(buf, val.effects().size());
                for (EffectEntry entry : val.effects()) {
                    EffectEntry.STREAM_CODEC.encode(buf, entry);
                }
                DispersalType.STREAM_CODEC.encode(buf, val.dispersal());
                ByteBufCodecs.VAR_INT.encode(buf, val.extentBonus());
                ByteBufCodecs.VAR_INT.encode(buf, val.lingerBonus());
                ByteBufCodecs.BOOL.encode(buf, val.disableBlockEffects());
                ByteBufCodecs.BOOL.encode(buf, val.disableEntityEffects());
                ByteBufCodecs.BOOL.encode(buf, val.noParticles());
                ByteBufCodecs.VAR_INT.encode(buf, val.drinkSpeedReduction());
                ByteBufCodecs.VAR_INT.encode(buf, val.colorRgb());
            },
            buf -> {
                int count = ByteBufCodecs.VAR_INT.decode(buf);
                List<EffectEntry> list = new ArrayList<>(count);
                for (int i = 0; i < count; i++) {
                    list.add(EffectEntry.STREAM_CODEC.decode(buf));
                }
                DispersalType disp = DispersalType.STREAM_CODEC.decode(buf);
                int ext = ByteBufCodecs.VAR_INT.decode(buf);
                int linger = ByteBufCodecs.VAR_INT.decode(buf);
                boolean disBlock = ByteBufCodecs.BOOL.decode(buf);
                boolean disEnt = ByteBufCodecs.BOOL.decode(buf);
                boolean noPart = ByteBufCodecs.BOOL.decode(buf);
                int drinkRed = ByteBufCodecs.VAR_INT.decode(buf);
                int col = ByteBufCodecs.VAR_INT.decode(buf);
                return new CauldronPotionData(list, disp, ext, linger, disBlock, disEnt, noPart, drinkRed, col);
            }
    );

    public int getDrinkTicks() {
        return Math.max(8, 32 - drinkSpeedReduction);
    }
}
