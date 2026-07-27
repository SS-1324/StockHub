package com.kh.demo.dictionary.controller;

import com.kh.demo.dictionary.service.GlossaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
public class GlossaryController {

    @Autowired
    private GlossaryService glossaryService;
}
