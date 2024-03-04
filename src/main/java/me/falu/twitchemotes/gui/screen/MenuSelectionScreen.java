package me.falu.twitchemotes.gui.screen;

import me.falu.twitchemotes.emote.Emote;
import me.falu.twitchemotes.gui.widget.LimitlessButtonWidget;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;

public class MenuSelectionScreen extends Screen {
    private final Emote credentialsIconEmote;
    private final Emote otherIconEmote;

    public MenuSelectionScreen() {
        super(new LiteralText("Menu Selection"));
        this.credentialsIconEmote = new Emote(
                "Nerd",
                "6134bc74f67d73ea27e44b0f",
                "https://cdn.7tv.app/emote/6134bc74f67d73ea27e44b0f/4x.webp",
                Emote.ImageType.WEBP
        );
        this.otherIconEmote = new Emote(
                "Chatting",
                "60ef410f48cde2fcc3eb5caa",
                "https://cdn.7tv.app/emote/60ef410f48cde2fcc3eb5caa/4x.webp",
                Emote.ImageType.WEBP
        );
    }

    @Override
    protected void init() {
        int buttonWidth = this.width / 4;
        int buttonHeight = this.height / 2;
        this.addButton(new LimitlessButtonWidget(buttonWidth, this.width / 4, buttonWidth, buttonHeight, new LiteralText("aaa"), b -> { }));
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
    }
}
