package com.bendevnull.SpotigoDownloader;

public class DownloadFinishedEvent {

    private TrackDownloader downloader;

    public DownloadFinishedEvent(TrackDownloader d) {
        this.downloader = d;
    }

    public TrackDownloader getDownloader() {
        return this.downloader;
    }
}