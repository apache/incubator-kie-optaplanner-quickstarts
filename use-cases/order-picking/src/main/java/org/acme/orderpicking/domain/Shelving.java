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
 * Represents the products container. In the order picking problem the warehouse is represented as a set of shelvings
 * that are organized into columns and rows. Each shelving has two sides where the products can be stored, and
 * a number of rows.
 * 
 * @see Warehouse
 */
public class Shelving {

    public static final int ROWS_SIZE = 10;

    /**
     * Available shelving sides where the products can be located.
     */
    public enum Side {
        LEFT,
        RIGHT
    }

    private String id;

    /**
     * Absolute x position of the shelving's left bottom corner within the warehouse.
     */
    private int x;
    /**
     * Absolute y position of the shelving's left bottom corner within the warehouse.
     */
    private int y;

    Shelving(String id, int x, int y) {
        this.id = id;
        this.x = x;
        this.y = y;
    }

    public static String newShelvingId(Warehouse.Column column, Warehouse.Row row) {
        return "(" + column.toString() + "," + row.toString() + ")";
    }

    public String getId() {
        return id;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
