package me.falu.twitchemotes.mixin.chat;

import com.mojang.blaze3d.systems.RenderSystem;
import me.falu.twitchemotes.TwitchEmotes;
import me.falu.twitchemotes.gui.screen.MenuSelectionScreen;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin extends Screen {
    @Unique private static final Identifier BUTTON_ICON = new Identifier("textures/item/feather.png");

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

    @Inject(method = "init", at = @At("TAIL"))
    public void addConfigButton(CallbackInfo ci) {
        this.addDrawableChild(new ButtonWidget(0, 0, 20, 20, new LiteralText(""), b -> {
            if (this.client != null) {
                this.client.openScreen(new MenuSelectionScreen());
            }
        }));
    }

    @Inject(method = "render", at = @At("TAIL"))
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        matrices.push();
        RenderSystem.setShaderTexture(0, BUTTON_ICON);
        RenderSystem.enableDepthTest();
        drawTexture(matrices, 2, 2, 0.0F, 0.0F, 16, 16, 16, 16);
        matrices.pop();
    }
}
