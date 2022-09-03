package Entity;

import java.util.ArrayList;
import java.util.Objects;

public class Team {
    String name;
    String link;
    ArrayList<String> players;

    public Team() {}

    public Team(String name, String link) {
        this.name = name;
        this.link = link;
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Team))
            return false;
        Team team = (Team) o;
        return Objects.equals(name, team.name);
    }

    @Override public int hashCode() {
        return Objects.hash(name);
    }
}
