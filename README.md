Akka-based Java Shopping Cart
=============================

This project demonstrates a scalable and resilient shopping cart system built with Akka Actors in Java. It leverages Akka's concurrency model to manage individual shopping cart states, ensuring high availability and responsiveness.

## Features

- **Actor-based Concurrency**: Each shopping cart is managed by a dedicated Akka Actor, allowing for independent and concurrent operations.
- **Scalability**: Designed to handle a large number of concurrent users and shopping carts.
- **Resilience**: Built with fault-tolerance in mind using Akka's supervision strategies.
- **Type-safe Messaging**: Uses Akka Typed for compile-time type safety.
- **Clean Architecture**: Separation of concerns with clear boundaries between models, commands, and actors.

## Technologies Used

- **Java 17+**: The core programming language.
- **Apache Maven**: For project management and dependency management.
- **Akka 2.8.0**: For building concurrent, distributed, and fault-tolerant applications.
- **SLF4J & Logback**: For logging.

## Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/
│   │       └── example/
│   │           └── shoppingcart/
│   │               ├── ShoppingCartApp.java         # Main application class
│   │               ├── actor/
│   │               │   ├── ShoppingCart.java        # Shopping cart actor implementation
│   │               │   └── ShoppingCartManager.java # Manages multiple shopping cart actors
│   │               └── model/                       # Message and data model classes
│   │                   ├── CartItem.java
│   │                   ├── ShoppingCartCommand.java # Command hierarchy
│   │                   └── ...
│   └── resources/
│       ├── application.conf     # Akka configuration
│       └── logback.xml          # Logging configuration
└── test/
    └── java/                   # Test classes (to be implemented)

```

Akka Actor Hierarchy
--------------------

The core of the application revolves around two main actor types:

-   **`ShoppingCartManager`**: A top-level actor responsible for managing the lifecycle of individual `ShoppingCart` actors. It receives requests to interact with specific carts and forwards them to or creates the appropriate `ShoppingCart` actor.

-   **`ShoppingCart`**: An actor representing a single user's shopping cart. It maintains the state of the items within that cart and processes commands like adding, removing, or updating items.

```
graph TD
    A[Akka Actor System] --> B(ShoppingCartManager)
    B --> C1(ShoppingCart-User1)
    B --> C2(ShoppingCart-User2)
    B --> C3(ShoppingCart-UserN)
    C1 -- Processes Commands --> D1[AddItem]
    C1 -- Processes Commands --> D2[RemoveItem]
    C1 -- Processes Commands --> D3[GetCart]
    D1 -.-> C1
    D2 -.-> C1
    D3 -.-> C1

```

Data Flow Diagram
-----------------

This diagram illustrates a simplified data flow, assuming an external client interacts with the system, possibly via a higher-level API (e.g., Akka HTTP or gRPC), which then sends messages to the `ShoppingCartManager`.

```
graph LR
    A[Client Request] --> B(API Layer)
    B --> C{ShoppingCartManager Actor}
    C -- "Create/Find CartId" --> D[ShoppingCart Actor (for CartId)]
    D -- "Update Cart State" --> E[Cart State Data]
    D -- "Respond to Manager" --> C
    C -- "Respond to API Layer" --> B
    B -- "Send Response" --> A

```

Getting Started
---------------

### Prerequisites

-   Java Development Kit (JDK) 17 or higher

-   Apache Maven 3.6 or higher

### Building the Project

Navigate to the project root directory and run:

```
mvn clean install

```

### Running the Application (Example)

You can create a main class to start the Akka Actor System and interact with it.

```
// Example Main class (not yet provided, but would go here)
public class Main {
    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create("ShoppingCartSystem");
        // ... create and interact with actors ...
    }
}

```

Usage
-----

Once the application is running, you can send messages to the `ShoppingCartManager` to interact with shopping carts. For instance, to add an item to a cart, you would send an `AddItem` command to the `ShoppingCartManager` actor.

Contributing
------------

Feel free to open issues or submit pull requests.

License
-------

This project is licensed under the MIT License - see the LICENSE file for details.