package me.falu.twitchemotes.mixin.chat;

import com.google.common.collect.Maps;
import me.falu.twitchemotes.TwitchEmotes;
import me.falu.twitchemotes.chat.TwitchMessageListOwner;
import me.falu.twitchemotes.emote.Emote;
import me.falu.twitchemotes.emote.EmoteStyleOwner;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.util.ChatMessages;
import net.minecraft.text.*;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.*;

@Mixin(ChatHud.class)
public abstract class ChatHudMixin implements TwitchMessageListOwner {
    @Unique private final Map<ChatHudLine, String> messageIds = new HashMap<>();
    @Unique private final Map<ChatHudLine, String> visibleMessageIds = new HashMap<>();
    @Shadow @Final private List<ChatHudLine> messages;
    @Shadow @Final private List<ChatHudLine> visibleMessages;
    @Shadow @Final private MinecraftClient client;
    @Shadow private int scrolledLines;
    @Shadow private boolean hasUnreadNewMessages;

    @Shadow
    public abstract int getWidth();

    @Shadow
    public abstract double getChatScale();

    @Shadow
    public abstract boolean isChatFocused();

    @Shadow
    public abstract void scroll(double amount);

    @Unique
    private MutableText transformText(Text prefix, String content, Map<String, Emote> specific) {
        return this.transformText(prefix, new LiteralText(content), specific);
    }

    @Unique
    private MutableText transformText(Text prefix, Text content, Map<String, Emote> specific) {
        MutableText message = prefix.shallowCopy();
        content.visit((style, string) -> {
            List<String> words = new ArrayList<>();
            StringBuilder split = new StringBuilder();
            for (int i = 0; i < string.length(); i++) {
                char character = string.charAt(i);
                split.append(character);
                if (character == ' ' || i == string.length() - 1) {
                    words.add(split.toString());
                    split = new StringBuilder();
                }
            }
            for (String word : words) {
                Emote emote = TwitchEmotes.getEmote(word.trim(), specific);
                if (emote != null) {
                    message.append(new LiteralText("_").styled(s -> ((EmoteStyleOwner) style).twitchemotes$withEmoteStyle(emote)));
                    String deleted = word.replace(word.trim(), "");
                    if (!deleted.isEmpty()) {
                        message.append(new LiteralText(deleted).setStyle(style));
                    }
                } else {
                    message.append(new LiteralText(word).setStyle(style));
                }
            }
            return Optional.empty();
        }, Style.EMPTY);
        return message;
    }

    @Override
    public void twitchemotes$clear() {
        List<ChatHudLine> lines = new ArrayList<>(this.messages);
        for (ChatHudLine line : lines) {
            if (this.messageIds.containsKey(line)) {
                this.messageIds.remove(line);
                this.messages.remove(line);
            }
        }
        List<ChatHudLine> visibleLines = new ArrayList<>(this.visibleMessages);
        for (ChatHudLine line : visibleLines) {
            if (this.visibleMessageIds.containsKey(line)) {
                this.visibleMessageIds.remove(line);
                this.visibleMessages.remove(line);
            }
        }
    }

    @Override
    public void twitchemotes$delete(String id) {
        Map<ChatHudLine, String> lines = new HashMap<>(this.messageIds);
        for (Map.Entry<ChatHudLine, String> entry : lines.entrySet()) {
            if (entry.getValue().equals(id)) {
                this.messageIds.remove(entry.getKey());
                this.messages.remove(entry.getKey());
            }
        }
        Map<ChatHudLine, String> visibleLines = new HashMap<>(this.visibleMessageIds);
        for (Map.Entry<ChatHudLine, String> entry : visibleLines.entrySet()) {
            if (entry.getValue().equals(id)) {
                this.visibleMessageIds.remove(entry.getKey());
                this.visibleMessages.remove(entry.getKey());
            }
        }
    }

    @Override
    public void twitchemotes$addMessage(Text prefix, String content, String id, Map<String, Emote> specific) {
        MutableText message = this.transformText(prefix, content, specific);
        int timestamp = this.client.inGameHud.getTicks();
        int i = MathHelper.floor((double) this.getWidth() / this.getChatScale());
        List<StringRenderable> list = ChatMessages.breakRenderedChatMessageLines(message, i, this.client.textRenderer);
        boolean bl2 = this.isChatFocused();
        for (StringRenderable stringRenderable2 : list) {
            if (bl2 && this.scrolledLines > 0) {
                this.hasUnreadNewMessages = true;
                this.scroll(1.0D);
            }
            ChatHudLine visibleLine = new ChatHudLine(timestamp, stringRenderable2, 0);
            this.visibleMessageIds.put(visibleLine, id);
            this.visibleMessages.add(0, visibleLine);
        }
        while (this.visibleMessages.size() > 100) {
            this.visibleMessageIds.remove(this.visibleMessages.remove(this.visibleMessages.size() - 1));
        }
        ChatHudLine line = new ChatHudLine(timestamp, message, 0);
        this.messageIds.put(line, id);
        this.messages.add(0, line);
        while (this.messages.size() > 100) {
            this.messageIds.remove(this.messages.remove(this.messages.size() - 1));
        }
    }

    @ModifyVariable(method = "addMessage(Lnet/minecraft/text/StringRenderable;IIZ)V", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private StringRenderable transformMessageText(StringRenderable text) {
        return this.transformText(new LiteralText(""), (Text) text, Maps.newHashMap());
    }
}
