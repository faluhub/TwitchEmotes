package me.quesia.twitchemotes.mixin;

import me.quesia.twitchemotes.TwitchEmotes;
import net.minecraft.client.gui.screen.CommandSuggestor;
import net.minecraft.client.network.ClientCommandSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Collection;
import java.util.Set;

@Mixin(CommandSuggestor.class)
public class CommandSuggestorMixin {
    @Redirect(method = "refresh", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientCommandSource;getPlayerNames()Ljava/util/Collection;"))
    private Collection<?> displayEmoteNames(ClientCommandSource instance) {
        Set<String> keys = TwitchEmotes.EMOTE_MAP.keySet();
        TwitchEmotes.FAILED_EMOTES.forEach(keys::remove);
        return keys;
    }

    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/font/TextRenderer;drawWithShadow(Lnet/minecraft/client/util/math/MatrixStack;Ljava/lang/String;FFI)I"), index = 1)
    private String shortenPreview(String text) {
        return TwitchEmotes.getShortenedString(text);
    }

    @ModifyArg(method = "showSuggestions", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/font/TextRenderer;getWidth(Ljava/lang/String;)I"), index = 0)
    private String smallerBox(String text) {
        return TwitchEmotes.getShortenedString(text);
    }
}
