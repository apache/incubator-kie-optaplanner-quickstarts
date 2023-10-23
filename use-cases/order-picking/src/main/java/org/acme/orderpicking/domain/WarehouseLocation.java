/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.acme.orderpicking.domain;

/**
 * Represents a location in the warehouse where a product can be stored. In the context of the order picking problem
 * the warehouse is modeled as set of shelvings. For picking a particular product the employees walks to the indicated
 * shelving side and row.
 * 
 * @see Warehouse
 * @see Shelving
 */
public class WarehouseLocation {

    private String shelvingId;
    private Shelving.Side side;
    private int row;

    public WarehouseLocation() {
        //marshalling constructor
    }

    public WarehouseLocation(String shelvingId, Shelving.Side side, int row) {
        this.shelvingId = shelvingId;
        this.side = side;
        this.row = row;
    }

    public String getShelvingId() {
        return shelvingId;
    }

    public void setShelvingId(String shelvingId) {
        this.shelvingId = shelvingId;
    }

    public Shelving.Side getSide() {
        return side;
    }

    public void setSide(Shelving.Side side) {
        this.side = side;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    @Override
    public String toString() {
        return "WarehouseLocation{" +
                "shelvingId='" + shelvingId + '\'' +
                ", side=" + side +
                ", row=" + row +
                '}';
    }
}
