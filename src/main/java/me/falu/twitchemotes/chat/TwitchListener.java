package me.falu.twitchemotes.chat;

import com.gikk.twirk.enums.EMOTE_SIZE;
import com.gikk.twirk.events.TwirkListener;
import com.gikk.twirk.types.clearChat.ClearChat;
import com.gikk.twirk.types.clearMsg.ClearMsg;
import com.gikk.twirk.types.twitchMessage.TwitchMessage;
import com.gikk.twirk.types.users.TwitchUser;
import me.falu.twitchemotes.TwitchEmotes;
import me.falu.twitchemotes.emote.Emote;
import net.minecraft.client.MinecraftClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TwitchListener implements TwirkListener {
    private TwitchMessageListOwner getMessageList() {
        return (TwitchMessageListOwner) MinecraftClient.getInstance().inGameHud.getChatHud();
    }

    private Map<String, Emote> convertMessageEmotes(List<com.gikk.twirk.types.emote.Emote> emotes) {
        Map<String, Emote> result = new HashMap<>();
        for (com.gikk.twirk.types.emote.Emote emote : emotes) {
            result.put(emote.getPattern(), Emote
                    .builder()
                    .name(emote.getPattern())
                    .id(emote.getEmoteIDString())
                    .url(emote.getEmoteImageUrl(EMOTE_SIZE.LARGE))
                    .zeroWidth(false)
                    .build());
        }
        return result;
    }

    @Override
    public void onPrivMsg(TwitchUser sender, TwitchMessage message) {
        this.getMessageList().twitchemotes$addMessage(
                "<" + sender.getDisplayName() + "> ",
                message.getContent().trim(),
                message.getMessageID(),
                this.convertMessageEmotes(message.getEmotes())
        );
    }

    @Override
    public void onClearChat(ClearChat clearChat) {
        this.getMessageList().twitchemotes$clear();
    }

    @Override
    public void onClearMsg(ClearMsg clearMsg) {
        this.getMessageList().twitchemotes$delete(clearMsg.getTargetMsgId());
    }

    @Override
    public void onConnect() {
        TwitchEmotes.log("Connected to Twitch chat.");
    }

    @Override
    public void onDisconnect() {
        TwitchEmotes.LOGGER.error("Disconnected from Twitch chat.");
    }
}
