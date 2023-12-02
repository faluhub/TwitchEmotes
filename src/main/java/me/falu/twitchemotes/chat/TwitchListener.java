package me.falu.twitchemotes.chat;

import com.gikk.twirk.events.TwirkListener;
import com.gikk.twirk.types.twitchMessage.TwitchMessage;
import com.gikk.twirk.types.users.TwitchUser;
import me.falu.twitchemotes.TwitchEmotes;
import me.falu.twitchemotes.emote.Emote;
import me.falu.twitchemotes.emote.EmoteStyleOwner;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public class TwitchListener implements TwirkListener {
    private void sendMessage(Text text) {
        MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(text);
    }

    @Override
    public void onPrivMsg(TwitchUser sender, TwitchMessage message) {
        MutableText text = MutableText.of(Text.literal("<" + sender.getDisplayName() + "> ").getContent());
        String content = message.getContent().trim();
        String[] words = content.split(" ");
        for (String word : words) {
            Emote emote = TwitchEmotes.getEmote(word);
            if (emote != null) {
                text.append(Text.literal(".").styled(style -> {
                    ((EmoteStyleOwner) style).twitchemotes$setEmoteStyle(emote);
                    return style;
                }));
                text.append(Text.literal(" "));
            } else {
                text.append(Text.literal(word + " "));
            }
        }
        this.sendMessage(text);
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
