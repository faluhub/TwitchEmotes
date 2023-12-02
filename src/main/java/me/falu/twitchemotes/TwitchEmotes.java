package me.falu.twitchemotes;

import com.gikk.twirk.Twirk;
import com.gikk.twirk.TwirkBuilder;
import me.falu.twitchemotes.chat.TwitchListener;
import me.falu.twitchemotes.config.ConfigValue;
import me.falu.twitchemotes.emote.Emote;
import me.falu.twitchemotes.emote.provider.BTTVEmoteProvider;
import me.falu.twitchemotes.emote.provider.EmoteProvider;
import me.falu.twitchemotes.emote.provider.FFZEmoteProvider;
import me.falu.twitchemotes.emote.provider.STVEmoteProvider;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TwitchEmotes implements ClientModInitializer {
    public static final ModContainer MOD_CONTAINER = FabricLoader.getInstance().getModContainer("twitchemotes").orElseThrow(RuntimeException::new);
    public static final String MOD_NAME = MOD_CONTAINER.getMetadata().getName();
    public static final String MOD_VERSION = String.valueOf(MOD_CONTAINER.getMetadata().getVersion());
    public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);

    public static final ConfigValue<String> TWITCH_NAME = new ConfigValue<>("twitch_name", "");
    public static final ConfigValue<String> TWITCH_ID = new ConfigValue<>("twitch_id", "");
    public static final ConfigValue<String> TWITCH_AUTH = new ConfigValue<>("twitch_auth", "");

    private static Twirk TWIRK;
    private static final EmoteProvider[] EMOTE_PROVIDERS = new EmoteProvider[] {
            new BTTVEmoteProvider(),
            new FFZEmoteProvider(),
            new STVEmoteProvider()
    };
    private static final Map<String, Emote> EMOTE_MAP = new HashMap<>();

    public static void log(Object msg) {
        LOGGER.log(Level.INFO, msg);
    }

    public static void sendChatMessage(String message) {
        TWIRK.channelMessage(message);
    }

    private static boolean validStrings(String ...strings) {
        for (String string : strings) {
            if (string == null || string.trim().equals("")) {
                return false;
            }
        }
        return true;
    }

    @Nullable
    public static Emote getEmote(String name) {
        return EMOTE_MAP.get(name);
    }

    public static void invalidateEmote(Emote emote) {
        EMOTE_MAP.remove(emote.name);
    }

    public static void reload() {
        String name = TWITCH_NAME.getValue();
        String id = TWITCH_ID.getValue();
        String auth = TWITCH_AUTH.getValue();
        if (!auth.startsWith("oauth:")) {
            auth = "oauth:" + auth;
        }

        if (validStrings(id)) {
            EMOTE_MAP.clear();
            for (EmoteProvider provider : EMOTE_PROVIDERS) {
                List<Emote> emotes = provider.collectEmotes(id);
                for (Emote emote : emotes) {
                    EMOTE_MAP.put(emote.name, emote);
                }
                log("Finished loading " + emotes.size() + " emotes from " + provider.getProviderName());
            }
        } else {
            LOGGER.warn("No Twitch user ID provided. Skipping loading emotes.");
        }

        if (validStrings(name, auth)) {
            if (TWIRK != null) {
                TWIRK.close();
                TWIRK = null;
            }
            TWIRK = new TwirkBuilder(name, name, auth)
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

    @Override
    public void onInitializeClient() {
        log("Using " + MOD_NAME + " v" + MOD_VERSION);
        reload();
    }
}
