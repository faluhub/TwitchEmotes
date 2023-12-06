package me.falu.twitchemotes.emote.provider;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.falu.twitchemotes.emote.Emote;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public abstract class EmoteProvider {
    private JsonElement getJsonResponse(String endpoint) throws IOException {
        URL url = new URL(endpoint);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        InputStream inputStream = connection.getInputStream();
        String result = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        return new JsonParser().parse(result);
    }

    protected final JsonArray getArrayResponse(String endpoint) {
        try {
            JsonElement response = this.getJsonResponse(endpoint);
            if (response == null || response.isJsonNull() || !response.isJsonArray()) {
                return new JsonArray();
            }
            return response.getAsJsonArray();
        } catch (IOException ignored) {
            return new JsonArray();
        }
    }

    protected final JsonObject getObjectResponse(String endpoint) {
        try {
            JsonElement response = this.getJsonResponse(endpoint);
            if (response == null || response.isJsonNull() || !response.isJsonObject()) {
                return new JsonObject();
            }
            return response.getAsJsonObject();
        } catch (IOException ignored) {
            return new JsonObject();
        }
    }

    public abstract String getProviderName();

    public abstract JsonArray getGlobalEmotes();

    public abstract JsonArray getUserEmotes(String userId);

    public abstract Emote createEmote(JsonObject data);

    public final List<Emote> collectEmotes(String userId) {
        List<Emote> result = new ArrayList<>();
        JsonArray emotes = this.getGlobalEmotes();
        emotes.addAll(this.getUserEmotes(userId));
        for (JsonElement element : emotes) {
            result.add(this.createEmote(element.getAsJsonObject()));
        }
        return result;
    }
}
