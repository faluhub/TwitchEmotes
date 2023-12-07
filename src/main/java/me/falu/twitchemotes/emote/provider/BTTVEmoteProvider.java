package me.falu.twitchemotes.emote.provider;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.falu.twitchemotes.emote.Emote;

public class BTTVEmoteProvider extends EmoteProvider {
    private static final String BASE_URL = "https://api.betterttv.net/3/cached";
    private static final String IMG_URL = "https://cdn.betterttv.net/emote/%s/3x";

    @Override
    public String getProviderName() {
        return "BetterTTV";
    }

    @Override
    public JsonArray getGlobalEmotes() {
        return this.getArrayResponse(BASE_URL + "/emotes/global");
    }

    @Override
    public JsonArray getUserEmotes(String userId) {
        return this.getArrayResponse(BASE_URL + "/users/twitch/" + userId);
    }

    @Override
    public Emote createEmote(JsonObject data) {
        String id;
        return Emote
                .builder()
                .name(data.get("code").getAsString())
                .id(id = data.get("id").getAsString())
                .url(String.format(IMG_URL, id))
                .imageType(Emote.ImageType.fromSuffix(data.get("imageType").getAsString()))
                .build();
    }
}
