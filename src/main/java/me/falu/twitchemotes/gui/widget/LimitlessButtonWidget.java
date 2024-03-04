package me.falu.twitchemotes.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.BackgroundHelper;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

public class LimitlessButtonWidget extends ButtonWidget {
    private static final int BG_COLOR = BackgroundHelper.ColorMixer.getArgb(150, 0, 0, 0);
    private static final int BG_INACTIVE_COLOR = BackgroundHelper.ColorMixer.getArgb(80, 0, 0, 0);

    public LimitlessButtonWidget(int x, int y, int width, int height, Text message, PressAction onPress) {
        super(x, y, width, height, message, onPress);
    }

    @SuppressWarnings("unused")
    public LimitlessButtonWidget(int x, int y, int width, int height, Text message, PressAction onPress, TooltipSupplier tooltipSupplier) {
        super(x, y, width, height, message, onPress, tooltipSupplier);
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
        float scale = 1.3F;
        RenderSystem.scalef(scale, scale, 1.0F);
        this.drawCenteredText(
                matrices,
                client.textRenderer,
                this.getMessage(),
                (int) ((this.x + this.width / 2) / scale),
                (int) ((this.y + this.height - (this.height + client.textRenderer.fontHeight) / 4) / scale),
                color | MathHelper.ceil(this.alpha * 255.0F) << 24
        );
        RenderSystem.popMatrix();

        if (this.isHovered() && this.active) {
            this.renderToolTip(matrices, mouseX, mouseY);
        }
    }
}
