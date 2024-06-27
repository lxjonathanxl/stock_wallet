package com.example.stockData.controllers;

import com.example.stockData.model.StockQuote;
import com.example.stockData.services.StockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(produces="application/json")
@CrossOrigin(origins = "http://localhost:8090")
public class StockController {

    private final static Logger stockControllerLogger = LoggerFactory.getLogger(StockController.class);

    @Autowired
    StockService stockService;

    @PostMapping("/save/{symbol}")
    public ResponseEntity<String> saveBySymbol(@PathVariable("symbol") String symbol) {

        String json;

        json = stockService.save(symbol);

        stockControllerLogger.info("user attempt to save stock");

        return new ResponseEntity<>(json, HttpStatus.OK);
    }

    @GetMapping("/get/{symbol}")
    @CrossOrigin
    public ResponseEntity<String> getBySymbol(@PathVariable("symbol") String symbol) {

        String json;

        json = stockService.findBySymbol(symbol);

        stockControllerLogger.info("user attempt to save stock");

        return new ResponseEntity<>(json, HttpStatus.OK);
    }
}
