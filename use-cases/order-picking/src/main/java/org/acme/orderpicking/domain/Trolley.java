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

public class Trolley extends PathElement {

    private int id;
    private int bucketCount;
    private int bucketCapacity;

    public Trolley() {
        //marshalling constructor
    }

    public Trolley(int id, int bucketCount, int bucketCapacity) {
        this.id = id;
        this.bucketCount = bucketCount;
        this.bucketCapacity = bucketCapacity;
    }

    public Trolley(int id, int bucketCount, int bucketCapacity, Location location) {
        super(location);
        this.id = id;
        this.bucketCount = bucketCount;
        this.bucketCapacity = bucketCapacity;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
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
}
