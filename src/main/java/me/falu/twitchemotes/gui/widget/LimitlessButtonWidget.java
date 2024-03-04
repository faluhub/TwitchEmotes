package me.falu.twitchemotes.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import me.falu.twitchemotes.emote.Emote;
import me.falu.twitchemotes.emote.texture.EmoteTextureHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.BackgroundHelper;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

public class LimitlessButtonWidget extends ButtonWidget {
    private static final int BG_COLOR = BackgroundHelper.ColorMixer.getArgb(150, 0, 0, 0);
    private static final int BG_INACTIVE_COLOR = BackgroundHelper.ColorMixer.getArgb(80, 0, 0, 0);
    private final ButtonEmote drawData;

    public LimitlessButtonWidget(int x, int y, int width, int height, Text message, ButtonEmote drawData, PressAction onPress) {
        super(x, y, width, height, message, onPress);
        this.drawData = drawData;
    }

    @SuppressWarnings("unused")
    public LimitlessButtonWidget(int x, int y, int width, int height, Text message, ButtonEmote drawData, PressAction onPress, TooltipSupplier tooltipSupplier) {
        super(x, y, width, height, message, onPress, tooltipSupplier);
        this.drawData = drawData;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        MinecraftClient client = MinecraftClient.getInstance();
        RenderSystem.pushMatrix();
        client.getTextureManager().bindTexture(WIDGETS_LOCATION);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, this.alpha);
        int yOffset = this.getYImage(this.isHovered());
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        this.drawTexture(matrices, this.x, this.y, 0, 46 + yOffset * 20, 3, 3);
        drawTexture(matrices, this.x + 3, this.y, this.width - 6, 3, 3, 46 + yOffset * 20, 1, 3, 256, 256);
        this.drawTexture(matrices, this.x + this.width - 3, this.y, 197, 46 + yOffset * 20, 3, 3);
        drawTexture(matrices, this.x, this.y + 3, 3, this.height - 6, 0, 49 + yOffset * 20, 3, 1, 256, 256);
        this.drawTexture(matrices, this.x, this.y + this.height - 3, 0, 46 + 17 + yOffset * 20, 3, 3);
        drawTexture(matrices, this.x + 3, this.y + this.height - 3, this.width - 6, 3, 3, 46 + 17 + yOffset * 20, 1, 3, 256, 256);
        this.drawTexture(matrices, this.x + this.width - 3, this.y + this.height - 3, 197, 46 + 17 + yOffset * 20, 3, 3);
        drawTexture(matrices, this.x + this.width - 3, this.y + 3, 3, this.height - 6, 197, 49 + yOffset * 20, 3, 1, 256, 256);
        fill(matrices, this.x + 3, this.y + 3, this.x + this.width - 3, this.y + this.height - 3, this.active ? BG_COLOR : BG_INACTIVE_COLOR);

        int color = this.active ? 0xFFFFFF : 0xA0A0A0;
        float textScale = this.drawData != null ? 1.3F : 1.0F;
        int textY = this.drawData != null
                    ? this.y + this.height - (this.height + client.textRenderer.fontHeight) / 4
                    : this.y + (this.height - 8) / 2;
        RenderSystem.scalef(textScale, textScale, 1.0F);
        this.drawCenteredText(
                matrices,
                client.textRenderer,
                this.getMessage(),
                (int) ((this.x + this.width / 2) / textScale),
                (int) (textY / textScale),
                color | MathHelper.ceil(this.alpha * 255.0F) << 24
        );
        RenderSystem.popMatrix();
        if (this.drawData != null) {
            RenderSystem.pushMatrix();
            float emoteScale = 6.0F;
            RenderSystem.scalef(emoteScale, emoteScale, 1.0F);
            EmoteTextureHandler textureHandler = this.drawData.emote.textureHandler;
            if (textureHandler.getImage() != null || textureHandler.loading) {
                this.drawData.emote.createTextureBuffer(
                        matrices.peek().getModel(),
                        this.drawData.x / emoteScale - textureHandler.getWidth() / 2.0F,
                        this.drawData.y / emoteScale,
                        1.0F
                );
                textureHandler.postRender();
            }
            RenderSystem.popMatrix();
        }

        if (this.isHovered() && this.active) {
            this.renderToolTip(matrices, mouseX, mouseY);
        }
    }

    @ToString
    @RequiredArgsConstructor
    public static class ButtonEmote {
        private final Emote emote;
        public final float x;
        public final float y;
    }
}
