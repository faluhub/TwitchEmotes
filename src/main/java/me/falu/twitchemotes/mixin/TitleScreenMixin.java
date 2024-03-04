package me.falu.twitchemotes.mixin;

import me.falu.twitchemotes.gui.screen.MenuSelectionScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
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

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {
    @Unique private static final Identifier BUTTON_ICON = new Identifier("textures/item/feather.png");

    protected TitleScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    public void addConfigButton(CallbackInfo ci) {
        this.addButton(new ButtonWidget(0, 0, 20, 20, new LiteralText(""), b -> {
            if (this.client != null) {
                this.client.openScreen(new MenuSelectionScreen());
            }
        }));
    }

    @Inject(method = "render", at = @At("TAIL"))
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (this.client == null) {
            return;
        }
        this.client.getTextureManager().bindTexture(BUTTON_ICON);
        drawTexture(matrices, 2, 2, 0.0F, 0.0F, 16, 16, 16, 16);
    }
}
