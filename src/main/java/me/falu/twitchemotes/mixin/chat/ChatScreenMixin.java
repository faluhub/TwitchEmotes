package me.falu.twitchemotes.mixin.chat;

import com.mojang.blaze3d.systems.RenderSystem;
import me.falu.twitchemotes.gui.screen.MenuSelectionScreen;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin extends Screen {
    @Unique private static final Identifier BUTTON_ICON = new Identifier("textures/item/feather.png");
    @Shadow protected TextFieldWidget chatField;
    @Unique private ButtonWidget button;

    protected ChatScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    public void addConfigButton(CallbackInfo ci) {
        this.addDrawableChild(this.button = ButtonWidget.builder(Text.literal(""), b -> {
            if (this.client != null) {
                this.client.setScreen(new MenuSelectionScreen());
            }
        }).dimensions(0, 0, 20, 20).build());
        this.setInitialFocus(this.chatField);
    }

    @Override
    public void setFocused(@Nullable Element focused) {
        if (focused != this.button) {
            super.setFocused(focused);
        }
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
