<a href='https://ko-fi.com/U7U1BYSR1' target='_blank'><img height='36' style='border:0px;height:36px;' src='https://storage.ko-fi.com/cdn/kofi1.png?v=3' border='0' alt='Buy Me a Coffee at ko-fi.com' /></a>
### [Consider supporting me on Ko-Fi!](https://ko-fi.com/quesia)

### [Join the Discord server for more mods & tech-support!](https://discord.gg/s9m8gf6pju)

# TwitchEmotes

A Fabric mod that brings the normal emojis, 7TV emotes, BTTV emotes and FFZ emotes to Minecraft!

If you want the mod to be ported to a specific version, open an issue or ask me in [my Discord server](https://discord.gg/s9m8gf6pju)!

## Features:

- Rendering emotes from 7TV, BTTV and FFZ in Minecraft chat.
- Rendering emojis in Minecraft chat.
- Replaces the player name suggestions when pressing tab in a message with your emote names.
- Messages from Twitch chat in Minecraft chat.
- A configurable limit to how long the preview of a command suggestion can be.

## How to set it up:

1. Create a [7TV](https://7tv.app/) account, and link it with your Twitch account.
   - This is needed for getting your Twitch user ID without having to do additional authentication, and for selecting emotes you want to use in Minecraft.
2. Launch and quit the game so that the configuration file gets created.
3. Head to `.minecraft/config/TwitchEmotes.json`.
4. In the `twitch_name` field type your Twitch username (preferably the **DISPLAY** name).
   - This is needed for getting all of your emotes.
5. Go to https://twitchapps.com/tmi, log in with Twitch and then copy the key that it gives you.
   - This is needed for listening to your Twitch chat.
6. In the `twitch_auth` field paste in the key you just copied.
7. You can change the rest of the config values. Everything is explained below.

## Config Values:

- `file`: This is a path to a file that houses the same config values listed below. This can be used for not having to copy the same across multiple instances of the game whenever you want to change the config file.

- `preview_character_limit`: This changes the amount of characters long a command suggestion (the things that pop up when you press tab in chat) can be.
- `chat_message_limit`: How many messages will be rendered in chat. It's crucial to keep this a low number as if it is too high there might be too many emotes stored in memory, which will make your computer bluescreen.
- `message_lifespan`: The amount of ticks it takes for a message to fade out. The vanilla value is `200 ticks`.
- `frames_per_second`: The frames per second of animated emojis. If the value is higher the animated emotes will go faster. This setting has nothing to do with performance.
- `twitch_name`: The display name of your Twitch account. Used to connect to your chat, to type in your chat and to get your Twitch ID.
- `twitch_auth`: The auth key to your Twitch chat. This is used to connect to your chat.
- `clear_chat_on_join`: If this is enabled, the chat will clear whenever you join a world.
- `enable_chat_back`: If this is enabled, whenever you type in Minecraft chat it will send a message to your Twitch chat. This does not include Minecraft commands.
- `send_alert_sounds`: If this is enabled, whenever someone follows/subs/etc... it will play a sound in game.
- `show_room_state_updates`: If this is enabled, you'll see a message in chat whenever follower/sub/emote only is enabled/disabled or when slowmode is changed.
- `show_follows`: If this is enabled, you'll see a message in chat whenever someone follows your Twitch channel.
- `show_subs`: If this is enabled, you'll see a message in chat whenever someone subs to your Twitch channel.
- `show_hypetrain`: If this is enabled, you'll see a message in chat whenever a hypetrain is started, when its level increments, and when it ends in your Twitch chat.
- `show_cheers`: If this is enabled, you'll see a message in chat whenever someone cheers bits in your Twitch chat.
- `show_raids`: If this is enabled, you'll see a message in chat whenever someone raids your Twitch channel.

## Credits:

- [Androkai/AndrosDiscordEmojis](https://github.com/Androkai/AndrosDiscordEmojis) for the emoji functionality.
- [Redstcne](https://twitch.tv/Redstcne) for suggesting the Twitch emote idea.
- [Gikkman/Java-Twirk](https://github.com/Gikkman/Java-Twirk) for the library that lets you connect to the Twitch chat.
- [giambaJ/jChat](https://github.com/giambaJ/jChat) for references for the emote platforms' APIs
- My awesome Twitch followers that helped me test this live!
