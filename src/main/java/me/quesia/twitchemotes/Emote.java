package me.quesia.twitchemotes;

import lombok.RequiredArgsConstructor;
import net.minecraft.client.texture.NativeImage;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class Emote {
    private final String name;
    private final String id;
    private final String image;
    private final boolean zeroWidth;
    private final List<NativeImage> imageCache = new ArrayList<>();
    private int currentFrame;
    private long firstFrameRender;
    private boolean cachingImages;
    @Nullable private Integer widthCache;

    public String getName() {
        return this.name;
    }

    public String getId() {
        return this.id;
    }

    public String getImage() {
        return this.image;
    }

    public boolean isZeroWidth() {
        return this.zeroWidth;
    }

    public int getCurrentFrame() {
        return this.currentFrame;
    }

    public void incrementCurrentFrame() {
        this.currentFrame++;
    }

    public void resetCurrentFrame() {
        this.currentFrame = 0;
    }

    public long getFirstFrameRender() {
        return this.firstFrameRender;
    }

    public void setFirstFrameRender(long time) {
        this.firstFrameRender = time;
    }

    public List<NativeImage> getImageCache() {
        return this.imageCache;
    }

    public void addImageToCache(NativeImage image) {
        this.imageCache.add(image);
    }

    public void clearImageCache() {
        for (NativeImage image : this.imageCache) {
            image.close();
        }
        this.imageCache.clear();
    }

    public boolean hasImageCache() {
        return !this.imageCache.isEmpty();
    }

    public boolean isCachingImages() {
        return this.cachingImages;
    }

    public void startCachingImages() {
        this.cachingImages = true;
    }

    public void stopCachingImages() {
        this.cachingImages = false;
    }

    public int getWidth() {
        if (this.widthCache != null) { return this.widthCache; }
        int ratio = 8;
        if (this.isZeroWidth()) { return 0; }
        else if (this.imageCache.isEmpty()) { return ratio; }
        int width = ratio;
        NativeImage image = this.imageCache.get(0);
        if (image.getWidth() != image.getHeight()) { width = (ratio * image.getWidth()) / image.getHeight(); }
        return this.widthCache = width;
    }
}
