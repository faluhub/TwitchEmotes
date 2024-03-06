package me.falu.twitchemotes.gui.widget;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.OrderedText;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CosmeticTextFieldWidget extends TextFieldWidget {
    private final List<OrderedText> lines = new ArrayList<>();
    private final TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
    private final float textScale = Math.min(1.0F, this.width / 209.0F * 0.8F);

    public CosmeticTextFieldWidget(TextRenderer textRenderer, int x, int y, int width, int height) {
        super(textRenderer, x + 2, y + 2, width - 4, height - 4, new LiteralText(""));
    }

    @SuppressWarnings("unused")
    public CosmeticTextFieldWidget(TextRenderer textRenderer, int x, int y, int width, int height, @Nullable TextFieldWidget copyFrom) {
        super(textRenderer, x + 2, y + 2, width - 4, height - 4, copyFrom, new LiteralText(""));
    }

    public void addTextAsLines(String text) {
        String[] parts = text.split("\n");
        for (String part : parts) {
            this.lines.addAll(this.textRenderer.wrapLines(new LiteralText(part), (int) (this.width / this.textScale)));
        }
    }

    @Override
    public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.renderButton(matrices, mouseX, mouseY, delta);
        matrices.push();
        matrices.scale(this.textScale, this.textScale, 1.0F);
        for (int i = 0; i < this.lines.size(); i++) {
            OrderedText line = this.lines.get(i);
            this.textRenderer.drawWithShadow(
                    matrices,
                    line,
                    (this.x + 2) / this.textScale,
                    (this.y + this.textRenderer.fontHeight * i + 2) / this.textScale,
                    0xFFFFFF
            );
        }
        matrices.pop();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    @Override
    public boolean charTyped(char chr, int keyCode) {
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return false;
    }
}
