package org.jembi.bsis.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("hello")
public class HelloWorldController {
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String getName(){
        return "Hello Getachew Sharew";
    }
}
