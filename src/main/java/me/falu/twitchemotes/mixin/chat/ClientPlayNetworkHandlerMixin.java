package me.falu.twitchemotes.mixin.chat;

import me.falu.twitchemotes.TwitchEmotes;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
    @Inject(method = "sendChatMessage", at = @At("TAIL"))
    private void sendTwitchMessage(String content, CallbackInfo ci) {
        TwitchEmotes.sendChatMessage(content);
    }
}
