package fr.farmvivi.discordbot.module.general.poll;

import net.dv8tion.jda.api.entities.emoji.Emoji;

public enum PollEmoji {
    UNKNOWN(0, Emoji.fromUnicode("❓")),
    ONE(1, Emoji.fromUnicode("1️⃣")),
    TWO(2, Emoji.fromUnicode("2️⃣")),
    THREE(3, Emoji.fromUnicode("3️⃣")),
    FOUR(4, Emoji.fromUnicode("4️⃣")),
    FIVE(5, Emoji.fromUnicode("5️⃣")),
    SIX(6, Emoji.fromUnicode("6️⃣")),
    SEVEN(7, Emoji.fromUnicode("7️⃣")),
    EIGHT(8, Emoji.fromUnicode("8️⃣")),
    NINE(9, Emoji.fromUnicode("9️⃣")),
    TEN(10, Emoji.fromUnicode("🔟"));

    private final int number;
    private final Emoji emoji;

    PollEmoji(int number, Emoji emoji) {
        this.number = number;
        this.emoji = emoji;
    }

    public static PollEmoji getEmoji(int number) {
        for (PollEmoji pollEmoji : values()) {
            if (pollEmoji.getNumber() == number) {
                return pollEmoji;
            }
        }
        return UNKNOWN;
    }

    public static PollEmoji getEmoji(Emoji emoji) {
        for (PollEmoji pollEmoji : values()) {
            if (pollEmoji.getEmoji().equals(emoji)) {
                return pollEmoji;
            }
        }
        return UNKNOWN;
    }

    public int getNumber() {
        return number;
    }

    public Emoji getEmoji() {
        return emoji;
    }
}
