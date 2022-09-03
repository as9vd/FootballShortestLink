import Entity.Footballer;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.json.simple.JSONObject;

import java.io.*;
import java.util.*;

// Pages to scrape:
// 1. European Cup goalscorers (check for pages that don't exist). Seems to be valid from 55-56 to 90-91.
// 2. https://en.wikipedia.org/wiki/List_of_footballers_with_500_or_more_goals
// 3. https://en.wikipedia.org/wiki/List_of_men%27s_footballers_with_50_or_more_international_goals
// 4. Champions League goalscorers (goals + assists to be fair). Seems to be valid from 92-today.
// 5. Premier League pages; has hat-tricks, goalscorers, etc. 2007 was last time it was FA (so 92-07 works); 2007-today works under new link also.
// 6. La Liga pages: get Pichichi trophy rankers, top scorers, etc. Might be a bit of a nightmare keeping track of all the tables, to be fair.
public class Scraper {
    public static void main(String[] args) throws Exception {
        BufferedReader reader;
        ArrayList<String> names = new ArrayList<>();
        reader = new BufferedReader(new FileReader("data files/decentStrings.txt"));
        String line = reader.readLine();
        while (line != null) {
            names.add(line.split(",")[0]);
            line = reader.readLine();
        }
        reader.close();

        HashMap<String, String> newMap = convertJsonToMap(new File("data files/betterMerged.json"));

        FileWriter fileWriter = new FileWriter("footballerDatabase.json");
        ArrayList<Footballer> footballerList = new ArrayList<>();

        int i = 0;
        for (String name: names) {
            String link = newMap.get(name);

            if (!(link == null)) {
                TreeMap<String,List<String>> career = scrapePlayerCareers(link);
                List<String> list = scrapeWikiNames(link);
                String playerName = list.get(0), birthday = list.get(1), country = list.get(2);

                Gson innerGson = new Gson();
                Footballer footballer = new Footballer(playerName, birthday, country);
                footballer.teams = career;

                footballerList.add(footballer);
                System.out.println(i);
                i++;
            }

        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        fileWriter.write(gson.toJson(footballerList));

        fileWriter.close();
    }

    public static TreeMap scrapePlayerCareers(String link) throws Exception {
        // table.infobox.vcard: what needs to be scraped. Seems to be the consistent across these decent players.
        // Every single bit of info is in the <tbody> element. Even on a page like Romario's.
        Document doc = Jsoup.connect(link).userAgent("Chrome").get();
        Elements playerTable = doc.select("table.infobox.vcard");
        Elements rows = playerTable.select("th.infobox-label");

        TreeMap<String, List<String>> return_val = new TreeMap<>();
        for (Element row: rows) {
            String potentialDuration = row.text();

            // 1. "-1992" (e.g. in case of Nwankwo Kanu) turns into 0000-1992.
            if (potentialDuration.matches("^[0-9]{4}$") || potentialDuration.isEmpty()) {
                String year = potentialDuration.split("\\[")[0];

                if (!(year.isEmpty())) {
                    String club = row.parent().select("td").eq(0).text().split("\\[")[0];
                    return_val.putIfAbsent(year, new ArrayList<>());
                    return_val.get(year).add(club.split("\\[")[0]);
                }
            } else if (row.text().contains("–")) {
                String[] years = potentialDuration.split("–");
                if (years.length == 2) {
                    String firstYear = years[0];
                    String secondYear = years[1];
                    String club = row.parent().select("td").eq(0).text().split("\\[")[0];

                    return_val.putIfAbsent(potentialDuration, new ArrayList<>());
                    return_val.get(potentialDuration).add(club.split("\\[")[0]);
                } else if (years.length == 1) {
                    String club = row.parent().select("td").eq(0).text().split("\\[")[0];

                    return_val.putIfAbsent(potentialDuration, new ArrayList<>());
                    return_val.get(potentialDuration).add(club.split("\\[")[0]);
                }
            }
        }

        return return_val;
    }

    public static List<String> scrapeWikiNames(String link) throws Exception {
        Document doc = Jsoup.connect(link).userAgent("Chrome").get();
        Elements playerTable = doc.select("table.infobox.vcard").select("tbody");
        Elements rows = playerTable.select("tr");

        String birthday = "", name = doc.select("h1.firstHeading").text().split(" \\(")[0], country = "";
        for (Element row: rows) {
            if (row.text().toLowerCase(Locale.ROOT).contains("date")) birthday = row.select("td").text().split(" ")[0];
            else if (row.text().toLowerCase(Locale.ROOT).contains("place")) country = row.select("td").text().split("\\[")[0];
        }

        String complete = name + ", born " + birthday + " in " + country;
        ArrayList<String> return_val = new ArrayList<>();
        return_val.add(name); return_val.add(birthday); return_val.add(country);

        if (!(birthday.contains("(")) ||
                (country.isEmpty())) return null;
        else return return_val;
    }

    public static HashMap<String, String> convertJsonToMap(File json) {
        ObjectMapper mapper = new ObjectMapper();
        HashMap<String, String> map = new HashMap<String, String>();
        try {
            map = mapper.readValue(json, new TypeReference<HashMap<String, String>>(){});
        } catch (IOException e) {
            map.clear();
        }
        return map;
    }

    public static void mergeJSONObjects(JSONObject jo1, JSONObject jo2) throws Exception {
        JSONObject ec = scrapeEuropeanCupWinners();
        JSONObject bdor = scrapeBallonDorPages();

        JSONObject merged = new JSONObject();
        JSONObject[] objs = new JSONObject[] {ec, bdor};
//        for (JSONObject obj : objs) {
//            Iterator it = obj.keys();
//            while (it.hasNext()) {
//                String key = (String)it.next();
//                merged.put(key, obj.get(key));
//            }
//        }

        FileWriter fileWriter = new FileWriter("merged.json");

        // Writting the jsonObject into merged.json
        fileWriter.write(merged.toString());
        fileWriter.close();
    }

    public static JSONObject scrapeEuropeanCupScorers() {
        Map<String, String> footballNames = new HashMap(); // footballer : link

        try {
            String baseLink = "https://en.wikipedia.org/wiki/";

            for (int i = 55; i <= 90; i++) {
                String link = baseLink + "19" + i + "-" + (i + 1) + "_European_Cup";
                Document doc = Jsoup.connect(link).userAgent("Chrome").get();
                String playerBaseLink = "https://en.wikipedia.org";

                Elements homeGoals = doc.select("div.footballbox").select("td.fhgoal").select("a");
                Elements awayGoals = doc.select("div.footballbox").select("td.fagoal").select("a");

                for (Element goal: homeGoals) {
                    String playerName = goal.attr("title");

                    if (playerName.contains("does not") ||
                            playerName.contains("Penalty kick") ||
                            playerName.contains("goal")) { // "does not exist", "penalty kick", "own goal"
                        continue;
                    } else {
                        playerName = playerName.split(" \\(")[0]; // Just in case of "xxxx xxxxx (footballer, born xxxx)".
                        String wikiLink = goal.attr("href");
                        footballNames.put(playerName, playerBaseLink + wikiLink);
                    }
                }
            }

            JSONObject object = new JSONObject(footballNames);

            FileWriter fileWriter = new FileWriter("scorers.json");

            // Writting the jsonObject into merged.json
            fileWriter.write(object.toString());
            fileWriter.close();

            return object;
        } catch(Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static JSONObject scrapeEuropeanCupWinners() {
        Map<String, String> footballNames = new HashMap(); // footballer : link
        try {
            String baseLink = "https://en.wikipedia.org/w/index.php?title=Category:UEFA_Champions_League_winning_players&from=";
            String playerBaseLink = "https://en.wikipedia.org";
            char[] alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

            for (Character letter: alphabet) {
                String currentLetter = Character.toString(letter);
                String link = baseLink + currentLetter;

                Document doc = Jsoup.connect(link).userAgent("Chrome").get();

                int i = 0;
                // https://i.gyazo.com/ebd564122d0e4bcee3fd52d2c70e3a9b.png: how to access the list elements.
                for (Element player: doc.select("div.mw-category-group").select("ul").select("li")) {
                    String playerName = player.text().split(" \\(")[0]; // dealing with "(footballer, born xxxx)" and the likes.
                    String playerLink = player.select("a").attr("href");

                    footballNames.put(playerName, playerBaseLink + playerLink);
                }

                JSONObject object = new JSONObject(footballNames);

                return object;
                //                ObjectMapper mapper = new ObjectMapper();
//                mapper.writeValue(new File("europeanCupWinners.json"), footballNames);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // ballonDorPages had an issue with grabbing nationality links.
    // Problem: past 2015 (e.g. starting 2016), it starts grabbing links to nations instead of the player's page.
    // This is because this is when they started using <a> to highlight a player's nationality, and the nationality was in another column in previous renditions.
    public static JSONObject scrapeBallonDorPages() {
        Map<String, String> footballNames = new HashMap(); // footballer : link
        try {
            String baseLink = "https://en.wikipedia.org/wiki/";

            // Links valid from 1956 - 2009 (2010 is messed up).
            // Sponsored by FIFA from 2010 - 2015.
            // Was cancelled in 2020.
            for (int i = 1956; i < 2022; i++) {
                if ((i >= 2010) && (i <= 2015)) {
                    baseLink += i + "_FIFA_Ballon_d%27Or";
                } else if (i == 2020) {
                    continue;
                } else {
                    baseLink += i + "_Ballon_d%27Or";
                }

                Document doc = Jsoup.connect(baseLink).userAgent("Chrome").get();

                for (Element element: doc.select("table.wikitable").select("tbody")) {
                    for (Element tr: element.select("tr")) {
                        // 1. Take care of "[notes 1]".
                        // 2. Women footballers start showing up 2010, managers as well (latter not a problem if they had a career).
                        // 3. 2010 is when FIFPro World XI comes up, and the first "td" there is positions (e.g. GK, etc.). So need a new test case for that.
                        // 4. 2021 has 'Best Club of the Year' table, which happens to be Chelsea (LOL, a club that bought it all).
                        Elements row = tr.select("tr");
                        Elements data = row.select("td");

                        // How this works:
                        // Basically, in these tables, player name + links can either be in the first td cell or the second.
                        // The ones in the first are for the newer Ballon D'or pages and for rankings with multiple players, and the ones in the second are usually in the older Ballon D'or pages.
                        // - First Cell: https://i.gyazo.com/6f5535b7f75402f8de7c71b0235c1273.png
                        // - Second Cell: https://i.gyazo.com/62ebc14e2416e54c82a7f232bf8b29a3.png
                        // We need to add the Wikipedia base link, because Wikipedia's href attributes are universally shortened links.
                        if (!(data.eq(0).text().isEmpty()) &&
                                ((int) data.eq(0).text().charAt(0) > 57) &&
                                (data.eq(0).text().length() > 2) // If this element isn't a empty, a position, or a ranking/number.
                        ) {
                            Elements zerothElement = data.eq(0);
                            if (!(zerothElement.select("a").text() == null)) { // If this player has a link attached to his name, we add it to the map.
                                if (i >= 2016) {
                                    // https://i.gyazo.com/86e3f24b920c43b94134dad020af9f29.png: dealing with this test case.
                                    if (zerothElement.select("a").size() == 1) continue;
                                    else {
                                        footballNames.put(zerothElement.text().split("\\[")[0], "https://en.wikipedia.org" + zerothElement.select("a").eq(1).attr("href"));
                                    }
                                } else {
                                    footballNames.put(zerothElement.text().split("\\[")[0], "https://en.wikipedia.org" + zerothElement.select("a").attr("href"));
                                }
                            }
                        } else { // If not, look at the second td element.
                            Elements firstElement = data.eq(1);
                            if (!(firstElement.select("a").text() == null)) {
                                if (i >= 2016) {
                                    if (firstElement.select("a").size() == 1) continue;
                                    else {
                                        footballNames.put(firstElement.text().split("\\[")[0], "https://en.wikipedia.org" + firstElement.select("a").eq(1).attr("href"));
                                    }
                                } else {
                                    footballNames.put(firstElement.text().split("\\[")[0], "https://en.wikipedia.org" + firstElement.select("a").attr("href"));
                                }
                            }
                        }
                    }
                }
                baseLink = "https://en.wikipedia.org/wiki/";
            }

            footballNames.remove(" "); footballNames.remove("");

            //                ObjectMapper mapper = new ObjectMapper();
            //                mapper.writeValue(new File("ballonDorVotees.json"), footballNames);

            JSONObject object = new JSONObject(footballNames);
            return object;
        } catch (Exception e) {
            System.out.println(e.fillInStackTrace());
        }
        return null;
    }
}
