package me.falu.twitchemotes.mixin.render;

import me.falu.twitchemotes.TwitchEmotes;
import me.falu.twitchemotes.emote.Emote;
import net.minecraft.client.font.TextRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TextRenderer.class)
public class TextRendererMixin {
    @Inject(
            method = {
                    "draw(Ljava/lang/String;FFILorg/joml/Matrix4f;ZZ)I",
                    "draw(Lnet/minecraft/text/OrderedText;FFILorg/joml/Matrix4f;Z)I"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;draw()V",
                    shift = At.Shift.AFTER
            )
    )
    private void drawScheduledEmotes(CallbackInfoReturnable<Integer> cir) {
        Emote.DrawData data;
        while ((data = TwitchEmotes.SCHEDULED_DRAW.poll()) != null) {
            data.draw();
        }
    }
}
