package com.example.shoppingcart.model;

import akka.actor.typed.ActorRef;

import java.io.Serializable;
import java.util.Map;

public interface ShoppingCartCommand extends Serializable {}

// Command to add an item to the cart
public final class AddItem implements ShoppingCartCommand {
    public final String cartId;
    public final CartItem item;
    public final ActorRef<Confirmation> replyTo;

    public AddItem(String cartId, CartItem item, ActorRef<Confirmation> replyTo) {
        this.cartId = cartId;
        this.item = item;
        this.replyTo = replyTo;
    }
}

// Command to remove an item from the cart
public final class RemoveItem implements ShoppingCartCommand {
    public final String cartId;
    public final String productId;
    public final int quantity;
    public final ActorRef<Confirmation> replyTo;

    public RemoveItem(String cartId, String productId, int quantity, ActorRef<Confirmation> replyTo) {
        this.cartId = cartId;
        this.productId = productId;
        this.quantity = quantity;
        this.replyTo = replyTo;
    }
}

// Command to get the current cart state
public final class GetCart implements ShoppingCartCommand {
    public final String cartId;
    public final ActorRef<CartState> replyTo;

    public GetCart(String cartId, ActorRef<CartState> replyTo) {
        this.cartId = cartId;
        this.replyTo = replyTo;
    }
}

// Response wrapper for the cart state
public final class CartState implements Serializable {
    public final String cartId;
    public final Map<String, CartItem> items;
    public final double totalPrice;

    public CartState(String cartId, Map<String, CartItem> items) {
        this.cartId = cartId;
        this.items = items;
        this.totalPrice = items.values().stream()
                .mapToDouble(CartItem::getTotalPrice)
                .sum();
    }
}

// Confirmation response for commands
public interface Confirmation extends Serializable {}

public final class Accepted implements Confirmation {
    public final CartState cartState;

    public Accepted(CartState cartState) {
        this.cartState = cartState;
    }
}

public final class Rejected implements Confirmation {
    public final String reason;

    public Rejected(String reason) {
        this.reason = reason;
    }
}
