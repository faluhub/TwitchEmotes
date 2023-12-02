package me.falu.twitchemotes.emote.provider;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.falu.twitchemotes.emote.Emote;

import java.util.Arrays;

public class BTTVEmoteProvider extends EmoteProvider {
    private static final String BASE_URL = "https://api.betterttv.net/3/cached";
    private static final String IMG_URL = "https://cdn.betterttv.net/emote/%s/3x";
    private static final String[] ZERO_WIDTH = new String[] {
            "5e76d338d6581c3724c0f0b2",
            "5e76d399d6581c3724c0f0b8",
            "567b5b520e984428652809b6",
            "5849c9a4f52be01a7ee5f79d",
            "567b5c080e984428652809ba",
            "567b5dc00e984428652809bd",
            "58487cc6f52be01a7ee5f205",
            "5849c9c8f52be01a7ee5f79e"
    };

    @Override
    public String getProviderName() {
        return "BetterTTV";
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
        String id;
        return Emote
                .builder()
                .name(data.get("code").getAsString())
                .id(id = data.get("id").getAsString())
                .url(IMG_URL.formatted(id))
                .zeroWidth(Arrays.asList(ZERO_WIDTH).contains(id))
                .build();
    }
}
