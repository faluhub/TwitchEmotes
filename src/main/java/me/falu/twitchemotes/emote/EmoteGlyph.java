package me.falu.twitchemotes.emote;

import net.minecraft.client.font.Glyph;
import net.minecraft.client.font.GlyphRenderer;
import net.minecraft.client.font.RenderableGlyph;
import net.minecraft.client.texture.NativeImage;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.function.Function;

public class EmoteGlyph implements Glyph {
    private final String url;

    public EmoteGlyph(Emote emote) {
        this.url = emote.url;
    }

    @Override
    public float getAdvance() {
        return 8.0F;
    }

    @Override
    public float getAdvance(boolean bold) {
        return this.getAdvance();
    }

    @Override
    public float getBoldOffset() {
        return 0.0F;
    }

    @Override
    public float getShadowOffset() {
        return 0.0F;
    }

    @Override
    public GlyphRenderer bake(Function<RenderableGlyph, GlyphRenderer> function) {
        return function.apply(new RenderableGlyph() {
            @Override
            public int getWidth() {
                return 16;
            }

            @Override
            public int getHeight() {
                return 16;
            }

            @Override
            public void upload(int x, int y) {
                try {
                    URL url = new URL(EmoteGlyph.this.url);
                    InputStream stream = url.openConnection().getInputStream();
                    NativeImage img = NativeImage.read(NativeImage.Format.RGBA, stream);
                    img.upload(0, x, y, 0, 0, this.getWidth(), this.getHeight(), false, true);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public boolean hasColor() {
                return true;
            }

            @Override
            public float getOversample() {
                return 1.0F;
            }
        });
    }
}
