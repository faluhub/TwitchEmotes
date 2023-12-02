package me.falu.twitchemotes.emote.provider;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.falu.twitchemotes.emote.Emote;

public class FFZEmoteProvider extends EmoteProvider {
    private static final String BASE_URL = "https://api.betterttv.net/3/cached/frankerfacez";

    @Override
    public String getProviderName() {
        return "FrankerFaceZ";
    }

    @Override
    public JsonArray getGlobalEmotes() {
        return this.getArrayResponse(BASE_URL + "/emotes/global");
    }

    @Override
    public JsonArray getUserGlobalEmotes(String userId) {
        return this.getArrayResponse(BASE_URL + "/users/twitch/" + userId);
    }

    @Override
    public Emote createEmote(JsonObject data) {
        JsonObject images = data.get("images").getAsJsonObject();
        int highest = -1;
        for (String key : images.keySet()) {
            int level = Integer.parseInt(key.replace("x", ""));
            if (highest == -1 || level > highest) {
                highest = level;
            }
        }
        return Emote
                .builder()
                .name(data.get("code").getAsString())
                .id(data.get("id").getAsString())
                .url(images.get(highest + "x").getAsString())
                .zeroWidth(false)
                .build();
    }
}
