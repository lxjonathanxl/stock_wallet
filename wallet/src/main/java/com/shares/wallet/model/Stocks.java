package com.shares.wallet.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;

import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@AllArgsConstructor
@Builder
public class Stocks implements Serializable {

    private static final long serialVersionUID = 1l;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "stock_id")
    private Long stockId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Users user;

    @Digits(integer = 10, fraction = 2, message = "Invalid quantity of stocks")
    private BigDecimal quant;

    @NotBlank(message = "name cant be empty")
    private String name;

    public Stocks(Users user, BigDecimal quant, String name) {
        this.user = user;
        this.quant = quant;
        this.name = name;
    }

    public Stocks() {

    }

    public Long getStockId() {
        return stockId;
    }

    public void setStockId(Long stockId) {
        this.stockId = stockId;
    }

    public Users getUser() {
        return user;
    }

    public void setUser(Users user) {
        this.user = user;
    }

    public BigDecimal getQuant() {
        return quant;
    }

    public void setQuant(BigDecimal quant) {
        this.quant = quant;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

