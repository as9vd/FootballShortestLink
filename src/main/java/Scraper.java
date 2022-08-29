import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.FileWriter;

public class Scraper {
    public static void main(String[] args) {
        try {
            String baseLink = "https://en.wikipedia.org/wiki/";
            FileWriter myWriter = new FileWriter("players.txt");

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
                    System.out.println(element.select("tr"));
                }

                myWriter.write(doc.select("table.wikitable")
                        .select("tbody")
                        .select("tr")
                        .select("td")
                        .get(1)
                        .text() + "\n");

                baseLink = "https://en.wikipedia.org/wiki/";
            }

            myWriter.close();
        } catch (Exception e) {
            System.out.println(e.fillInStackTrace());
        }
    }
}
