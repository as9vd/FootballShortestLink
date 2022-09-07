package Scrapers;

import Entity.Footballer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.json.simple.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.Normalizer;
import java.util.*;

public class Scraper {
    // Last class was good and functional, but this one is better because it'll cover all the ground:
    // https://en.wikipedia.org/wiki/Category:Association_football_defenders
    // https://en.wikipedia.org/wiki/Category:Association_football_forwards
    // https://en.wikipedia.org/wiki/Category:Association_football_goalkeepers
    // https://en.wikipedia.org/wiki/Category:Association_football_midfielders
    // These are all the links necessary to get a proper database in.
    public static void main(String[] args) throws Exception {
        BufferedReader reader;
        ArrayList<String> links = new ArrayList<>();
        reader = new BufferedReader(new FileReader("AllDefenders.txt"));
        String line = reader.readLine();
        while (line != null) {
            links.add(line.split(": ")[1]);
            line = reader.readLine();
        }

        Gson gson = new Gson();
        ArrayList<Footballer> list = new ArrayList<>();
        for (String link: links) {
            Footballer footballer = scrapePlayerCareers(link);
            if (!(footballer == null)) {
                System.out.println(footballer);
                list.add(footballer);
            }
        }

        FileWriter fileWriter = new FileWriter("AllDefenders.json");
        Gson gsonBuilder = new GsonBuilder().setPrettyPrinting().create();
        fileWriter.write(gson.toJson(list));
        fileWriter.close();
    }

    public static void scrapePages(String link) throws Exception {
        Document doc = Jsoup.connect(link).userAgent("Chrome").get();
        Elements footer = doc.select("div#mw-pages");
        String baseLink = "https://en.wikipedia.org";

        for (Element button: footer) {
            String buttonText = button.text();
            if (buttonText.toLowerCase(Locale.ROOT).contains("next")) {
                for (Element a: button.select("a")) {
                    if (a.text().toLowerCase(Locale.ROOT).contains("next")) {
                        String nextPage = a.attr("href");
                        System.out.println(baseLink + nextPage);
                        scrapePages(baseLink + nextPage);
                    }
                }
            }
        }

    }

    public static Footballer scrapePlayerCareers(String link) throws Exception {
        // table.infobox.vcard: what needs to be scraped. Seems to be the consistent across these decent players.
        // Every single bit of info is in the <tbody> element. Even on a page like Romario's.
        Document doc = Jsoup.connect(link).userAgent("Chrome").get();
        Elements playerTable = doc.select("table.infobox.vcard");
        Elements rows = playerTable.select("th.infobox-label");
        String wikiBaseLink = "https://en.wikipedia.org";

        String birthday = "", name = doc.select("h1.firstHeading").text().split(" \\(")[0], country = "";
        for (Element row: rows) {
            if (row.text().toLowerCase(Locale.ROOT).contains("date")) {
                birthday = row.parent().select("td").text().split(" ")[0];
            } else if (row.text().toLowerCase(Locale.ROOT).contains("place")) {
                country = row.parent().select("td").text().split("\\[")[0];
            }
        }
        if (!(birthday.contains("(")) ||
                (country.isEmpty())) return null;

        TreeMap<String, List<String>> return_val = new TreeMap<>();
        for (Element row: rows) {
            String potentialDuration = row.text();

            // 1. "-1992" (e.g. in case of Nwankwo Kanu) turns into 0000-1992.
            if (potentialDuration.matches("^[0-9]{4}$") || potentialDuration.isEmpty()) {
                String year = potentialDuration.split("\\[")[0];

                if (!(year.isEmpty())) {
                    String club = Normalizer.normalize(row.parent().select("td").eq(0).text().split("\\[")[0], Normalizer.Form.NFD)
                            .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "")
                            .replace("→", "") // in the case of loan
                            .replace("(loan)", "") // self-explanatory
                            .split("\\(")[0]
                            .trim() // remove leading and trailing zeroes
                            .replaceAll("\\[. *\\]", "") // remove anything inside brackets ..
                            .replaceAll("\\(. *\\)", ""); // .. and parentheses;
                    String clubLink = row.parent().select("td").eq(0).select("a").attr("href").split("\\[")[0];

                    return_val.putIfAbsent(year, new ArrayList<>());
                    return_val.get(year).add(club.split("\\[")[0]);

                    if (clubLink.isEmpty() || clubLink.toLowerCase(Locale.ROOT).contains("redlink") || (return_val.containsKey("clubLinks") && return_val.get("clubLinks").contains(wikiBaseLink + clubLink))) continue;
                    return_val.putIfAbsent("clubLinks", new ArrayList<>());
                    return_val.get("clubLinks").add(wikiBaseLink + clubLink);
                }
            } else if (row.text().contains("–")) {
                String[] years = potentialDuration.split("–");
                if (years.length == 2) {
                    String firstYear = years[0];
                    String secondYear = years[1];
                    String club = Normalizer.normalize(row.parent().select("td").eq(0).text().split("\\[")[0], Normalizer.Form.NFD)
                            .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "")
                            .replace("→", "") // in the case of loan
                            .replace("(loan)", "") // self-explanatory
                            .split("\\(")[0]
                            .trim() // remove leading and trailing zeroes
                            .replaceAll("\\[. *\\]", "") // remove anything inside brackets ..
                            .replaceAll("\\(. *\\)", ""); // .. and parentheses;
                    String clubLink = row.parent().select("td").eq(0).select("a").attr("href").split("\\[")[0];

                    return_val.putIfAbsent(potentialDuration, new ArrayList<>());
                    return_val.get(potentialDuration).add(club.split("\\[")[0]);

                    if (clubLink.isEmpty() || clubLink.toLowerCase(Locale.ROOT).contains("redlink") || (return_val.containsKey("clubLinks") && return_val.get("clubLinks").contains(wikiBaseLink + clubLink))) continue;
                    return_val.putIfAbsent("clubLinks", new ArrayList<>());
                    return_val.get("clubLinks").add(wikiBaseLink + clubLink);
                } else if (years.length == 1) {
                    String club = Normalizer.normalize(row.parent().select("td").eq(0).text().split("\\[")[0], Normalizer.Form.NFD)
                            .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "")
                            .replace("→", "") // in the case of loan
                            .replace("(loan)", "") // self-explanatory
                            .split("\\(")[0]
                            .trim() // remove leading and trailing zeroes
                            .replaceAll("\\[. *\\]", "") // remove anything inside brackets ..
                            .replaceAll("\\(. *\\)", ""); // .. and parentheses;
                    String clubLink = row.parent().select("td").eq(0).select("a").attr("href").split("\\[")[0];

                    return_val.putIfAbsent(potentialDuration, new ArrayList<>());
                    return_val.get(potentialDuration).add(club.split("\\[")[0]);

                    if (clubLink.isEmpty() || clubLink.toLowerCase(Locale.ROOT).contains("redlink") || (return_val.containsKey("clubLinks") && return_val.get("clubLinks").contains(wikiBaseLink + clubLink))) continue;
                    return_val.putIfAbsent("clubLinks", new ArrayList<>());
                    return_val.get("clubLinks").add(wikiBaseLink + clubLink);
                }
            }
        }

        Footballer footballer = new Footballer(name, birthday, country, link);
        footballer.teams = return_val;

        return footballer;
    }

    public static String scrapePlayerLinks(String link) throws Exception {
        Document doc = Jsoup.connect(link).userAgent("Chrome").get();
        Elements list = doc.select("div#mw-pages").select("div.mw-content-ltr").select("div.mw-category-group");
        String baseLink = "https://en.wikipedia.org";
        StringBuilder return_val = new StringBuilder();
        HashMap<String, String> footballers = new HashMap<>(); // name : link

        for (Element group: list) {
            String groupText = group.text();
            if (groupText.toLowerCase(Locale.ROOT).contains("next page")) continue;
            else {
                Elements unorderedList = group.select("li");
                for (Element li: unorderedList) {
                    String playerName = li.select("a").attr("title");
                    String playerLink = baseLink + li.select("a").attr("href");
                    return_val.append(playerName + ": " + playerLink + "\n");

                }
            }
        }

        return return_val.toString();
    }
}
