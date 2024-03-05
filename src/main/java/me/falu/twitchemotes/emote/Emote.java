package me.falu.twitchemotes.emote;

import com.mojang.blaze3d.systems.RenderSystem;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import me.falu.twitchemotes.TwitchEmotes;
import me.falu.twitchemotes.emote.texture.EmoteTextureHandler;
import net.minecraft.client.render.*;
import org.joml.Matrix4f;

@Builder
@RequiredArgsConstructor
@ToString
public class Emote {
    public final String name;
    public final String id;
    public final String url;
    public final ImageType imageType;
    public final EmoteTextureHandler textureHandler = new EmoteTextureHandler(this);

    public boolean scheduleDraw(float x, float y, Matrix4f matrix, float alpha) {
        if (this.textureHandler.getImage() != null || this.textureHandler.loading) {
            return TwitchEmotes.SCHEDULED_DRAW.add(new DrawData(this, x, y, matrix, alpha));
        }
        return false;
    }

    public void draw(DrawData data) {
        this.createTextureBuffer(data.matrix, data.x, data.y - 1.0F, data.alpha);
        this.textureHandler.postRender();
    }

    public void createTextureBuffer(Matrix4f matrix, float x, float y, float alpha) {
        int glId = this.textureHandler.getGlId();
        if (glId == -1) {
            return;
        }
        RenderSystem.setShaderTexture(0, glId);
        RenderSystem.setShader(GameRenderer::getPositionColorTexProgram);
        RenderSystem.enableBlend();
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        float size = TwitchEmotes.EMOTE_SIZE;
        float width = this.textureHandler.getWidth();
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE);
        bufferBuilder
                .vertex(matrix, x, y, 100.0F)
                .color(1.0F, 1.0F, 1.0F, alpha)
                .texture(0.0F, 0.0F)
                .next();
        bufferBuilder
                .vertex(matrix, x, y + size, 100.0F)
                .color(1.0F, 1.0F, 1.0F, alpha)
                .texture(0.0F, 1.0F)
                .next();
        bufferBuilder
                .vertex(matrix, x + width, y + size, 100.0F)
                .color(1.0F, 1.0F, 1.0F, alpha)
                .texture(1.0F, 1.0F)
                .next();
        bufferBuilder
                .vertex(matrix, x + width, y, 100.0F)
                .color(1.0F, 1.0F, 1.0F, alpha)
                .texture(1.0F, 0.0F)
                .next();
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
        RenderSystem.disableBlend();
    }

    public enum ImageType {
        GIF("gif"),
        WEBP("webp"),
        STATIC("png");
        public final String suffix;

        ImageType(String suffix) {
            this.suffix = suffix;
        }

        public static ImageType fromSuffix(String suffix) {
            for (ImageType type : ImageType.values()) {
                if (type.suffix.equals(suffix)) {
                    return type;
                }
            }
            return STATIC;
        }
    }

    @ToString
    @RequiredArgsConstructor
    public static class DrawData {
        public final Emote emote;
        public final float x;
        public final float y;
        public final Matrix4f matrix;
        public final float alpha;

        public void draw() {
            this.emote.draw(this);
        }
    }
}
