package Scrapers;

import Entity.Footballer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import org.json.simple.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.*;
import java.text.Normalizer;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static Scrapers.AntiquatedScraper.convertJsonToMap;

public class Scraper {
    static String[] months = new String[]{"jan","feb","march","april","may","june","july","aug","sep","oct","nov","dec"};

    // Last class was good and functional, but this one is better because it'll cover all the ground:
    // https://en.wikipedia.org/wiki/Category:Association_football_defenders
    // https://en.wikipedia.org/wiki/Category:Association_football_forwards
    // https://en.wikipedia.org/wiki/Category:Association_football_goalkeepers
    // https://en.wikipedia.org/wiki/Category:Association_football_midfielders
    // These are all the links necessary to get a proper database in.
    public static void main(String[] args) throws Exception {

    }

    // THIS IS WHERE THE MAGIC HAPPENS.
    // This is the function that's actually used the most.
    public static Footballer scrapePlayerCareers(String link) throws Exception {
        // table.infobox.vcard: what needs to be scraped. Seems to be the consistent across these decent players.
        // Every single bit of info is in the <tbody> element. Even on a page like Romario's.
        Document doc = Jsoup.connect(link).userAgent("Chrome").get();
        Elements playerTable = doc.select("table.infobox.vcard");
        Elements rows = playerTable.select("th.infobox-label");
        String wikiBaseLink = "https://en.wikipedia.org";

        String birthday = "";
        String name = doc.select("h1.firstHeading").text().replaceAll("\\(.*?\\)","").trim();
        for (Element row: rows) {
            String labelText = row.text().toLowerCase(Locale.ROOT).replaceAll("\\(.*?\\)","");
            Element parent = row.parent();
            parent.select("td").select("a").remove();
            String tdText = parent.select("td").text().replaceAll("\\(.*?\\)","");

            if ((labelText.contains("birth") && labelText.contains("date")) || labelText.contains("born")) {
                for (String month: months) {
                    if (tdText.toLowerCase(Locale.ROOT).contains(month)) {
                        // WE HAVE THREE PATTERNS TO CHECK:
                        // 1. ex: 6 May 1941.
                        // 2. ex: September 16 1959.
                        // 3. ex: July 1959.
                        // WITH THIS IN PLACE, THE INITIAL 2400+ FOOTBALLERS HAVE ALL THEIR BIRTHDAYS ACCURATE. THANK GOODNESS.
                        birthday = tdText.trim().replaceAll(name, "").replace(",","").trim();
                        Pattern firstPattern = Pattern.compile("[0-9]+ [a-zA-Z]+ [0-9]+"); // e.g. "6 May 1941".
                        Pattern secondPattern = Pattern.compile("[a-zA-Z]+ [0-9]+ [0-9]+"); // e.g. "September 16 1959".
                        Pattern thirdPattern = Pattern.compile("[a-zA-Z]+ [0-9]+"); // e.g. "July 1959".

                        Matcher firstMatcher = firstPattern.matcher(birthday);
                        Matcher secondMatcher = secondPattern.matcher(birthday);
                        Matcher thirdMatcher = thirdPattern.matcher(birthday);

                        // A bit boilerplatey, but unfortunately, it has to be done.
                        // This is finding out which of the three birthday formats it fits.
                        boolean found = false;
                        while (firstMatcher.find()) {
                            birthday = firstMatcher.group();
                            found = true;
                        } if (!found){
                            while (secondMatcher.find()) {
                                birthday = secondMatcher.group();
                                found = true;
                            } if (!found){
                                while (thirdMatcher.find()) {
                                    birthday = thirdMatcher.group();
                                    found = true;
                                } if (!found){
                                    System.out.println("Sorry, no match!");
                                }
                            }
                        }
                    }
                }
            }
        }

        // Have to reset after I deleted the a ref's in the previous page. So we reset the document and get the link a second time.
        doc = Jsoup.connect(link).userAgent("Chrome").get();
        playerTable = doc.select("table.infobox.vcard");
        rows = playerTable.select("th.infobox-label");
        TreeMap<String, List<String>> return_val = new TreeMap<>();
        for (Element row: rows) {
            String potentialDuration = row.text();

            // 1. "-1992" (e.g. in case of Nwankwo Kanu) turns into 0000-1992.
            if (potentialDuration.length() == 4 || potentialDuration.isEmpty()) {
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
            }
            else if (potentialDuration.contains("–")) {
                String[] years = potentialDuration.split("–");
                if (years.length == 2) {
                    String firstYear = years[0].split("\\[")[0];
                    String secondYear = years[1].split("\\[")[0];

                    // This club thing might be a bit stupid, but it's hard to find a good regex expression for it.
                    // Some clubs have numbers, some are one word, some are five words, you never know basically.
                    String club = Normalizer.normalize(row.parent().select("td").eq(0).text().split("\\[")[0], Normalizer.Form.NFD)
                            .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "")
                            .replace("→", "") // in the case of loan
                            .replace("(loan)", "") // self-explanatory
                            .split("\\(")[0]
                            .split("\\[")[0]
                            .trim(); // remove leading and trailing zeroes

                    // If the club is unknown, if it's uncertain the player played for the club, or if we don't know when the player stopped playing for the club.
                    // Just skip these, they're basically unparsable and have no value.
                    if (club.contains("?") || firstYear.contains("?") || secondYear.contains("?")) continue;

                    String clubLink = row.parent().select("td").eq(0).select("a").attr("href").split("\\[")[0];

                    // Getting rid of any unnecessary square brackets. A bit silly but it works for the 2400+ objects.
                    potentialDuration = potentialDuration.split("\\[")[0];

                    return_val.putIfAbsent(potentialDuration, new ArrayList<>());
                    return_val.get(potentialDuration).add(club.split("\\[")[0]);

                    // Basically, if the team page doesn't exist, or if there's no link for it, or if it's already present in the clubLinks section.
                    if (clubLink.isEmpty() || clubLink.toLowerCase(Locale.ROOT).contains("redlink") || (return_val.containsKey("clubLinks") && return_val.get("clubLinks").contains(wikiBaseLink + clubLink)) || firstYear.isEmpty()) continue;
                    return_val.putIfAbsent("clubLinks", new ArrayList<>());
                    return_val.get("clubLinks").add(wikiBaseLink + clubLink);
                } else if (years.length == 1) {
                    String club = Normalizer.normalize(row.parent().select("td").eq(0).text().split("\\[")[0], Normalizer.Form.NFD)
                            .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "")
                            .replace("→", "") // in the case of loan
                            .replace("(loan)", "") // self-explanatory
                            .split("\\(")[0]
                            .split("\\[")[0]
                            .trim(); // remove leading and trailing zeroes
                    String clubLink = row.parent().select("td").eq(0).select("a").attr("href").split("\\[")[0];

                    if (club.contains("?") || years[0].contains("?")) continue;

                    potentialDuration = potentialDuration.split("\\[")[0];
                    return_val.putIfAbsent(potentialDuration, new ArrayList<>());
                    return_val.get(potentialDuration).add(club.split("\\[")[0]);

                    if (clubLink.isEmpty() || clubLink.toLowerCase(Locale.ROOT).contains("redlink") || (return_val.containsKey("clubLinks") && return_val.get("clubLinks").contains(wikiBaseLink + clubLink))) continue;
                    return_val.putIfAbsent("clubLinks", new ArrayList<>());
                    return_val.get("clubLinks").add(wikiBaseLink + clubLink);
                }
            }
        }

        Footballer footballer = new Footballer(name, birthday.trim(), link);
        footballer.teams = return_val;

        return footballer;
    }

    // This creates the database but does not generate the children.
    // Right now, the children are generated in the BFS class.
    public static void createDatabase() throws Exception {
        HashMap<String, String> newMap = convertJsonToMap(new File("data files/old/betterMerged.json"));
        ArrayList<Footballer> footballerList = new ArrayList<>();
        FileWriter fileWriter = new FileWriter("Testing.json");
        Gson gson = new Gson();

        int i = 0;
        for (String name: newMap.keySet()) {
            String link = newMap.get(name);

            i++;

            if (!(link == null)) {
                Footballer footballer = scrapePlayerCareers(link);
                System.out.println(i + ": " + footballer.teams);
                if (!(footballer == null) && !(footballer.teams == null)) footballerList.add(footballer);
            }
        }

        Gson gsonBuilder = new GsonBuilder().setPrettyPrinting().create();
        fileWriter.write(gson.toJson(footballerList));
        fileWriter.close();
    }

    // This is only here for debugging purposes. Don't use this.
    public static void scrapeInitialListOfLinks() throws Exception {
        // Get all the links from betterMerged and put them into a map. Name : Link.
        HashMap<String, String> newMap = convertJsonToMap(new File("data files/old/betterMerged.json"));

        // The rest of this function is me writing a new file and creating the database of footballers.
        FileWriter fileWriter = new FileWriter("newFootballerDatabase.json");
        ArrayList<Footballer> footballerList = new ArrayList<>();

        for (String name: newMap.keySet()) {
            String link = newMap.get(name);

            if (!(link == null)) {
                Footballer footballer = scrapePlayerCareers(link);
                footballerList.add(footballer);
            }
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        fileWriter.write(gson.toJson(footballerList));

        fileWriter.close();
    }

    // This function was used to get the links of pages of every defender. All 236 pages for the AllDefenders.json.
    // Probably won't need this in the future, depending on how fast I want my application to be.
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
                        scrapePages(baseLink + nextPage);
                    }
                }
            }
        }

    }

    // When going through the 236 defender pages, this is what was needed to get the player names and links.
    // Again, depending on how quick I want the application to be, this might not be necessary.
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
