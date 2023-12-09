package me.falu.twitchemotes.chat;

import me.falu.twitchemotes.emote.Emote;
import net.minecraft.text.Text;

import java.util.Map;

public interface TwitchMessageListOwner {
    void twitchemotes$clear();
    void twitchemotes$delete(String id);
    void twitchemotes$addMessage(Text prefix, String content, String id, Map<String, Emote> specific);
}
