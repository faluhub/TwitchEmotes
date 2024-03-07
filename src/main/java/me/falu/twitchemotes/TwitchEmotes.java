package me.falu.twitchemotes;

import com.gikk.twirk.Twirk;
import com.gikk.twirk.TwirkBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.falu.twitchemotes.chat.TwitchListener;
import me.falu.twitchemotes.emote.Badge;
import me.falu.twitchemotes.emote.Emote;
import me.falu.twitchemotes.emote.provider.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import webpdecoderjn.WebPDecoder;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class TwitchEmotes implements ClientModInitializer {
    public static final ModContainer MOD_CONTAINER = FabricLoader.getInstance().getModContainer("twitchemotes").orElseThrow(RuntimeException::new);
    public static final String MOD_NAME = MOD_CONTAINER.getMetadata().getName();
    public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);
    public static final String MOD_VERSION = String.valueOf(MOD_CONTAINER.getMetadata().getVersion());
    public static final float EMOTE_SIZE = 9.0F;
    public static final Queue<Emote.DrawData> SCHEDULED_DRAW = new ArrayDeque<>();
    private static final EmoteProvider[] EMOTE_PROVIDERS = new EmoteProvider[] {
            new BTTVEmoteProvider(),
            new FFZEmoteProvider(),
            new STVEmoteProvider(),
            new TwitchEmoteProvider()
    };
    private static final Map<String, Emote> EMOTE_MAP = new HashMap<>();
    private static final Map<String, Badge> BADGE_MAP = new HashMap<>();
    public static boolean CHAT_CONNECTED = false;
    private static Twirk TWIRK;

    public static void log(Object msg) {
        LOGGER.log(Level.INFO, msg);
    }

    public static void sendChatMessage(String message) {
        TWIRK.channelMessage(message);
    }

    private static boolean validStrings(String... strings) {
        for (String string : strings) {
            if (string == null || string.trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public static Emote getEmote(String name, Map<String, Emote> specific) {
        if (EMOTE_MAP.containsKey(name)) {
            return EMOTE_MAP.get(name);
        }
        return specific.get(name);
    }

    public static Badge getBadge(String name) {
        return BADGE_MAP.get(name);
    }

    public static Set<String> getEmoteKeys() {
        return EMOTE_MAP.keySet();
    }

    public static void invalidateEmote(Emote emote) {
        if (emote instanceof Badge) {
            BADGE_MAP.remove(emote.name);
        } else {
            EMOTE_MAP.remove(emote.name);
        }
    }

    public static JsonElement getJsonAuthResponse(String endpoint) {
        String auth = TwitchEmotesOptions.TWITCH_AUTH.getValue();
        String clientId = TwitchEmotesOptions.TWITCH_CLIENT_ID.getValue();
        if (validStrings(auth, clientId)) {
            try {
                URL url = new URL(endpoint);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.addRequestProperty("Authorization", "Bearer " + TwitchEmotesOptions.TWITCH_AUTH.getValue());
                connection.addRequestProperty("Client-Id", TwitchEmotesOptions.TWITCH_CLIENT_ID.getValue());
                connection.setUseCaches(false);
                InputStream inputStream = connection.getInputStream();
                String result = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
                return JsonParser.parseString(result);
            } catch (IOException e) {
                TwitchEmotes.LOGGER.error("Error while making HTTP request", e);
            }
        }
        return null;
    }

    private static String getTwitchId() {
        String user = TwitchEmotesOptions.TWITCH_NAME.getValue();
        String channel = TwitchEmotesOptions.TWITCH_CHANNEL_NAME.getValue();
        if (!channel.isEmpty() && !user.equals(channel)) {
            JsonElement response = getJsonAuthResponse("https://api.twitch.tv/helix/users?login=" + channel);
            if (response != null && !response.isJsonNull() && response.isJsonObject()) {
                JsonObject object = response.getAsJsonObject();
                if (object.has("data")) {
                    JsonArray results = object.get("data").getAsJsonArray();
                    if (!results.isEmpty()) {
                        JsonObject data = results.get(0).getAsJsonObject();
                        if (data.get("login").getAsString().equalsIgnoreCase(channel)) {
                            return data.get("id").getAsString();
                        }
                    }
                }
            }
        }
        return TwitchEmotesOptions.TWITCH_ID.getValue();
    }

    public static void reloadEmotes() {
        String id = getTwitchId();
        String auth = TwitchEmotesOptions.TWITCH_AUTH.getValue();
        String clientId = TwitchEmotesOptions.TWITCH_CLIENT_ID.getValue();

        EMOTE_MAP.clear();
        BADGE_MAP.clear();
        if (validStrings(id, auth, clientId)) {
            for (EmoteProvider provider : EMOTE_PROVIDERS) {
                List<Emote> emotes = provider.collectEmotes(id);
                for (Emote emote : emotes) {
                    EMOTE_MAP.put(emote.name, emote);
                }
                log("Finished loading " + emotes.size() + " emotes from " + provider.getProviderName() + ".");
            }
            BADGE_MAP.putAll(Badge.getBadges());
        } else {
            LOGGER.warn("No Twitch user ID provided. Skipping loading emotes.");
        }
    }

    public static void reloadChat() {
        String name = TwitchEmotesOptions.TWITCH_NAME.getValue();
        String auth = TwitchEmotesOptions.TWITCH_AUTH.getValue();
        String channel = TwitchEmotesOptions.TWITCH_CHANNEL_NAME.getValue().isEmpty() ? name : TwitchEmotesOptions.TWITCH_CHANNEL_NAME.getValue();

        CHAT_CONNECTED = false;
        if (TWIRK != null) {
            TWIRK.close();
            TWIRK = null;
        }
        if (validStrings(channel, name, auth)) {
            TWIRK = new TwirkBuilder(channel, name, "oauth:" + auth)
                    .setDebugLogMethod(s -> {
                        if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
                            log(s);
                        }
                    })
                    .build();
            TWIRK.addIrcListener(new TwitchListener());
            try {
                if (!TWIRK.connect()) {
                    LOGGER.error("Couldn't successfully connect to Twitch chat.");
                }
            } catch (IOException | InterruptedException e) {
                LOGGER.error("Error while connecting to Twitch chat", e);
            }
        } else {
            LOGGER.warn("Invalid Twitch credentials provided. Skipping connecting to chat.");
        }
    }

    public static void reload() {
        reloadEmotes();
        reloadChat();
    }

    @Override
    public void onInitializeClient() {
        log("Using " + MOD_NAME + " v" + MOD_VERSION);
        try {
            WebPDecoder.init();
            log("WebP decoder test: " + WebPDecoder.test());
        } catch (IOException e) {
            LOGGER.error("Couldn't initialize WebP decoder", e);
        }
        reload();
    }
}
