package me.falu.twitchemotes.emote;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.ToString;
import me.falu.twitchemotes.TwitchEmotes;

import java.util.HashMap;
import java.util.Map;

@ToString
public class Badge extends Emote {
    private static final String API_URL = "https://api.twitch.tv/helix/chat/badges/global";
    private static final String DEFAULT_CLICK_URL = "https://help.twitch.tv/customer/en/portal/articles/659115-twitch-chat-badges-guide";
    public final String description;
    public final String clickUrl;

    public Badge(String name, String id, String url, String description, String clickUrl) {
        super(name, id, url, ImageType.STATIC);
        this.description = description;
        this.clickUrl = clickUrl;
    }

    public static Map<String, Badge> getBadges() {
        Map<String, Badge> result = new HashMap<>();
        JsonElement response = TwitchEmotes.getJsonAuthResponse(API_URL);
        if (response == null || response.isJsonNull() || !response.isJsonObject()) {
            return result;
        }
        if (response.getAsJsonObject().has("data")) {
            JsonArray badges = response.getAsJsonObject().get("data").getAsJsonArray();
            for (JsonElement element : badges) {
                JsonObject badge = element.getAsJsonObject();
                String name = badge.get("set_id").getAsString();
                JsonArray versions = badge.get("versions").getAsJsonArray();
                if (versions.size() > 0) {
                    JsonObject version = versions.get(0).getAsJsonObject();
                    String clickUrl = version.get("click_url").isJsonNull() ? DEFAULT_CLICK_URL : version.get("click_url").getAsString();
                    result.put(name, new Badge(
                            name,
                            version.get("id").getAsString(),
                            version.get("image_url_4x").getAsString(),
                            version.get("description").getAsString(),
                            clickUrl
                    ));
                }
            }
        }
        TwitchEmotes.log("Finished loading " + result.size() + " badges.");
        return result;
    }
}
