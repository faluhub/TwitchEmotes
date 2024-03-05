package me.falu.twitchemotes.gui.screen;

import me.falu.twitchemotes.TwitchEmotes;
import me.falu.twitchemotes.TwitchEmotesOptions;
import me.falu.twitchemotes.emote.EmoteConstants;
import me.falu.twitchemotes.gui.widget.CosmeticTextFieldWidget;
import me.falu.twitchemotes.gui.widget.LimitlessButtonWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

import java.util.regex.Pattern;

public class CredentialsConfigScreen extends Screen {
    private static final Pattern PATTERN = Pattern.compile("(username=[^;]+;user_id=[^;]+;client_id=[^;]+;oauth_token=[^;]+;)");
    private ButtonWidget pasteButton;
    private ButtonWidget loginButton;
    private CosmeticTextFieldWidget hintField;

    public CredentialsConfigScreen() {
        super(Text.literal("Credentials Config"));
    }

    @Override
    protected void init() {
        int buttonWidth = this.width / 2;
        int buttonHeight = this.height / 3;
        int x = this.width / 2 - buttonWidth / 2;

        this.hintField = this.addDrawableChild(new CosmeticTextFieldWidget(
                this.textRenderer,
                x,
                buttonHeight / 4,
                buttonWidth,
                buttonHeight
        ));
        this.hintField.addTextAsLines("""
                                              - Clicking on the 'Login' button will open a website where you will be asked to log in.
                                              - After logging in, click the 'Copy' button on the website.
                                              - Then you can click the 'Paste Info' button in-game.
                                              - TwitchEmotes will use these credentials to listen to chat and query your emotes.""");

        this.loginButton = this.addDrawableChild(new LimitlessButtonWidget(
                x,
                this.hintField.getY() + this.hintField.getHeight() + 10,
                buttonWidth,
                buttonHeight,
                Text.literal("Login"),
                EmoteConstants.HMM,
                b -> {
                    Util.getOperatingSystem().open("https://chatterino.com/client_login");
                    b.visible = false;
                    b.active = false;
                    if (this.pasteButton != null) {
                        this.pasteButton.visible = true;
                        this.pasteButton.active = true;
                    }
                }
        ));
        this.pasteButton = this.addDrawableChild(new LimitlessButtonWidget(
                this.loginButton.getX(),
                this.loginButton.getY(),
                buttonWidth,
                buttonHeight,
                Text.literal("Paste Info"),
                EmoteConstants.OK,
                b -> {
                    b.active = false;
                    if (this.client != null) {
                        String clipboard = this.client.keyboard.getClipboard();
                        if (PATTERN.matcher(clipboard).matches()) {
                            for (String pair : clipboard.split(";")) {
                                String[] relation = pair.split("=");
                                if (relation.length != 2) {
                                    continue;
                                }
                                String key = relation[0];
                                String value = relation[1];
                                switch (key) {
                                    default:
                                        break;
                                    case "username":
                                        TwitchEmotesOptions.TWITCH_NAME.setValue(value);
                                        if (!TwitchEmotesOptions.TWITCH_CHANNEL_NAME.isDefault()) {
                                            TwitchEmotesOptions.TWITCH_CHANNEL_NAME.setValue(value);
                                        }
                                        break;
                                    case "user_id":
                                        TwitchEmotesOptions.TWITCH_ID.setValue(value);
                                        break;
                                    case "client_id":
                                        TwitchEmotesOptions.TWITCH_CLIENT_ID.setValue(value);
                                        break;
                                    case "oauth_token":
                                        TwitchEmotesOptions.TWITCH_AUTH.setValue(value);
                                        break;
                                }
                            }
                            TwitchEmotes.reload();
                        }
                    }
                    b.visible = false;
                    if (this.loginButton != null) {
                        this.loginButton.visible = true;
                        this.loginButton.active = true;
                    }
                }
        ));
        this.pasteButton.visible = false;
        this.pasteButton.active = false;

        this.addDrawableChild(new LimitlessButtonWidget(
                x,
                this.height - 20 - 10,
                buttonWidth,
                20,
                ScreenTexts.DONE,
                null,
                b -> this.close()
        ));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        if (this.hintField != null) {
            context.drawTextWithShadow(
                    this.textRenderer,
                    "HOW TO:",
                    this.hintField.getX(),
                    this.hintField.getY() - this.textRenderer.fontHeight - 4,
                    0xFFFFFF
            );
        }
    }

    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(new MenuSelectionScreen());
        }
    }
}
