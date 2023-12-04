package me.falu.twitchemotes.emote;

import net.minecraft.text.Style;

public interface EmoteStyleOwner {
    Style twitchemotes$withEmoteStyle(Emote emoteStyle);
    void twitchemotes$setEmoteStyle(Emote emoteStyle);
    Emote twitchemotes$getEmoteStyle();
}
