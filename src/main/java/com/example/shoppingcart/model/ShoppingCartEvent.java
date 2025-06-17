package com.example.shoppingcart.model;

import java.io.Serializable;

// Marker interface for all shopping cart events
public interface ShoppingCartEvent extends Serializable {}

public final class ItemAdded implements ShoppingCartEvent {
    public final String cartId;
    public final CartItem item;
    
    public ItemAdded(String cartId, CartItem item) {
        this.cartId = cartId;
        this.item = item;
    }
}

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
