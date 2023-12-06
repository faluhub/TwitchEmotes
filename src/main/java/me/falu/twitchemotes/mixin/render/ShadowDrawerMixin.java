package me.falu.twitchemotes.mixin.render;

import me.falu.twitchemotes.emote.Emote;
import me.falu.twitchemotes.emote.EmoteStyleOwner;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.Style;
import net.minecraft.util.math.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TextRenderer.ShadowDrawer.class)
public class ShadowDrawerMixin {
    @Shadow private float x;
    @Shadow private float y;
    @Shadow @Final private Matrix4f matrix;
    @Shadow @Final private float alpha;

    @Inject(
            method = "onChar",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/VertexConsumerProvider;getBuffer(Lnet/minecraft/client/render/RenderLayer;)Lnet/minecraft/client/render/VertexConsumer;",
                    shift = At.Shift.BEFORE
            ),
            cancellable = true
    )
    private void drawEmote(int i, Style style, int j, CallbackInfoReturnable<Boolean> cir) {
        Emote emote = ((EmoteStyleOwner) style).twitchemotes$getEmoteStyle();
        if (emote != null) {
            if (emote.draw(this.x, this.y, this.matrix, this.alpha)) {
                this.x += emote.textureHandler.getWidth();
                cir.setReturnValue(true);
                cir.cancel();
                return;
            }
            ((EmoteStyleOwner) style).twitchemotes$setEmoteStyle(null);
        }
    }
}
