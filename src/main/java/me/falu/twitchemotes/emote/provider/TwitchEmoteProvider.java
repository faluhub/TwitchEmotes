package me.falu.twitchemotes.emote.provider;

import com.google.gson.*;
import lombok.RequiredArgsConstructor;
import me.falu.twitchemotes.TwitchEmotes;
import me.falu.twitchemotes.config.ConfigValue;
import me.falu.twitchemotes.emote.Emote;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@SuppressWarnings("BlockingMethodInNonBlockingContext")
@RequiredArgsConstructor
public class TwitchEmoteProvider extends EmoteProvider {
    private static final String BASE_URL = "https://api.twitch.tv/helix/chat/emotes";
    // id, format, theme, scale
    private static final String IMAGE_URL_TEMPLATE = "https://static-cdn.jtvnw.net/emoticons/v2/%s/%s/%s/%s";
    private final ConfigValue<String> auth;
    private final ConfigValue<String> clientId;

    private JsonElement getJsonAuthResponse(String endpoint) {
        try {
            URL url = new URL(endpoint);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.addRequestProperty("Authorization", "Bearer " + this.auth.getValue());
            connection.addRequestProperty("Client-Id", this.clientId.getValue());
            connection.setUseCaches(false);
            InputStream inputStream = connection.getInputStream();
            String result = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
            return JsonParser.parseString(result);
        } catch (IOException e) {
            TwitchEmotes.LOGGER.error("Error while making HTTP request", e);
        }
        return null;
    }

    @Override
    public String getProviderName() {
        return "Twitch";
    }

    @Override
    public JsonArray getGlobalEmotes() {
        JsonElement response = this.getJsonAuthResponse(BASE_URL + "/global");
        if (response == null || response.isJsonNull() || !response.isJsonObject()) {
            return new JsonArray();
        }
        return response.getAsJsonObject().get("data").getAsJsonArray();
    }

    @Override
    public JsonArray getUserEmotes(String userId) {
        JsonElement response = this.getJsonAuthResponse(BASE_URL + "?broadcaster_id=" + userId);
        if (response == null || response.isJsonNull() || !response.isJsonObject()) {
            return new JsonArray();
        }
        return response.getAsJsonObject().get("data").getAsJsonArray();
    }

    @Override
    public Emote createEmote(JsonObject data) {
        String id = data.get("id").getAsString();
        Emote.ImageType type = data.get("format").getAsJsonArray().contains(new JsonPrimitive("animated")) ? Emote.ImageType.GIF : Emote.ImageType.STATIC;
        float highest = -1.0F;
        for (JsonElement element : data.get("scale").getAsJsonArray()) {
            float size = Float.parseFloat(element.getAsString());
            if (highest == -1.0F || size > highest) {
                highest = size;
            }
        }
        return Emote
                .builder()
                .name(data.get("name").getAsString())
                .id(id)
                .url(IMAGE_URL_TEMPLATE.formatted(id, type.name().toLowerCase(), "dark", highest))
                .imageType(type)
                .build();
    }
}
