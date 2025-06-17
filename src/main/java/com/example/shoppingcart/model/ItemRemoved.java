package com.example.shoppingcart.model;

public final class ItemRemoved implements ShoppingCartEvent {
    public final String cartId;
    public final String productId;
    public final int quantity;
    
    public ItemRemoved(String cartId, String productId, int quantity) {
        this.cartId = cartId;
        this.productId = productId;
        this.quantity = quantity;
    }
} 