package com.example.shoppingcart.http;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.Scheduler;
import akka.actor.typed.javadsl.AskPattern;
import akka.cluster.sharding.typed.javadsl.ClusterSharding;
import akka.cluster.sharding.typed.javadsl.EntityRef;
import akka.cluster.sharding.typed.javadsl.EntityTypeKey;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.Route;
import static akka.http.javadsl.server.Directives.complete;
import static akka.http.javadsl.server.Directives.concat;
import static akka.http.javadsl.server.Directives.delete;
import static akka.http.javadsl.server.Directives.entity;
import static akka.http.javadsl.server.Directives.get;
import static akka.http.javadsl.server.Directives.onSuccess;
import static akka.http.javadsl.server.Directives.parameter;
import static akka.http.javadsl.server.Directives.path;
import static akka.http.javadsl.server.Directives.pathPrefix;
import static akka.http.javadsl.server.Directives.post;
import static akka.http.javadsl.server.PathMatchers.segment; // Canonical import for segment()

import com.example.shoppingcart.actor.ShoppingCartManager;
import com.example.shoppingcart.model.*;

import java.time.Duration;
import java.util.concurrent.CompletionStage;


public class ShoppingCartRoutes {

    private final ClusterSharding sharding;
    private final Duration askTimeout = Duration.ofSeconds(5);
    private final Scheduler scheduler;
    private final EntityTypeKey<ShoppingCartCommand> entityTypeKey = ShoppingCartManager.ENTITY_TYPE_KEY;

    public ShoppingCartRoutes(ActorSystem<?> system) {
        this.scheduler = system.scheduler();
        this.sharding = ClusterSharding.get(system);
    }

    // --- Private Ask-Pattern Methods ---

    private EntityRef<ShoppingCartCommand> getEntityRef(String cartId) {
        return sharding.entityRefFor(entityTypeKey, cartId);
    }

    private CompletionStage<Confirmation> addItem(String cartId, CartItem item) {
        return AskPattern.ask(getEntityRef(cartId), ref -> new AddItem(cartId, item, ref), askTimeout, scheduler);
    }

    private CompletionStage<CartState> getCart(String cartId) {
        return AskPattern.ask(getEntityRef(cartId), ref -> new GetCart(cartId, ref), askTimeout, scheduler);
    }

    private CompletionStage<Confirmation> removeItem(String cartId, String productId, int quantity) {
        return AskPattern.ask(getEntityRef(cartId), ref -> new RemoveItem(cartId, productId, quantity, ref), askTimeout, scheduler);
    }

    // --- Private Route Definitions ---

    private Route handleConfirmation(CompletionStage<Confirmation> confirmationStage) {
        return onSuccess(confirmationStage, confirmation -> {
            if (confirmation instanceof Accepted) {
                return complete(StatusCodes.OK, ((Accepted) confirmation).cartState, Jackson.marshaller());
            } else {
                return complete(StatusCodes.BAD_REQUEST, ((Rejected) confirmation).reason, Jackson.marshaller());
            }
        });
    }

    // Defines the GET /carts/{cartId} route
    private Route getRoute(String cartId) {
        return get(() ->
            onSuccess(getCart(cartId), state ->
                complete(StatusCodes.OK, state, Jackson.marshaller())
            )
        );
    }

    // Defines the POST /carts/{cartId}/items route
    private Route postRoute(String cartId) {
        return post(() ->
            path("items", () ->
                entity(Jackson.unmarshaller(CartItem.class), item ->
                    handleConfirmation(addItem(cartId, item))
                )
            )
        );
    }

    // Defines the DELETE /carts/{cartId}/items/{productId}?quantity=1 route
    private Route deleteRoute(String cartId) {
        return delete(() ->
            pathPrefix("items", () ->
                path(segment(), productId ->
                    parameter("quantity", quantity ->
                        handleConfirmation(removeItem(cartId, productId, Integer.parseInt(quantity)))
                    )
                )
            )
        );
    }

    // --- Public Top-Level Route ---

    public Route shoppingCartRoutes() {
        return pathPrefix("carts", () ->
            pathPrefix(segment(), cartId ->
                concat(
                    getRoute(cartId),
                    postRoute(cartId),
                    deleteRoute(cartId)
                )
            )
        );
    }
}
