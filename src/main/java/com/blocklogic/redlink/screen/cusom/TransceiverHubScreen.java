package com.blocklogic.redlink.screen.cusom;

import com.blocklogic.redlink.Config;
import com.blocklogic.redlink.RedLink;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.ArrayList;
import java.util.List;

public class TransceiverHubScreen extends AbstractContainerScreen<TransceiverHubMenu> {
    private static final ResourceLocation GUI_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(RedLink.MODID, "textures/gui/transceiver_hub_gui.png");

    private final List<EditBox> channelNameBoxes = new ArrayList<>();
    private final List<EditBox> pulseFrequencyBoxes = new ArrayList<>();

    private EditBox hubNameBox;
    private static final int HUB_NAME_X = 39;
    private static final int HUB_NAME_Y = 8;
    private static final int HUB_NAME_WIDTH = 158;
    private static final int HUB_NAME_HEIGHT = 10;

    private static final int RESET_BUTTON_X = 133;
    private static final int RESET_BUTTON_Y = 154;
    private static final int RESET_BUTTON_WIDTH = 36;
    private static final int RESET_BUTTON_HEIGHT = 11;
    private static final int RESET_BUTTON_U = 176;
    private static final int RESET_BUTTON_V = 0;
    private static final int RESET_BUTTON_HOVER_V = 11;

    private Button resetAllButton;
    private final List<Button> resetChannelButtons = new ArrayList<>();

    private static final int CHANNEL_NAME_X = 9;
    private static final int CHANNEL_NAME_WIDTH = 98;
    private static final int CHANNEL_NAME_HEIGHT = 10;

    private static final int COLOR_SQUARE_X = 114;
    private static final int COLOR_SQUARE_SIZE = 14;

    private static final int PULSE_FREQ_X = 135;
    private static final int PULSE_FREQ_WIDTH = 34;
    private static final int PULSE_FREQ_HEIGHT = 12;

    private static final int[] CHANNEL_Y_POSITIONS = {26, 42, 58, 74, 90, 106, 122, 138};

    public TransceiverHubScreen(TransceiverHubMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 256;
        this.inventoryLabelY = this.imageHeight - 96;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;

        // Hub Name Box
        hubNameBox = new EditBox(font,
                leftPos + HUB_NAME_X,
                topPos + HUB_NAME_Y,
                HUB_NAME_WIDTH,
                HUB_NAME_HEIGHT,
                Component.translatable("redlink.gui.hub_name"));

        hubNameBox.setMaxLength(menu.getMaxChannelNameLength());
        hubNameBox.setValue(menu.getHubName());
        hubNameBox.setResponder(this::onHubNameChanged);
        hubNameBox.setBordered(false);
        addRenderableWidget(hubNameBox);

        channelNameBoxes.clear();
        pulseFrequencyBoxes.clear();
        resetChannelButtons.clear();

        for (int i = 0; i < 8; i++) {
            createChannelWidgets(i);
        }
    }

    private void onHubNameChanged(String text) {
        if (menu.isValidHubName(text)) {
            menu.updateHubName(text);
        }
    }

    private void createChannelWidgets(int channel) {
        int yPos = CHANNEL_Y_POSITIONS[channel];

        EditBox nameBox = new EditBox(font,
                leftPos + CHANNEL_NAME_X,
                topPos + yPos,
                CHANNEL_NAME_WIDTH,
                CHANNEL_NAME_HEIGHT,
                Component.translatable("redlink.gui.channel_name", channel + 1));

        nameBox.setMaxLength(menu.getMaxChannelNameLength());
        nameBox.setValue(menu.getChannelName(channel));
        nameBox.setResponder(text -> onChannelNameChanged(channel, text));
        channelNameBoxes.add(nameBox);
        nameBox.setBordered(false);;
        addRenderableWidget(nameBox);

        EditBox freqBox = new EditBox(font,
                leftPos + PULSE_FREQ_X,
                topPos + yPos,
                PULSE_FREQ_WIDTH,
                PULSE_FREQ_HEIGHT,
                Component.translatable("redlink.gui.pulse_frequency", channel + 1));

        freqBox.setMaxLength(4);
        freqBox.setValue(String.valueOf(menu.getPulseFrequency(channel)));
        freqBox.setFilter(this::isValidFrequencyInput);
        freqBox.setResponder(text -> onPulseFrequencyChanged(channel, text));
        pulseFrequencyBoxes.add(freqBox);
        freqBox.setBordered(false);
        addRenderableWidget(freqBox);
    }

    private void onChannelNameChanged(int channel, String text) {
        if (menu.isValidChannelName(text)) {
            updateChannel(channel);
        }
    }

    private void onPulseFrequencyChanged(int channel, String text) {
        try {
            int frequency = Integer.parseInt(text);
            if (menu.isValidPulseFrequency(frequency)) {
                updateChannel(channel);
            }
        } catch (NumberFormatException e) {

        }
    }

    private void updateChannel(int channel) {
        String name = channelNameBoxes.get(channel).getValue();
        String freqText = pulseFrequencyBoxes.get(channel).getValue();

        try {
            int frequency = Integer.parseInt(freqText);
            menu.updateChannel(channel, name, frequency);
        } catch (NumberFormatException e) {
            menu.updateChannel(channel, name, menu.getPulseFrequency(channel));
        }
    }

    private boolean isValidFrequencyInput(String text) {
        if (text.isEmpty()) return true;
        try {
            int value = Integer.parseInt(text);
            return value >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, GUI_TEXTURE);

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        guiGraphics.blit(GUI_TEXTURE, x, y, 0, 0, imageWidth, imageHeight);

        renderResetButton(guiGraphics, x, y, mouseX, mouseY);

    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderTooltip(guiGraphics, mouseX, mouseY);

        for (int i = 0; i < 8; i++) {
            int squareX = leftPos + COLOR_SQUARE_X;
            int squareY = topPos + CHANNEL_Y_POSITIONS[i] + 1;

            if (mouseX >= squareX && mouseX < squareX + COLOR_SQUARE_SIZE &&
                    mouseY >= squareY && mouseY < squareY + COLOR_SQUARE_SIZE) {

                int currentCount = menu.getHubEntity().getTransceiverCount(i);
                int maxCount = Config.getMaxTransceiversPerChannel();

                List<Component> tooltip = List.of(
                        Component.translatable("redlink.gui.channel_color_tooltip", i + 1),
                        Component.translatable("redlink.gui.channel_info",
                                menu.getChannelName(i), menu.getPulseFrequency(i))
                );
                guiGraphics.renderComponentTooltip(font, tooltip, mouseX, mouseY);
                break;
            }
        }

        for (int i = 0; i < pulseFrequencyBoxes.size(); i++) {
            EditBox freqBox = pulseFrequencyBoxes.get(i);
            int freqX = leftPos + PULSE_FREQ_X;
            int freqY = topPos + CHANNEL_Y_POSITIONS[i];

            if (mouseX >= freqX && mouseX < freqX + PULSE_FREQ_WIDTH &&
                    mouseY >= freqY && mouseY < freqY + PULSE_FREQ_HEIGHT) {

                List<Component> tooltip = List.of(
                        Component.translatable("redlink.gui.frequency_range_tooltip",
                                menu.getMinPulseFrequency(), menu.getMaxPulseFrequency())
                );
                guiGraphics.renderComponentTooltip(font, tooltip, mouseX, mouseY);
                break;
            }
        }

        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        if (isMouseOverButton(mouseX, mouseY, x + RESET_BUTTON_X, y + RESET_BUTTON_Y, RESET_BUTTON_WIDTH, RESET_BUTTON_HEIGHT)) {
            List<Component> tooltip = List.of(
                    Component.translatable("redlink.gui.reset_all_tooltip").withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.DARK_RED),
                    Component.translatable("redlink.gui.reset_all_warning").withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.DARK_GRAY)
            );
            guiGraphics.renderComponentTooltip(font, tooltip, mouseX, mouseY);
        }

        if (isMouseOverButton(mouseX, mouseY, x + HUB_NAME_X, y + HUB_NAME_Y, HUB_NAME_WIDTH, HUB_NAME_HEIGHT)) {
            List<Component> tooltip = List.of(
                    Component.translatable("redlink.gui.hub_name_info", menu.getMaxChannelNameLength())
            );
            guiGraphics.renderComponentTooltip(font, tooltip, mouseX, mouseY);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        boolean anyFieldFocused = hubNameBox.isFocused() ||
                channelNameBoxes.stream().anyMatch(EditBox::isFocused) ||
                pulseFrequencyBoxes.stream().anyMatch(EditBox::isFocused);

        if (anyFieldFocused) {
            if (this.minecraft.options.keyInventory.matches(keyCode, scanCode)) {
                return true;
            }
        }

        if (hubNameBox.isFocused()) {
            if (hubNameBox.keyPressed(keyCode, scanCode, modifiers)) {
                return true;
            }
        }

        for (EditBox nameBox : channelNameBoxes) {
            if (nameBox.isFocused()) {
                if (nameBox.keyPressed(keyCode, scanCode, modifiers)) {
                    return true;
                }
            }
        }

        for (EditBox freqBox : pulseFrequencyBoxes) {
            if (freqBox.isFocused()) {
                if (freqBox.keyPressed(keyCode, scanCode, modifiers)) {
                    return true;
                }
            }
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (hubNameBox.isFocused()) {
            return hubNameBox.charTyped(codePoint, modifiers);
        }

        for (EditBox nameBox : channelNameBoxes) {
            if (nameBox.isFocused()) {
                return nameBox.charTyped(codePoint, modifiers);
            }
        }

        for (EditBox freqBox : pulseFrequencyBoxes) {
            if (freqBox.isFocused()) {
                return freqBox.charTyped(codePoint, modifiers);
            }
        }

        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        boolean anyFieldFocused = channelNameBoxes.stream().anyMatch(EditBox::isFocused) ||
                pulseFrequencyBoxes.stream().anyMatch(EditBox::isFocused);

        if (anyFieldFocused) {
            channelNameBoxes.forEach(box -> box.setFocused(false));
            pulseFrequencyBoxes.forEach(box -> box.setFocused(false));
            return;
        }

        super.onClose();
    }

    private void renderResetButton(GuiGraphics guiGraphics, int guiX, int guiY, int mouseX, int mouseY) {
        int buttonX = guiX + RESET_BUTTON_X;
        int buttonY = guiY + RESET_BUTTON_Y;

        boolean isHovered = isMouseOverButton(mouseX, mouseY, buttonX, buttonY, RESET_BUTTON_WIDTH, RESET_BUTTON_HEIGHT);

        int v = isHovered ? RESET_BUTTON_HOVER_V : RESET_BUTTON_V;

        guiGraphics.blit(GUI_TEXTURE, buttonX, buttonY, RESET_BUTTON_U, v, RESET_BUTTON_WIDTH, RESET_BUTTON_HEIGHT);

        Component resetText = Component.translatable("redlink.gui.reset_button");
        int textWidth = font.width(resetText);
        float scale = 0.7f;
        int scaledTextWidth = (int)(textWidth * scale);

        int textX = buttonX + (RESET_BUTTON_WIDTH - scaledTextWidth) / 2;
        int textY = buttonY + (RESET_BUTTON_HEIGHT - (int)(font.lineHeight * scale)) / 2;

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(textX, textY, 0);
        guiGraphics.pose().scale(scale, scale, 1.0f);
        guiGraphics.drawString(font, resetText, 0, 0, 0x404040, false);
        guiGraphics.pose().popPose();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        if (isMouseOverButton(mouseX, mouseY, x + RESET_BUTTON_X, y + RESET_BUTTON_Y, RESET_BUTTON_WIDTH, RESET_BUTTON_HEIGHT)) {
            menu.resetAllChannels();
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean isMouseOverButton(double mouseX, double mouseY, int buttonX, int buttonY, int width, int height) {
        return mouseX >= buttonX && mouseX < buttonX + width &&
                mouseY >= buttonY && mouseY < buttonY + height;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    public void containerTick() {
        super.containerTick();

        String currentHubName = menu.getHubName();
        if (!hubNameBox.isFocused() && !hubNameBox.getValue().equals(currentHubName)) {
            hubNameBox.setValue(currentHubName);
        }

        for (int i = 0; i < 8; i++) {
            EditBox nameBox = channelNameBoxes.get(i);
            EditBox freqBox = pulseFrequencyBoxes.get(i);

            String currentName = menu.getChannelName(i);
            String currentFreq = String.valueOf(menu.getPulseFrequency(i));

            if (!nameBox.isFocused() && !nameBox.getValue().equals(currentName)) {
                nameBox.setValue(currentName);
            }

            if (!freqBox.isFocused() && !freqBox.getValue().equals(currentFreq)) {
                freqBox.setValue(currentFreq);
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0x404040, false);
    }
}