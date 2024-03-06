package me.falu.twitchemotes.gui.overlay;

import me.falu.twitchemotes.TwitchEmotes;
import me.falu.twitchemotes.emote.EmoteConstants;
import me.falu.twitchemotes.emote.texture.EmoteTextureHandler;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Util;

import java.util.Random;

public class PpHopOverlay extends DrawableHelper {
    private long sequenceStart;
    private float scale;
    private int lastHeight = -1;
    private int y;

    public PpHopOverlay() {
        this.sequenceStart = Util.getMeasuringTimeMs();
        this.scale = this.randomScale();
        this.y = -1;
    }

    private float randomScale() {
        return new Random().nextFloat(3.0F, 16.0F);
    }

    public void render(MatrixStack matrices, int width, int height) {
        EmoteTextureHandler textureHandler = EmoteConstants.PP_BOUNCE.textureHandler;
        long diff = Util.getMeasuringTimeMs() - this.sequenceStart;
        float progress = (float) diff / 6000;

        matrices.push();
        NativeImage image = textureHandler.getImage();
        if (image != null) {
            if (this.y == -1 || this.lastHeight != height) {
                this.y = new Random().nextInt(height - (int) TwitchEmotes.EMOTE_SIZE);
                this.lastHeight = height;
            }
            matrices.scale(this.scale, this.scale, 0.0F);
            EmoteConstants.PP_BOUNCE.createTextureBuffer(
                    matrices.peek().getModel(),
                    (textureHandler.getWidth() * -2 + (width + textureHandler.getWidth()) * progress) / this.scale,
                    this.y / this.scale,
                    0.5F
            );
            textureHandler.postRender();
        }
        matrices.pop();

        if (diff >= 6000) {
            this.sequenceStart = Util.getMeasuringTimeMs();
            this.scale = this.randomScale();
            this.y = -1;
        }
    }
}
