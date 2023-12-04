package me.falu.twitchemotes.emote.texture;

import lombok.RequiredArgsConstructor;
import lombok.ToString;
import me.falu.twitchemotes.TwitchEmotes;
import me.falu.twitchemotes.emote.Emote;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Util;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@ToString
public class EmoteTextureHandler {
    private final Emote emote;
    private final List<EmoteBackedTexture> textures = new ArrayList<>();
    private int currentFrame = 0;
    private long lastRenderTime = 0L;

    private String getImageType() {
        String[] parts = this.emote.url.split("\\.");
        return parts[parts.length - 1];
    }

    private byte[] convertImageToBytes(BufferedImage image) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "png", out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public float getWidth() {
        if (this.emote.zeroWidth) { return 0.0F; }
        if (this.textures.isEmpty()) { return TwitchEmotes.EMOTE_SIZE; }
        NativeImageBackedTexture texture = this.textures.get(this.currentFrame);
        if (texture.getImage() == null) { return TwitchEmotes.EMOTE_SIZE; }
        NativeImage img = texture.getImage();
        return (TwitchEmotes.EMOTE_SIZE * img.getWidth()) / img.getHeight();
    }

    public NativeImage getImage() {
        if (this.textures.isEmpty()) {
            try {
                URL url = new URL(this.emote.url);
                ImageInputStream stream = ImageIO.createImageInputStream(url.openStream());
                ImageReader reader = ImageIO.getImageReadersBySuffix(this.getImageType()).next();
                reader.setInput(stream);
                reader.getNumImages(true); // This method reads image header + pre-loads all frames.

                // I hope someone got fired over this. All the readers are package-private.
                Field framesField = reader.getClass().getDeclaredField("frames");
                framesField.setAccessible(true);
                List<?> frames = (List<?>) framesField.get(reader);

                if (!frames.isEmpty()) {
                    for (int i = 0; i < frames.size(); i++) {
                        Object frame = frames.get(i);
                        Field durationField = frame.getClass().getDeclaredField("duration");
                        durationField.setAccessible(true);
                        int duration = durationField.getInt(frame);
                        BufferedImage bufferedImage = reader.read(i);
                        NativeImage img = NativeImage.read(NativeImage.Format.RGBA, new ByteArrayInputStream(this.convertImageToBytes(bufferedImage)));
                        this.textures.add(new EmoteBackedTexture(img, duration));
                    }
                } else {
                    BufferedImage bufferedImage = reader.read(0);
                    NativeImage img = NativeImage.read(NativeImage.Format.RGBA, new ByteArrayInputStream(this.convertImageToBytes(bufferedImage)));
                    this.textures.add(new EmoteBackedTexture(img));
                }
            } catch (IOException | IllegalAccessException | NoSuchFieldException e) {
                TwitchEmotes.LOGGER.error("Error while reading image for '" + this.emote.name + "'", e);
                TwitchEmotes.invalidateEmote(this.emote);
                return null;
            }
        }
        return this.textures.get(this.currentFrame).getImage();
    }

    public void postRender() {
        if (this.textures.size() <= 1) { return; }
        long now = Util.getMeasuringTimeMs();
        if (this.lastRenderTime > 0L) {
            long difference = now - this.lastRenderTime;
            long duration = this.textures.get(this.currentFrame).duration;
            if (difference >= duration) {
                this.currentFrame++;
                if (this.currentFrame >= this.textures.size()) {
                    this.currentFrame = 0;
                }
            }
        }
        this.lastRenderTime = now;
    }

    public int getGlId() {
        try {
            return this.textures.get(this.currentFrame).getGlId();
        } catch (IndexOutOfBoundsException ignored) {
            TwitchEmotes.LOGGER.error("Requested frame doesn't exist for emote '" + this.emote.name + "'.");
            TwitchEmotes.invalidateEmote(this.emote);
        }
        return -1;
    }
}
