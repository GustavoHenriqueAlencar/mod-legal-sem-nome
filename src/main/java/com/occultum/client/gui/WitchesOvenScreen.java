package com.occultum.client.gui;

import com.occultum.common.machine.oven.WitchesOvenMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public class WitchesOvenScreen extends AbstractContainerScreen<WitchesOvenMenu> {

    public WitchesOvenScreen(WitchesOvenMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int left = this.leftPos;
        int top = this.topPos;

        guiGraphics.fill(left, top, left + imageWidth, top + imageHeight, 0xEE1E1A18);
        guiGraphics.fill(left + 2, top + 2, left + imageWidth - 2, top + imageHeight - 2, 0xFF2D2522);

        for (Slot slot : this.menu.slots) {
            int sx = left + slot.x - 1;
            int sy = top + slot.y - 1;
            guiGraphics.fill(sx, sy, sx + 18, sy + 18, 0xFF15100E);
            guiGraphics.fill(sx + 1, sy + 1, sx + 17, sy + 17, 0xFF3E3430);
        }

        // Progress arrow (x=76, y=22, width=28, height=12)
        int arrowX = left + 76;
        int arrowY = top + 22;
        guiGraphics.fill(arrowX, arrowY, arrowX + 28, arrowY + 12, 0xFF181210);
        int prog = (int) (this.menu.getBurnProgress() * 28.0F);
        if (prog > 0) {
            guiGraphics.fill(arrowX, arrowY, arrowX + prog, arrowY + 12, 0xFFFF8820);
        }

        // Burn flame indicator (x=58, y=36, width=12, height=14)
        int flameX = left + 58;
        int flameY = top + 36;
        guiGraphics.fill(flameX, flameY, flameX + 12, flameY + 14, 0xFF181210);
        if (this.menu.isLit()) {
            int litH = (int) (this.menu.getLitProgress() * 14.0F);
            guiGraphics.fill(flameX, flameY + (14 - litH), flameX + 12, flameY + 14, 0xFFFF5500);
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
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0xFFF0D8C0, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0xFFD0B8A0, false);
    }
}
