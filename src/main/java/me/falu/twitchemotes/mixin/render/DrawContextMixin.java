package me.falu.twitchemotes.mixin.render;

import me.falu.twitchemotes.TwitchEmotes;
import me.falu.twitchemotes.emote.Emote;
import net.minecraft.client.gui.DrawContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DrawContext.class)
public class DrawContextMixin {
    @Inject(
            method = {
                    "drawText(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;IIIZ)I",
                    "drawText(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/OrderedText;IIIZ)I"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/DrawContext;tryDraw()V",
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
