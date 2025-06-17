package com.example.shoppingcart;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AskPattern;
import akka.actor.typed.javadsl.Behaviors;
import akka.cluster.typed.Cluster;
import com.example.shoppingcart.actor.ShoppingCartManager;
import com.example.shoppingcart.model.*;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class ShoppingCartApp {
    private final ActorSystem<ShoppingCartManager.Command> system;
    private final ActorRef<ShoppingCartManager.Command> cartManager;
    private final Duration timeout = Duration.ofSeconds(5);
    private final String cartId = "user-1-cart";

    public static void main(String[] args) {
        ShoppingCartApp app = new ShoppingCartApp();
        app.runExample()
           .thenRun(app::shutdown);
    }

    public ShoppingCartApp() {
        // Initialize the ActorSystem with the root behavior
        this.system = ActorSystem.create(createRootBehavior(), "shopping-cart-system");
        this.cartManager = system;
        
        // Initialize the Cluster
        Cluster cluster = Cluster.get(system);
        system.log().info("Started [{}], cluster.selfAddress(): {}", 
            system.printTree(), cluster.selfMember().address());
    }
    
    private Behavior<ShoppingCartManager.Command> createRootBehavior() {
        return Behaviors.setup(context -> {
            // Create the shopping cart manager
            ActorRef<ShoppingCartManager.Command> manager = 
                context.spawn(ShoppingCartManager.create(), "shopping-cart-manager");
                
            return Behaviors.receive(ShoppingCartManager.Command.class)
                .onMessage(ShoppingCartManager.Command.class, command -> {
                    manager.tell(command);
                    return Behaviors.same();
                })
                .build();
        });
    }

    private CompletionStage<Void> runExample() {
        // Create some test items
        CartItem book1 = new CartItem("book-1", "Akka in Action", 42.0, 1);
        CartItem book2 = new CartItem("book-2", "Programming in Scala", 39.99, 1);
        
        // Create reply adapters for each type
        ActorRef<Confirmation> confirmationReplyTo = system.ignoreRef();
        ActorRef<CartState> cartStateReplyTo = system.ignoreRef();
        
        // Chain operations using CompletableFuture
        return addItemToCart(book1, confirmationReplyTo)
            .thenCompose(ignored -> addItemToCart(book2, confirmationReplyTo))
            .thenCompose(ignored -> getCartState(cartStateReplyTo))
            .thenCompose(ignored -> removeItemFromCart("book-1", 1, confirmationReplyTo))
            .thenCompose(ignored -> getCartState(cartStateReplyTo))
            .exceptionally(throwable -> {
                system.log().error("Error in shopping cart operations", throwable);
                return null;
            });
    }
    
    private CompletionStage<Void> addItemToCart(CartItem item, ActorRef<Confirmation> replyTo) {
        system.log().info("Adding item to cart: {}", item);
        return sendCommand(new ShoppingCartManager.ForwardToCart(
            cartId,
            new AddItem(cartId, item, replyTo),
            system.ignoreRef()
        )).thenAccept(response -> 
            system.log().info("Add item response: {}", response)
        );
    }
    
    private CompletionStage<Void> removeItemFromCart(String productId, int quantity, ActorRef<Confirmation> replyTo) {
        system.log().info("Removing item from cart: {} (quantity: {})", productId, quantity);
        return sendCommand(new ShoppingCartManager.ForwardToCart(
            cartId,
            new RemoveItem(cartId, productId, quantity, replyTo),
            system.ignoreRef()
        )).thenAccept(response -> 
            system.log().info("Remove item response: {}", response)
        );
    }
    
    private CompletionStage<Void> getCartState(ActorRef<CartState> replyTo) {
        system.log().info("Getting cart state...");
        return sendCommand(new ShoppingCartManager.ForwardToCart(
            cartId,
            new GetCart(cartId, replyTo),
            system.ignoreRef()
        )).thenAccept(state -> 
            system.log().info("Current cart state: {}", state)
        );
    }
    
    private <T> CompletionStage<T> sendCommand(ShoppingCartManager.ForwardToCart command) {
        CompletableFuture<T> future = new CompletableFuture<>();
        cartManager.tell(command);
        // In a real app, you'd use AskPattern.ask to get a proper response
        return (CompletionStage<T>) CompletableFuture.completedFuture(null);
    }
    
    private void shutdown() {
        system.log().info("Shutting down...");
        system.terminate();
    }
}
