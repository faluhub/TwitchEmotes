package me.falu.twitchemotes.mixin.chat;

import com.google.common.collect.Maps;
import me.falu.twitchemotes.TwitchEmotes;
import me.falu.twitchemotes.chat.TwitchMessageListOwner;
import me.falu.twitchemotes.emote.Emote;
import me.falu.twitchemotes.emote.EmoteStyleOwner;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.client.util.ChatMessages;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.*;

@Mixin(ChatHud.class)
public abstract class ChatHudMixin implements TwitchMessageListOwner {
    @Shadow @Final private List<ChatHudLine> messages;
    @Shadow @Final private List<ChatHudLine.Visible> visibleMessages;
    @Shadow @Final private MinecraftClient client;
    @Shadow private int scrolledLines;
    @Shadow private boolean hasUnreadNewMessages;
    @Unique private final Map<ChatHudLine, String> messageIds = new HashMap<>();
    @Unique private final Map<ChatHudLine.Visible, String> visibleMessageIds = new HashMap<>();

    @Shadow
    @SuppressWarnings("SameParameterValue")
    protected abstract void logChatMessage(Text message, @Nullable MessageIndicator indicator);
    @Shadow
    public abstract int getWidth();
    @Shadow
    public abstract double getChatScale();
    @Shadow
    protected abstract boolean isChatFocused();
    @Shadow
    public abstract void scroll(int scroll);

    @Unique
    private MutableText transformText(Text prefix, String content, Map<String, Emote> specific) {
        return this.transformText(prefix, Text.of(content), specific);
    }

    @Unique
    private MutableText transformText(Text prefix, Text content, Map<String, Emote> specific) {
        MutableText message = prefix.copy();
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
                    message.append(Text.literal("_").styled(s -> ((EmoteStyleOwner) style).twitchemotes$withEmoteStyle(emote)));
                    String deleted = word.replace(word.trim(), "");
                    if (!deleted.isEmpty()) {
                        message.append(Text.literal(deleted).setStyle(style));
                    }
                } else {
                    message.append(Text.literal(word).setStyle(style));
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
        List<ChatHudLine.Visible> visibleLines = new ArrayList<>(this.visibleMessages);
        for (ChatHudLine.Visible line : visibleLines) {
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
        Map<ChatHudLine.Visible, String> visibleLines = new HashMap<>(this.visibleMessageIds);
        for (Map.Entry<ChatHudLine.Visible, String> entry : visibleLines.entrySet()) {
            if (entry.getValue().equals(id)) {
                this.visibleMessageIds.remove(entry.getKey());
                this.visibleMessages.remove(entry.getKey());
            }
        }
    }

    @Override
    public void twitchemotes$addMessage(Text prefix, String content, String id, Map<String, Emote> specific) {
        MutableText message = this.transformText(prefix, content, specific);
        this.logChatMessage(Text.of(prefix + content), null);
        int i = MathHelper.floor((double) this.getWidth() / this.getChatScale());
        List<OrderedText> list = ChatMessages.breakRenderedChatMessageLines(message, i, this.client.textRenderer);
        boolean bl = this.isChatFocused();
        for (int j = 0; j < list.size(); ++j) {
            OrderedText orderedText = list.get(j);
            if (bl && this.scrolledLines > 0) {
                this.hasUnreadNewMessages = true;
                this.scroll(1);
            }
            boolean bl2 = j == list.size() - 1;
            ChatHudLine.Visible visibleLine = new ChatHudLine.Visible(this.client.inGameHud.getTicks(), orderedText, null, bl2);
            this.visibleMessages.add(0, visibleLine);
            this.visibleMessageIds.put(visibleLine, id);
        }
        while (this.visibleMessages.size() > 100) {
            this.visibleMessageIds.remove(this.visibleMessages.remove(this.visibleMessages.size() - 1));
        }
        ChatHudLine line = new ChatHudLine(this.client.inGameHud.getTicks(), message, null, null);
        this.messages.add(0, line);
        this.messageIds.put(line, id);
        while (this.messages.size() > 100) {
            this.messageIds.remove(this.messages.remove(this.messages.size() - 1));
        }
    }

    @ModifyVariable(method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;ILnet/minecraft/client/gui/hud/MessageIndicator;Z)V", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private Text transformMessageText(Text text) {
        return this.transformText(Text.literal(""), text, Maps.newHashMap());
    }
}
