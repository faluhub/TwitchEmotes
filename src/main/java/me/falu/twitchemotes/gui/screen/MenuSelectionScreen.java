package me.falu.twitchemotes.gui.screen;

import me.falu.twitchemotes.emote.Emote;
import me.falu.twitchemotes.gui.widget.LimitlessButtonWidget;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;

public class MenuSelectionScreen extends Screen {
    private static final Emote NERD = new Emote(
            "Nerd",
            "6134bc74f67d73ea27e44b0f",
            "https://cdn.7tv.app/emote/6134bc74f67d73ea27e44b0f/4x.webp",
            Emote.ImageType.WEBP
    );
    private static final Emote CHATTING = new Emote(
            "Chatting",
            "60ef410f48cde2fcc3eb5caa",
            "https://cdn.7tv.app/emote/60ef410f48cde2fcc3eb5caa/4x.webp",
            Emote.ImageType.WEBP
    );

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

        ButtonWidget credentialsButton = this.addButton(new LimitlessButtonWidget(
                this.width / 4 - gap / 2,
                y,
                buttonWidth,
                buttonHeight,
                new LiteralText("Credentials"),
                new LimitlessButtonWidget.ButtonEmote(
                        NERD,
                        (this.width / 4.0F - gap / 2.0F) + buttonWidth / 2.0F,
                        y + buttonHeight / 4.0F
                ),
                b -> {
                    if (this.client != null) {
                        this.client.openScreen(new CredentialsConfigScreen());
                    }
                }
        ));
        ButtonWidget otherButton = this.addButton(new LimitlessButtonWidget(
                this.width / 2 + this.width / 4 - buttonWidth + gap / 2,
                y,
                buttonWidth,
                buttonHeight,
                new LiteralText("Other"),
                new LimitlessButtonWidget.ButtonEmote(
                        CHATTING,
                        (this.width / 2.0F + this.width / 4.0F - buttonWidth + gap / 2.0F) + buttonWidth / 2.0F,
                        y + buttonHeight / 4.0F
                ),
                b -> {
                    if (this.client != null) {
                        this.client.openScreen(null);
                    }
                }
        ));
        int closeButtonWidth = (otherButton.x + otherButton.getWidth()) - credentialsButton.x;
        this.addButton(new ButtonWidget(
                this.width / 2 - closeButtonWidth / 2,
                this.height - 20 - yOffset / 2,
                closeButtonWidth,
                20,
                ScreenTexts.DONE,
                b -> this.onClose()
        ));
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
    }
}
