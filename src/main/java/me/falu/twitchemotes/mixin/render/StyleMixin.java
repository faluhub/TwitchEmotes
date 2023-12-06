package me.falu.twitchemotes.mixin.render;

import me.falu.twitchemotes.emote.Emote;
import me.falu.twitchemotes.emote.EmoteStyleOwner;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
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
    @Shadow public abstract Style setHoverEvent(@Nullable HoverEvent hoverEvent);

    @Inject(method = {
            "withColor(Lnet/minecraft/text/TextColor;)Lnet/minecraft/text/Style;",
            "withBold",
            "withItalic",
            "withClickEvent",
            "setHoverEvent",
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
        Style style = this.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText(emoteStyle.name)));
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
