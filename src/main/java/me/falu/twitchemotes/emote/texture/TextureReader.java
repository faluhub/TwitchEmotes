package me.falu.twitchemotes.emote.texture;

import me.falu.twitchemotes.emote.Emote;
import net.minecraft.client.texture.NativeImage;
import webpdecoderjn.WebPDecoder;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class TextureReader {
    private final URL url;
    private final Emote.ImageType imageType;

    public TextureReader(URL url, Emote.ImageType imageType) {
        this.url = url;
        this.imageType = imageType;
    }

    public List<EmoteBackedTexture> read() throws IOException, NoSuchFieldException, IllegalAccessException {
        switch (this.imageType) {
            default:
            case STATIC: return this.readStatic();
            case WEBP: return this.readWebP();
            case GIF: return this.readGIF();
        }
    }

    private List<EmoteBackedTexture> readWebP() throws IOException {
        WebPDecoder.WebPImage image = WebPDecoder.decodeUrl(this.url.toString());
        if (image.frames.size() == 1) {
            NativeImage img = NativeImage.read(NativeImage.Format.ABGR, this.convertImageToBytes(image.frames.get(0).img));
            return List.of(new EmoteBackedTexture(img));
        }
        List<EmoteBackedTexture> textures = new ArrayList<>();
        for (int i = 0; i < image.frames.size(); i++) {
            WebPDecoder.WebPImageFrame frame = image.frames.get(i);
            NativeImage img = NativeImage.read(NativeImage.Format.ABGR, this.convertImageToBytes(frame.img));
            textures.add(new EmoteBackedTexture(img, frame.delay));
        }
        return textures;
    }

    private List<EmoteBackedTexture> readGIF() throws IOException, NoSuchFieldException, IllegalAccessException {
        List<EmoteBackedTexture> textures = new ArrayList<>();
        ImageReader reader = this.getImageReader();
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
        return textures;
    }

    private List<EmoteBackedTexture> readStatic() throws IOException {
        InputStream imageStream = this.url.openStream();
        return List.of(new EmoteBackedTexture(NativeImage.read(NativeImage.Format.ABGR, imageStream)));
    }

    private ImageReader getImageReader() throws IOException {
        InputStream imageStream = this.url.openStream();
        ImageInputStream stream = ImageIO.createImageInputStream(imageStream);
        ImageReader reader = ImageIO.getImageReadersBySuffix(this.imageType.suffix).next();
        reader.setInput(stream);
        return reader;
    }

    private ByteArrayInputStream convertImageToBytes(BufferedImage image) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "png", out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
