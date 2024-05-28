package com.example.stockData.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Random;

@Data
@AllArgsConstructor
@Document(collection = "stocks")
public class StockQuote {

    @Id
    private String id;

    @BsonProperty("01. symbol")
    private String symbol;

    @BsonProperty("02. open")
    private String open;

    @BsonProperty("03. high")
    private String high;

    @BsonProperty("04. low")
    private String low;

    @BsonProperty("05. price")
    private String price;

    @BsonProperty("06. volume")
    private String volume;

    @BsonProperty("07. latest trading day")
    private String latestTradingDay;

    @BsonProperty("08. previous close")
    private String previousClose;

    @BsonProperty("09. change")
    private String change;

    @BsonProperty("10. change percent")
    private String changePercent;

    public StockQuote(String symbol) {
        Random random = new Random();

        this.symbol = symbol;
        this.open = String.valueOf(random.nextInt(1000 - 500 + 1) + 500);
        this.high = String.valueOf(random.nextInt(100000 - 50000 + 1) + 50000);
        this.low = String.valueOf(random.nextInt(100) + 1);
        this.price = String.valueOf(random.nextInt(2000) + 1);
        this.volume = String.valueOf(random.nextInt(100000) + 1);
        this.latestTradingDay = "2024-"
                + String.valueOf(random.nextInt(11) + 1) + "-"
                + String.valueOf(random.nextInt(11) + 1);
        this.previousClose = String.valueOf(random.nextInt(100000) + 1);
        this.change = String.valueOf(random.nextFloat());
        this.changePercent = String.valueOf(random.nextFloat()) + "%";
    }

    public StockQuote() {
        Random random = new Random();

        this.symbol = "TEST" + String.valueOf(random.nextInt());
        this.open = String.valueOf(random.nextInt(1000 - 500 + 1) + 500);
        this.high = String.valueOf(random.nextInt(100000 - 50000 + 1) + 50000);
        this.low = String.valueOf(random.nextInt(100) + 1);
        this.price = String.valueOf(random.nextInt(100000) + 1);
        this.volume = String.valueOf(random.nextInt(100000) + 1);
        this.latestTradingDay = "2024-"
                + String.valueOf(random.nextInt(11) + 1) + "-"
                + String.valueOf(random.nextInt(11) + 1);
        this.previousClose = String.valueOf(random.nextInt(100000) + 1);
        this.change = String.valueOf(random.nextFloat());
        this.changePercent = String.valueOf(random.nextFloat()) + "%";
    }

    public String convertToJson() {
        org.bson.Document document = new org.bson.Document();
        document.append("01. symbol", this.symbol);
        document.append("02. open", this.open);
        document.append("03. high", this.high);
        document.append("04. low", this.low);
        document.append("05. price", this.price);
        document.append("06. volume", this.volume);
        document.append("07. latest trading day", this.latestTradingDay);
        document.append("08. previous close", this.previousClose);
        document.append("09. change", this.change);
        document.append("10. change percent", this.changePercent);

        return document.toJson();
    }
}
