package com.occultum.client.gui;

import com.occultum.common.altar.AltarMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class AltarScreen extends AbstractContainerScreen<AltarMenu> {

    public AltarScreen(AltarMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 90;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int left = this.leftPos;
        int top = this.topPos;

        // Fundo Cerimonial Elegante com Moldura
        guiGraphics.fill(left, top, left + imageWidth, top + imageHeight, 0xEE1E1428); // Roxo Místico Escuro
        guiGraphics.fill(left + 2, top + 2, left + imageWidth - 2, top + imageHeight - 2, 0xFF281C38);

        // Barra de Energia Telúrica
        int barX = left + 18;
        int barY = top + 50;
        int barWidth = 140;
        int barHeight = 16;

        guiGraphics.fill(barX, barY, barX + barWidth, barY + barHeight, 0xFF10081C);

        int current = menu.getCurrentPower();
        int max = menu.getMaxPower();
        if (max > 0) {
            float ratio = Math.min(1.0f, (float) current / (float) max);
            int filledWidth = (int) (barWidth * ratio);
            guiGraphics.fill(barX, barY, barX + filledWidth, barY + barHeight, 0xFF9932CC); // Púrpura Místico Brilhante
            guiGraphics.fill(barX, barY, barX + filledWidth, barY + 2, 0xFFDA70D6); // Realce Superior
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // Título centralizado: "Altar"
        int titleX = (this.imageWidth - this.font.width(this.title)) / 2;
        guiGraphics.drawString(this.font, this.title, titleX, 12, 0xFFE0E0E0, false);

        // Informação de estado formatada: <P_atual> / <P_max> (x<M_rec>)
        // Exemplo da especificação: 1200 / 4800 (x3)
        String statusText = String.format("%d / %d (x%d)", menu.getCurrentPower(), menu.getMaxPower(), menu.getRechargeMultiplier());
        int statusX = (this.imageWidth - this.font.width(statusText)) / 2;
        guiGraphics.drawString(this.font, Component.literal(statusText), statusX, 32, 0xFF55FF55, false);
    }
}
