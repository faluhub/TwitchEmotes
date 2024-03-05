package me.falu.twitchemotes.gui.widget;

import me.falu.twitchemotes.config.ConfigValue;
import me.falu.twitchemotes.emote.Emote;
import net.minecraft.text.MutableText;

public class LimitlessBooleanButtonWidget extends LimitlessButtonWidget {
    public LimitlessBooleanButtonWidget(int x, int y, int width, int height, MutableText key, Emote emote, ConfigValue<Boolean> option) {
        super(x, y, width, height, key.copy().append(": " + getBooleanString(option.getValue())), emote, b -> {
            boolean value = option.getValue();
            ((LimitlessButtonWidget) b).setText(key.copy().append(": " + getBooleanString(!value)));
            option.setValue(!value);
        });
    }

    private static String getBooleanString(boolean value) {
        return value ? "Enabled" : "Disabled";
    }
}
