package com.example.osu_parse;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.*;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class Backend {

    private static Map<String, String> userList = new HashMap<>();
    private static Map<String, Beatmap> beatmapList = new LinkedHashMap<>();
    private static String basePath = new File("").getAbsolutePath();

    public static void main(String[] args) {
        try {
            addMappoolToMapList(basePath + "/mappool");
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            for (String matchLink : getMatchesFromFile(basePath + "/matches"))  {
                parseMatch(getMatchFromOsu(matchLink));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            writeToFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static JSONObject getMatchFromOsu(String matchLink) throws Exception {
        if (matchLink == null) {
            return null;
        }
        // Make a URL to the web page
        URL url = new URL(matchLink);
        // Get the input stream through URL Connection
        URLConnection con = url.openConnection();
        InputStream is = con.getInputStream();
        JSONParser parser = new JSONParser();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line = null;

        // read each line
        while ((line = br.readLine()) != null) {
            line = line.strip();
            // Find the json from the webpage
            if (line.length() > 100 && line.substring(2, 7).equals("match")) {
                return (JSONObject) parser.parse(line);
            }
        }
        return null;
    }

    public static List<String> getMatchesFromFile(String filePath) throws IOException {
        File file = new File(filePath);
        BufferedReader br = new BufferedReader(new FileReader(file));
        List<String> matchLinks = new ArrayList<>();
        String line = null;
        while ((line = br.readLine()) != null) {
            matchLinks.add(line);
        }
        return matchLinks;
    }

    public static void addMappoolToMapList(String filePath) throws IOException {
        File file = new File(filePath);
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line = null;
        while ((line = br.readLine()) != null) {
            String[] idToName = line.split(":");
            beatmapList.put(idToName[1], new Beatmap(idToName[0]));
        }
    }

    public static void parseMatch(JSONObject o) {
        //addUsersToMapFromJSON((JSONArray) o.get("users"));
        try {
            addUsersToMapFromFile(basePath + "/userlist");
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Get games from events:
        List<JSONObject> games = new ArrayList<>();
        List<JSONObject> events = (JSONArray) o.get("events");
        for (JSONObject event : events) {
            if (event.containsKey("game")) {
                games.add((JSONObject) event.get("game"));
            }
        }
        // Then parse the games:
        for (JSONObject game : games) {
            String beatMapId = ((JSONObject) game.get("beatmap")).get("id").toString();
            Beatmap beatmap = null;
            if (beatmapList.containsKey(beatMapId)) {
                beatmap = beatmapList.get(beatMapId);
            } else {
                beatmap = new Beatmap(beatMapId.toString());
                beatmapList.put(beatMapId, beatmap);
            }
            List<JSONObject> scores = (JSONArray) game.get("scores");
            for (JSONObject score : scores) {
                String scoring_type = game.get("scoring_type").toString();
                String scorePoints = score.get("score").toString();
                String mods = score.get("mods").toString();
                String accuracy = score.get("accuracy").toString().replace(".", ",");
                Score score_object = new Score(scoring_type, scorePoints, mods, accuracy);

                String userId = score.get("user_id").toString();
                String user = userList.get(userId);
                if (user != null) {
                    System.out.println(userId);
                    beatmap.put(user, score_object);
                }
            }
        }
    }

    // Puts all userId's and their names into a map, so you can later get the username from the userid.
    public static void addUsersToMapFromJSON(List<JSONObject> users) {
        for (JSONObject user : users) {
            String userId = user.get("id").toString();
            String userName = user.get("username").toString();
            if (!userList.containsKey(userId)) {
                userList.put(userId, userName);
            }
        }
    }

    public static void addUsersToMapFromFile(String filePath) throws IOException {
        File file = new File(filePath);
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line = null;
        while ((line = br.readLine()) != null) {
            String[] idToName = line.split(":");
            userList.put(idToName[1], idToName[0]);
        }
    }

    public static void writeToFile() throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet spreadsheet = workbook.createSheet("Users");
        XSSFRow row;
        List<String> mapIds = new ArrayList<>(beatmapList.keySet());
        List<String> userNames = new ArrayList<>(userList.keySet());

        int rowid = 0;
        row = spreadsheet.createRow(rowid++);
        row.createCell(0).setCellValue("Naam");
        int cellid = 1;
        for (String beatmapKey : mapIds) { // For every beatmap
            row.createCell(cellid).setCellValue(beatmapList.get(beatmapKey).getName());
            cellid += 5;
        }
        for (String key : userNames) { // For every user
            row = spreadsheet.createRow(rowid++);
            cellid = 0;
            row.createCell(cellid++).setCellValue(userList.get(key));
            for (String beatmapKey : mapIds) { // For every beatmap
                Score score = beatmapList.get(beatmapKey).get(userList.get(key));
                row.createCell(cellid++).setCellValue(score.getScorePoints());
                row.createCell(cellid++).setCellValue(score.getAccuracy());
                row.createCell(cellid++).setCellValue(score.getMods());
                row.createCell(cellid++).setCellValue(score.getScoring_type());
                row.createCell(cellid++);
            }
        }

        FileOutputStream out = new FileOutputStream(new File(basePath + "/output.xlsx"));
        workbook.write(out);
        out.close();
    }

}
