package com.bendevnull.SpotigoDownloader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import com.google.common.eventbus.EventBus;
import com.joshuadoes.Spotigo.Album;
import com.joshuadoes.Spotigo.SpotigoClient;
import com.joshuadoes.Spotigo.SpotigoGID;
import com.joshuadoes.Spotigo.Track;
import com.mpatric.mp3agic.ID3v1Tag;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.ID3v24Tag;
import com.mpatric.mp3agic.Mp3File;

import org.apache.commons.io.IOUtils;

import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.builder.FFmpegBuilder;

public class TrackDownloader extends Thread {

    private SpotigoClient client;
    private Config config;
    private Track track;
    private Album album;

    private EventBus eventBus = EventBusFactory.getEventBus();
    
    public File newFile;

    public TrackDownloader(SpotigoClient client, String trackURL, Config config, Album album) {
        this.client = client;
        this.track = this.client.getTrackInfo(trackURL);
        this.config = config;
        this.album = album;
    }

    public TrackDownloader(SpotigoClient client, Track track, Config config, Album album) {
        this.client = client;
        this.track = track;
        this.config = config;
        this.album = album;
    }

    public TrackDownloader(SpotigoClient client, String trackURL, Config config) {
        this.client = client;
        this.track = this.client.getTrackInfo(trackURL);
        this.config = config;
        this.album = this.client.getAlbumInfo(new SpotigoGID(this.track.albumGid));
    }
    
    public void run() {
        try {
            String title = cleanString(track.title);
            String tempOgg = "./temp_" + title + ".ogg";
            String tempMp3 = "./temp_" + title + ".mp3";

            System.out.println(track.title + " - Starting!");
            this.downloadFile(new URL(track.streamURL), new File(tempOgg));
            System.out.println(track.title + " - Finished downloading!");
            this.convertTrack(tempOgg, tempMp3);
            System.out.println(track.title + " - Finished converting!");
            this.setMP3TrackInfo(tempMp3, track);
            System.out.println(track.title + " - Finished!");

            new File(tempMp3).delete();
            new File(tempOgg).delete();

            DownloadFinishedEvent event = new DownloadFinishedEvent(this);
            eventBus.post(event);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void convertTrack(String infilename, String outfilename) {
        try {
            FFmpeg ffmpeg = new FFmpeg();
            FFmpegBuilder builder = new FFmpegBuilder()
                .setInput(infilename)
                .overrideOutputFiles(true)
                .addOutput(outfilename)
                .done();
            FFmpegExecutor executor = new FFmpegExecutor(ffmpeg);
            executor.createJob(builder).run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setMP3TrackInfo(String path, Track track) {
        try {
            Mp3File mp3 = new Mp3File(path);
            ID3v1Tag tag1 = new ID3v1Tag();
            ID3v2 tag2 = new ID3v24Tag();

            File artistFolder = new File(config.musicDir, cleanString(track.artist));
            File albumFolder = new File(artistFolder.getAbsolutePath(), cleanString(album.title));
            if (album.discs.size() > 1) {
                albumFolder = new File(albumFolder.getAbsolutePath(), "Disc " + track.discNumber);
            }
            if (!albumFolder.exists()) {
                albumFolder.mkdirs();
            }

            String newTrackName;
            if (track.number < 10) {
                newTrackName = "0" + track.number + " " + cleanString(track.title) + ".mp3";
            } else {
                newTrackName = track.number + " " + cleanString(track.title) + ".mp3";
            }
            newFile = new File(albumFolder.getAbsolutePath(), newTrackName);

            mp3.setId3v1Tag(tag1);
            mp3.setId3v2Tag(tag2);
            tag1.setTrack(Long.toString(track.number));
            tag2.setTrack(Long.toString(track.number));
            tag1.setArtist(track.artist);
            tag2.setArtist(track.artist);
            tag1.setTitle(track.title);
            tag2.setTitle(track.title);
            tag1.setAlbum(album.title);
            tag2.setAlbum(album.title);
            tag1.setYear(Long.toString(album.date.year));
            tag2.setYear(Long.toString(album.date.year));
            tag2.setAlbumImage(IOUtils.toByteArray(new URL(track.artURL).openStream()), "image/jpeg");
            mp3.save(newFile.getAbsolutePath());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void downloadFile(URL url, File filepath) {
        OutputStream os;
        InputStream is;
        try {
            os = new FileOutputStream(filepath);
            is = url.openStream();

            IOUtils.copy(is, os);

        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private String cleanString(String s) {
        return s.replace("?", "")
                .replace("\"", "")
                .replace("\'", "")
                .replace("/", ",")
                .trim();
    }

    public Track getTrack() {
        return this.track;
    }
}