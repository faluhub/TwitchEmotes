package me.quesia.twitchemotes.mixin;

import me.quesia.twitchemotes.TwitchEmotes;
import net.minecraft.client.gui.screen.CommandSuggestor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(CommandSuggestor.SuggestionWindow.class)
public class SuggestionWindowMixin {
    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/font/TextRenderer;drawWithShadow(Lnet/minecraft/client/util/math/MatrixStack;Ljava/lang/String;FFI)I"), index = 1)
    private String shortenPreview(String text) {
        return TwitchEmotes.getShortenedString(text);
    }
}
