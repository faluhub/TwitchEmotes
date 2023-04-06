package me.quesia.twitchemotes.owner;

public interface TwitchMessageListOwner {
    void onMessageDelete(String messageId);
    void onMessagesClear();
    void addMessage(String message, String messageId);
}
