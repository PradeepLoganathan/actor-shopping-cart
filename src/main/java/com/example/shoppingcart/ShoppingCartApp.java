package com.example.shoppingcart;

import akka.actor.typed.ActorSystem;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.server.Route;
import com.example.shoppingcart.actor.ShoppingCartManager;
import com.example.shoppingcart.http.ShoppingCartRoutes;

import java.util.concurrent.CompletionStage;

public class ShoppingCartApp {

    public static void main(String[] args) {
        // The ShoppingCartManager starts sharding when it is created
        ActorSystem<ShoppingCartManager.Command> system = ActorSystem.create(ShoppingCartManager.create(), "shopping-cart-system");

        try {
            startHttpServer(system);
            System.out.println("Server online at http://localhost:8080/\nPress ENTER to stop.");
            System.in.read();
        } catch (Exception e) {
            system.log().error("Error starting the system", e);
        } finally {
            system.log().info("Shutting down...");
            system.terminate();
        }
    }

    static void startHttpServer(ActorSystem<?> system) {
        // Create the routes, passing the system
        ShoppingCartRoutes routes = new ShoppingCartRoutes(system);
        Route httpRoutes = routes.shoppingCartRoutes();

        // Bind the routes to a port
        CompletionStage<ServerBinding> futureBinding =
            Http.get(system).newServerAt("localhost", 8080).bind(httpRoutes);

        futureBinding.whenComplete((binding, exception) -> {
            if (binding != null) {
                // Server is bound
            } else {
                system.log().error("Failed to bind HTTP endpoint, terminating system", exception);
                system.terminate();
            }
        });
    }
}