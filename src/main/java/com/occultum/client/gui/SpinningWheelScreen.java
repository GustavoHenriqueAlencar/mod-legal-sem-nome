package com.occultum.client.gui;

import com.occultum.common.machine.spinningwheel.SpinningWheelMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public class SpinningWheelScreen extends AbstractContainerScreen<SpinningWheelMenu> {

    public SpinningWheelScreen(SpinningWheelMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int left = this.leftPos;
        int top = this.topPos;

        guiGraphics.fill(left, top, left + imageWidth, top + imageHeight, 0xEE221C14);
        guiGraphics.fill(left + 2, top + 2, left + imageWidth - 2, top + imageHeight - 2, 0xFF342A1E);

        for (Slot slot : this.menu.slots) {
            int sx = left + slot.x - 1;
            int sy = top + slot.y - 1;
            guiGraphics.fill(sx, sy, sx + 18, sy + 18, 0xFF16120C);
            guiGraphics.fill(sx + 1, sy + 1, sx + 17, sy + 17, 0xFF483A2A);
        }

        // Spinning progress bar (x=66, y=38, width=44, height=10)
        int barX = left + 66;
        int barY = top + 38;
        guiGraphics.fill(barX, barY, barX + 44, barY + 10, 0xFF16120C);
        int prog = (int) (this.menu.getSpinProgress() * 44.0F);
        if (prog > 0) {
            int color = this.menu.hasAltarPower() ? 0xFFFFD840 : 0xFFCC3333;
            guiGraphics.fill(barX, barY, barX + prog, barY + 10, color);
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
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0xFFFFF0C8, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0xFFD8C8A0, false);
    }
}
