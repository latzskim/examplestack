@org.springframework.modulith.ApplicationModule(
    displayName = "Order",
    allowedDependencies = {"shared", "catalog", "cart", "inventory", "identity"}
)
package com.simpleshop.order;
