package com.translator.main.controller;

import com.translator.main.service.RequestCounterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

    private final RequestCounterService requestCounterService;

    @Autowired
    public StatsController(RequestCounterService requestCounterService) {
        this.requestCounterService = requestCounterService;
    }

    @GetMapping("/count")
    public ResponseEntity<Integer> getRequestCount() {
        return ResponseEntity.ok(requestCounterService.getCount());
    }

    @GetMapping("/reset")
    public ResponseEntity<String> resetCounter() {
        requestCounterService.reset();
        return ResponseEntity.ok("Счетчик сброшен");
    }
}
