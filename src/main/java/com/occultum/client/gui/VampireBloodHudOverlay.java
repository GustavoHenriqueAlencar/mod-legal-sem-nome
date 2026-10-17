package com.occultum.client.gui;

import com.occultum.common.registry.ModAttachmentTypes;
import com.occultum.common.vampire.VampirismSystem;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.world.entity.player.Player;

/**
 * Medidor de Sangue Vampírico e Indicador de Transformação no HUD (`08_VAMPIRISMO_E_LICANTROPIA.md` Seção 1 & 2.1.2):
 * - Renderiza a reserva atual de sangue, a capacidade máxima (`500 + Lv*250` ou híbrido), o nível de Vampiro,
 *   o poder ativo selecionado (`DRINK`, `MESMERIZE`, `SPEED`, `BAT`, `ULTIMATE`) e o estado lupino (`WOLF`, `WOLFMAN`).
 */
public class VampireBloodHudOverlay implements LayeredDraw.Layer {

    @Override
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.options.hideGui) return;

        int vampireLv = player.getData(ModAttachmentTypes.VAMPIRE_LEVEL.get());
        int werewolfLv = player.getData(ModAttachmentTypes.WEREWOLF_LEVEL.get());

        if (vampireLv <= 0 && werewolfLv <= 0) return;

        int screenWidth = guiGraphics.guiWidth();
        int screenHeight = guiGraphics.guiHeight();

        if (vampireLv >= 1) {
            int blood = VampirismSystem.getBlood(player);
            int maxBlood = Math.max(500, VampirismSystem.getMaxBlood(player));
            float ratio = Math.min(1.0F, Math.max(0.0F, (float) blood / (float) maxBlood));

            int barWidth = 82;
            int barHeight = 7;
            int x = (screenWidth / 2) + 10;
            int y = screenHeight - 49;

            guiGraphics.fill(x - 1, y - 1, x + barWidth + 1, y + barHeight + 1, 0xCC1A0505);
            guiGraphics.fill(x, y, x + barWidth, y + barHeight, 0xDD330A0A);

            int filledWidth = Math.round(barWidth * ratio);
            if (filledWidth > 0) {
                guiGraphics.fill(x, y, x + filledWidth, y + barHeight, 0xFFC8102E);
            }

            String power = player.getData(ModAttachmentTypes.VAMPIRE_SELECTED_POWER.get());
            String label = "Sangue: " + blood + "/" + maxBlood + " [Lv." + vampireLv + " | " + power + "]";
            guiGraphics.drawString(mc.font, label, x, y - 10, 0xFFFF4D6D, true);
        }

        if (werewolfLv >= 1) {
            String form = player.getData(ModAttachmentTypes.WEREWOLF_FORM.get());
            int x = 8;
            int y = screenHeight - 22;
            String wolfText = "Licantropia Lv." + werewolfLv + " [" + form + "]";
            guiGraphics.drawString(mc.font, wolfText, x, y, 0xFFE0A96D, true);
        }
    }
}
