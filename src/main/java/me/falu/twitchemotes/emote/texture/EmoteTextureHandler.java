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
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InaccessibleObjectException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("BlockingMethodInNonBlockingContext")
@RequiredArgsConstructor
@ToString
public class EmoteTextureHandler {
    private final Emote emote;
    private final List<EmoteBackedTexture> textures = new ArrayList<>();
    public boolean loading;
    private int currentFrame = 0;
    private long lastAdvanceTime = 0L;

    private ByteArrayInputStream convertImageToBytes(BufferedImage image) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "png", out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public float getWidth() {
        if (this.textures.isEmpty()) {
            return TwitchEmotes.EMOTE_SIZE;
        }
        NativeImageBackedTexture texture = this.textures.get(this.currentFrame);
        if (texture.getImage() == null) {
            return TwitchEmotes.EMOTE_SIZE;
        }
        NativeImage img = texture.getImage();
        return (TwitchEmotes.EMOTE_SIZE * img.getWidth()) / img.getHeight();
    }

    public NativeImage getImage() {
        if (this.textures.isEmpty() && !this.loading) {
            new Thread(() -> {
                List<EmoteBackedTexture> textures = new ArrayList<>();
                URL url;
                try {
                    url = new URL(this.emote.url.replace("http:", "https:"));
                } catch (MalformedURLException ignored) {
                    TwitchEmotes.LOGGER.error("Invalid URL for emote '" + this.emote.name + "'.");
                    TwitchEmotes.invalidateEmote(this.emote);
                    return;
                }
                try {
                    InputStream imageStream = url.openStream();
                    ImageInputStream stream = ImageIO.createImageInputStream(imageStream);
                    ImageReader reader = ImageIO.getImageReadersBySuffix(this.emote.imageType.suffix).next();
                    reader.setInput(stream);
                    switch (this.emote.imageType) {
                        case WEBP:
                            reader.getNumImages(true);
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
                                    NativeImage img = NativeImage.read(NativeImage.Format.ABGR, this.convertImageToBytes(bufferedImage));
                                    textures.add(new EmoteBackedTexture(img, duration));
                                }
                            } else {
                                NativeImage img = NativeImage.read(NativeImage.Format.ABGR, this.convertImageToBytes(reader.read(0)));
                                textures.add(new EmoteBackedTexture(img));
                            }
                            break;
                        case GIF:
                            for (int i = 0; i < reader.getNumImages(true); i++) {
                                BufferedImage bufferedImage = reader.read(i);
                                Field metadataField = reader.getClass().getDeclaredField("imageMetadata");
                                metadataField.setAccessible(true);
                                Object metadata = metadataField.get(reader);
                                Field delayField = metadata.getClass().getDeclaredField("delayTime");
                                int delay = delayField.getInt(metadata);
                                NativeImage img = NativeImage.read(NativeImage.Format.ABGR, this.convertImageToBytes(bufferedImage));
                                textures.add(new EmoteBackedTexture(img, delay));
                            }
                            break;
                        case STATIC:
                            textures.add(new EmoteBackedTexture(NativeImage.read(NativeImage.Format.ABGR, imageStream)));
                            break;
                    }
                    stream.close();
                    imageStream.close();
                    reader.dispose();
                } catch (IOException | IllegalAccessException | NoSuchFieldException | InaccessibleObjectException e) {
                    TwitchEmotes.LOGGER.error("Error while reading image for '" + this.emote.name + "'", e);
                    TwitchEmotes.invalidateEmote(this.emote);
                    this.loading = false;
                    return;
                }
                this.textures.addAll(textures);
                this.loading = false;
            }).start();
            this.loading = true;
            return null;
        } else if (this.loading) {
            return null;
        }
        return this.textures.get(this.currentFrame).getImage();
    }

    public void postRender() {
        if (this.textures.size() <= 1) {
            return;
        }
        long now = Util.getMeasuringTimeMs();
        long duration = this.textures.get(this.currentFrame).duration;
        if (this.lastAdvanceTime > 0L) {
            long difference = now - this.lastAdvanceTime;
            if (difference >= duration) {
                this.currentFrame++;
                if (this.currentFrame >= this.textures.size()) {
                    this.currentFrame = 0;
                }
                this.lastAdvanceTime = now;
            }
        } else {
            this.lastAdvanceTime = now + duration;
        }
    }

    public int getGlId() {
        try {
            return this.textures.get(this.currentFrame).getGlId();
        } catch (IndexOutOfBoundsException ignored) {
            if (!this.loading) {
                TwitchEmotes.LOGGER.error("Requested frame doesn't exist for emote '" + this.emote.name + "'.");
                TwitchEmotes.invalidateEmote(this.emote);
            }
        }
        return -1;
    }
}
