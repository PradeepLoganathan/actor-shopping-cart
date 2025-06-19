package com.example.shoppingcart.actor;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;
import akka.persistence.typed.PersistenceId;
import akka.persistence.typed.javadsl.*;
import com.example.shoppingcart.model.*;
import java.util.HashMap;



public class ShoppingCart extends EventSourcedBehavior<ShoppingCartCommand, ShoppingCartEvent, PersistentCartState> {
    private final String cartId;
    private final ActorContext<ShoppingCartCommand> context;

    public static Behavior<ShoppingCartCommand> create(String cartId) {
        return Behaviors.setup(context -> 
            new ShoppingCart(PersistenceId.of("ShoppingCart", cartId), cartId, context)
        );
    }

    private ShoppingCart(PersistenceId persistenceId, String cartId, ActorContext<ShoppingCartCommand> context) {
        super(persistenceId);
        this.cartId = cartId;
        this.context = context;
        context.getLog().info("Shopping cart {} started", cartId);
    }

    @Override
    public PersistentCartState emptyState() {
        return new PersistentCartState(cartId, new HashMap<>());
    }

    @Override
    public CommandHandler<ShoppingCartCommand, ShoppingCartEvent, PersistentCartState> commandHandler() {
        return newCommandHandlerBuilder()
            .forAnyState()
            .onCommand(AddItem.class, this::onAddItem)
            .onCommand(RemoveItem.class, this::onRemoveItem)
            .onCommand(GetCart.class, this::onGetCart)
            .build();
    }

    @Override
    public EventHandler<PersistentCartState, ShoppingCartEvent> eventHandler() {
        return (state, event) -> state.applyEvent(event);
    }

    private Effect<ShoppingCartEvent, PersistentCartState> onAddItem(PersistentCartState state, AddItem cmd) {
        if (cmd.item.getQuantity() <= 0) {
            cmd.replyTo.tell(new Rejected("Quantity must be positive"));
            return Effect().none();
        }
        
        ItemAdded event = new ItemAdded(cartId, cmd.item);
        return Effect()
            .persist(event)
            .thenRun(updatedState -> 
                cmd.replyTo.tell(new Accepted(updatedState.toCartState()))
            );
    }

    private Effect<ShoppingCartEvent, PersistentCartState> onRemoveItem(PersistentCartState state, RemoveItem cmd) {
        if (cmd.quantity <= 0) {
            cmd.replyTo.tell(new Rejected("Quantity must be positive"));
            return Effect().none();
        }
        
        if (!state.items.containsKey(cmd.productId)) {
            cmd.replyTo.tell(new Rejected("Product not found in cart"));
            return Effect().none();
        }
        
        ItemRemoved event = new ItemRemoved(cartId, cmd.productId, cmd.quantity);
        return Effect()
            .persist(event)
            .thenRun(updatedState -> 
                cmd.replyTo.tell(new Accepted(updatedState.toCartState()))
            );
    }

    private Effect<ShoppingCartEvent, PersistentCartState> onGetCart(PersistentCartState state, GetCart cmd) {
        cmd.replyTo.tell(state.toCartState());
        return Effect().none();
    }
}
