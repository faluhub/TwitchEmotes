package me.falu.twitchemotes.gui.overlay;

import com.mojang.blaze3d.systems.RenderSystem;
import me.falu.twitchemotes.emote.Emote;
import me.falu.twitchemotes.emote.texture.EmoteTextureHandler;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Util;

import java.util.Random;

public class PpHopOverlay extends DrawableHelper {
    private static final Emote PP_BOUNCE = new Emote(
            "ppBounce",
            "6379879d541f8c821fb1fe17",
            "https://cdn.7tv.app/emote/6379879d541f8c821fb1fe17/4x.webp",
            Emote.ImageType.WEBP
    );
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

    @SuppressWarnings("deprecation")
    public void render(MatrixStack matrices, int width, int height) {
        EmoteTextureHandler textureHandler = PP_BOUNCE.textureHandler;
        long diff = Util.getMeasuringTimeMs() - this.sequenceStart;
        float progress = (float) diff / 6000;

        RenderSystem.pushMatrix();
        NativeImage image = textureHandler.getImage();
        if (image != null) {
            if (this.y == -1 || this.lastHeight != height) {
                this.y = new Random().nextInt(height - image.getHeight());
                this.lastHeight = height;
            }
            RenderSystem.scalef(this.scale, this.scale, 0.0F);
            PP_BOUNCE.createTextureBuffer(
                    matrices.peek().getModel(),
                    (-image.getWidth() + (width + image.getWidth()) * progress) / this.scale,
                    this.y / this.scale,
                    0.5F
            );
            textureHandler.postRender();
        }
        RenderSystem.popMatrix();

        if (diff >= 6000) {
            this.sequenceStart = Util.getMeasuringTimeMs();
            this.scale = this.randomScale();
            this.y = -1;
        }
    }
}
