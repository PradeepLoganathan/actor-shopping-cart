package com.example.shoppingcart.model;

import java.io.Serializable;
import java.util.Map;

// Response wrapper for the cart state
public final class CartState implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public final String cartId;
    public final Map<String, CartItem> items;
    public final double totalPrice;

    public CartState(String cartId, Map<String, CartItem> items) {
        if (cartId == null) {
            throw new IllegalArgumentException("cartId cannot be null");
        }
        if (items == null) {
            throw new IllegalArgumentException("items cannot be null");
        }
        this.cartId = cartId;
        this.items = Map.copyOf(items); // Defensive copy
        this.totalPrice = calculateTotalPrice();
    }
    
    private double calculateTotalPrice() {
        return items.values().stream()
            .mapToDouble(CartItem::getTotalPrice)
            .sum();
    }
    
    @Override
    public String toString() {
        return "CartState{" +
                "cartId='" + cartId + '\'' +
                ", items=" + items +
                ", totalPrice=" + totalPrice +
                '}';
    }
}
