package me.quesia.twitchemotes.mixin;

import me.quesia.twitchemotes.TwitchEmotes;
import me.quesia.twitchemotes.owner.TwitchMessageListOwner;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
    @Shadow @Final public InGameHud inGameHud;

    @Inject(method = "joinWorld", at = @At("HEAD"))
    private void clearChat(ClientWorld world, CallbackInfo ci) {
        if (TwitchEmotes.CLEAR_CHAT_ON_JOIN) {
            ((TwitchMessageListOwner) this.inGameHud.getChatHud()).onMessagesClear();
        }
    }
}
