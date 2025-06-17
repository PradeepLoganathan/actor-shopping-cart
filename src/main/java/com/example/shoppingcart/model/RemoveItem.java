package com.example.shoppingcart.model;

import akka.actor.typed.ActorRef;

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