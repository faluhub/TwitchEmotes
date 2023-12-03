package me.falu.twitchemotes.mixin;

import me.falu.twitchemotes.TwitchEmotes;
import me.falu.twitchemotes.emote.Emote;
import me.falu.twitchemotes.emote.EmoteStyleOwner;
import net.minecraft.text.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;

@Mixin(Style.class)
public class StyleMixin implements EmoteStyleOwner {
    @Unique private Map<Integer, Emote> emoteStyles;

    @Inject(method = {
            "withColor(Lnet/minecraft/text/TextColor;)Lnet/minecraft/text/Style;",
            "withBold",
            "withItalic",
            "withUnderline",
            "withStrikethrough",
            "withObfuscated",
            "withClickEvent",
            "withHoverEvent",
            "withInsertion",
            "withFont",
            "withFormatting(Lnet/minecraft/util/Formatting;)Lnet/minecraft/text/Style;",
            "withExclusiveFormatting",
            "withFormatting([Lnet/minecraft/util/Formatting;)Lnet/minecraft/text/Style;",
            "withParent"
    }, at = @At("RETURN"), cancellable = true)
    private void addEmoteStyle(CallbackInfoReturnable<Style> cir) {
        Style result = cir.getReturnValue();
        ((EmoteStyleOwner) result).twitchemotes$setEmoteStyles(this.emoteStyles);
        cir.setReturnValue(result);
    }

    @Override
    public void twitchemotes$addEmoteStyle(int index, Emote emoteStyle) {
        if (this.emoteStyles == null) { this.emoteStyles = new HashMap<>(); }
        this.emoteStyles.put(index, emoteStyle);
    }

    @Override
    public Emote twitchemotes$getEmoteStyle(int index) {
        return this.emoteStyles != null ? this.emoteStyles.get(index) : null;
    }

    @Override
    public void twitchemotes$debugEmoteStyles() {
        if (this.emoteStyles != null) {
            for (Integer key : this.emoteStyles.keySet()) {
                TwitchEmotes.log(key + ": " + this.emoteStyles.get(key).name);
            }
        }
    }

    @Override
    public void twitchemotes$setEmoteStyles(Map<Integer, Emote> emoteStyles) {
        this.emoteStyles = emoteStyles;
    }
}
