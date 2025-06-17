package com.example.shoppingcart.model;

import akka.actor.typed.ActorRef;

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