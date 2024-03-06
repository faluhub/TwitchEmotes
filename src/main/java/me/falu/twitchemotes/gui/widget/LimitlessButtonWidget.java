package me.falu.twitchemotes.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import me.falu.twitchemotes.TwitchEmotes;
import me.falu.twitchemotes.emote.Emote;
import me.falu.twitchemotes.emote.texture.EmoteTextureHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;

public class LimitlessButtonWidget extends ButtonWidget {
    private static final ButtonTextures TEXTURES = new ButtonTextures(
            new Identifier("textures/gui/sprites/widget/button.png"),
            new Identifier("textures/gui/sprites/widget/button_disabled.png"),
            new Identifier("textures/gui/sprites/widget/button_highlighted.png")
    );
    private static final int BG_COLOR = ColorHelper.Argb.getArgb(150, 0, 0, 0);
    private static final int BG_INACTIVE_COLOR = ColorHelper.Argb.getArgb(80, 0, 0, 0);
    private final Emote emote;

    public LimitlessButtonWidget(int x, int y, int width, int height, Text message, Emote emote, PressAction onPress) {
        super(x, y, width, height, message, onPress, ButtonWidget.DEFAULT_NARRATION_SUPPLIER);
        this.emote = emote;
    }

    @Override
    public void renderButton(DrawContext context, int mouseX, int mouseY, float delta) {
        MinecraftClient client = MinecraftClient.getInstance();
        MatrixStack matrices = context.getMatrices();

        matrices.push();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);

        Identifier texture = TEXTURES.get(this.active, this.isSelected());

        context.drawTexture(texture, this.getX(), this.getY(), 0, 0, 3, 3, 200, 20);
        context.drawTexture(texture, this.getX() + this.width - 3, this.getY(), 200 - 3, 0, 3, 3, 200, 20);
        context.drawTexture(texture, this.getX(), this.getY() + this.height - 3, 0, 20 - 3, 3, 3, 200, 20);
        context.drawTexture(texture, this.getX() + this.width - 3, this.getY() + this.height - 3, 200 - 3, 20 - 3, 3, 3, 200, 20);

        context.drawTexture(texture, this.getX() + 3, this.getY(), this.width - 6, 3, 3, 0, 1, 3, 200, 20);
        context.drawTexture(texture, this.getX(), this.getY() + 3, 3, this.height - 6, 0, 3, 3, 1, 200, 20);
        context.drawTexture(texture, this.getX() + 3, this.getY() + this.height - 3, this.width - 6, 3, 3, 20 - 3, 1, 3, 200, 20);
        context.drawTexture(texture, this.getX() + this.width - 3, this.getY() + 3, 3, this.height - 6, 200 - 3, 3, 3, 1, 200, 20);

        context.fill(this.getX() + 3, this.getY() + 3, this.getX() + this.width - 3, this.getY() + this.height - 3, this.active ? BG_COLOR : BG_INACTIVE_COLOR);

        int color = this.active ? 0xFFFFFF : 0xA0A0A0;
        float textScale = this.emote != null ? 1.3F : 1.0F;
        int textY = this.emote != null
                    ? this.getY() + this.height - (this.height + client.textRenderer.fontHeight) / 4
                    : this.getY() + (this.height - 8) / 2;
        matrices.scale(textScale, textScale, 1.0F);
        context.drawCenteredTextWithShadow(
                client.textRenderer,
                this.getMessage(),
                (int) ((this.getX() + this.width / 2.0F) / textScale),
                (int) (textY / textScale),
                color | MathHelper.ceil(this.alpha * 255.0F) << 24
        );
        matrices.pop();
        if (this.emote != null) {
            matrices.push();
            // 6.0F scale looked good on the big vertical buttons, so I just tested what that would've been on the size of the button
            float emoteScale = this.height / 27.0F;
            matrices.scale(emoteScale, emoteScale, 1.0F);
            EmoteTextureHandler textureHandler = this.emote.textureHandler;
            NativeImage image = textureHandler.getImage();
            if (image != null) {
                this.emote.createTextureBuffer(
                        matrices.peek().getPositionMatrix(),
                        (this.getX() + this.width / 2.0F) / emoteScale - textureHandler.getWidth() / 2.0F,
                        this.getY() / emoteScale + TwitchEmotes.EMOTE_SIZE / 2.0F,
                        1.0F
                );
                textureHandler.postRender();
            }
            matrices.pop();
        }
    }
}
