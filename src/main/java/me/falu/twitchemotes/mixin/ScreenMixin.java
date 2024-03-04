package me.falu.twitchemotes.mixin;

import me.falu.twitchemotes.TwitchEmotes;
import me.falu.twitchemotes.gui.overlay.PpHopOverlay;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public class ScreenMixin {
    @Unique private static final PpHopOverlay PP_HOP_OVERLAY = new PpHopOverlay();
    @Shadow public int width;
    @Shadow public int height;

    @Inject(method = "renderBackground(Lnet/minecraft/client/util/math/MatrixStack;I)V", at = @At("TAIL"))
    private void renderPpOverlay(MatrixStack matrices, int vOffset, CallbackInfo ci) {
        if (TwitchEmotes.SHOW_PP_HOP_OVERLAY.getValue()) {
            PP_HOP_OVERLAY.render(matrices, this.width, this.height);
        }
    }
}