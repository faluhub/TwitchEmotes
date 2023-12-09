<a href='https://ko-fi.com/U7U1BYSR1' target='_blank'><img height='36' style='border:0px;height:36px;' src='https://storage.ko-fi.com/cdn/kofi1.png?v=3' border='0' alt='Buy Me a Coffee at ko-fi.com' /></a>
### [Consider supporting me on Ko-Fi!](https://ko-fi.com/quesia)

### [Join the Discord server for more mods & tech-support!](https://discord.gg/s9m8gf6pju)

# TwitchEmotes

A Fabric mod that brings the normal emojis, 7TV emotes, BTTV emotes and FFZ emotes to Minecraft!

If you want the mod to be ported to a specific version, open an issue or ask me in [my Discord server](https://discord.gg/s9m8gf6pju)!

## Features:

- Rendering emotes from 7TV, BTTV and FFZ in Minecraft chat.
- Messages from Twitch chat in Minecraft chat.

## How to set it up:

1. Launch and quit the game so that the configuration file gets created.
2. Head to `.minecraft/config/TwitchEmotes_v3.json`.
3. Go to [this website](https://chatterino.com/client_login) and log in. **Don't show this on stream!**
4. Press the Copy button and use that data to fill out the config file.
    - Don't include the `;`'s at the end of each property.
    - You can ignore the `twitch_channel` property, unless you want to listen to someone else's chat.

## Other Config Values:

- `twitch_channel`: The name of the Twitch channel you want to join. This property is optional and will default to the `twitch_name` property.
- `show_user_colors`: Whether it shows the color that a chatter uses in the Twitch chat.
- `show_badges`: Whether it shows the chatter's Twitch badges.

## Credits:

- [Androkai/AndrosDiscordEmojis](https://github.com/Androkai/AndrosDiscordEmojis) for the emoji functionality.
- [Redstcne](https://twitch.tv/Redstcne) for suggesting the Twitch emote idea.
- [Gikkman/Java-Twirk](https://github.com/Gikkman/Java-Twirk) for the library that lets you connect to the Twitch chat.
- [giambaJ/jChat](https://github.com/giambaJ/jChat) for references for the emote platforms' APIs
- My awesome [Twitch](https://twitch.tv/faluhub) followers that helped me test this live!
