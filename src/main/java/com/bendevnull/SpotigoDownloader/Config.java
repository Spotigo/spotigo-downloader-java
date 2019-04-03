package com.bendevnull.SpotigoDownloader;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Config {

    public String spotigoHost;
    public String spotigoPass;
    public String musicDir;

    public Config(String filename) {
        JSONObject json = this.loadFile(filename);
        
        this.parseConfig(json);
    }

    public Config(JSONObject json) {
        this.parseConfig(json);
    }

    private void parseConfig(JSONObject json) {
        this.spotigoHost = (String) json.get("SpotigoHost");
        this.spotigoPass = (String) json.get("SpotigoPass");
        this.musicDir = (String) json.get("DefaultMusicDir");
    }

    private JSONObject loadFile(String filename) {
        JSONParser parser = new JSONParser();
        Scanner scanner;
        File file;
        String str;
        JSONObject obj;
        try {
            file = new File(filename);
            scanner = new Scanner(file);
            scanner.useDelimiter("\\Z");
            str = scanner.next();
            scanner.close();
            obj = (JSONObject) parser.parse(str);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
			return null;
        } catch (ParseException e) {
            e.printStackTrace();
			return null;
        }
        return obj;
    }
}