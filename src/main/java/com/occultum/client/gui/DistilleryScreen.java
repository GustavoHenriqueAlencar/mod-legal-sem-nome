package com.occultum.client.gui;

import com.occultum.common.machine.distillery.DistilleryMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public class DistilleryScreen extends AbstractContainerScreen<DistilleryMenu> {

    public DistilleryScreen(DistilleryMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int left = this.leftPos;
        int top = this.topPos;

        guiGraphics.fill(left, top, left + imageWidth, top + imageHeight, 0xEE181C24);
        guiGraphics.fill(left + 2, top + 2, left + imageWidth - 2, top + imageHeight - 2, 0xFF242C38);

        for (Slot slot : this.menu.slots) {
            int sx = left + slot.x - 1;
            int sy = top + slot.y - 1;
            guiGraphics.fill(sx, sy, sx + 18, sy + 18, 0xFF10141C);
            guiGraphics.fill(sx + 1, sy + 1, sx + 17, sy + 17, 0xFF344050);
        }

        // Distillation progress bar (x=72, y=30, width=32, height=12)
        int barX = left + 72;
        int barY = top + 30;
        guiGraphics.fill(barX, barY, barX + 32, barY + 12, 0xFF10141C);
        int prog = (int) (this.menu.getDistillationProgress() * 32.0F);
        if (prog > 0) {
            int color = this.menu.hasAltarPower() ? 0xFF40D8FF : 0xFFCC3333;
            guiGraphics.fill(barX, barY, barX + prog, barY + 12, color);
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
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0xFFD8F0FF, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0xFFB0C8D8, false);
    }
}
