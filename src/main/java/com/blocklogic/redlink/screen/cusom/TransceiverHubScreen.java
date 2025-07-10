package com.blocklogic.redlink.screen.cusom;

import com.blocklogic.redlink.Config;
import com.blocklogic.redlink.RedLink;
import com.mojang.blaze3d.systems.RenderSystem;
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

    private static final int[] CHANNEL_Y_POSITIONS = {25, 41, 57, 73, 89, 105, 121, 137};

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

        channelNameBoxes.clear();
        pulseFrequencyBoxes.clear();
        resetChannelButtons.clear();

        for (int i = 0; i < 8; i++) {
            createChannelWidgets(i);
        }

        resetAllButton = Button.builder(Component.translatable("redlink.gui.reset_all"),
                        button -> menu.resetAllChannels())
                .bounds(leftPos + 133, topPos + 144, 36, 11)
                .build();
        addRenderableWidget(resetAllButton);
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

        if (resetAllButton.isHoveredOrFocused()) {
            guiGraphics.renderTooltip(font, Component.translatable("redlink.gui.reset_all_tooltip"), mouseX, mouseY);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        boolean anyFieldFocused = channelNameBoxes.stream().anyMatch(EditBox::isFocused) ||
                pulseFrequencyBoxes.stream().anyMatch(EditBox::isFocused);

        if (anyFieldFocused) {
            if (this.minecraft.options.keyInventory.matches(keyCode, scanCode)) {
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

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    public void containerTick() {
        super.containerTick();

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
}