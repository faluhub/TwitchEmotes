package me.quesia.twitchemotes.mixin;

import me.quesia.twitchemotes.owner.TwitchMessageOwner;
import net.minecraft.client.gui.hud.ChatHudLine;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ChatHudLine.class)
public class ChatHudLineMixin implements TwitchMessageOwner {
    private String messageId;

    @Override
    public String getMessageId() {
        return this.messageId;
    }

    @Override
    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }
}
