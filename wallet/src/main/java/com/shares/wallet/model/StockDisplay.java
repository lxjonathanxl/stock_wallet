package com.shares.wallet.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class StockDisplay {

    public String name;
    public BigDecimal quant;
    public BigDecimal price;
    public BigDecimal total;

    public StockDisplay(String name, BigDecimal quant, BigDecimal price, BigDecimal total) {
        this.name = name;
        this.quant = quant;
        this.price = price;
        this.total = total;
    }

}
