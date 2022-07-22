package fr.farmvivi.discordbot;

public class Main {
    // TODO Add features
    // https://web.archive.org/web/20210911150725/https://rythm.fm/docs/features#player
    public static void main(String[] args) {
        long start = System.currentTimeMillis();

        new Bot();

        long finish = System.currentTimeMillis();

        Bot.logger.info("Done (" + (float) (finish - start) / 1000 + "s)!");
    }
}
