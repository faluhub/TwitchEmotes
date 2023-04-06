package me.quesia.twitchemotes.mixin;

import net.minecraft.client.font.BlankGlyph;
import net.minecraft.client.font.RenderableGlyph;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BlankGlyph.class)
public class BlankGlyphMixin implements RenderableGlyph {
    @Override
    public int getWidth() {
        return 0;
    }

    @Override
    public int getHeight() {
        return 0;
    }

    @Override
    public void upload(int x, int y) {}

    @Override
    public boolean hasColor() {
        return false;
    }

    @Override
    public float getOversample() {
        return 0.0F;
    }

    @Override
    public float getXMax() {
        return this.getXMin();
    }

    @Override
    public float getAdvance() {
        return 0;
    }
}
