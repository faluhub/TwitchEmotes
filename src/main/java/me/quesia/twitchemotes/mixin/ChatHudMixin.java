package me.quesia.twitchemotes.mixin;

import me.quesia.twitchemotes.Emote;
import me.quesia.twitchemotes.TwitchEmotes;
import me.quesia.twitchemotes.owner.TwitchMessageListOwner;
import me.quesia.twitchemotes.owner.TwitchMessageOwner;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.ChatMessages;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.StringRenderable;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Mixin(ChatHud.class)
public abstract class ChatHudMixin extends DrawableHelper implements TwitchMessageListOwner {
    @Shadow @Final private MinecraftClient client;
    @Shadow @Final private List<ChatHudLine> messages;
    @Shadow public abstract int getWidth();
    @Shadow public abstract double getChatScale();
    @Shadow public abstract boolean isChatFocused();
    @Shadow @Final private List<ChatHudLine> visibleMessages;
    @Shadow private int scrolledLines;
    @Shadow private boolean hasUnreadNewMessages;
    @Shadow public abstract void scroll(double amount);
    private final List<ChatHudLine> removeLines = new ArrayList<>();
    private final List<ChatHudLine> removeVisibleLines = new ArrayList<>();

    @Inject(method = "render", at = @At("HEAD"))
    private void removeQueuedLines(MatrixStack matrixStack, int i, CallbackInfo ci) {
        for (ChatHudLine line : this.removeLines) {
            this.messages.remove(line);
        }
        this.removeLines.clear();
        for (ChatHudLine line : this.removeVisibleLines) {
            this.visibleMessages.remove(line);
        }
        this.removeVisibleLines.clear();
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/font/TextRenderer;drawWithShadow(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/text/StringRenderable;FFI)I", ordinal = 0))
    private int renderEmotes(TextRenderer instance, MatrixStack matrices, StringRenderable text, float x, float y, int color) {
        String chat = this.renderChat(instance, matrices, text, y);
        boolean renderVanilla = chat.isEmpty();
        return instance.drawWithShadow(matrices, renderVanilla ? text : new LiteralText(chat), x, y, color);
    }

    private String getStyledMessage(StringRenderable text) {
        StringBuilder stringBuilder = new StringBuilder();
        text.visit(string -> {
            stringBuilder.append(string);
            return Optional.empty();
        });
        return stringBuilder.toString();
    }

    private String renderChat(TextRenderer instance, MatrixStack matrices, StringRenderable text, float y) {
        String[] words = this.getStyledMessage(text).split(" ");
        int ratio = 8;
        int space = instance.getWidth(" ");

        StringBuilder rendered = new StringBuilder();
        boolean renderVanilla = true;
        for (String word : words) {
            if (TwitchEmotes.EMOTE_MAP.containsKey(word) && !TwitchEmotes.FAILED_EMOTES.contains(word)) {
                Emote emote = TwitchEmotes.EMOTE_MAP.get(word);
                if (!emote.isCachingImages()) {
                    List<NativeImage> images = TwitchEmotes.getNativeImages(emote);
                    if (emote.hasImageCache()) {
                        long time = Util.getMeasuringTimeMs();
                        long delta = time - emote.getFirstFrameRender();
                        if (emote.getFirstFrameRender() == 0) { emote.setFirstFrameRender(time); }
                        else if (delta > 1000 / TwitchEmotes.FRAMES_PER_SECOND) {
                            emote.incrementCurrentFrame();
                            if (emote.getCurrentFrame() > images.size() - 1) {
                                emote.resetCurrentFrame();
                            }
                            emote.setFirstFrameRender(time);
                        }
                        NativeImage image = images.get(emote.getCurrentFrame());

                        int offset = instance.getWidth(rendered.toString());
                        int width = emote.getWidth();

                        NativeImageBackedTexture texture = new NativeImageBackedTexture(image);
                        Identifier id = new Identifier(TwitchEmotes.MOD_NAME.toLowerCase(Locale.ROOT), emote.getId() + emote.getCurrentFrame());
                        if (this.client.getTextureManager().getTexture(id) == null) {
                            this.client.getTextureManager().registerTexture(id, texture);
                        }
                        this.client.getTextureManager().bindTexture(id);
                        drawTexture(matrices, offset, (int) y, 0.0F, 0.0F, width, ratio, width, ratio);
                        renderVanilla = false;

                        int amount = width / space;
                        rendered.append(" ".repeat(Math.max(0, amount + 1)));
                    }
                } else { renderVanilla = false; }
            } else { rendered.append(word).append(" "); }
        }

        return renderVanilla ? "" : rendered.append(" ").toString();
    }

    @Override
    public void onMessageDelete(String messageId) {
        for (ChatHudLine line : this.messages) {
            TwitchMessageOwner message = (TwitchMessageOwner) line;
            if (message.getMessageId().equals(messageId)) {
                this.removeLines.add(line);
            }
        }
        for (ChatHudLine line : this.visibleMessages) {
            TwitchMessageOwner message = (TwitchMessageOwner) line;
            if (message.getMessageId().equals(messageId)) {
                this.removeVisibleLines.add(line);
            }
        }
    }

    @Override
    public void onMessagesClear() {
        for (ChatHudLine line : this.messages) {
            TwitchMessageOwner message = (TwitchMessageOwner) line;
            if (message.getMessageId() != null) {
                this.removeLines.add(line);
            }
        }
        for (ChatHudLine line : this.visibleMessages) {
            TwitchMessageOwner message = (TwitchMessageOwner) line;
            if (message.getMessageId() != null) {
                this.removeVisibleLines.add(line);
            }
        }
    }

    @Override
    public void addMessage(String message, String messageId) {
        StringRenderable stringRenderable = new LiteralText(message);
        int timestamp = this.client.inGameHud.getTicks();
        int i = MathHelper.floor((double) this.getWidth() / this.getChatScale());
        List<StringRenderable> list = ChatMessages.breakRenderedChatMessageLines(stringRenderable, i, this.client.textRenderer);
        boolean bl2 = this.isChatFocused();
        for (StringRenderable stringRenderable2 : list) {
            if (bl2 && this.scrolledLines > 0) {
                this.hasUnreadNewMessages = true;
                this.scroll(1.0);
            }
            ChatHudLine line = new ChatHudLine(timestamp, stringRenderable2, 0);
            ((TwitchMessageOwner) line).setMessageId(messageId);
            this.visibleMessages.add(0, line);
        }
        if (this.visibleMessages.size() > 100) {
            this.visibleMessages.remove(this.visibleMessages.size() - 1);
        }
        ChatHudLine line = new ChatHudLine(timestamp, stringRenderable, 0);
        ((TwitchMessageOwner) line).setMessageId(messageId);
        this.messages.add(0, line);
        if (this.messages.size() > 100) {
            this.messages.remove(this.messages.size() - 1);
        }
    }
}
