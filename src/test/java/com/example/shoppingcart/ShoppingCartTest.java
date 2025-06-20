package com.example.shoppingcart;

import akka.actor.testkit.typed.javadsl.TestKitJunitResource;
import akka.actor.typed.ActorRef;
import com.example.shoppingcart.actor.ShoppingCart;
import com.example.shoppingcart.actor.ShoppingCartManager;
import com.example.shoppingcart.model.*;

import org.junit.Test;



import org.junit.ClassRule;
import static org.junit.Assert.*;

import com.typesafe.config.ConfigFactory;

public class ShoppingCartTest {

    @ClassRule
    public static final TestKitJunitResource testKit = new TestKitJunitResource(ConfigFactory.load("test-akka.conf"));

    @Test
    public void testAddItemToCart() {
        // Create a test probe
        var probe = testKit.createTestProbe(Confirmation.class);
        
        // Create a shopping cart actor
        var cart = testKit.spawn(ShoppingCart.create("test-cart-1"));
        
        // Create a test item
        CartItem item = new CartItem("p1", "Test Product", 19.99, 2);
        
        // Send AddItem command
        cart.tell(new AddItem("test-cart-1", item, probe.getRef()));
        
        // Expect a confirmation
        Confirmation response = probe.receiveMessage();
        assertTrue(response instanceof Accepted);
        
        // Verify the cart state
        CartState state = ((Accepted) response).cartState;
        assertEquals(1, state.items.size());
        assertTrue(state.items.containsKey("p1"));
        assertEquals(2, state.items.get("p1").getQuantity());
        assertEquals(39.98, state.totalPrice, 0.01);
    }
    
    @Test
    public void testRemoveItemFromCart() {
        // Create test probes
        var probe = testKit.createTestProbe(Confirmation.class);
        
        // Create a shopping cart actor
        var cart = testKit.spawn(ShoppingCart.create("test-cart-2"));
        
        // Add an item first
        CartItem item = new CartItem("p1", "Test Product", 19.99, 3);
        cart.tell(new AddItem("test-cart-2", item, probe.getRef()));
        probe.expectMessageClass(Accepted.class);
        
        // Remove some quantity
        cart.tell(new RemoveItem("test-cart-2", "p1", 2, probe.getRef()));
        
        // Expect a confirmation
        Confirmation response = probe.receiveMessage();
        assertTrue(response instanceof Accepted);
        
        // Verify the cart state
        CartState state = ((Accepted) response).cartState;
        assertEquals(1, state.items.size());
        assertEquals(1, state.items.get("p1").getQuantity());
    }
    
    @Test
    public void testGetCart() {
        // Create test probes
        var confirmationProbe = testKit.createTestProbe(Confirmation.class);
        var cartStateProbe = testKit.createTestProbe(CartState.class);
        
        // Create a shopping cart manager
        var manager = testKit.spawn(ShoppingCartManager.create());
        
        // Add an item to a cart
        CartItem item = new CartItem("p1", "Test Product", 19.99, 1);

        manager.tell(new ShoppingCartManager.ForwardToCart(
            "test-cart-3",
            new AddItem("test-cart-3", item, confirmationProbe.getRef()),
            null
        ));
        
        // Get the cart
        manager.tell(new ShoppingCartManager.ForwardToCart(
            "test-cart-3",
            new GetCart("test-cart-3", cartStateProbe.getRef()),
            null
        ));
        
        // Verify the cart state
        CartState state = cartStateProbe.receiveMessage();
        assertEquals(1, state.items.size());
        assertTrue(state.items.containsKey("p1"));
    }
}
