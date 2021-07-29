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
 * Represents the trolley that will be filled with the order items.
 * 
 * @see TrolleyStep for more information about the model constructed by the Solver.
 */
public class Trolley extends TrolleyOrTrolleyStep {

    private String id;
    private int bucketCount;
    private int bucketCapacity;
    private WarehouseLocation location;

    public Trolley() {
        //marshalling constructor
    }

    public Trolley(String id, int bucketCount, int bucketCapacity, WarehouseLocation location) {
        this.id = id;
        this.bucketCount = bucketCount;
        this.bucketCapacity = bucketCapacity;
        this.location = location;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getBucketCount() {
        return bucketCount;
    }

    public void setBucketCount(int bucketCount) {
        this.bucketCount = bucketCount;
    }

    public int getBucketCapacity() {
        return bucketCapacity;
    }

    public void setBucketCapacity(int bucketCapacity) {
        this.bucketCapacity = bucketCapacity;
    }

    @Override
    public WarehouseLocation getLocation() {
        return location;
    }

    public void setLocation(WarehouseLocation location) {
        this.location = location;
    }
}
