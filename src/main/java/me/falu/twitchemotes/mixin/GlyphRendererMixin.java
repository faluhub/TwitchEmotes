package me.falu.twitchemotes.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import me.falu.twitchemotes.emote.Emote;
import me.falu.twitchemotes.emote.EmoteStyleOwner;
import net.minecraft.client.font.GlyphRenderer;
import net.minecraft.client.render.*;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

@Mixin(GlyphRenderer.class)
public class GlyphRendererMixin implements EmoteStyleOwner {
    @Unique private Emote emoteStyle;
    @Unique private NativeImageBackedTexture texture;

    @Override
    public void twitchemotes$setEmoteStyle(Emote emote) {
        this.emoteStyle = emote;
    }

    @Override
    public Emote twitchemotes$getEmoteStyle() {
        return this.emoteStyle;
    }

    @Inject(method = "draw", at = @At("HEAD"), cancellable = true)
    private void drawEmote(boolean italic, float x, float y, Matrix4f matrix, VertexConsumer vertexConsumer, float red, float green, float blue, float alpha, int light, CallbackInfo ci) {
        if (this.emoteStyle != null) {
            if (this.texture == null) {
                try {
                    URL url = new URL(this.emoteStyle.url);
                    InputStream stream = url.openConnection().getInputStream();
                    NativeImage img = NativeImage.read(NativeImage.Format.RGBA, stream);
                    this.texture = new NativeImageBackedTexture(img);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            NativeImage img = this.texture.getImage();
            if (img != null) {
                ci.cancel();
                int tempGlId = RenderSystem.getShaderTexture(0);
                RenderSystem.setShaderTexture(0, this.texture.getGlId());
                RenderSystem.setShader(GameRenderer::getPositionTexProgram);
                BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
                bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
                bufferBuilder.vertex(matrix, x, y, 50).texture(0.0F, 0.0F).next();
                bufferBuilder.vertex(matrix, x, y + 16.0F, 50).texture(0.0F, img.getHeight() / 16.0F).next();
                bufferBuilder.vertex(matrix, x + 16.0F, y + 16.0F, 50).texture(img.getWidth() / 16.0F, img.getHeight() / 16.0F).next();
                bufferBuilder.vertex(matrix, x + 16.0F, y, 50).texture(img.getWidth() / 16.0F, 0.0F).next();
                BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
                RenderSystem.setShaderTexture(0, tempGlId);
            }
        }
    }
}
