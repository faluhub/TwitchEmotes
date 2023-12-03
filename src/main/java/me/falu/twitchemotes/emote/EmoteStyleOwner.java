package me.falu.twitchemotes.emote;

import java.util.Map;

public interface EmoteStyleOwner {
    void twitchemotes$addEmoteStyle(int index, Emote emoteStyle);
    void twitchemotes$setEmoteStyles(Map<Integer, Emote> emoteStyles);
    Emote twitchemotes$getEmoteStyle(int index);
    void twitchemotes$debugEmoteStyles();
}
