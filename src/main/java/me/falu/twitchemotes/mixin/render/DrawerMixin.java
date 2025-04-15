package me.falu.twitchemotes.mixin.render;

import me.falu.twitchemotes.emote.Emote;
import me.falu.twitchemotes.emote.EmoteStyleOwner;
import net.minecraft.SharedConstants;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.Style;
import net.minecraft.util.math.ColorHelper;
import org.joml.Math;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TextRenderer.Drawer.class)
public class DrawerMixin {
    @Shadow float x;
    @Shadow float y;
    @Shadow @Final private Matrix4f matrix;
    @Shadow @Final private int color;

    @Inject(method = "accept", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", shift = At.Shift.BEFORE), cancellable = true)
    private void drawEmote(int i, Style style, int j, CallbackInfoReturnable<Boolean> cir) {
        Emote emote = ((EmoteStyleOwner) style).twitchemotes$getEmoteStyle();
        if (emote != null) {
            // Version specific because there's a bug and I don't want to make another branch
            String versionString = SharedConstants.getGameVersion().getId();
            int version = versionString.startsWith("1.21.") ? Integer.parseInt(versionString.split("\\.")[2]) : 0;
            float alpha = version < 4 ? Math.clamp(0.0F, 255.0F, 255.0F - ColorHelper.getAlpha(this.color)) : ColorHelper.getAlpha(this.color) / 255.0F;

            if (emote.scheduleDraw(this.x, this.y, this.matrix, alpha)) {
                this.x += emote.textureHandler.getWidth();
                cir.setReturnValue(true);
                cir.cancel();
                return;
            }
            ((EmoteStyleOwner) style).twitchemotes$setEmoteStyle(null);
        }
    }
}
