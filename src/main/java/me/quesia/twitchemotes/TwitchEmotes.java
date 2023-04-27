package me.quesia.twitchemotes;

import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.chat.events.channel.*;
import com.github.twitch4j.eventsub.events.ChannelRaidEvent;
import com.github.twitch4j.pubsub.domain.ChatModerationAction;
import com.github.twitch4j.pubsub.events.*;
import com.google.gson.*;
import io.github.xanthic.cache.api.exception.NoDefaultCacheImplementationException;
import io.github.xanthic.cache.core.CacheApiSettings;
import io.github.xanthic.cache.provider.caffeine.CaffeineProvider;
import me.quesia.twitchemotes.owner.TwitchMessageListOwner;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
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
    public static TwitchClient TWITCH_CLIENT;
    public static final String TEMP_IMAGE_FORMAT = "png";
    public static final File TEMP_IMAGE_FILE = FabricLoader.getInstance().getConfigDir().resolve("temp." + TEMP_IMAGE_FORMAT).toFile();
    public static final Map<String, Emote> EMOTE_MAP = new HashMap<>();
    public static final List<String> FAILED_EMOTES = new ArrayList<>();

    public static int PREVIEW_CHARACTER_LIMIT;
    public static int CHAT_MESSAGE_LIMIT;
    public static int MESSAGE_LIFESPAN;
    public static int FRAMES_PER_SECOND;
    public static String TWITCH_NAME;
    public static String TWITCH_ID;
    public static String TWITCH_AUTH;
    public static boolean CLEAR_CHAT_ON_JOIN;
    public static boolean ENABLE_CHAT_BACK;
    public static boolean SEND_ALERT_SOUNDS;
    public static boolean SHOW_ROOM_STATE_UPDATES;
    public static boolean SHOW_FOLLOWS;
    public static boolean SHOW_SUBS;
    public static boolean SHOW_HYPETRAIN;
    public static boolean SHOW_CHEERS;
    public static boolean SHOW_RAIDS;

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

    private boolean getBoolValue(String key, boolean def, JsonObject object) {
        if (!object.has(key)) {
            object.addProperty(key, def);
            return def;
        }
        return object.get(key).getAsBoolean();
    }

    private JsonObject parseObject(File file) throws IOException {
        FileReader reader = new FileReader(file);
        JsonParser parser = new JsonParser();
        Object obj = parser.parse(reader);
        reader.close();
        JsonObject object = obj == null || obj.equals(JsonNull.INSTANCE) ? new JsonObject() : (JsonObject) obj;
        if (object.has("file")) {
            File other = new File(object.get("file").getAsString());
            if (other.exists()) {
                return this.parseObject(other);
            }
        }
        return object;
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
            } else { object = this.parseObject(configFile); }

            PREVIEW_CHARACTER_LIMIT = this.getIntValue("preview_character_limit", 24, object);
            CHAT_MESSAGE_LIMIT = this.getIntValue("chat_message_limit", 10, object);
            MESSAGE_LIFESPAN = this.getIntValue("message_lifespan", 200, object);
            FRAMES_PER_SECOND = this.getIntValue("frames_per_second", 30, object);
            TWITCH_NAME = this.getStringValue("twitch_name", "", object);
            TWITCH_AUTH = this.getStringValue("twitch_auth", "", object);
            CLEAR_CHAT_ON_JOIN = this.getBoolValue("clear_chat_on_join", false, object);
            ENABLE_CHAT_BACK = this.getBoolValue("enable_chat_back", true, object);
            SEND_ALERT_SOUNDS = this.getBoolValue("send_alert_sounds", false, object);
            SHOW_ROOM_STATE_UPDATES = this.getBoolValue("show_room_state_updates", true, object);
            SHOW_FOLLOWS = this.getBoolValue("show_follows", true, object);
            SHOW_SUBS = this.getBoolValue("show_subs", true, object);
            SHOW_HYPETRAIN = this.getBoolValue("show_hypetrain", true, object);
            SHOW_CHEERS = this.getBoolValue("show_cheers", true, object);
            SHOW_RAIDS = this.getBoolValue("show_raids", true, object);

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

    private void playSound(SoundEvent event, MinecraftClient client) {
        if (SEND_ALERT_SOUNDS) {
            client.submit(() -> {
                if (client.player != null) {
                    client.player.playSound(event, SoundCategory.MASTER, 1.0F, 1.0F);
                }
            });
        }
    }

    private String getDisplayName(IRCMessageEvent e) {
        Optional<String> displayName = e.getTagValue("display-name");
        return displayName.orElseGet(() -> e.getUser().getName());
    }

    public void setupTwitchListeners() {
        CacheApiSettings.getInstance().setDefaultCacheProvider(new CaffeineProvider());

        MinecraftClient client = MinecraftClient.getInstance();
        OAuth2Credential cred = new OAuth2Credential("twitch", TWITCH_AUTH);
        TWITCH_CLIENT = TwitchClientBuilder.builder()
                .withEnablePubSub(true)
                .withEnableChat(true)
                .withChatAccount(cred)
                .build();

        TWITCH_CLIENT.getChat().joinChannel(TWITCH_NAME);
        TWITCH_CLIENT.getEventManager().onEvent(ChannelMessageEvent.class, e -> ((TwitchMessageListOwner) client.inGameHud.getChatHud()).addMessage("<" + this.getDisplayName(e.getMessageEvent()) + "> " + e.getMessage(), e.getMessageEvent().getMessageId().orElse("")));
        TWITCH_CLIENT.getEventManager().onEvent(ClearChatEvent.class, e -> ((TwitchMessageListOwner) client.inGameHud.getChatHud()).onMessagesClear());
        TWITCH_CLIENT.getEventManager().onEvent(DeleteMessageEvent.class, e -> ((TwitchMessageListOwner) client.inGameHud.getChatHud()).onMessageDelete(e.getMsgId()));

        if (SHOW_ROOM_STATE_UPDATES) {
            TWITCH_CLIENT.getPubSub().listenForModerationEvents(cred, TWITCH_ID, TWITCH_ID);
            TWITCH_CLIENT.getEventManager().onEvent(ChatModerationEvent.class, e -> {
                ChatModerationAction.ModerationAction action = e.getData().getModerationAction();
                if (action.equals(ChatModerationAction.ModerationAction.EMOTE_ONLY) || action.equals(ChatModerationAction.ModerationAction.EMOTE_ONLY_OFF)) {
                    boolean enabled = action.equals(ChatModerationAction.ModerationAction.EMOTE_ONLY);
                    String text = "Emote only mode has been turned " + (enabled ? "on" : "off") + ".";
                    client.inGameHud.getChatHud().addMessage(new LiteralText(text).formatted(Formatting.DARK_GRAY, Formatting.ITALIC));
                } else if (action.equals(ChatModerationAction.ModerationAction.SUBSCRIBERS) || action.equals(ChatModerationAction.ModerationAction.SUBSCRIBERS_OFF)) {
                    boolean enabled = action.equals(ChatModerationAction.ModerationAction.SUBSCRIBERS);
                    String text = "Subscriber only mode has been turned " + (enabled ? "on" : "off") + ".";
                    client.inGameHud.getChatHud().addMessage(new LiteralText(text).formatted(Formatting.DARK_GRAY, Formatting.ITALIC));
                } else if (action.equals(ChatModerationAction.ModerationAction.FOLLOWERS) || action.equals(ChatModerationAction.ModerationAction.FOLLOWERS_OFF)) {
                    boolean enabled = action.equals(ChatModerationAction.ModerationAction.FOLLOWERS);
                    String text = "Follower only mode has been turned " + (enabled ? "on" : "off") + ".";
                    client.inGameHud.getChatHud().addMessage(new LiteralText(text).formatted(Formatting.DARK_GRAY, Formatting.ITALIC));
                } else if (action.equals(ChatModerationAction.ModerationAction.SLOW)) {
                    client.inGameHud.getChatHud().addMessage(new LiteralText("Slow mode has been set to " + e.getData().getSlowDuration() + " seconds."));
                } else if (action.equals(ChatModerationAction.ModerationAction.SLOW_OFF)) {
                    client.inGameHud.getChatHud().addMessage(new LiteralText("Slow mode has been turned off."));
                } else { return; }
                this.playSound(SoundEvents.ENTITY_VILLAGER_WORK_LIBRARIAN, client);
            });
        }
        if (SHOW_FOLLOWS) {
            TWITCH_CLIENT.getPubSub().listenForFollowingEvents(cred, TWITCH_ID);
            TWITCH_CLIENT.getEventManager().onEvent(FollowEvent.class, e -> {
                this.playSound(SoundEvents.BLOCK_BELL_USE, client);
                client.inGameHud.getChatHud().addMessage(new LiteralText(e.getUser().getName() + " is now following!").formatted(Formatting.DARK_GRAY, Formatting.ITALIC));
            });
        }
        if (SHOW_SUBS) {
            TWITCH_CLIENT.getPubSub().listenForSubscriptionEvents(cred, TWITCH_ID);
            TWITCH_CLIENT.getEventManager().onEvent(SubscriptionEvent.class, e -> {
                this.playSound(SoundEvents.ENTITY_PLAYER_LEVELUP, client);
                if (e.getGifted()) {
                    client.inGameHud.getChatHud().addMessage(new LiteralText(e.getGiftedBy().getName() + " gifted a sub to " + e.getUser().getName() + "! (Tier " + e.getSubscriptionPlan() + ")").formatted(Formatting.DARK_GRAY, Formatting.ITALIC));
                    return;
                }
                String suffix = e.getMonths() > 1 ? " for " + e.getMonths() + " months!" : "!";
                String streakSuffix = e.getSubStreak() > 0 ? " (" + e.getSubStreak() + " month" + (e.getSubStreak() > 1 ? "s" : "") + " streak)" : "";
                client.inGameHud.getChatHud().addMessage(new LiteralText(e.getUser().getName() + " subscribed at tier " + e.getSubscriptionPlan() + suffix + streakSuffix).formatted(Formatting.DARK_GRAY, Formatting.ITALIC));
            });
        }
        if (SHOW_HYPETRAIN) {
            TWITCH_CLIENT.getPubSub().listenForHypeTrainEvents(cred, TWITCH_ID);
            TWITCH_CLIENT.getEventManager().onEvent(HypeTrainStartEvent.class, e -> {
                this.playSound(SoundEvents.BLOCK_ANVIL_LAND, client);
                client.inGameHud.getChatHud().addMessage(new LiteralText("A hypetrain has started!").formatted(Formatting.DARK_GRAY, Formatting.ITALIC));
            });
            TWITCH_CLIENT.getEventManager().onEvent(HypeTrainLevelUpEvent.class, e -> {
                this.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, client);
                client.inGameHud.getChatHud().addMessage(new LiteralText("The hypetrain has levelled up to level " + e.getData().getProgress().getLevel() + "!").formatted(Formatting.DARK_GRAY, Formatting.ITALIC));
            });
            TWITCH_CLIENT.getEventManager().onEvent(HypeTrainEndEvent.class, e -> {
                this.playSound(SoundEvents.ITEM_SHIELD_BREAK, client);
                client.inGameHud.getChatHud().addMessage(new LiteralText("The hypetrain has ended!" + (EMOTE_MAP.containsKey("Sadge") ? " Sadge" : "")).formatted(Formatting.DARK_GRAY, Formatting.ITALIC));
            });
        }
        if (SHOW_CHEERS) {
            TWITCH_CLIENT.getPubSub().listenForCheerEvents(cred, TWITCH_ID);
            TWITCH_CLIENT.getEventManager().onEvent(ChannelBitsEvent.class, e -> {
                this.playSound(SoundEvents.ENTITY_VILLAGER_WORK_CARTOGRAPHER, client);
                client.inGameHud.getChatHud().addMessage(new LiteralText(e.getData().getUserName() + " cheered " + e.getData().getBitsUsed() + " bits!").formatted(Formatting.DARK_GRAY, Formatting.ITALIC));
            });
        }
        if (SHOW_RAIDS) {
            TWITCH_CLIENT.getPubSub().listenForRaidEvents(cred, TWITCH_ID);
            TWITCH_CLIENT.getEventManager().onEvent(ChannelRaidEvent.class, e -> {
                this.playSound(SoundEvents.EVENT_RAID_HORN, client);
                client.inGameHud.getChatHud().addMessage(new LiteralText(e.getFromBroadcasterUserName() + " is raiding with " + e.getViewers() + " viewers!").formatted(Formatting.DARK_GRAY, Formatting.ITALIC));
            });
        }
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
        this.setupTwitchListeners();
        this.collectEmotes();
    }
}
