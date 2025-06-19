#!/bin/bash

# Clean and compile
mvn clean compile

# Create classpath with all dependencies
mvn dependency:build-classpath -Dmdep.outputFile=target/classpath.txt

# Run the application with JVM args
java \
  --add-opens=java.base/java.nio=ALL-UNNAMED \
  --add-opens=java.base/sun.nio.ch=ALL-UNNAMED \
  --add-opens=java.base/java.io=ALL-UNNAMED \
  --add-exports=java.base/sun.nio.ch=ALL-UNNAMED \
  -cp target/classes:$(cat target/classpath.txt) \
  com.example.shoppingcart.ShoppingCartApp 