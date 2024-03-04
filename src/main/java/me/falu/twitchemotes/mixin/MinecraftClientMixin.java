package me.falu.twitchemotes.mixin;

import me.falu.twitchemotes.TwitchEmotes;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Inject(method = "reloadResources", at = @At("TAIL"))
    private void reloadEmotes(CallbackInfoReturnable<CompletableFuture<Void>> cir) {
        TwitchEmotes.reload();
    }
}
