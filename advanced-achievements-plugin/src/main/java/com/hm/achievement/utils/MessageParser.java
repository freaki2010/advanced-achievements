package com.hm.achievement.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class MessageParser {
    public static Component getParsedMessage(String message)
    {
        var mm = MiniMessage.miniMessage();

        return mm.deserialize(message);
    }
}
