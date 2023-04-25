package me.quesia.twitchemotes.mixin;

import me.quesia.twitchemotes.TwitchEmotes;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin extends Screen {
    protected ChatScreenMixin(Text title) {
        super(title);
    }

    @Override
    public void sendMessage(String message) {
        if (TwitchEmotes.ENABLE_CHAT_BACK && !message.startsWith("/")) {
            TwitchEmotes.TWITCH_CLIENT.getChat().sendMessage(TwitchEmotes.TWITCH_NAME, message);
        }
        super.sendMessage(message);
    }
}
