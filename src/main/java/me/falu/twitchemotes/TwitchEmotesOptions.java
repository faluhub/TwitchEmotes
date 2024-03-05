package me.falu.twitchemotes;

import me.falu.twitchemotes.config.ConfigValue;

public class TwitchEmotesOptions {
    public static final ConfigValue<String> TWITCH_NAME = new ConfigValue<>("twitch_name", "");
    public static final ConfigValue<String> TWITCH_CHANNEL_NAME = new ConfigValue<>("twitch_channel", "");
    public static final ConfigValue<String> TWITCH_ID = new ConfigValue<>("twitch_id", "");
    public static final ConfigValue<String> TWITCH_CLIENT_ID = new ConfigValue<>("twitch_client_id", "");
    public static final ConfigValue<String> TWITCH_AUTH = new ConfigValue<>("twitch_auth", "");
    public static final ConfigValue<Boolean> SHOW_USER_COLORS = new ConfigValue<>("show_user_colors", true);
    public static final ConfigValue<Boolean> SHOW_BADGES = new ConfigValue<>("show_badges", true);
    public static final ConfigValue<Boolean> SHOW_PP_HOP_OVERLAY = new ConfigValue<>("show_pp_hop_overlay", true);
}
