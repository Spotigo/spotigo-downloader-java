package com.bendevnull.SpotigoDownloader;

import com.joshuadoes.Spotigo.Album;
import com.joshuadoes.Spotigo.Disc;
import com.joshuadoes.Spotigo.SpotigoClient;
import com.joshuadoes.Spotigo.Track;

public class Program {

    public static void main(String[] args) {
        Config config = new Config("./config.json");
        // System.out.println(args);
        SpotigoClient client = new SpotigoClient(config.spotigoHost, config.spotigoPass);
        DownloadList list = new DownloadList("list.json");
        ThreadHandler th = new ThreadHandler();
        for (Object obj : list.albums) {
            String albumUrl = (String) obj;
            System.out.println("Getting info for " + albumUrl);
            Album album = client.getAlbumInfo(albumUrl);
            for (Disc disc : album.discs) {
                for (Track track : disc.tracks) {
                    th.register(new TrackDownloader(client, track, config, album));
                    if (!th.isAlive()) {
                        th.start();
                    }
                }
            }
        }
        for (Object obj : list.tracks) {
            String trackUrl = (String) obj;
            th.register(new TrackDownloader(client, trackUrl, config));
            if (!th.isAlive()) {
                th.start();
            }
        }
        System.out.println("Program - Finished adding tracks and albums");
    }
}