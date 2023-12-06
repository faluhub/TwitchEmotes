package me.falu.twitchemotes.emote.provider;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.falu.twitchemotes.emote.Emote;

import java.util.Map;

public class FFZEmoteProvider extends EmoteProvider {
    private static final String BASE_URL = "https://api.frankerfacez.com/v1";

    private JsonArray getSetEmotes(long setId) {
        JsonObject ffzSetData = this.getObjectResponse(BASE_URL + "/_set/" + setId);
        if (ffzSetData.has("set")) {
            JsonObject setData = ffzSetData.get("set").getAsJsonObject();
            if (setData.has("emoticons")) {
                return setData.get("emoticons").getAsJsonArray();
            }
        }
        return new JsonArray();
    }

    @Override
    public String getProviderName() {
        return "FrankerFaceZ";
    }

    @Override
    public JsonArray getGlobalEmotes() {
        JsonArray result = new JsonArray();
        JsonArray defaultSets = this.getObjectResponse(BASE_URL + "/_set/global").get("default_sets").getAsJsonArray();
        for (JsonElement element : defaultSets) {
            result.addAll(this.getSetEmotes(element.getAsLong()));
        }
        return result;
    }

    @Override
    public JsonArray getUserEmotes(String userId) {
        JsonArray result = new JsonArray();
        JsonObject ffzData = this.getObjectResponse(BASE_URL + "/_user/id/" + userId);
        if (ffzData.has("user")) {
            JsonObject userData = ffzData.get("user").getAsJsonObject();
            if (userData.has("emote_sets")) {
                JsonArray sets = userData.get("emote_sets").getAsJsonArray();
                for (JsonElement element : sets) {
                    result.addAll(this.getSetEmotes(element.getAsLong()));
                }
            }
        }
        return result;
    }

    @Override
    public Emote createEmote(JsonObject data) {
        Emote.ImageType type = data.has("animated") ? Emote.ImageType.WEBP : Emote.ImageType.STATIC;
        JsonObject images = type.equals(Emote.ImageType.WEBP) ? data.get("animated").getAsJsonObject() : data.get("urls").getAsJsonObject();
        int highest = -1;
        for (Map.Entry<String, JsonElement> entry : images.entrySet()) {
            String key = entry.getKey();
            int level = Integer.parseInt(key);
            if (highest == -1 || level > highest) {
                highest = level;
            }
        }
        return Emote
                .builder()
                .name(data.get("name").getAsString())
                .id(data.get("id").getAsString())
                .url(images.get(String.valueOf(highest)).getAsString() + (type.equals(Emote.ImageType.WEBP) ? ".webp" : ""))
                .imageType(type)
                .build();
    }
}
