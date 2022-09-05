package Entity;

import java.util.List;
import java.util.Objects;
import java.util.TreeMap;

public class Footballer {
    public TreeMap<String, List<String>> teams = new TreeMap<>(); // team name : duration (a list in the case of a Paul Pogba or Mathieu Flamini).
    public String birthday;
    public String name;
    public String country;
    public String link;

    public Footballer() {}

    public Footballer(String name, String birthday, String country, String link) {
        this.name = name;
        this.birthday = birthday;
        this.country = country;
        this.link = link;
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Footballer))
            return false;
        Footballer that = (Footballer) o;
        return Objects.equals(name, that.name) && Objects.equals(link, that.link);
    }

    @Override public int hashCode() {
        return Objects.hash(teams, birthday, name, country, link);
    }

    @Override public String toString() {
        return name + " " + birthday + ", born " + country + "; " + teams;
    }
}
