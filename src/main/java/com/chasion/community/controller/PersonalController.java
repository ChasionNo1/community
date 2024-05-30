package com.chasion.community.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class PersonalController {


    @RequestMapping(value = "/personal", method = RequestMethod.GET)
    public String personal() {
        return "/site/personal";
    }
}
