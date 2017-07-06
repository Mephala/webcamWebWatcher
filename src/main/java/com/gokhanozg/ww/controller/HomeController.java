package com.gokhanozg.ww.controller;

import com.gokhanozg.ww.CompletedCapture;
import com.gokhanozg.ww.WebCamEngine;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Set;

/**
 * Created by mephala on 6/2/17.
 */
@Controller
public class HomeController {
    private WebCamEngine webCamEngine = WebCamEngine.getInstance();

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public Object showHome(HttpServletRequest request, HttpServletResponse response) {
        ModelAndView model = new ModelAndView("home");
        Set<CompletedCapture> completedCaptures = webCamEngine.getCaptures();
        model.addObject("captures", completedCaptures);
        return model;
    }

    @RequestMapping(value = "/play", method = RequestMethod.GET)
    public void play(HttpServletRequest request, HttpServletResponse response) {
        PlayCatSound.play();
    }
}
