package me.falu.twitchemotes.emote;

import lombok.Builder;
import lombok.ToString;

@Builder
@ToString
public class Emote {
    public final String name;
    public final String id;
    public final String url;
    public final boolean zeroWidth;
}
