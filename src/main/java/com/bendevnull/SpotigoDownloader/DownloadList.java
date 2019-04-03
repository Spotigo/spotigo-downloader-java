package com.bendevnull.SpotigoDownloader;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class DownloadList {

    public JSONArray tracks;
    public JSONArray albums;

    public DownloadList(String filename) {
        JSONObject json = this.loadFile(filename);
        
        this.parseList(json);
    }

    private void parseList(JSONObject json) {
        this.tracks = (JSONArray) json.get("tracks");
        this.albums = (JSONArray) json.get("albums");
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