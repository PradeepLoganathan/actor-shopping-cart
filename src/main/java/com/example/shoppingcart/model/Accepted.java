package com.example.shoppingcart.model;

public final class Accepted implements Confirmation {
    public final CartState cartState;

    public Accepted(CartState cartState) {
        this.cartState = cartState;
    }
} 