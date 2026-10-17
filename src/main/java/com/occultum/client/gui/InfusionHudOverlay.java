package com.occultum.client.gui;

import com.occultum.common.registry.ModAttachmentTypes;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.world.entity.player.Player;

/**
 * Barra de Poder Espiritual de Infusão no HUD (Seção 1 de 07_INFUSOES_E_SIMBOLOS_DE_VARINHA.md):
 * - Exibida na borda esquerda da tela quando o jogador possui uma Infusão ativa (200 a 400 cargas).
 */
public class InfusionHudOverlay implements LayeredDraw.Layer {

    @Override
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.options.hideGui) return;

        String infusionType = player.getData(ModAttachmentTypes.INFUSION_TYPE.get());
        if (infusionType == null || infusionType.isEmpty()) return;

        int power = player.getData(ModAttachmentTypes.INFUSION_POWER.get());
        int maxPower = Math.max(200, player.getData(ModAttachmentTypes.INFUSION_MAX_POWER.get()));
        float ratio = Math.min(1.0F, Math.max(0.0F, (float) power / (float) maxPower));

        int screenHeight = guiGraphics.guiHeight();
        int barWidth = 8;
        int barHeight = 84;
        int x = 8;
        int y = (screenHeight - barHeight) / 2;

        // Fundo e moldura da barra
        guiGraphics.fill(x - 1, y - 1, x + barWidth + 1, y + barHeight + 1, 0xCC14101C);
        guiGraphics.fill(x, y, x + barWidth, y + barHeight, 0xDD241E30);

        int color = switch (infusionType) {
            case "light" -> 0xFFF5E663;
            case "overworld" -> 0xFF48C764;
            case "otherwhere" -> 0xFFA245E8;
            case "nether" -> 0xFFE8452C;
            case "grave" -> 0xFF5CE1E6;
            default -> 0xFF9B6BFF;
        };

        int filledHeight = Math.round(barHeight * ratio);
        if (filledHeight > 0) {
            guiGraphics.fill(x, y + (barHeight - filledHeight), x + barWidth, y + barHeight, color);
        }

        String label = power + "/" + maxPower;
        guiGraphics.drawString(mc.font, label, x + barWidth + 4, y + barHeight - 8, color, true);
    }
}
