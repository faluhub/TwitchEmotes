package me.falu.twitchemotes.emote.texture;

import lombok.RequiredArgsConstructor;
import lombok.ToString;
import me.falu.twitchemotes.TwitchEmotes;
import me.falu.twitchemotes.emote.Emote;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Util;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@ToString
public class EmoteTextureHandler {
    private final Emote emote;
    private final List<EmoteBackedTexture> textures = new ArrayList<>();
    public boolean loading;
    public boolean failed;
    private int currentFrame = 0;
    private long lastAdvanceTime = 0L;

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
        try{
            if (this.textures.isEmpty() && !this.loading && !this.failed) {
                new Thread(() -> {
                    List<EmoteBackedTexture> textures;
                    URL url;
                    try {
                        url = new URL(this.emote.url.replace("http:", "https:"));
                    } catch (MalformedURLException ignored) {
                        TwitchEmotes.LOGGER.error("Invalid URL for emote '" + this.emote.name + "'.");
                        TwitchEmotes.invalidateEmote(this.emote);
                        return;
                    }
                    try {
                        TextureReader textureReader = new TextureReader(url, this.emote.imageType);
                        textures = new ArrayList<>(textureReader.read());
                    } catch (IOException e) {
                        TwitchEmotes.LOGGER.error("Error while reading image for '" + this.emote.name + "'", e);
                        TwitchEmotes.invalidateEmote(this.emote);
                        this.failed = true;
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
        } catch (Exception e) {
            TwitchEmotes.LOGGER.error("Error while reading image for '" + this.emote.name + "'", e);
            TwitchEmotes.invalidateEmote(this.emote);
            this.failed = true;
            this.loading = false;
            return null;
        }
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
