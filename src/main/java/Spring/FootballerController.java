package Spring;

import Scrapers.BreadthFirstSearch;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin("*")
public class FootballerController {
    @RequestMapping(path = "/{footballer1}+{footballer2}", method = RequestMethod.GET)
    public String bfsLink(@PathVariable String footballer1, @PathVariable String footballer2) throws Exception {
        footballer1 = footballer1.replaceAll("SEP"," ");
        footballer2 = footballer2.replaceAll("SEP"," ");

        String return_val = BreadthFirstSearch.bfs(footballer1, footballer2);

        return return_val;
    }
}
