package com.example.shoppingcart.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class PersistentCartState implements Serializable {
    public final String cartId;
    public final Map<String, CartItem> items;
    public final double totalPrice;

    public static final PersistentCartState EMPTY = new PersistentCartState("", new HashMap<>());

    public PersistentCartState(String cartId, Map<String, CartItem> items) {
        this.cartId = cartId;
        this.items = new HashMap<>(items);
        this.totalPrice = calculateTotalPrice();
    }

    public PersistentCartState applyEvent(ShoppingCartEvent event) {
        if (event instanceof ItemAdded) {
            return applyItemAdded((ItemAdded) event);
        } else if (event instanceof ItemRemoved) {
            return applyItemRemoved((ItemRemoved) event);
        }
        return this; // Ignore unknown events
    }

    private PersistentCartState applyItemAdded(ItemAdded event) {
        Map<String, CartItem> newItems = new HashMap<>(items);
        newItems.merge(
            event.item.getProductId(),
            event.item,
            (existing, newItem) -> {
                existing.setQuantity(existing.getQuantity() + newItem.getQuantity());
                return existing;
            }
        );
        return new PersistentCartState(cartId, newItems);
    }

    private PersistentCartState applyItemRemoved(ItemRemoved event) {
        if (!items.containsKey(event.productId)) {
            return this;
        }

        Map<String, CartItem> newItems = new HashMap<>(items);
        CartItem existingItem = newItems.get(event.productId);
        int newQuantity = existingItem.getQuantity() - event.quantity;

        if (newQuantity <= 0) {
            newItems.remove(event.productId);
        } else {
            CartItem updatedItem = new CartItem(
                existingItem.getProductId(),
                existingItem.getName(),
                existingItem.getPrice(),
                newQuantity
            );
            newItems.put(event.productId, updatedItem);
        }
        
        return new PersistentCartState(cartId, newItems);
    }

    private double calculateTotalPrice() {
        return items.values().stream()
            .mapToDouble(CartItem::getTotalPrice)
            .sum();
    }

    public CartState toCartState() {
        return new CartState(cartId, new HashMap<>(items));
    }
}
