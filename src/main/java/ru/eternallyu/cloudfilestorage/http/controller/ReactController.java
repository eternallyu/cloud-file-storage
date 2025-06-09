package ru.eternallyu.cloudfilestorage.http.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ReactController {

    @GetMapping(value = {"/registration", "/login", "/files/**"})
    public String redirect() {
        return "forward:/index.html";
    }
}
