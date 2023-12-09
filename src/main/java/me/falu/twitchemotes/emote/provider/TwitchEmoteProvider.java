package me.falu.twitchemotes.emote.provider;

import com.google.gson.*;
import me.falu.twitchemotes.TwitchEmotes;
import me.falu.twitchemotes.emote.Emote;

@SuppressWarnings("BlockingMethodInNonBlockingContext")
public class TwitchEmoteProvider extends EmoteProvider {
    private static final String BASE_URL = "https://api.twitch.tv/helix/chat/emotes";
    // id, format, theme, scale
    private static final String IMAGE_URL_TEMPLATE = "https://static-cdn.jtvnw.net/emoticons/v2/%s/%s/%s/%s";

    @Override
    public String getProviderName() {
        return "Twitch";
    }

    @Override
    public JsonArray getGlobalEmotes() {
        JsonElement response = TwitchEmotes.getJsonAuthResponse(BASE_URL + "/global");
        if (response == null || response.isJsonNull() || !response.isJsonObject()) {
            return new JsonArray();
        }
        return response.getAsJsonObject().get("data").getAsJsonArray();
    }

    @Override
    public JsonArray getUserEmotes(String userId) {
        JsonElement response = TwitchEmotes.getJsonAuthResponse(BASE_URL + "?broadcaster_id=" + userId);
        if (response == null || response.isJsonNull() || !response.isJsonObject()) {
            return new JsonArray();
        }
        return response.getAsJsonObject().get("data").getAsJsonArray();
    }

    @Override
    public Emote createEmote(JsonObject data) {
        String id = data.get("id").getAsString();
        boolean isAnimated = data.get("format").getAsJsonArray().contains(new JsonPrimitive("animated"));
        Emote.ImageType type = isAnimated ? Emote.ImageType.GIF : Emote.ImageType.STATIC;
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
                .url(String.format(IMAGE_URL_TEMPLATE, id, isAnimated ? "animated" : "static", "dark", highest))
                .imageType(type)
                .build();
    }
}
