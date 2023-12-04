package me.falu.twitchemotes.emote;

import com.mojang.blaze3d.systems.RenderSystem;
import lombok.Builder;
import lombok.ToString;
import me.falu.twitchemotes.TwitchEmotes;
import me.falu.twitchemotes.emote.texture.EmoteTextureHandler;
import net.minecraft.client.render.*;
import net.minecraft.client.texture.NativeImage;
import org.joml.Matrix4f;

@Builder
@ToString
public class Emote {
    public final String name;
    public final String id;
    public final String url;
    public final boolean zeroWidth;
    public final EmoteTextureHandler textureHandler = new EmoteTextureHandler(this);

    public boolean draw(float x, float y, Matrix4f matrix, float alpha) {
        NativeImage img = this.textureHandler.getImage();
        if (img != null) {
            this.createTextureBuffer(matrix, x - 1.0F, y, alpha);
            this.textureHandler.postRender();
            return true;
        }
        return false;
    }

    private void createTextureBuffer(Matrix4f matrix, float x, float y, float alpha) {
        int glId = this.textureHandler.getGlId();
        if (glId == -1) { return; }
        RenderSystem.setShaderTexture(0, glId);
        RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        float size = TwitchEmotes.EMOTE_SIZE;
        float width = this.textureHandler.getWidth();
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        bufferBuilder
                .vertex(matrix, x, y, 100.0F)
                .texture(0.0F, 0.0F)
                .color(1.0F, 1.0F, 1.0F, alpha)
                .next();
        bufferBuilder
                .vertex(matrix, x, y + size, 100.0F)
                .texture(0.0F, 1.0F)
                .color(1.0F, 1.0F, 1.0F, alpha)
                .next();
        bufferBuilder
                .vertex(matrix, x + width, y + size, 100.0F)
                .texture(1.0F, 1.0F)
                .color(1.0F, 1.0F, 1.0F, alpha)
                .next();
        bufferBuilder
                .vertex(matrix, x + width, y, 100.0F)
                .texture(1.0F, 0.0F)
                .color(1.0F, 1.0F, 1.0F, alpha)
                .next();
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
    }
}
