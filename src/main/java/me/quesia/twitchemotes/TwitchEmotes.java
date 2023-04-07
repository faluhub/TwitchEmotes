package me.quesia.twitchemotes;

import com.gikk.twirk.Twirk;
import com.gikk.twirk.TwirkBuilder;
import com.gikk.twirk.events.TwirkListener;
import com.gikk.twirk.types.clearChat.ClearChat;
import com.gikk.twirk.types.clearMsg.ClearMsg;
import com.gikk.twirk.types.twitchMessage.TwitchMessage;
import com.gikk.twirk.types.users.TwitchUser;
import com.google.gson.*;
import me.quesia.twitchemotes.owner.TwitchMessageListOwner;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.*;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;

public class TwitchEmotes implements ClientModInitializer {
    public static final String MOD_ID = "twitchemotes";
    public static final ModContainer MOD_CONTAINER = FabricLoader.getInstance().getModContainer(MOD_ID).orElseThrow(RuntimeException::new);
    public static final String MOD_NAME = MOD_CONTAINER.getMetadata().getName();
    public static final String MOD_VERSION = String.valueOf(MOD_CONTAINER.getMetadata().getVersion());
    public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);
    public static Twirk TWIRK;
    public static final String TEMP_IMAGE_FORMAT = "png";
    public static final File TEMP_IMAGE_FILE = FabricLoader.getInstance().getConfigDir().resolve("temp." + TEMP_IMAGE_FORMAT).toFile();
    public static final Map<String, Emote> EMOTE_MAP = new HashMap<>();
    public static final List<String> FAILED_EMOTES = new ArrayList<>();

    public static int PREVIEW_CHARACTER_LIMIT;
    public static int FRAMES_PER_SECOND;
    public static String TWITCH_NAME;
    public static String TWITCH_ID;
    public static String TWITCH_AUTH;

    public static void log(Object msg) {
        LOGGER.log(Level.INFO, msg);
    }

    public static String getShortenedString(String text) {
        if (text.length() > PREVIEW_CHARACTER_LIMIT) {
            text = text.substring(0, PREVIEW_CHARACTER_LIMIT) + "...";
        }
        return text;
    }

    public static JsonElement getJsonResponse(String endpoint) throws IOException {
        URL url = new URL(endpoint);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        InputStream inputStream = connection.getInputStream();
        String result = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        JsonParser parser = new JsonParser();
        return parser.parse(result);
    }

    public static List<NativeImage> getNativeImages(Emote emote) {
        if (emote.hasImageCache() && !emote.isCachingImages()) { return emote.getImageCache(); }
        new Thread(() -> {
            try {
                URL url = new URL(emote.getImage());
                if (emote.getImage().endsWith(".webp")) {
                    ImageInputStream stream = ImageIO.createImageInputStream(url.openStream());
                    ImageReader reader = ImageIO.getImageReadersByMIMEType("image/webp").next();
                    reader.setInput(stream);
                    emote.clearImageCache();
                    int frame = 0;
                    while (true) {
                        try {
                            BufferedImage image = reader.read(frame);
                            ImageIO.write(image, TEMP_IMAGE_FORMAT, TEMP_IMAGE_FILE);
                            if (image.getAlphaRaster() != null) {
                                boolean transparent = true;
                                for (int x = 0; x < image.getWidth(); x++) {
                                    if (!transparent) { break; }
                                    for (int y = 0; y < image.getHeight(); y++) {
                                        int[] alpha = image.getAlphaRaster().getPixel(x, y, new int[image.getAlphaRaster().getNumBands()]);
                                        if (alpha[0] > 0) {
                                            transparent = false;
                                            break;
                                        }
                                    }
                                }
                                if (transparent) {
                                    emote.clearImageCache();
                                    throw new IOException("Frame " + frame + " from emote '" + emote.getName() + "' is fully transparent!");
                                }
                            }
                            InputStream inputStream = TEMP_IMAGE_FILE.toURI().toURL().openStream();
                            NativeImage frameImage = NativeImage.read(NativeImage.Format.ABGR, inputStream);
                            inputStream.close();
                            emote.addImageToCache(frameImage);
                            frame++;
                        } catch (IndexOutOfBoundsException ignored) { break; }
                    }
                } else {
                    URLConnection conn = url.openConnection();
                    InputStream inputStream = conn.getInputStream();
                    emote.clearImageCache();
                    emote.addImageToCache(NativeImage.read(NativeImage.Format.ABGR, inputStream));
                }
            } catch (IOException e) {
                TwitchEmotes.FAILED_EMOTES.add(emote.getName());
                TwitchEmotes.LOGGER.error(e.getMessage());
                TwitchEmotes.LOGGER.warn("Emote '" + emote.getName() + "' failed to render. Will no longer retry.");
            }
            emote.stopCachingImages();
            if (!TEMP_IMAGE_FILE.delete()) {
                LOGGER.warn("Couldn't delete temp image file.");
            }
        }).start();
        emote.startCachingImages();
        return emote.getImageCache();
    }

    private String getStringValue(String key, String def, JsonObject object) {
        if (!object.has(key)) {
            object.addProperty(key, def);
            return def;
        }
        return object.get(key).getAsString();
    }

    private int getIntValue(String key, int def, JsonObject object) {
        if (!object.has(key)) {
            object.addProperty(key, def);
            return def;
        }
        return object.get(key).getAsInt();
    }

    public void getValues() {
        try {
            File configFile = FabricLoader.getInstance().getConfigDir().resolve(MOD_NAME + ".json").toFile();
            JsonObject object;
            if (!configFile.exists()) {
                if (configFile.createNewFile()) { object = new JsonObject(); }
                else {
                    LOGGER.warn("No access to the config file.");
                    return;
                }
            } else {
                FileReader reader = new FileReader(configFile);
                JsonParser parser = new JsonParser();
                Object obj = parser.parse(reader);
                reader.close();
                object = obj == null || obj.equals(JsonNull.INSTANCE) ? new JsonObject() : (JsonObject) obj;
            }

            PREVIEW_CHARACTER_LIMIT = this.getIntValue("preview_character_limit", 18, object);
            FRAMES_PER_SECOND = this.getIntValue("frames_per_second", 30, object);
            TWITCH_NAME = this.getStringValue("twitch_name", "", object);
            TWITCH_AUTH = this.getStringValue("twitch_auth", "", object);

            FileWriter writer = new FileWriter(configFile);
            writer.write(new GsonBuilder().setPrettyPrinting().create().toJson(object));
            writer.flush();
            writer.close();

            JsonObject user = getJsonResponse("https://api.7tv.app/v2/users/" + TWITCH_NAME).getAsJsonObject();
            if (user.has("twitch_id")) { TWITCH_ID = user.get("twitch_id").getAsString(); }
            else if (object.has("twitch_id")) { TWITCH_ID = user.get("twitch_id").getAsString(); }
            else { throw new RuntimeException("You are required to have a 7TV account with this username."); }
        } catch (IOException ignored) {
            LOGGER.error("Couldn't read/write config file.");
        }
    }

    public void setupTwirk() {
        TWIRK = new TwirkBuilder(TWITCH_NAME, TWITCH_NAME, TWITCH_AUTH).setDebugLogMethod(s -> {
            if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
                System.out.println(s);
            }
        }).build();
        TWIRK.addIrcListener(new TwirkListener() {
            private final MinecraftClient client = MinecraftClient.getInstance();

            @Override
            public void onPrivMsg(TwitchUser sender, TwitchMessage message) {
                ((TwitchMessageListOwner) this.client.inGameHud.getChatHud()).addMessage("<" + sender.getDisplayName() + "> " + message.getContent(), message.getMessageID());
                TwirkListener.super.onPrivMsg(sender, message);
            }

            @Override
            public void onClearChat(ClearChat clearChat) {
                ((TwitchMessageListOwner) this.client.inGameHud.getChatHud()).onMessagesClear();
                TwirkListener.super.onClearChat(clearChat);
            }

            @Override
            public void onClearMsg(ClearMsg clearMsg) {
                ((TwitchMessageListOwner) this.client.inGameHud.getChatHud()).onMessageDelete(clearMsg.getTargetMsgId());
                TwirkListener.super.onClearMsg(clearMsg);
            }

            @Override
            public void onConnect() {
                log("Connected to Twitch chat.");
                TwirkListener.super.onConnect();
            }

            @Override
            public void onDisconnect() {
                log("Disconnected from Twitch chat.");
                try {
                    if (!TWIRK.connect()) {
                        LOGGER.error("Couldn't reconnect to Twitch chat.");
                        TWIRK.close();
                    }
                } catch (IOException | InterruptedException e) { TWIRK.close(); }
                TwirkListener.super.onDisconnect();
            }
        });
        try { TWIRK.connect(); }
        catch (IOException | InterruptedException e) { LOGGER.warn("Couldn't connect to Twitch chat. This feature will be disabled."); }
    }

    public void collectEmotes() {
        try {
            JsonArray bttv = getJsonResponse("https://api.betterttv.net/3/cached/emotes/global").getAsJsonArray();
            JsonObject bttvUser = getJsonResponse("https://api.betterttv.net/3/cached/users/twitch/" + TWITCH_ID).getAsJsonObject();
            bttv.addAll(bttvUser.get("channelEmotes").getAsJsonArray());
            bttv.addAll(bttvUser.get("sharedEmotes").getAsJsonArray());

            for (JsonElement element : bttv) {
                JsonObject emote = element.getAsJsonObject();
                String name = emote.get("code").getAsString();
                String id = emote.get("id").getAsString();
                String image = "https://cdn.betterttv.net/emote/" + id + "/3x";
                boolean zeroWidth = List.of("5e76d338d6581c3724c0f0b2", "5e76d399d6581c3724c0f0b8", "567b5b520e984428652809b6", "5849c9a4f52be01a7ee5f79d", "567b5c080e984428652809ba", "567b5dc00e984428652809bd", "58487cc6f52be01a7ee5f205", "5849c9c8f52be01a7ee5f79e").contains(id);
                EMOTE_MAP.put(name, new Emote(name, id, image, zeroWidth));
            }
        } catch (IOException ignored) { LOGGER.warn("Couldn't load BTTV emotes."); }

        try {
            JsonArray ffz = getJsonResponse("https://api.betterttv.net/3/cached/frankerfacez/emotes/global").getAsJsonArray();
            JsonArray ffzUser = getJsonResponse("https://api.betterttv.net/3/cached/frankerfacez/users/twitch/" + TWITCH_ID).getAsJsonArray();
            ffz.addAll(ffzUser);

            for (JsonElement element : ffz) {
                JsonObject emote = element.getAsJsonObject();
                String name = emote.get("code").getAsString();
                String id = emote.get("id").getAsString();
                JsonObject images = emote.get("images").getAsJsonObject();
                String image;
                if (images.has("4x")) { image = images.get("4x").getAsString(); }
                else if (images.has("2x")) { image = images.get("2x").getAsString(); }
                else { image = images.get("1x").getAsString(); }
                EMOTE_MAP.put(name, new Emote(name, id, image, false));
            }
        } catch (IOException ignored) { LOGGER.warn("Couldn't load FFZ emotes."); }

        try {
            JsonArray stv = getJsonResponse("https://api.7tv.app/v2/emotes/global").getAsJsonArray();
            JsonArray stvUser = getJsonResponse("https://api.7tv.app/v2/users/" + TWITCH_ID + "/emotes").getAsJsonArray();
            stv.addAll(stvUser);

            for (JsonElement element : stv) {
                JsonObject emote = element.getAsJsonObject();
                String name = emote.get("name").getAsString();
                String id = emote.get("id").getAsString();
                JsonArray urls = emote.get("urls").getAsJsonArray();
                String image = urls.get(urls.size() - 1).getAsJsonArray().get(1).getAsString();
                boolean zeroWidth = emote.get("visibility_simple").getAsJsonArray().contains(new JsonPrimitive("ZERO_WIDTH"));
                EMOTE_MAP.put(name, new Emote(name, id, image, zeroWidth));
            }
        } catch (IOException ignored) { LOGGER.warn("Couldn't load 7TV emotes."); }

        log("Loaded " + EMOTE_MAP.size() + " emotes.");
    }

    @Override
    public void onInitializeClient() {
        log("Using " + MOD_NAME + " v" + MOD_VERSION);

        this.getValues();
        this.setupTwirk();
        this.collectEmotes();
    }
}
