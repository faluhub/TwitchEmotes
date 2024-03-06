package me.falu.twitchemotes.mixin.render;

import com.llamalad7.mixinextras.sugar.Local;
import me.falu.twitchemotes.emote.Emote;
import me.falu.twitchemotes.emote.EmoteStyleOwner;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.text.Style;
import net.minecraft.util.math.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TextRenderer.Drawer.class)
public class ShadowDrawerMixin {
    @Shadow float x;
    @Shadow float y;
    @Shadow @Final private Matrix4f matrix;
    @Shadow @Final private float alpha;

    @Inject(
            method = "accept",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/font/TextRenderer;drawGlyph(Lnet/minecraft/client/font/GlyphRenderer;ZZFFFLnet/minecraft/util/math/Matrix4f;Lnet/minecraft/client/render/VertexConsumer;FFFFI)V",
                    shift = At.Shift.BEFORE
            ),
            cancellable = true
    )
    private void drawEmote(int i, Style style, int j, CallbackInfoReturnable<Boolean> cir, @Local(ordinal = 0) VertexConsumer vertexConsumer) {
        Emote emote = ((EmoteStyleOwner) style).twitchemotes$getEmoteStyle();
        if (emote != null) {
            if (emote.scheduleDraw(this.x, this.y, this.matrix, this.alpha)) {
                this.x += emote.textureHandler.getWidth();
                cir.setReturnValue(true);
                cir.cancel();
                return;
            }
            ((EmoteStyleOwner) style).twitchemotes$setEmoteStyle(null);
        }
    }
}