package fr.farmvivi.animecity;

public class Main {
    //TODO Spotify playing support https://github.com/sedmelluq/lavaplayer/issues/519
    //TODO Use spotify api to view current track image, artist and track name https://github.com/spotify-web-api-java/spotify-web-api-java
    //TODO Improve youtube search to not play clip audio
    //TODO Fix seek cmd in MusicController
    //TODO Add equalizer feature (bassboost, ect...) https://github.com/sedmelluq/lavaplayer/blob/master/testbot/src/main/java/com/sedmelluq/discord/lavaplayer/demo/music/MusicController.java
    public static void main(String[] args) {
        Bot.setInstance(new Bot());
    }
}
