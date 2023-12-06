package me.falu.twitchemotes.emote.provider;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import me.falu.twitchemotes.emote.Emote;

public class STVEmoteProvider extends EmoteProvider {
    private static final String BASE_URL = "https://7tv.io/v3";

    @Override
    public String getProviderName() {
        return "7TV";
    }

    @Override
    public JsonArray getGlobalEmotes() {
        return this.getArrayResponse(BASE_URL + "/emote-sets/global");
    }

    @Override
    public JsonArray getUserEmotes(String userId) {
        JsonObject user = this.getObjectResponse(BASE_URL + "/users/twitch/" + userId);
        if (user.has("emote_set")) {
            JsonObject emoteSet = user.get("emote_set").getAsJsonObject();
            return emoteSet.get("emotes").getAsJsonArray();
        }
        return new JsonArray();
    }

    @Override
    public Emote createEmote(JsonObject data) {
        JsonObject emoteData = data.get("data").getAsJsonObject();
        JsonObject host = emoteData.get("host").getAsJsonObject();
        JsonArray files = host.get("files").getAsJsonArray();
        int highest = -1;
        for (JsonElement element : files) {
            JsonObject file = element.getAsJsonObject();
            if (file.get("format").getAsString().equals("WEBP")) {
                int size = Integer.parseInt(file.get("name").getAsString().replace("x.webp", ""));
                if (highest == -1 || size > highest) {
                    highest = size;
                }
            }
        }
        return Emote
                .builder()
                .name(data.get("name").getAsString())
                .id(data.get("id").getAsString())
                .url("https:" + host.get("url").getAsString() + "/" + highest + "x.webp")
                .imageType(Emote.ImageType.WEBP)
                .build();
    }
}
