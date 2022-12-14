package Entity;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Team {
    public String name;
    public String link;
    public Set<String> players = new HashSet<>();

    public Team() {}

    public Team(String name, String link) {
        this.name = name;
        this.link = link;
    }

    @Override public String toString() {
        return "Team{" + "name='" + name + '\'' + ", link='" + link + '\'' + ", players=" + players + '}';
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
