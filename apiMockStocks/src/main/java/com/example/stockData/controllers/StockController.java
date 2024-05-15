package com.example.stockData.controllers;

import com.example.stockData.model.StockQuote;
import com.example.stockData.services.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(produces="application/json")
@CrossOrigin(origins = "http://localhost:8090")
public class StockController {

    @Autowired
    StockService stockService;

    @PostMapping("/save/{symbol}")
    @CrossOrigin
    public ResponseEntity<String> saveBySymbol(@PathVariable("symbol") String symbol) {

        String json;

        json = stockService.save(symbol);

        return new ResponseEntity<>(json, HttpStatus.OK);
    }

    @GetMapping("/get/{symbol}")
    @CrossOrigin
    public ResponseEntity<String> getBySymbol(@PathVariable("symbol") String symbol) {

        String json;

        json = stockService.findBySymbol(symbol);

        return new ResponseEntity<>(json, HttpStatus.OK);
    }
}
