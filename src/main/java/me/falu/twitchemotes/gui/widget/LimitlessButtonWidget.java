package me.falu.twitchemotes.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import me.falu.twitchemotes.TwitchEmotes;
import me.falu.twitchemotes.emote.Emote;
import me.falu.twitchemotes.emote.texture.EmoteTextureHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;

public class LimitlessButtonWidget extends ButtonWidget {
    private static final int BG_COLOR = ColorHelper.Argb.getArgb(150, 0, 0, 0);
    private static final int BG_INACTIVE_COLOR = ColorHelper.Argb.getArgb(80, 0, 0, 0);
    private final Emote emote;

    public LimitlessButtonWidget(int x, int y, int width, int height, Text message, Emote emote, PressAction onPress) {
        super(x, y, width, height, message, onPress, ButtonWidget.DEFAULT_NARRATION_SUPPLIER);
        this.emote = emote;
    }

    @Override
    public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        MinecraftClient client = MinecraftClient.getInstance();

        matrices.push();
        RenderSystem.setShaderTexture(0, WIDGETS_TEXTURE);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();

        int textureY = this.getTextureY();

        this.drawTexture(matrices, this.getX(), this.getY(), 0, textureY, 3, 3);
        this.drawTexture(matrices, this.getX() + this.width - 3, this.getY(), 200 - 3, textureY, 3, 3);
        this.drawTexture(matrices, this.getX(), this.getY() + this.height - 3, 0, 20 - 3 + textureY, 3, 3);
        this.drawTexture(matrices, this.getX() + this.width - 3, this.getY() + this.height - 3, 200 - 3, 20 - 3 + textureY, 3, 3);

        drawTexture(matrices, this.getX() + 3, this.getY(), this.width - 6, 3, 3, textureY, 1, 3, 256, 256);
        drawTexture(matrices, this.getX(), this.getY() + 3, 3, this.height - 6, 0, 3 + textureY, 3, 1, 256, 256);
        drawTexture(matrices, this.getX() + 3, this.getY() + this.height - 3, this.width - 6, 3, 3, 20 - 3 + textureY, 1, 3, 256, 256);
        drawTexture(matrices, this.getX() + this.width - 3, this.getY() + 3, 3, this.height - 6, 200 - 3, 3 + textureY, 3, 1, 256, 256);

        fill(matrices, this.getX() + 3, this.getY() + 3, this.getX() + this.width - 3, this.getY() + this.height - 3, this.active ? BG_COLOR : BG_INACTIVE_COLOR);

        float textScale = this.emote != null ? 1.3F : 1.0F;
        int color = this.active ? 0xFFFFFF : 0xA0A0A0;
        int textY = this.emote != null
                    ? this.getY() + this.height - (this.height + client.textRenderer.fontHeight) / 4
                    : this.getY() + (this.height - 8) / 2;
        matrices.scale(textScale, textScale, 1.0F);
        drawCenteredTextWithShadow(
                matrices,
                client.textRenderer,
                this.getMessage().asOrderedText(),
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

    private int getTextureY() {
        return 46 + (!this.active ? 0 : (this.isHovered() ? 2 : 1)) * 20;
    }
}
