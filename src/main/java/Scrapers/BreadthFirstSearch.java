package Scrapers;

import Entity.Footballer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import javafx.util.Pair;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;

// How I'm going to do this:
// 1. Parse JSON file and create a new one where a player is linked to his teammates.
// The identifiers here will be a player's name and their Wikipedia link, because there are duplicate names.
// 2. Link it to the Angular front-end and do BFS there (e.g. a visited HashSet, a Queue, all this bollocks).
// Wow. Not as complicated as I thought it was.

@Component
@ComponentScan("src")
public class BreadthFirstSearch {
    public static void main(String[] args) throws Exception {
        buildAdjacencyList();
    }

    // 1. Ryan Betrtrand and Max Kilman.
    // 2. BFS returns the second they find the player, not if the player is the shortest route. Fix this.
    public static String bfs(String start, String dest) throws Exception {
        Gson gson = new GsonBuilder().create();
        JsonReader reader = new JsonReader(new FileReader("Kids.json"));
        TypeToken<List<Footballer>> token = new TypeToken<List<Footballer>>() {};
        List<Footballer> footballers = gson.fromJson(reader, token.getType());
        Footballer currFootballer = null;

        Set<String> visited = new HashSet<>();
        Queue<Pair<Pair<Integer,String>,String>> queue = new LinkedList<>(); // (steps : player) : route so far
        queue.offer(new Pair(new Pair(1, start), ""));

        while (!(queue.isEmpty())) {
            int currSteps = queue.peek().getKey().getKey();
            String currPlayer = queue.peek().getKey().getValue();
            String route = queue.poll().getValue();

            System.out.println(route);

            if (currPlayer.equals(dest)) {
                route += "->" + dest;
                return route;
            }
            else if (visited.contains(currPlayer)) continue;

            visited.add(currPlayer);
            for (Footballer footballer: footballers) {
                if (footballer.name.equals(currPlayer)) {
                    currFootballer = footballer;
                    break;
                }
            }

            if (currFootballer == null) return "";

            Set<String> children = currFootballer.children;
            for (String name: children) {
                if (route.isEmpty()) {
                    queue.offer(new Pair(new Pair(currSteps + 1, name), currPlayer));
                } else {
                    queue.offer(new Pair(new Pair(currSteps + 1, name), route + "->" + currPlayer));
                }
            }
        }

        return "";
    }

    public static void buildAdjacencyList() throws Exception {
        Gson gson = new GsonBuilder().create();
        JsonReader reader = new JsonReader(new FileReader("AllDefenders.json"));

        Footballer[] list = gson.fromJson(reader, Footballer[].class);

        int i = 0;
        for (Footballer currFootballer: list) {
            for (Footballer iterFootballer: list) {
                if (currFootballer.equals(iterFootballer)) continue;
                checkForOverlap(currFootballer, iterFootballer);
            }
            i++;
            System.out.println(i + " (" + currFootballer.name + "): " + currFootballer.children);
        }

        FileWriter fileWriter = new FileWriter("BetterKids.json");
        Gson gsonBuilder = new GsonBuilder().setPrettyPrinting().create();
        fileWriter.write(gson.toJson(list));
        fileWriter.close();
        reader.close();
    }

    public static void checkForOverlap(Footballer footballer1, Footballer footballer2) {
        // The point of initialise queue is to make every single duration one format (e.g. year-year, except in the case of a singular year).
        // In the case of "unknown", then push it according to its end year.
        // (team : player) : year
        PriorityQueue<Pair<Pair<String,String>,String>> queue = new PriorityQueue<Pair<Pair<String,String>,String>>((a, b) -> {
            int aStart, bStart;
            if (a.getValue().split("–")[0].equals("unknown")) {
                aStart = Integer.parseInt(a.getValue().split("–")[1]);
            } else {
                aStart = Integer.parseInt(a.getValue().split("–")[0]);
            }

            if (b.getValue().split("–")[0].equals("unknown")) {
                bStart = Integer.parseInt(b.getValue().split("–")[1]);
            } else {
                bStart = Integer.parseInt(b.getValue().split("–")[0]);
            }

            return aStart - bStart;
        });

        if (footballer1.name.equals(footballer2.name)) return;

        initialiseQueue(footballer1, queue);
        initialiseQueue(footballer2, queue);
        ArrayList<Pair<Pair<String,String>,String>> intervals = new ArrayList<>();
        while (!(queue.isEmpty())) {
            intervals.add(queue.poll());
        }

        if (intervals.isEmpty()) return;
        Pair<Pair<String,String>,String> newInterval = intervals.get(0);
        for (Pair<Pair<String,String>,String> interval: intervals) {
            String team = interval.getKey().getKey();
            String player = interval.getKey().getValue();
            String years = interval.getValue(), start = years.split("–")[0], end;

            if (years.split("–").length == 1) { // in the case of singular years.
                end = start;
            } else {
                end = years.split("–")[1];
            }

            String newTeam = newInterval.getKey().getKey();
            String newPlayer = newInterval.getKey().getValue();
            String newYears = newInterval.getValue(), newStart = years.split("–")[0], newEnd;
            if (newYears.split("–").length == 1) { // in case of singular years
                newEnd = newStart;
            } else {
                newEnd = newYears.split("–")[1];
            }

            if (newEnd.equals("unknown") || start.equals("unknown") ||
                    (!(newEnd.charAt(0) == '1') && !(newEnd.charAt(0) == '2')) ||
                    (!(start.charAt(0) == '1') && !(start.charAt(0) == '2'))) {
                continue;
            }

            if (Integer.parseInt(start) < Integer.parseInt(newEnd)) {
                if (end.equals("unknown")) continue;
                else if ((end.equals("current") || (Integer.parseInt(end) > Integer.parseInt(newEnd)))) {
                    if (newPlayer.equals(player)) continue;
                    else if (team.equals(newTeam)) {
                        footballer1.children.add(footballer2.name + " " + footballer2.birthday);
                        footballer2.children.add(footballer1.name + " " + footballer1.birthday);
                    }
                }
            } else {
                newInterval = interval;
                continue;
            }
        }
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
                Pair<Pair<String,String>, String> pair;
                // Some inconsistencies here. If currentInterval either:
                // 1. Isn't fully complete (e.g. some entries have intervals like "68-1972" or "1956-58").
                // 2. Is only one year (e.g. Thierry Henry with "1999").
                // 3. Is incomplete somewhere (e.g. Kanu's youth career "0000-1992").
                String team = listOfTeams.get(i);

                if (years.split("–").length == 1) { // This is either A) case #2 or B) if a player is currently playing for the club.
                    if (years.isEmpty() ||
                            years.toLowerCase(Locale.ROOT).equals("present") ||
                            (!(years.charAt(0) == '1') && !(years.charAt(0) == '2')) ||
                            years.contains("/") || years.contains("/") ||
                            years.length() > 5 || years.contains("x") || years.contains("?")) {
                        continue; // not worth the headache dealing with these daft edge cases
                    } else if (years.contains("–")) { // Case B.
                        pair = new Pair<>(new Pair(team, footballer.name), years + (Calendar.getInstance().get(Calendar.YEAR) + 1));
                    } else { // Case A/#2.
                        pair = new Pair<>(new Pair(team, footballer.name), years);
                    }
                } else {
                    String[] splitYears = years.split("–");
                    String startYear = splitYears[0];
                    String endYear = splitYears[1];

                    if (startYear.isEmpty()) {
                        continue;
                    } else if (endYear.toLowerCase(Locale.ROOT).equals("present") ||
                            ((!(startYear.charAt(0) == '1') && !(startYear.charAt(0) == '2')) || // if there's no exact date, just bin it off
                                    (!(endYear.charAt(0) == '1') && !(endYear.charAt(0) == '2'))) ||
                            startYear.contains("/") || endYear.contains("/") ||
                            startYear.length() > 4 || endYear.length() > 4 ||
                            startYear.contains("?") || startYear.contains("x") &&
                            endYear.contains("?") || endYear.contains("x")) { // if the years aren't years for some reason
                        continue;
                    } else if (startYear.contains("?") || startYear.contains("x") || startYear.contains("X")) {
                        pair = new Pair<>(new Pair(team, footballer.name), "unknown–" + endYear);
                    } else if (endYear.contains("?") || endYear.contains("x") || endYear.contains("X")) {
                        pair = new Pair<>(new Pair(team, footballer.name), startYear + "–unknown");
                    } else if (startYear.equals("0000")) {
                        pair = new Pair<>(new Pair(team, footballer.name),"unknown–" + endYear);
                    } else if (endYear.equals("0000")) { // these two take care of case #3.
                        pair = new Pair<>(new Pair(team, footballer.name), startYear + "–unknown");
                    } else {
                        if (startYear.length() == 2 && endYear.length() == 2) {
                            pair = new Pair<>(new Pair(team, footballer.name),"19" + startYear + "-19" + endYear); // realistically, it's only antiquated players from the 1900s. so it's a safe bet
                        } else if (startYear.length() == 2) {
                            if (endYear.charAt(1) == '9') { // could be 1800s or 1900s
                                pair = new Pair<>(new Pair(team, footballer.name),"19" + startYear + "–" + endYear);
                            } else if (endYear.charAt(1) == '8') {
                                pair = new Pair<>(new Pair(team, footballer.name),"18" + startYear + "–" + endYear);
                            } else {
                                pair = new Pair<>(new Pair(team, footballer.name),"20" + startYear + "–" + endYear);
                            }
                        } else if (endYear.length() == 2) { // these three take care of case #1.
                            if (startYear.charAt(1) == '9') { // could be 1800s or 1900s
                                pair = new Pair<>(new Pair(team, footballer.name),startYear + "–19" + endYear);
                            } else if (startYear.charAt(1) == '8') {
                                pair = new Pair<>(new Pair(team, footballer.name),startYear + "–18" + endYear);
                            } else {
                                pair = new Pair<>(new Pair(team, footballer.name),startYear + "–20" + endYear);
                            }
                        } else { // normal
                            pair = new Pair<>(new Pair(team, footballer.name), years);
                        }
                    }
                }

                queue.offer(pair);
            }
        }
    }
}