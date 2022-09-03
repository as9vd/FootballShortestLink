package Entity;

import java.util.List;
import java.util.TreeMap;

public class Footballer {
    public TreeMap<String, List<String>> teams = new TreeMap<>(); // team name : duration (a list in the case of a Paul Pogba or Mathieu Flamini).
    String birthday;
    String name;
    String country;
    String link;

    public Footballer() {}

    public Footballer(String name, String birthday, String country, String link) {
        this.name = name;
        this.birthday = birthday;
        this.country = country;
        this.link = link;
    }

    @Override public String toString() {
        return name + " (" + birthday + "), born " + country + "; " + teams;
    }
}
