package fr.farmvivi.animecity.radio;

public class Radio {
    private final long guildID;
    private final long channelID;
    private final String playlistURL;

    public Radio(long guildID, long channelID, String playlistURL) {
        this.guildID = guildID;
        this.channelID = channelID;
        this.playlistURL = playlistURL;
    }

    public long getGuildID() {
        return guildID;
    }

    public long getChannelID() {
        return channelID;
    }

    public String getPlaylistURL() {
        return playlistURL;
    }
}
