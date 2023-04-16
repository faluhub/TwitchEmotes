package me.quesia.twitchemotes.mixin;

import me.quesia.twitchemotes.TwitchEmotes;
import me.quesia.twitchemotes.owner.TwitchMessageListOwner;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.screen.FatalErrorScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.text.LiteralText;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Shadow @Final public InGameHud inGameHud;

    @Inject(method = "joinWorld", at = @At("HEAD"))
    private void clearChat(ClientWorld world, CallbackInfo ci) {
        if (TwitchEmotes.CLEAR_CHAT_ON_JOIN) {
            ((TwitchMessageListOwner) this.inGameHud.getChatHud()).onMessagesClear();
        }
    }

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;openScreen(Lnet/minecraft/client/gui/screen/Screen;)V", ordinal = 1))
    private void openFatalScreen(MinecraftClient instance, Screen screen) {
        instance.openScreen(TwitchEmotes.TWIRK.isConnected() ? screen : new FatalErrorScreen(
                new LiteralText("Couldn't connect to Twitch chat!"),
                new LiteralText("This is a problem with Twitch itself and can only be fixed by relaunching the game.")
        ));
    }
}
