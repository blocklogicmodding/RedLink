package com.blocklogic.redlink.screen.cusom;

import com.blocklogic.redlink.RedLink;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class TransceiverHubScreen extends AbstractContainerScreen<TransceiverHubMenu> {
    private static final ResourceLocation GUI_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(RedLink.MODID, "textures/gui/transceiver_gui.png");

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
    }

    public TransceiverHubScreen(TransceiverHubMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);

        this.imageHeight = 243;
        this.inventoryLabelY = this.imageHeight - 96;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float v, int i, int i1) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, GUI_TEXTURE);

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        guiGraphics.blit(GUI_TEXTURE, x, y, 0, 0, imageWidth, imageHeight);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
