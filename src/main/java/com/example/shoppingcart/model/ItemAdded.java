package com.example.shoppingcart.model;

public final class ItemAdded implements ShoppingCartEvent {
    public final String cartId;
    public final CartItem item;
    
    public ItemAdded(String cartId, CartItem item) {
        this.cartId = cartId;
        this.item = item;
    }
} 