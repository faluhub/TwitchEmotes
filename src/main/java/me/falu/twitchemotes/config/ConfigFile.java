package me.falu.twitchemotes.config;

import com.google.gson.*;
import me.falu.twitchemotes.TwitchEmotes;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

@SuppressWarnings("BlockingMethodInNonBlockingContext")
public class ConfigFile {
    private static File FILE = FabricLoader.getInstance().getConfigDir().resolve(TwitchEmotes.MOD_NAME + "_v3.json").toFile();

    public static void init() {
        if (!FILE.exists()) {
            try { boolean ignored = FILE.createNewFile(); }
            catch (IOException e) { throw new RuntimeException(e); }
        }
        JsonObject config = get(false);
        if (config.has("file")) {
            File tempFile = new File(config.get("file").getAsString());
            if (tempFile.exists()) {
                FILE = tempFile;
                init();
            }
        }
    }

    protected static JsonObject get() {
        return get(true);
    }

    protected static JsonObject get(boolean initialise) {
        if (initialise) { init(); }
        try {
            FileReader reader = new FileReader(FILE);
            Object obj = JsonParser.parseReader(reader);
            reader.close();

            return JsonNull.INSTANCE.equals(obj) ? new JsonObject() : (JsonObject) obj;
        } catch (IOException ignored) {}
        return new JsonObject();
    }

    protected static void write(JsonObject config) {
        try {
            FileWriter writer = new FileWriter(FILE);

            writer.write(new GsonBuilder().setPrettyPrinting().create().toJson(config));
            writer.flush();
            writer.close();
        } catch (IOException ignored) {}
    }
}
