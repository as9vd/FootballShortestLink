import org.json.JSONException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.json.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

// Pages to scrape:
// 1. European Cup goalscorers (check for pages that don't exist). Seems to be valid from 55-56 to 90-91.
// 2. https://en.wikipedia.org/wiki/List_of_footballers_with_500_or_more_goals
// 3. https://en.wikipedia.org/wiki/List_of_men%27s_footballers_with_50_or_more_international_goals
// 4. Champions League goalscorers (goals + assists to be fair). Seems to be valid from 92-today.
// 5. Premier League pages; has hat-tricks, goalscorers, etc. 2007 was last time it was FA (so 92-07 works); 2007-today works under new link also.
// 6. La Liga pages: get Pichichi trophy rankers, top scorers, etc. Might be a bit of a nightmare keeping track of all the tables, to be fair.

public class Scraper {
    public static void main(String[] args) throws Exception {
        mergeJSONObjects(scrapeEuropeanCupWinners(), scrapeBallonDorPages());
    }

    public static void mergeJSONObjects(JSONObject jo1, JSONObject jo2) throws Exception {
        JSONObject ec = scrapeEuropeanCupWinners();
        JSONObject bdor = scrapeBallonDorPages();

        JSONObject merged = new JSONObject();
        JSONObject[] objs = new JSONObject[] {ec, bdor};
        for (JSONObject obj : objs) {
            Iterator it = obj.keys();
            while (it.hasNext()) {
                String key = (String)it.next();
                merged.put(key, obj.get(key));
            }
        }

        FileWriter fileWriter = new FileWriter("merged.json");

        // Writting the jsonObject into merged.json
        fileWriter.write(merged.toString());
        fileWriter.close();
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
                            if (!(data.eq(0).select("a").text() == null)) { // If this player has a link attached to his name, we add it to the map.
                                footballNames.put(zerothElement.text().split("\\[")[0], "https://en.wikipedia.org" + zerothElement.select("a").attr("href"));
                            }
                        } else { // If not, look at the second td element.
                            Elements firstElement = data.eq(1);
                            if (!(data.eq(1).select("a").text() == null)) {
                                footballNames.put(firstElement.text().split("\\[")[0], "https://en.wikipedia.org" + firstElement.select("a").attr("href"));
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
