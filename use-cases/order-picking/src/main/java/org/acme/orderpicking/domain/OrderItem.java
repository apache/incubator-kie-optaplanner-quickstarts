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

package org.acme.orderpicking.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * In the order picking problem context, the order item represents an indivisible product that was added to the order.
 * 
 * @see Product
 */
public class OrderItem {

    private String id;
    @JsonIgnore
    private Order order;
    private Product product;

    public OrderItem() {
        //marshalling constructor
    }

    public OrderItem(String id, Order order, Product product) {
        this.id = id;
        this.order = order;
        this.product = product;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public int getVolume() {
        return product.getVolume();
    }

    /**
     * Helper method, facilitates UI building.
     */
    public String getOrderId() {
        return order != null ? order.getId() : null;
    }

    @Override
    public String toString() {
        return "OrderItem{" +
                "id='" + id + '\'' +
                ", order=" + order +
                ", product=" + product +
                '}';
    }
}
