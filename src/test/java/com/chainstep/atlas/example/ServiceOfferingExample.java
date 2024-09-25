package com.chainstep.atlas.example;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/offering")
public class ServiceOfferingExample {

    @GetMapping("/greeting")
    public String greeting() {
        return "Hello World";
    }
}
