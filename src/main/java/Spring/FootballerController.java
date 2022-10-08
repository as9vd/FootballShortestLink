package Spring;

import Scrapers.BreadthFirstSearch;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin("*")
// Next steps as a whole:
// 1. Show where the players played with one another on the front-end.
// 3. Add some sort of loading to make the data update look smoother.

// PROBLEMS:
// 1. SAME NAME (E.G. LUIS SUAREZ)
// 2. CHILDREN NOT GENERATED PROPERLY (E.G. GIGGSY AND EVRA)
// 3. DUPLICATES (E.G. FIGO)
// 4. THREE NAMED BLOKES (E.G. ALAN BALL JR.)
public class FootballerController {
    @RequestMapping(path = "/{footballer1}+{footballer2}", method = RequestMethod.GET)
    public String bfsLink(@PathVariable String footballer1, @PathVariable String footballer2) throws Exception {
        footballer1 = footballer1.replaceAll("SEP"," ").trim();
        footballer2 = footballer2.replaceAll("SEP"," ").trim();

        String return_val = BreadthFirstSearch.bfs(footballer1, footballer2, "FootballerGraphFormatted.json");

        return return_val;
    }
}
