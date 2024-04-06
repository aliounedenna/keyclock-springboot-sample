package com.camel.solutions.keyclockAuth.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value = "api/v1/test")
public class EntryPoint {


    @GetMapping
    public String getTest() {
       return "hello word";
    }
}
