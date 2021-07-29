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

/**
 * Represents an indivisible store article that can be included in an order, e.g. "6 milk bricks pack". In the context
 * of the order picking problem the volume of a product is measured in cm3, and every product is located in a
 * particular shelving, this information is represented by the WarehouseLocation.
 * 
 * @see WarehouseLocation
 * @see Shelving
 */
public class Product {

    private String id;
    private String name;
    /**
     * The volume of a product is measured in cm3.
     */
    private int volume;
    private WarehouseLocation location;

    public Product() {
        //marshalling constructor
    }

    public Product(String id, String name, int volume, WarehouseLocation location) {
        this.id = id;
        this.name = name;
        this.volume = volume;
        this.location = location;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

    public WarehouseLocation getLocation() {
        return location;
    }

    public void setLocation(WarehouseLocation location) {
        this.location = location;
    }

    @Override
    public String toString() {
        return "Product{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", volume=" + volume +
                ", location=" + location +
                '}';
    }
}
