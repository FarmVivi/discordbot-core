package fr.farmvivi.animecity;

public class Main {
    //TODO Fix seek cmd in MusicController
    //TODO Add equalizer feature (bassboost, ect...) https://github.com/sedmelluq/lavaplayer/blob/master/testbot/src/main/java/com/sedmelluq/discord/lavaplayer/demo/music/MusicController.java
    //TODO Add features https://web.archive.org/web/20210911150725/https://rythm.fm/docs/features#player
    //TODO Commande viewqueue
    //TODO Commande help
    public static void main(String[] args) {
        Bot.setInstance(new Bot());
    }
}
