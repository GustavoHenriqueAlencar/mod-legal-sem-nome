package com.occultum.common.registry;

import com.mojang.serialization.Codec;
import com.occultum.OccultumMod;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class ModAttachmentTypes {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, OccultumMod.MOD_ID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Integer>> VAMPIRE_LEVEL =
            ATTACHMENT_TYPES.register("vampire_level", () -> AttachmentType.builder(() -> 0)
                    .serialize(Codec.INT)
                    .build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Integer>> WEREWOLF_LEVEL =
            ATTACHMENT_TYPES.register("werewolf_level", () -> AttachmentType.builder(() -> 0)
                    .serialize(Codec.INT)
                    .build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Integer>> BLOOD_LEVEL =
            ATTACHMENT_TYPES.register("blood_level", () -> AttachmentType.builder(() -> 0)
                    .serialize(Codec.INT)
                    .build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Integer>> INFUSION_POWER =
            ATTACHMENT_TYPES.register("infusion_power", () -> AttachmentType.builder(() -> 0)
                    .serialize(Codec.INT)
                    .build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Integer>> COVEN_LEVEL =
            ATTACHMENT_TYPES.register("coven_level", () -> AttachmentType.builder(() -> 0)
                    .serialize(Codec.INT)
                    .build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<String>> TRANSMUTATION_DISGUISE =
            ATTACHMENT_TYPES.register("transmutation_disguise", () -> AttachmentType.builder(() -> "")
                    .serialize(Codec.STRING)
                    .build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<String>> FAMILIAR_TYPE =
            ATTACHMENT_TYPES.register("familiar_type", () -> AttachmentType.builder(() -> "")
                    .serialize(Codec.STRING)
                    .build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Integer>> BOTTLING_XP =
            ATTACHMENT_TYPES.register("bottling_xp", () -> AttachmentType.builder(() -> 0)
                    .serialize(Codec.INT)
                    .build());

    /** Tipo de infusão ativa: "", "light", "overworld", "otherwhere", "nether" ou "grave". */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<String>> INFUSION_TYPE =
            ATTACHMENT_TYPES.register("infusion_type", () -> AttachmentType.builder(() -> "")
                    .serialize(Codec.STRING)
                    .copyOnDeath()
                    .build());

    /** Teto da barra de poder espiritual de infusão (200 a 400). */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Integer>> INFUSION_MAX_POWER =
            ATTACHMENT_TYPES.register("infusion_max_power", () -> AttachmentType.builder(() -> 200)
                    .serialize(Codec.INT)
                    .copyOnDeath()
                    .build());

    /** ID da profecia ativa da Bola de Cristal (Seção 2.3 de 13_BRASEIRO_NECROMANCIA_E_ADIVINHACAO.md). */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<String>> ACTIVE_PROPHECY =
            ATTACHMENT_TYPES.register("active_prophecy", () -> AttachmentType.builder(() -> "")
                    .serialize(Codec.STRING)
                    .copyOnDeath()
                    .build());

    /** Timer regressivo de concretização forçada da profecia (9.600 a 36.000 ticks). */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Integer>> PROPHECY_TIMER =
            ATTACHMENT_TYPES.register("prophecy_timer", () -> AttachmentType.builder(() -> 0)
                    .serialize(Codec.INT)
                    .copyOnDeath()
                    .build());

    // =========================================================================
    // SPRINT 4: VAMPIRISMO, LICANTROPIA E AS 3 DIMENSÕES
    // =========================================================================

    /** Teto de evolução do Livro dos Vampiros (padrão inicial: 3, sobe até 10 lendo as 9 páginas). */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Integer>> VAMPIRE_LEVEL_CAP =
            ATTACHMENT_TYPES.register("vampire_level_cap", () -> AttachmentType.builder(() -> 3)
                    .serialize(Codec.INT)
                    .copyOnDeath()
                    .build());

    /** Reserva de sangue humano para humanos/vítimas (máximo 500; forçado a 0 em vampiros nível >= 1). */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Integer>> HUMAN_BLOOD =
            ATTACHMENT_TYPES.register("human_blood", () -> AttachmentType.builder(() -> 500)
                    .serialize(Codec.INT)
                    .copyOnDeath()
                    .build());

    /** Habilidade vampírica selecionada ("DRINK", "MESMERIZE", "SPEED", "BAT", "ULTIMATE"). */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<String>> VAMPIRE_SELECTED_POWER =
            ATTACHMENT_TYPES.register("vampire_selected_power", () -> AttachmentType.builder(() -> "DRINK")
                    .serialize(Codec.STRING)
                    .copyOnDeath()
                    .build());

    /** Poder Supremo sintonizado no Crisol de Sangue ("NONE", "STORM", "SWARM", "FARM"). */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<String>> VAMPIRE_ULTIMATE_TYPE =
            ATTACHMENT_TYPES.register("vampire_ultimate_type", () -> AttachmentType.builder(() -> "NONE")
                    .serialize(Codec.STRING)
                    .copyOnDeath()
                    .build());

    /** Cargas acumuladas do Poder Supremo Vampírico. */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Integer>> VAMPIRE_ULTIMATE_CHARGES =
            ATTACHMENT_TYPES.register("vampire_ultimate_charges", () -> AttachmentType.builder(() -> 0)
                    .serialize(Codec.INT)
                    .copyOnDeath()
                    .build());

    /** Estado da Visão Vampírica contínua (Nível >= 2). */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Boolean>> VAMPIRE_VISION_ACTIVE =
            ATTACHMENT_TYPES.register("vampire_vision_active", () -> AttachmentType.builder(() -> false)
                    .serialize(Codec.BOOL)
                    .copyOnDeath()
                    .build());

    /** Estado de transfiguração ativa em Morcego (Nível >= 7). */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Boolean>> VAMPIRE_BAT_FORM =
            ATTACHMENT_TYPES.register("vampire_bat_form", () -> AttachmentType.builder(() -> false)
                    .serialize(Codec.BOOL)
                    .build());

    /** Ticks acumulados sob sol direto com 0 de sangue (morte instantânea ao atingir 400 ticks). */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Integer>> VAMPIRE_SUN_TICKS =
            ATTACHMENT_TYPES.register("vampire_sun_ticks", () -> AttachmentType.builder(() -> 0)
                    .serialize(Codec.INT)
                    .build());

    /** Contador de progresso da missão de ascensão atual do Vampiro. */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Integer>> VAMPIRE_QUEST_COUNTER =
            ATTACHMENT_TYPES.register("vampire_quest_counter", () -> AttachmentType.builder(() -> 0)
                    .serialize(Codec.INT)
                    .copyOnDeath()
                    .build());

    /** Dados auxiliares serializados da missão de Vampiro (ex: UUIDs de aldeões drenados ou centros de vilas visitadas). */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<String>> VAMPIRE_QUEST_DATA =
            ATTACHMENT_TYPES.register("vampire_quest_data", () -> AttachmentType.builder(() -> "")
                    .serialize(Codec.STRING)
                    .copyOnDeath()
                    .build());

    /** Forma atual de Lobisomem ("NONE", "WOLF", "WOLFMAN"). */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<String>> WEREWOLF_FORM =
            ATTACHMENT_TYPES.register("werewolf_form", () -> AttachmentType.builder(() -> "NONE")
                    .serialize(Codec.STRING)
                    .build());

    /** Contador de progresso da missão de ascensão atual do Lobisomem. */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Integer>> WEREWOLF_QUEST_COUNTER =
            ATTACHMENT_TYPES.register("werewolf_quest_counter", () -> AttachmentType.builder(() -> 0)
                    .serialize(Codec.INT)
                    .copyOnDeath()
                    .build());

    /** Lista serializada de chunks onde o Lobisomem uivou na missão 6 -> 7 (16 chunks distintos). */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<String>> WEREWOLF_QUEST_DATA =
            ATTACHMENT_TYPES.register("werewolf_quest_data", () -> AttachmentType.builder(() -> "")
                    .serialize(Codec.STRING)
                    .copyOnDeath()
                    .build());

    /** Cooldown em ticks do Uivo Paralisante (1200 ticks = 60s). */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Integer>> WEREWOLF_HOWL_COOLDOWN =
            ATTACHMENT_TYPES.register("werewolf_howl_cooldown", () -> AttachmentType.builder(() -> 0)
                    .serialize(Codec.INT)
                    .build());

    /** Estado de Manifestação Fantasma / Espectral ao atravessar o Portal Espiritual. */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Boolean>> SPIRIT_GHOST_FORM =
            ATTACHMENT_TYPES.register("spirit_ghost_form", () -> AttachmentType.builder(() -> false)
                    .serialize(Codec.BOOL)
                    .build());

    /** Condição atual da viagem ao Mundo Espiritual (true = Pesadelo ativo, false = Bom Sonho). */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Boolean>> SPIRIT_IS_NIGHTMARE =
            ATTACHMENT_TYPES.register("spirit_is_nightmare", () -> AttachmentType.builder(() -> false)
                    .serialize(Codec.BOOL)
                    .build());

    /** Timestamp (gameTime) da última vitória contra o Nightmare. */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Long>> NIGHTMARE_TRIUMPH_TIME =
            ATTACHMENT_TYPES.register("nightmare_triumph_time", () -> AttachmentType.builder(() -> 0L)
                    .serialize(Codec.LONG)
                    .copyOnDeath()
                    .build());

    /** Coordenada serializada ("x,y,z") e HP do Corpo Físico Adormecido no Overworld. */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<String>> CORPSE_RETURN_DATA =
            ATTACHMENT_TYPES.register("corpse_return_data", () -> AttachmentType.builder(() -> "")
                    .serialize(Codec.STRING)
                    .copyOnDeath()
                    .build());

    /** Andar designado na Dimensão do Tormento (0 a 5). */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Integer>> TORMENT_FLOOR =
            ATTACHMENT_TYPES.register("torment_floor", () -> AttachmentType.builder(() -> 0)
                    .serialize(Codec.INT)
                    .copyOnDeath()
                    .build());

    /** Coordenadas da célula de entrada na Dimensão do Espelho ("x,y,z"). */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<String>> MIRROR_ENTRY_POS =
            ATTACHMENT_TYPES.register("mirror_entry_pos", () -> AttachmentType.builder(() -> "")
                    .serialize(Codec.STRING)
                    .copyOnDeath()
                    .build());

    /** Timestamp (gameTime) de expiração do cooldown de "Mirror mirror send me home" (6.000 ticks = 5 min). */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Long>> MIRROR_SEND_HOME_COOLDOWN =
            ATTACHMENT_TYPES.register("mirror_send_home_cooldown", () -> AttachmentType.builder(() -> 0L)
                    .serialize(Codec.LONG)
                    .copyOnDeath()
                    .build());

    /** Timestamp (gameTime) de expiração do cooldown de "Mirror mirror I give up" (72.000 ticks = 60 min). */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Long>> MIRROR_GIVE_UP_COOLDOWN =
            ATTACHMENT_TYPES.register("mirror_give_up_cooldown", () -> AttachmentType.builder(() -> 0L)
                    .serialize(Codec.LONG)
                    .copyOnDeath()
                    .build());

    public static void register(IEventBus eventBus) {
        ATTACHMENT_TYPES.register(eventBus);
    }
}
