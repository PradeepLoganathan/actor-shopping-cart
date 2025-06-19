package com.example.shoppingcart.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CartItem implements Serializable {
    private final String productId;
    private final String name;
    private final double price;
    private int quantity;

    @JsonCreator 
    public CartItem(
            @JsonProperty("productId") String productId,
            @JsonProperty("name") String name,
            @JsonProperty("price") double price,
            @JsonProperty("quantity") int quantity) {
        this.productId = productId;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    // Getters
    public String getProductId() { return productId; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getTotalPrice() {
        return price * quantity;
    }

    @Override
    public String toString() {
        return String.format("%s (x%d) - $%.2f", name, quantity, getTotalPrice());
    }
}
