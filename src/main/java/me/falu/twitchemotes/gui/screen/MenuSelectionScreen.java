package me.falu.twitchemotes.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import me.falu.twitchemotes.emote.Emote;
import me.falu.twitchemotes.emote.texture.EmoteTextureHandler;
import me.falu.twitchemotes.gui.widget.LimitlessButtonWidget;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.Matrix4f;

public class MenuSelectionScreen extends Screen {
    private final Emote credentialsIconEmote;
    private ButtonWidget credentialsButton;
    private final Emote otherIconEmote;
    private ButtonWidget otherButton;

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
        int buttonWidth = this.width / 5;
        int buttonHeight = this.height - this.height / 3;
        int gap = 10;
        int y = this.height / 2 - buttonHeight / 2;
        int yOffset = 16;

        this.credentialsButton = this.addButton(new LimitlessButtonWidget(
                this.width / 4 - gap / 2,
                y - yOffset,
                buttonWidth,
                buttonHeight,
                new LiteralText("Credentials"),
                b -> { }
        ));
        this.otherButton = this.addButton(new LimitlessButtonWidget(
                this.width / 2 + this.width / 4 - buttonWidth + gap / 2,
                y - yOffset,
                buttonWidth,
                buttonHeight,
                new LiteralText("Other"),
                b -> { }
        ));
        int closeButtonWidth = (this.otherButton.x + this.otherButton.getWidth()) - this.credentialsButton.x;
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
    @SuppressWarnings("deprecation")
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);

        if (this.credentialsButton == null || this.otherButton == null) {
            return;
        }

        float scale = 6.0F;
        Matrix4f matrix = matrices.peek().getModel();
        Emote.DrawData[] emotes = new Emote.DrawData[] {
                new Emote.DrawData(
                        this.credentialsIconEmote,
                        this.credentialsButton.x + this.credentialsButton.getWidth() / 2.0F,
                        this.credentialsButton.y + this.credentialsButton.getHeight() / 4.0F,
                        matrix,
                        1.0F
                ),
                new Emote.DrawData(
                        this.otherIconEmote,
                        this.otherButton.x + this.otherButton.getWidth() / 2.0F,
                        this.otherButton.y + this.otherButton.getHeight() / 4.0F,
                        matrix,
                        1.0F
                )
        };

        for (Emote.DrawData data : emotes) {
            RenderSystem.pushMatrix();
            EmoteTextureHandler textureHandler = data.emote.textureHandler;
            if (textureHandler.getImage() != null || textureHandler.loading) {
                RenderSystem.scalef(scale, scale, 1.0F);
                data.emote.createTextureBuffer(
                        data.matrix,
                        data.x / scale - textureHandler.getWidth() / 2.0F,
                        data.y / scale,
                        data.alpha
                );
                textureHandler.postRender();
            }
            RenderSystem.popMatrix();
        }
    }
}
