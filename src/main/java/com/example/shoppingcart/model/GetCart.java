package com.example.shoppingcart.model;

import akka.actor.typed.ActorRef;

public final class GetCart implements ShoppingCartCommand {
    public final String cartId;
    public final ActorRef<CartState> replyTo;

    public GetCart(String cartId, ActorRef<CartState> replyTo) {
        this.cartId = cartId;
        this.replyTo = replyTo;
    }
} 