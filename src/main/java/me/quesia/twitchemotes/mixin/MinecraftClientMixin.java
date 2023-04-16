package me.quesia.twitchemotes.mixin;

import me.quesia.twitchemotes.TwitchEmotes;
import me.quesia.twitchemotes.owner.TwitchMessageListOwner;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.screen.FatalErrorScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.text.LiteralText;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
    @Shadow @Final public InGameHud inGameHud;

    @Shadow public abstract void openScreen(@Nullable Screen screen);

    @Inject(method = "joinWorld", at = @At("HEAD"))
    private void clearChat(ClientWorld world, CallbackInfo ci) {
        if (TwitchEmotes.CLEAR_CHAT_ON_JOIN) {
            ((TwitchMessageListOwner) this.inGameHud.getChatHud()).onMessagesClear();
        }
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void openFatalScreen(RunArgs args, CallbackInfo ci) {
        if (!TwitchEmotes.TWIRK.isConnected()) {
            this.openScreen(new FatalErrorScreen(
                    new LiteralText("Couldn't connect to Twitch chat!"),
                    new LiteralText("This is a problem with Twitch itself and can only be fixed by relaunching the game.")
            ));
        }
    }
}
