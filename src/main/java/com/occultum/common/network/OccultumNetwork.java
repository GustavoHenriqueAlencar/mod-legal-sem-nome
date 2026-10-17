package com.occultum.common.network;

import com.occultum.OccultumMod;
import com.occultum.common.registry.ModAttachmentTypes;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * Sincronização de Rede Servidor -> Cliente dos estados de Vampirismo, Licantropia,
 * Disfarce de Identidade e Forma Fantasma para renderização no HUD e Modelos 3D.
 */
@EventBusSubscriber(modid = OccultumMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class OccultumNetwork {

    public record SyncTransformationPayload(
            int vampireLevel,
            int bloodLevel,
            int vampireLevelCap,
            String selectedPower,
            String ultimateType,
            int ultimateCharges,
            boolean batForm,
            int werewolfLevel,
            String werewolfForm,
            boolean ghostForm,
            String disguise
    ) implements CustomPacketPayload {

        public static final Type<SyncTransformationPayload> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath(OccultumMod.MOD_ID, "sync_transformation"));

        public static final StreamCodec<ByteBuf, SyncTransformationPayload> STREAM_CODEC = StreamCodec.of(
                (buf, p) -> {
                    ByteBufCodecs.VAR_INT.encode(buf, p.vampireLevel);
                    ByteBufCodecs.VAR_INT.encode(buf, p.bloodLevel);
                    ByteBufCodecs.VAR_INT.encode(buf, p.vampireLevelCap);
                    ByteBufCodecs.STRING_UTF8.encode(buf, p.selectedPower);
                    ByteBufCodecs.STRING_UTF8.encode(buf, p.ultimateType);
                    ByteBufCodecs.VAR_INT.encode(buf, p.ultimateCharges);
                    ByteBufCodecs.BOOL.encode(buf, p.batForm);
                    ByteBufCodecs.VAR_INT.encode(buf, p.werewolfLevel);
                    ByteBufCodecs.STRING_UTF8.encode(buf, p.werewolfForm);
                    ByteBufCodecs.BOOL.encode(buf, p.ghostForm);
                    ByteBufCodecs.STRING_UTF8.encode(buf, p.disguise);
                },
                buf -> new SyncTransformationPayload(
                        ByteBufCodecs.VAR_INT.decode(buf),
                        ByteBufCodecs.VAR_INT.decode(buf),
                        ByteBufCodecs.VAR_INT.decode(buf),
                        ByteBufCodecs.STRING_UTF8.decode(buf),
                        ByteBufCodecs.STRING_UTF8.decode(buf),
                        ByteBufCodecs.VAR_INT.decode(buf),
                        ByteBufCodecs.BOOL.decode(buf),
                        ByteBufCodecs.VAR_INT.decode(buf),
                        ByteBufCodecs.STRING_UTF8.decode(buf),
                        ByteBufCodecs.BOOL.decode(buf),
                        ByteBufCodecs.STRING_UTF8.decode(buf)
                )
        );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(
                SyncTransformationPayload.TYPE,
                SyncTransformationPayload.STREAM_CODEC,
                OccultumNetwork::handleClientSync
        );
    }

    private static void handleClientSync(SyncTransformationPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            if (player != null) {
                player.setData(ModAttachmentTypes.VAMPIRE_LEVEL.get(), payload.vampireLevel());
                player.setData(ModAttachmentTypes.BLOOD_LEVEL.get(), payload.bloodLevel());
                player.setData(ModAttachmentTypes.VAMPIRE_LEVEL_CAP.get(), payload.vampireLevelCap());
                player.setData(ModAttachmentTypes.VAMPIRE_SELECTED_POWER.get(), payload.selectedPower());
                player.setData(ModAttachmentTypes.VAMPIRE_ULTIMATE_TYPE.get(), payload.ultimateType());
                player.setData(ModAttachmentTypes.VAMPIRE_ULTIMATE_CHARGES.get(), payload.ultimateCharges());
                player.setData(ModAttachmentTypes.VAMPIRE_BAT_FORM.get(), payload.batForm());
                player.setData(ModAttachmentTypes.WEREWOLF_LEVEL.get(), payload.werewolfLevel());
                player.setData(ModAttachmentTypes.WEREWOLF_FORM.get(), payload.werewolfForm());
                player.setData(ModAttachmentTypes.SPIRIT_GHOST_FORM.get(), payload.ghostForm());
                player.setData(ModAttachmentTypes.TRANSMUTATION_DISGUISE.get(), payload.disguise());
                player.refreshDimensions();
            }
        });
    }

    public static void syncToClient(Player player) {
        if (player instanceof ServerPlayer sp) {
            SyncTransformationPayload payload = new SyncTransformationPayload(
                    sp.getData(ModAttachmentTypes.VAMPIRE_LEVEL.get()),
                    sp.getData(ModAttachmentTypes.BLOOD_LEVEL.get()),
                    sp.getData(ModAttachmentTypes.VAMPIRE_LEVEL_CAP.get()),
                    sp.getData(ModAttachmentTypes.VAMPIRE_SELECTED_POWER.get()),
                    sp.getData(ModAttachmentTypes.VAMPIRE_ULTIMATE_TYPE.get()),
                    sp.getData(ModAttachmentTypes.VAMPIRE_ULTIMATE_CHARGES.get()),
                    sp.getData(ModAttachmentTypes.VAMPIRE_BAT_FORM.get()),
                    sp.getData(ModAttachmentTypes.WEREWOLF_LEVEL.get()),
                    sp.getData(ModAttachmentTypes.WEREWOLF_FORM.get()),
                    sp.getData(ModAttachmentTypes.SPIRIT_GHOST_FORM.get()),
                    sp.getData(ModAttachmentTypes.TRANSMUTATION_DISGUISE.get())
            );
            PacketDistributor.sendToPlayer(sp, payload);
        }
    }
}
