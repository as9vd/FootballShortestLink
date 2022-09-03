package Spring;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FootballerController {
    @RequestMapping("/brazzers")
    public String test() {
        return "Brazzers";
    }
}
