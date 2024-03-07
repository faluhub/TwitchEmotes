package me.falu.twitchemotes.emote.texture;

import me.falu.twitchemotes.emote.Emote;
import net.minecraft.client.texture.NativeImage;
import org.w3c.dom.Node;
import webpdecoderjn.WebPDecoder;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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

    public List<EmoteBackedTexture> read() throws IOException {
        return switch (this.imageType) {
            default -> this.readStatic();
            case WEBP -> this.readWebP();
            case GIF -> this.readGIF();
        };
    }

    private List<EmoteBackedTexture> readWebP() throws IOException {
        WebPDecoder.WebPImage image = WebPDecoder.decodeUrl(this.url.toString());
        if (image.frames.size() == 1) {
            NativeImage img = NativeImage.read(NativeImage.Format.RGBA, this.convertImageToBytes(image.frames.get(0).img));
            return List.of(new EmoteBackedTexture(img));
        }
        List<EmoteBackedTexture> textures = new ArrayList<>();
        for (int i = 0; i < image.frames.size(); i++) {
            WebPDecoder.WebPImageFrame frame = image.frames.get(i);
            NativeImage img = NativeImage.read(NativeImage.Format.RGBA, this.convertImageToBytes(frame.img));
            textures.add(new EmoteBackedTexture(img, frame.delay));
        }
        return textures;
    }

    private List<EmoteBackedTexture> readGIF() throws IOException {
        List<EmoteBackedTexture> textures = new ArrayList<>();
        ImageReader reader = this.getImageReader();
        for (int i = 0; i < reader.getNumImages(true); i++) {
            BufferedImage bufferedImage = reader.read(i);
            IIOMetadata metadata = reader.getImageMetadata(i);
            Node metadataRoot = metadata.getAsTree("javax_imageio_gif_image_1.0");
            for (int j = 0; j < metadataRoot.getChildNodes().getLength(); j++) {
                Node node = metadataRoot.getChildNodes().item(j);
                if (node.getNodeName().equals("GraphicControlExtension")) {
                    Node delayAttribute = node.getAttributes().getNamedItem("delayTime");
                    int delay = Integer.parseInt(delayAttribute.getNodeValue()) * 10;
                    NativeImage img = NativeImage.read(NativeImage.Format.RGBA, this.convertImageToBytes(bufferedImage));
                    textures.add(new EmoteBackedTexture(img, delay));
                    break;
                }
            }
        }
        return textures;
    }

    private List<EmoteBackedTexture> readStatic() throws IOException {
        InputStream imageStream = this.url.openStream();
        return List.of(new EmoteBackedTexture(NativeImage.read(NativeImage.Format.RGBA, imageStream)));
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
