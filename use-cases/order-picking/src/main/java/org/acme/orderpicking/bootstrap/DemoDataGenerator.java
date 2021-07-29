/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.acme.orderpicking.bootstrap;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;

import org.acme.orderpicking.domain.Order;
import org.acme.orderpicking.domain.OrderItem;
import org.acme.orderpicking.domain.Product;
import org.acme.orderpicking.domain.Shelving;
import org.acme.orderpicking.domain.Trolley;
import org.acme.orderpicking.domain.TrolleyStep;
import org.acme.orderpicking.domain.WarehouseLocation;

/**
 * Helper class for generating data sets.
 *
 * @see ProductDB
 */
@ApplicationScoped
public class DemoDataGenerator {

    private static final int ORDER_ITEMS_SIZE_MINIMUM = 1;

    private final Random random = new Random(37);

    public List<Order> buildOrders(int size) {
        List<Product> products = buildProducts();
        return buildOrders(size, products);
    }

    public List<Trolley> buildTrolleys(int size, int bucketCount, int bucketCapacity, WarehouseLocation startLocation) {
        List<Trolley> result = new ArrayList<>(size);
        for (int i = 1; i <= size; i++) {
            result.add(new Trolley(Integer.toString(i), bucketCount, bucketCapacity, startLocation));
        }
        return result;
    }

    public List<TrolleyStep> buildTrolleySteps(List<Order> orders) {
        List<TrolleyStep> result = new ArrayList<>();
        for (Order order : orders) {
            result.addAll(buildTrolleySteps(order));
        }
        return result;
    }

    public List<TrolleyStep> buildTrolleySteps(Order order) {
        List<TrolleyStep> steps = new ArrayList<>();
        for (OrderItem item : order.getItems()) {
            TrolleyStep trolleyStep = new TrolleyStep(item);
            steps.add(trolleyStep);
        }
        return steps;
    }

    public void validateBucketCapacity(int bucketCapacity) {
        if (bucketCapacity < ProductDB.getMaxProductSize()) {
            throw new IllegalArgumentException("The selected bucketCapacity: " + bucketCapacity + ", is lower than the maximum product size: " + ProductDB.getMaxProductSize() + "." +
                    " However for a matter of simplicity the problem was simplified on the assumption that products can always fit in a trolley bucket." +
                    " Please use a higher value");
        }
    }

    private List<Order> buildOrders(int size, List<Product> products) {
        List<Order> orderList = new ArrayList<>();
        Order order;
        for (int orderNumber = 1; orderNumber <= size; orderNumber++) {
            int orderItemsSize = ORDER_ITEMS_SIZE_MINIMUM + random.nextInt(products.size() - ORDER_ITEMS_SIZE_MINIMUM);
            List<OrderItem> orderItems = new ArrayList<>();
            Set<String> orderProducts = new HashSet<>();
            order = new Order(Integer.toString(orderNumber), orderItems);
            int itemNumber = 1;
            for (int i = 0; i < orderItemsSize; i++) {
                int productItemIndex = random.nextInt(products.size());
                Product product = products.get(productItemIndex);
                if (!orderProducts.contains(product.getId())) {
                    orderItems.add(new OrderItem(Integer.toString(itemNumber++), order, product));
                    orderProducts.add(product.getId());
                }
            }
            orderList.add(order);
        }
        return orderList;
    }

    private List<Product> buildProducts() {
        return ProductDB.getProducts().stream()
                .map(productRecord -> {
                    List<String> shelvingIds = ProductDB.getShelvings(productRecord.getFamily());
                    int shelvingIndex = random.nextInt(shelvingIds.size());
                    Shelving.Side shelvingSide = Shelving.Side.values()[random.nextInt(Shelving.Side.values().length)];
                    int shelvingRow = random.nextInt(Shelving.ROWS_SIZE) + 1;
                    WarehouseLocation warehouseLocation = new WarehouseLocation(shelvingIds.get(shelvingIndex), shelvingSide, shelvingRow);
                    return new Product(productRecord.getProduct().getId(),
                            productRecord.getProduct().getName(),
                            productRecord.getProduct().getVolume(),
                            warehouseLocation);
                }).collect(Collectors.toList());
    }
}
