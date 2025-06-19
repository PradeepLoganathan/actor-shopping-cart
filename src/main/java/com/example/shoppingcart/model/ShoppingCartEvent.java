package com.example.shoppingcart.model;

import java.io.Serializable;

// Marker interface for all shopping cart events
// This interface is used to tag all events related to the shopping cart,
// allowing for type-safe handling of events in the shopping cart system.
// It extends Serializable to ensure that events can be serialized for persistence or messaging.
public interface ShoppingCartEvent extends Serializable {}

