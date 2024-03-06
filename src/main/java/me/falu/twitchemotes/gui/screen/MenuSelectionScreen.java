package me.falu.twitchemotes.gui.screen;

import me.falu.twitchemotes.TwitchEmotes;
import me.falu.twitchemotes.emote.EmoteConstants;
import me.falu.twitchemotes.gui.widget.LimitlessButtonWidget;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;

public class MenuSelectionScreen extends Screen {
    private ButtonWidget credentialsButton;
    private ButtonWidget otherButton;

    public MenuSelectionScreen() {
        super(new LiteralText("Menu Selection"));
    }

    @Override
    protected void init() {
        int buttonWidth = this.width / 5;
        int buttonHeight = this.height - this.height / 3;
        int gap = 10;
        int yOffset = 16;
        int y = this.height / 2 - buttonHeight / 2 - yOffset;

        this.credentialsButton = this.addDrawableChild(new LimitlessButtonWidget(
                this.width / 4 - gap / 2,
                y,
                buttonWidth,
                buttonHeight,
                new LiteralText("Credentials"),
                EmoteConstants.NERD,
                b -> {
                    if (this.client != null) {
                        this.client.setScreen(new CredentialsConfigScreen());
                    }
                }
        ));
        this.otherButton = this.addDrawableChild(new LimitlessButtonWidget(
                this.width / 2 + this.width / 4 - buttonWidth + gap / 2,
                y,
                buttonWidth,
                buttonHeight,
                new LiteralText("Other"),
                EmoteConstants.CHATTING,
                b -> {
                    if (this.client != null) {
                        this.client.setScreen(new OtherConfigScreen());
                    }
                }
        ));
        int closeButtonWidth = (this.otherButton.x + this.otherButton.getWidth()) - this.credentialsButton.x;
        this.addDrawableChild(new LimitlessButtonWidget(
                this.width / 2 - closeButtonWidth / 2,
                this.height - 20 - 10,
                closeButtonWidth,
                20,
                ScreenTexts.DONE,
                null,
                b -> this.close()
        ));
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);

        MutableText statusText = new LiteralText("Chat: ").append(new LiteralText(TwitchEmotes.CHAT_CONNECTED ? Formatting.GREEN + "Connected" : Formatting.RED + "Disconnected"));
        drawCenteredText(
                matrices,
                this.textRenderer,
                statusText,
                this.credentialsButton.x / 2,
                this.height / 2 - this.textRenderer.fontHeight / 2,
                0xFFFFFF
        );
        MutableText emotesText = new LiteralText("Emotes: ").append(new LiteralText("" + TwitchEmotes.getEmoteKeys().size()).formatted(Formatting.BLUE));
        drawCenteredText(
                matrices,
                this.textRenderer,
                emotesText,
                this.width - (this.width - (this.otherButton.x + this.otherButton.getWidth())) / 2,
                this.height / 2 - this.textRenderer.fontHeight / 2,
                0xFFFFFF
        );
    }
}
