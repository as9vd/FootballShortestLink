import Entity.Footballer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import javafx.util.Pair;
import java.io.FileReader;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

// How I'm going to do this:
// 1. Parse JSON file and create a new one where a player is linked to his teammates.
// The identifiers here will be a player's name and their Wikipedia link, because there are duplicate names.
// 2. Link it to the Angular front-end and do BFS there (e.g. a visited HashSet, a Queue, all this bollocks).
// Wow. Not as complicated as I thought it was.
public class BreadthFirstSearch {
    public static void main(String[] args) throws Exception {
        Gson gson = new GsonBuilder().create();
        JsonReader reader = new JsonReader(new FileReader("footballerDatabase.json"));

        Footballer[] list = gson.fromJson(reader, Footballer[].class);

        for (Footballer currFootballer: list) {
            for (Footballer iterFootballer: list) {
                if (currFootballer.equals(iterFootballer)) continue;
                else if (checkForOverlap(currFootballer, iterFootballer)) {

                }
            }
        }
    }

    public static boolean checkForOverlap(Footballer footballer1, Footballer footballer2) {
        // The point of initialise queue is to make every single duration one format (e.g. year-year, except in the case of a singular year).
        // In the case of "unknown", then push it according to its end year.
        PriorityQueue<Pair<String,String>> queue = new PriorityQueue<Pair<String,String>>((a, b) -> {
            int aStart, bStart;
            if (a.getValue().split("–")[0].equals("unknown")) aStart = Integer.parseInt(a.getValue().split("–")[1]);
            else aStart = Integer.parseInt(a.getValue().split("–")[0]);

            if (b.getValue().split("–")[0].equals("unknown")) bStart = Integer.parseInt(b.getValue().split("–")[1]);
            else bStart = Integer.parseInt(b.getValue().split("–")[0]);

            return aStart - bStart;
        });

        initialiseQueue(footballer1, queue);
        initialiseQueue(footballer2, queue);

        System.out.println(queue);

        return false;
    }

    public static void initialiseQueue(Footballer footballer, PriorityQueue queue) {
        for (Map.Entry<String, List<String>> entry: footballer.teams.entrySet()) {
            String years = entry.getKey()
                    .split("\\[")[0]
                    .split("\\(")[0]
                    .replaceAll("\\s+",""); // removing any potential brackets and spaces
            if (years.equals("clubLinks")) continue;

            List<String> listOfTeams = entry.getValue();
            for (int i = 0; i < listOfTeams.size(); i++) {
                Pair<String, String> pair;
                // Some inconsistencies here. If currentInterval either:
                // 1. Isn't fully complete (e.g. some entries have intervals like "68-1972" or "1956-58").
                // 2. Is only one year (e.g. Thierry Henry with "1999").
                // 3. Is incomplete somewhere (e.g. Kanu's youth career "0000-1992").
                String team = listOfTeams.get(i);
                if (years.split("–").length == 1) { // This is either A) case #2 or B) if a player is currently playing for the club.
                    if (years.contains("?") || years.contains("x")) {
                        continue; // not worth the headache dealing with these daft edge cases
                    } else if (years.contains("–")) { // Case B.
                        pair = new Pair<>(team, years + "current");
                    } else { // Case A/#2.
                        pair = new Pair<>(team, years);
                    }
                } else {
                    String[] splitYears = years.split("–");
                    String startYear = splitYears[0];
                    String endYear = splitYears[1];

                    if (startYear.isEmpty()) {
                        pair = new Pair<>(team, "unknown" + years);
                    } else if ((startYear.contains("?") || startYear.contains("x")) &&
                            (endYear.contains("?") || endYear.contains("x"))) {
                        continue; // if both are unknown, just bin the lot
                    } else if (startYear.contains("?") || startYear.contains("x")) {
                        pair = new Pair<>(team, "unknown–" + endYear);
                    } else if (startYear == "0000") {
                        pair = new Pair<>(team,"unknown–" + endYear);
                    } else if (endYear == "0000") { // these two take care of case #3.
                        pair = new Pair<>(team,startYear + "–unknown");
                    } else {
                        if (startYear.length() == 2 && endYear.length() == 2) {
                            pair = new Pair<>(team,"19" + startYear + "-19" + endYear); // realistically, it's only antiquated players from the 1900s. so it's a safe bet
                        } else if (startYear.length() == 2) {
                            if (endYear.charAt(1) == '9') { // could be 1800s or 1900s
                                pair = new Pair<>(team,"19" + startYear + "–" + endYear);
                            } else if (endYear.charAt(1) == '8') {
                                pair = new Pair<>(team,"18" + startYear + "–" + endYear);
                            } else {
                                pair = new Pair<>(team,"20" + startYear + "–" + endYear);
                            }
                        } else if (endYear.length() == 2) { // these three take care of case #1.
                            if (startYear.charAt(1) == '9') { // could be 1800s or 1900s
                                pair = new Pair<>(team,startYear + "–19" + endYear);
                            } else if (startYear.charAt(1) == '8') {
                                pair = new Pair<>(team,startYear + "–18" + endYear);
                            } else {
                                pair = new Pair<>(team,startYear + "–20" + endYear);
                            }
                        } else { // normal
                            pair = new Pair<>(team, years);
                        }
                    }
                }

                queue.offer(pair);
            }
        }
    }
}
