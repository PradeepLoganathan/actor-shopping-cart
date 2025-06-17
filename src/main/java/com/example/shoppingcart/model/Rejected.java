package com.example.shoppingcart.model;

public final class Rejected implements Confirmation {
    public final String reason;

    public Rejected(String reason) {
        this.reason = reason;
    }
} 