package me.falu.twitchemotes.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import me.falu.twitchemotes.emote.Emote;
import me.falu.twitchemotes.emote.EmoteStyleOwner;
import net.minecraft.client.font.FontStorage;
import net.minecraft.client.font.GlyphRenderer;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(TextRenderer.Drawer.class)
public class DrawerMixin {
    @Redirect(method = "accept", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/font/FontStorage;getGlyphRenderer(I)Lnet/minecraft/client/font/GlyphRenderer;"))
    private GlyphRenderer sendEmoteStyle(FontStorage instance, int codePoint, @Local(argsOnly = true) Style style) {
        GlyphRenderer renderer = instance.getGlyphRenderer(codePoint);
        Emote emote = ((EmoteStyleOwner) style).twitchemotes$getEmoteStyle();
        ((EmoteStyleOwner) renderer).twitchemotes$setEmoteStyle(emote);
        return renderer;
    }
}
