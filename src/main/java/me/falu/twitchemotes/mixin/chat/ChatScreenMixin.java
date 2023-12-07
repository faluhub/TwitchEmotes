package me.falu.twitchemotes.mixin.chat;

import me.falu.twitchemotes.TwitchEmotes;
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
        if (!message.startsWith("/")) {
            TwitchEmotes.sendChatMessage(message);
        }
        super.sendMessage(message);
    }
}
