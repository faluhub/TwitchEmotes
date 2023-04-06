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
   - You can skip this, and the next step if you don't want the Twitch chat feature.
6. In the `twitch_auth` field paste in the key you just copied.
7. You can change the `frames_per_second` field to configure the FPS of the animated emotes.
8. You can change the `preview_character_limit` field to change the amount of characters the command suggestion has to be for it to get cut off.

## Credits:

- [Androkai/AndrosDiscordEmojis](https://github.com/Androkai/AndrosDiscordEmojis) for the emoji functionality.
- [Redstcne](https://twitch.tv/Redstcne) for suggesting the Twitch emote idea.
- [Gikkman/Java-Twirk](https://github.com/Gikkman/Java-Twirk) for the library that lets you connect to the Twitch chat.
- [giambaJ/jChat](https://github.com/giambaJ/jChat) for references for the emote platforms' APIs
- My awesome Twitch followers that helped me test this live!
