package com.example.shoppingcart.actor;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;
import akka.actor.typed.receptionist.Receptionist;
import akka.actor.typed.receptionist.ServiceKey;
import akka.cluster.sharding.typed.javadsl.ClusterSharding;
import akka.cluster.sharding.typed.javadsl.Entity;
import akka.cluster.sharding.typed.javadsl.EntityTypeKey;
import com.example.shoppingcart.model.*;

public class ShoppingCartManager extends AbstractBehavior<ShoppingCartManager.Command> {
    // Public commands that the manager can handle
    public interface Command {}

    // Command to forward to the appropriate shopping cart
    public static final class ForwardToCart implements Command {
        public final String cartId;
        public final ShoppingCartCommand command;
        public final ActorRef<Object> replyTo;

        public ForwardToCart(String cartId, ShoppingCartCommand command, ActorRef<Object> replyTo) {
            this.cartId = cartId;
            this.command = command;
            this.replyTo = replyTo;
        }
    }

    // Command to handle listing all shopping carts (for demonstration)
    public static final class ListCarts implements Command {
        public final ActorRef<Object> replyTo;

        public ListCarts(ActorRef<Object> replyTo) {
            this.replyTo = replyTo;
        }
    }

    // Service key for discovering shopping cart managers
    public static final ServiceKey<Command> SERVICE_KEY = 
        ServiceKey.create(Command.class, "shopping-cart-manager");
        
    // Entity type key for sharding
    public static final EntityTypeKey<ShoppingCartCommand> ENTITY_TYPE_KEY =
        EntityTypeKey.create(ShoppingCartCommand.class, "ShoppingCart");

    public static Behavior<Command> create() {
        return Behaviors.setup(ShoppingCartManager::new);
    }
    
    private final ActorContext<Command> context;
    private final ClusterSharding sharding;

    private ShoppingCartManager(ActorContext<Command> context) {
        super(context);
        this.context = context;
        
        // Initialize Cluster Sharding
        this.sharding = ClusterSharding.get(context.getSystem());
        
        // Register the entity type with the sharding system
        sharding.init(
            Entity.of(
                ENTITY_TYPE_KEY,
                entityContext -> ShoppingCart.create(entityContext.getEntityId())
            )
        );
        
        // Register this manager with the receptionist
        context.getSystem().receptionist()
            .tell(Receptionist.register(SERVICE_KEY, context.getSelf()));
        
        context.getLog().info("Shopping Cart Manager started");
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
            .onMessage(ForwardToCart.class, this::onForwardToCart)
            .onMessage(ListCarts.class, this::onListCarts)
            .build();
    }

    private Behavior<Command> onForwardToCart(ForwardToCart cmd) {
        // Get or create the entity ref for the cart
        ActorRef<ShoppingCartCommand> cartEntity = sharding.entityRefFor(
            ENTITY_TYPE_KEY, 
            cmd.cartId
        );
        
        // Forward the command to the entity
        cartEntity.tell(cmd.command);
        return this;
    }

    private Behavior<Command> onListCarts(ListCarts cmd) {
        // In a real application, you might want to query the shard regions
        // or use Akka Persistence Query to get a list of active carts
        context.getLog().info("Listing all shopping carts is not fully implemented in this example");
        cmd.replyTo.tell("List carts functionality would go here");
        return this;
    }
}
