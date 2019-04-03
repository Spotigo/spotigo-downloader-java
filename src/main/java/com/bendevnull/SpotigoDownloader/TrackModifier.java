package com.bendevnull.SpotigoDownloader;

import java.io.File;
import java.net.URL;

import com.joshuadoes.Spotigo.Album;
import com.joshuadoes.Spotigo.SpotigoClient;
import com.joshuadoes.Spotigo.Track;
import com.mpatric.mp3agic.ID3v1Tag;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.ID3v24Tag;
import com.mpatric.mp3agic.Mp3File;

import org.apache.commons.io.IOUtils;

import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.builder.FFmpegBuilder;

public class TrackModifier extends Thread {

    SpotigoClient client;
    Track track;
    Config config;
    Album album;

    public TrackModifier(SpotigoClient client, Track track, Config config, Album album) {
        this.client = client;
        this.track = track;
        this.config = config;
        this.album = album;
    }

    public void run() {
        String title = cleanString(track.title);
        convertTrack("./temp_" + title + ".ogg", "./temp_" + title + ".mp3");
        System.out.println(track.title + " - Finished converting!");
        setMP3TrackInfo("./temp_" + title + ".mp3", track);
        System.out.println(track.title + " - Finished!");
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
            File albumFolder = new File(artistFolder.getAbsolutePath(), cleanString(track.artist));
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
            File newFile = new File(albumFolder.getAbsolutePath(), newTrackName);

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

    private String cleanString(String s) {
        return s.replace("?", "")
                .replace("\"", "")
                .replace("\'", "")
                .replace("/", ",")
                .trim();
    }
}