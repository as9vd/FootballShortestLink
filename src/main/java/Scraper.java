import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashMap;
import java.util.Map;

public class Scraper {
    public static void main(String[] args) {
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
                        if (!(row.select("td").eq(0).text().isEmpty()) &&
                                ((int) row.select("td").eq(0).text().charAt(0) > 57) &&
                                (row.select("td").eq(0).text().length() > 2)
                        ) { // If this element isn't a empty, a position, or a ranking/number.
//                            System.out.println(i + ": " + tr.select("td").eq(0).text().split("\\[")[0]);
                            if (!(tr.select("td").eq(0).select("a[href]").text() == null)) {
                                footballNames.put(tr.select("td").eq(0).text().split("\\[")[0], tr.select("td").eq(0).attr("href"));
                            }
                        } else {
//                            System.out.println(i + ": " + tr.select("td").eq(1).text().split("\\[")[0]);
                            if (!(tr.select("td").eq(1).select("a[href").text() == null)) {
                                footballNames.put(tr.select("td").eq(1).text().split("\\[")[0], tr.select("td").eq(1).attr("href"));
                            }
                        }
                    }
                }
                baseLink = "https://en.wikipedia.org/wiki/";
            }

            footballNames.remove(" "); footballNames.remove("");
            System.out.println(footballNames);
        } catch (Exception e) {
            System.out.println(e.fillInStackTrace());
        }
    }
}
