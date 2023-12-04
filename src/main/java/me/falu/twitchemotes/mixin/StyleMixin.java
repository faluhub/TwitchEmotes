package me.falu.twitchemotes.mixin;

import me.falu.twitchemotes.emote.Emote;
import me.falu.twitchemotes.emote.EmoteStyleOwner;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Style.class)
public abstract class StyleMixin implements EmoteStyleOwner {
    @Unique private Emote emoteStyle;
    @Shadow public abstract Style withHoverEvent(@Nullable HoverEvent hoverEvent);

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
    public Style twitchemotes$withEmoteStyle(Emote emoteStyle) {
        Style style = this.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of(emoteStyle.name)));
        ((EmoteStyleOwner) style).twitchemotes$setEmoteStyle(emoteStyle);
        return style;
    }

    @Override
    public void twitchemotes$setEmoteStyle(Emote emoteStyle) {
        this.emoteStyle = emoteStyle;
    }

    @Override
    public Emote twitchemotes$getEmoteStyle() {
        return this.emoteStyle;
    }
}
