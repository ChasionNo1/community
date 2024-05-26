package com.chasion.community.controller;

import com.chasion.community.service.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

@Controller
public class DataController {

    @Autowired
    private DataService dataService;


    @RequestMapping(path = "/data", method = {RequestMethod.GET, RequestMethod.POST})
    public String getDataPage(Model model) {
        return "/site/admin/data";
    }

    @RequestMapping(path = "/data/uv", method = RequestMethod.POST)
    public String getUV(@DateTimeFormat(pattern = "yyyy-MM-dd") Date start, @DateTimeFormat(pattern = "yyyy-MM-dd")Date end, Model model) {
        long uv = dataService.calculateUV(start, end);
        model.addAttribute("uv", uv);
        model.addAttribute("uvStartDate", start);
        model.addAttribute("uvEndDate", end);

        return "forward:/data";
    }

    @RequestMapping(value = "/data/dau", method = RequestMethod.POST)
    public String getDau(@DateTimeFormat(pattern = "yyyy-MM-dd") Date start, @DateTimeFormat(pattern = "yyyy-MM-dd")Date end ,Model model) {
        long dau = dataService.calculateDau(start, end);
        model.addAttribute("dau", dau);
        model.addAttribute("dauStartDate", start);
        model.addAttribute("dauEndDate", end);
        return "forward:/data";
    }
}
