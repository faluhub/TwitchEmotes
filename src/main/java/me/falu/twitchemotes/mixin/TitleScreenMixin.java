package me.falu.twitchemotes.mixin;

import me.falu.twitchemotes.gui.screen.MenuSelectionScreen;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {
    @Unique private static final Identifier BUTTON_ICON = new Identifier("textures/item/feather.png");

    protected TitleScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    public void addConfigButton(CallbackInfo ci) {
        this.addDrawableChild(ButtonWidget.builder(Text.literal(""), b -> {
            if (this.client != null) {
                this.client.setScreen(new MenuSelectionScreen());
            }
        }).dimensions(0, 0, 20, 20).build());
    }

    @Inject(method = "render", at = @At("TAIL"))
    public void render(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        context.drawTexture(BUTTON_ICON, 2, 2, 0.0F, 0.0F, 16, 16, 16, 16);
    }
}
