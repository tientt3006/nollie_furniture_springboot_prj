package indiv.neitdev.nollie_furniture.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloSpringController {

    @GetMapping
    String hello() {
        return "Hello Spring Boot!";
    }
}
