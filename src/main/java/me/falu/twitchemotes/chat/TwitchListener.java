package me.falu.twitchemotes.chat;

import com.gikk.twirk.enums.EMOTE_SIZE;
import com.gikk.twirk.events.TwirkListener;
import com.gikk.twirk.types.clearChat.ClearChat;
import com.gikk.twirk.types.clearMsg.ClearMsg;
import com.gikk.twirk.types.twitchMessage.TwitchMessage;
import com.gikk.twirk.types.users.TwitchUser;
import me.falu.twitchemotes.TwitchEmotes;
import me.falu.twitchemotes.emote.Badge;
import me.falu.twitchemotes.emote.Emote;
import me.falu.twitchemotes.emote.EmoteStyleOwner;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.TextColor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class TwitchListener implements TwirkListener {
    private TwitchMessageListOwner getMessageList() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.inGameHud != null) {
            return (TwitchMessageListOwner) client.inGameHud.getChatHud();
        }
        return null;
    }

    private Map<String, Emote> convertMessageEmotes(List<com.gikk.twirk.types.emote.Emote> emotes) {
        Map<String, Emote> result = new HashMap<>();
        for (com.gikk.twirk.types.emote.Emote emote : emotes) {
            String url = emote.getEmoteImageUrl(EMOTE_SIZE.LARGE);
            result.put(emote.getPattern(), Emote
                    .builder()
                    .name(emote.getPattern())
                    .id(emote.getEmoteIDString())
                    .url(url)
                    .imageType(url.contains("/animated/") ? Emote.ImageType.GIF : Emote.ImageType.STATIC)
                    .build());
        }
        return result;
    }

    @Override
    public void onPrivMsg(TwitchUser sender, TwitchMessage message) {
        MutableText prefix = new LiteralText("");
        if (TwitchEmotes.SHOW_BADGES.getValue()) {
            for (String badgeId : sender.getBadges()) {
                Badge badge = TwitchEmotes.getBadge(badgeId.split("/")[0]);
                if (badge != null) {
                    prefix.append(new LiteralText("_").styled(s -> ((EmoteStyleOwner) s).twitchemotes$withBadgeStyle(badge)));
                }
            }
            if (sender.getBadges().length > 0) {
                prefix.append(" ");
            }
        }
        prefix.append("<");
        prefix.append(new LiteralText(sender.getDisplayName()).styled(style -> {
            if (TwitchEmotes.SHOW_USER_COLORS.getValue()) {
                style = style.withColor(TextColor.fromRgb(sender.getColor()));
            }
            return style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://twitch.tv/" + sender.getUserName()));
        }));
        prefix.append("> ");
        Optional.ofNullable(this.getMessageList()).ifPresent(l -> l.twitchemotes$addMessage(
                prefix,
                message.getContent().trim(),
                message.getMessageID(),
                this.convertMessageEmotes(message.getEmotes())
        ));
    }

    @Override
    public void onClearChat(ClearChat clearChat) {
        Optional.ofNullable(this.getMessageList()).ifPresent(TwitchMessageListOwner::twitchemotes$clear);
    }

    @Override
    public void onClearMsg(ClearMsg clearMsg) {
        Optional.ofNullable(this.getMessageList()).ifPresent(l -> l.twitchemotes$delete(clearMsg.getTargetMsgId()));
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
