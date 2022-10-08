package com.example.osu_parse;

import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

public class MatchParser {

    private static Map<String, String> userMap = null;
    private static Map<String, Beatmap> beatmapMap = null;

    /**
     * Takes userinput and writes to an excel file
     * @param matchLinks A string containing matchLinks on each line
     * @param users A string containing usernames and userIds on each line
     * @param mapPool A string containing identifiers and mapIds on each line
     * @param outputPath The directory where the output sheet should go
     * @param settings Boolean array of the following settings in this order: [showScore, showAcc, showMods, showNormalisedScore, showScoringType]
     * @throws Exception There's a lot that can go wrong
     */
    public static String parseMatch(String matchLinks,
                                  String users,
                                  String mapPool,
                                  String outputPath,
                                  Boolean[] settings) throws Exception {
        String[] matchLinkList = matchLinks.split("\\r?\\n");
        userMap = stringToMap(users);
        beatmapMap = stringBeatmapToMap(mapPool);
        boolean isWhiteList = !userMap.isEmpty();
        for (String matchLink : matchLinkList) {
            List<JSONObject> games = getGamesFromMatch(getMatchFromOsu(matchLink), isWhiteList);
            for (JSONObject game : games) {
                parseGame(game);
            }
        }
        writeToFile(outputPath);
        return "You can find the sheet at: " + outputPath + "/output.xlsx";
    }

    /**
     * Takes a String and adds all of its lines to a map.
     * @param inputString A string where each line consists of string1:string2
     * @return A map that maps string1 to string2
     * @throws IOException
     */
    public static Map<String, String> stringToMap(String inputString) throws IOException {
        Map<String, String> outputMap = new LinkedHashMap<>();
        String[] lines = inputString.split("\\r?\\n");
        for (String line : lines) {
            String[] idToName = line.split(":");
            if (idToName.length > 1) {
                outputMap.put(idToName[1], idToName[0]);
            }
        }
        return outputMap;
    }

    /**
     * Takes a String and add all of its lines to a map
     * @param inputString A string where each line consists of string1:string2
     * @return A map that maps string1 to a Beatmap with name string2.
     * @throws IOException
     */
    public static Map<String, Beatmap> stringBeatmapToMap(String inputString) throws IOException {
        Map<String, Beatmap> outputMap = new LinkedHashMap<>();
        String[] lines = inputString.split("\\r?\\n");
        for (String line : lines) {
            String[] idToName = line.split(":");
            if (idToName.length > 1) {
                outputMap.put(idToName[1], new Beatmap(idToName[0]));
            }
        }
        return outputMap;
    }

    /**
     * Takes a weblink, searches the match-JSON on the webpage, and returns it as JSONObject.
     * @param matchLink a link to a webpage (an osu match)
     * @return a JSONObject containing all match-data
     * @throws Exception
     */
    public static JSONObject getMatchFromOsu(String matchLink) throws Exception {
        if (matchLink == null) {
            return null;
        }
        URL url = new URL(matchLink);
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

    /**
     * In Osu, one 'match' has multiple 'games', where every game is a round where all players play one beatmap
     * This method also adds all users to the userMap if there is no whitelist.
     * @param o The JSONObject of the match
     * @return A list of games as JSONObjects
     */
    public static List<JSONObject> getGamesFromMatch(JSONObject o, boolean isWhitelist) {
        if (!isWhitelist) {
            addUsersToMapFromJSON((JSONArray) o.get("users"));
        }
        // Get games from events:
        List<JSONObject> games = new ArrayList<>();
        List<JSONObject> events = (JSONArray) o.get("events");
        for (JSONObject event : events) {
            if (event.containsKey("game")) {
                games.add((JSONObject) event.get("game"));
            }
        }
        return games;
    }

    /**
     * Adds all users that played in a match to a map
     * @param users The JSONObject from a match containing all users
     */
    public static void addUsersToMapFromJSON(List<JSONObject> users) {
        for (JSONObject user : users) {
            String userId = user.get("id").toString();
            String userName = user.get("username").toString();
            if (!userMap.containsKey(userId)) {
                userMap.put(userId, userName);
            }
        }
    }

    /**
     * Takes a 'game' and parses its JSON, it then puts the info into the Maps.
     * @param game The JSONObject of the game
     */
    public static void parseGame(JSONObject game) {
        String beatMapId = ((JSONObject) game.get("beatmap")).get("id").toString();
        Beatmap beatmap = null;
        if (beatmapMap.containsKey(beatMapId)) {
            beatmap = beatmapMap.get(beatMapId);
        } else {
            beatmap = new Beatmap(beatMapId.toString());
            beatmapMap.put(beatMapId, beatmap);
        }
        List<JSONObject> scores = (JSONArray) game.get("scores");
        for (JSONObject score : scores) {
            String scoring_type = game.get("scoring_type").toString();
            String scorePoints = score.get("score").toString();
            String mods = score.get("mods").toString();
            String accuracy = score.get("accuracy").toString().replace(".", ",");
            Score score_object = new Score(scoring_type, scorePoints, mods, accuracy);

            String userId = score.get("user_id").toString();
            String user = userMap.get(userId);
            if (user != null) {
                beatmap.put(user, score_object);
            }
        }
    }

    public static void writeToFile(String path) throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet spreadsheet = workbook.createSheet("Users");
        XSSFRow row;
        List<String> mapIds = new ArrayList<>(beatmapMap.keySet());
        List<String> userNames = new ArrayList<>(userMap.keySet());

        int rowid = 0;
        row = spreadsheet.createRow(rowid++);
        row.createCell(0).setCellValue("Naam");
        int cellid = 1;
        for (String beatmapKey : mapIds) { // For every beatmap
            row.createCell(cellid).setCellValue(beatmapMap.get(beatmapKey).getName());
            cellid += 5;
        }
        for (String key : userNames) { // For every user
            row = spreadsheet.createRow(rowid++);
            cellid = 0;
            row.createCell(cellid++).setCellValue(userMap.get(key));
            for (String beatmapKey : mapIds) { // For every beatmap
                Score score = beatmapMap.get(beatmapKey).get(userMap.get(key));
                row.createCell(cellid++).setCellValue(score.getScorePoints());
                row.createCell(cellid++).setCellValue(score.getAccuracy());
                row.createCell(cellid++).setCellValue(score.getMods());
                row.createCell(cellid++).setCellValue(score.getScoring_type());
                row.createCell(cellid++);
            }
        }

        FileOutputStream out = new FileOutputStream(new File(path + "/output.xlsx"));
        workbook.write(out);
        out.close();
    }

}
