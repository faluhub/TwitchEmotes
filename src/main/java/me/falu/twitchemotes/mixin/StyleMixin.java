package me.falu.twitchemotes.mixin;

import me.falu.twitchemotes.emote.EmoteStyleOwner;
import me.falu.twitchemotes.emote.Emote;
import net.minecraft.text.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Style.class)
public class StyleMixin implements EmoteStyleOwner {
    @Unique private Emote emoteStyle;

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
        ((EmoteStyleOwner) result).twitchemotes$setEmoteStyle(this.emoteStyle);
        cir.setReturnValue(result);
    }

    @Override
    public void twitchemotes$setEmoteStyle(Emote emote) {
        this.emoteStyle = emote;
    }

    @Override
    public Emote twitchemotes$getEmoteStyle() {
        return this.emoteStyle;
    }
}
