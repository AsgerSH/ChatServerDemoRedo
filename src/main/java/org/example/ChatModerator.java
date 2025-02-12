package org.example;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class ChatModerator {
    private static final List<String> BANNED_WORDS = Arrays.asList("fuck", "lort", "din mor", "luder");

    public static String filterMessage(String message) {
        for (String word : BANNED_WORDS) {
            if (message.contains(word)) {
                String change = "\\b" + Pattern.quote(word) + "\\b";
                message = message.replaceAll(change, "**");
            }
        } return message;
    }
}
