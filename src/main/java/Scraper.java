import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
        reader = new BufferedReader(new FileReader("linkstoscrape.txt"));
        String line = reader.readLine();
        while (line != null) {
            links.add(line.split(",")[0]);
            line = reader.readLine();
        }

        BufferedWriter writer = new BufferedWriter(new FileWriter("AllDefenders.txt"));
        for (String link: links) {
            writer.write(scrapePlayerLinks(link));
        }
        writer.close();
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

    public static String scrapePlayerLinks(String link) throws Exception {
        Document doc = Jsoup.connect(link).userAgent("Chrome").get();
        Elements list = doc.select("div#mw-pages").select("div.mw-content-ltr").select("div.mw-category-group");
        String baseLink = "https://en.wikipedia.org";
        StringBuilder return_val = new StringBuilder();

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
