package me.falu.twitchemotes.emote;

import com.mojang.blaze3d.systems.RenderSystem;
import lombok.Builder;
import lombok.ToString;
import me.falu.twitchemotes.TwitchEmotes;
import net.minecraft.client.render.*;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import org.joml.Matrix4f;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

@Builder
@ToString
public class Emote {
    public final String name;
    public final String id;
    public final String url;
    public final boolean zeroWidth;
    public NativeImageBackedTexture texture;

    public void draw(float x, float y, Matrix4f matrix, float alpha) {
        if (this.texture == null) {
            try {
                URL url = new URL(this.url);
                InputStream stream = url.openConnection().getInputStream();
                NativeImage img = NativeImage.read(NativeImage.Format.RGBA, stream);
                this.texture = new NativeImageBackedTexture(img);
            } catch (IOException e) {
                TwitchEmotes.LOGGER.error("Error while reading image for '" + this.name + "'", e);
                TwitchEmotes.invalidateEmote(this);
                return;
            }
        }
        NativeImage img = this.texture.getImage();
        if (img != null) {
//            this.createTextureBuffer(matrix, x, y, alpha, img);
            this.createDebugBuffer(matrix, x, y);
            return;
        }
        TwitchEmotes.LOGGER.error("Couldn't get image for emote '" + this.name + "'.");
        TwitchEmotes.invalidateEmote(this);
    }

    private void createTextureBuffer(Matrix4f matrix, float x, float y, float alpha, NativeImage img) {
        RenderSystem.setShaderTexture(0, this.texture.getGlId());
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        bufferBuilder
                .vertex(matrix, x, y, 100.0F)
                .texture(0.0F, 0.0F)
                .color(1.0F, 1.0F, 1.0F, alpha)
                .next();
        bufferBuilder
                .vertex(matrix, x, y + 16.0F, 100.0F)
                .texture(0.0F, img.getHeight())
                .color(1.0F, 1.0F, 1.0F, alpha)
                .next();
        bufferBuilder
                .vertex(matrix, x + 16.0F, y + 16.0F, 100.0F)
                .texture(img.getWidth(), img.getHeight())
                .color(1.0F, 1.0F, 1.0F, alpha)
                .next();
        bufferBuilder
                .vertex(matrix, x + 16.0F, y, 100.0F)
                .texture(img.getWidth(), 0.0F)
                .color(1.0F, 1.0F, 1.0F, alpha)
                .next();
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
    }

    private void createDebugBuffer(Matrix4f matrix, float x, float y) {
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        bufferBuilder
                .vertex(matrix, x, y, 100.0F)
                .color(0.0F, 1.0F, 0.0F, 1.0F)
                .next();
        bufferBuilder
                .vertex(matrix, x, y + 16.0F, 100.0F)
                .color(0.0F, 1.0F, 0.0F, 1.0F)
                .next();
        bufferBuilder
                .vertex(matrix, x + 16.0F, y + 16.0F, 100.0F)
                .color(0.0F, 1.0F, 0.0F, 1.0F)
                .next();
        bufferBuilder
                .vertex(matrix, x + 16.0F, y, 100.0F)
                .color(0.0F, 1.0F, 0.0F, 1.0F)
                .next();
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
    }
}
