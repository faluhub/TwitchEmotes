package me.falu.twitchemotes.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import me.falu.twitchemotes.emote.Emote;
import me.falu.twitchemotes.emote.EmoteStyleOwner;
import me.falu.twitchemotes.mixin.access.TextRendererAccessor;
import net.minecraft.client.font.GlyphRenderer;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.text.Style;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(TextRenderer.Drawer.class)
public class DrawerMixin {
    @Shadow float x;
    @Shadow float y;
    @Shadow @Final private Matrix4f matrix;
    @Shadow @Final private float alpha;

    @Redirect(method = "accept", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/font/TextRenderer;drawGlyph(Lnet/minecraft/client/font/GlyphRenderer;ZZFFFLorg/joml/Matrix4f;Lnet/minecraft/client/render/VertexConsumer;FFFFI)V"))
    private void drawEmote(TextRenderer instance, GlyphRenderer glyphRenderer, boolean bold, boolean italic, float weight, float x, float y, Matrix4f matrix, VertexConsumer vertexConsumer, float red, float green, float blue, float alpha, int light, @Local(argsOnly = true, ordinal = 0) int i, @Local(argsOnly = true) Style style) {
        Emote emote = ((EmoteStyleOwner) style).twitchemotes$getEmoteStyle(i);
        if (emote != null) {
            emote.draw(this.x, this.y, this.matrix, this.alpha);
            return;
        }
        ((TextRendererAccessor) instance).invokeDrawGlyph(glyphRenderer, bold, italic, weight, x, y, matrix, vertexConsumer, red, green, blue, alpha, light);
    }
}
