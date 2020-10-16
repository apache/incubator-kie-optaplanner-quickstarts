/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.acme.factoriolayout.domain;

import java.util.LinkedHashSet;
import java.util.Set;

public class Recipe {

    private String id;

    private String name;
    private String wikiUrl;
    private String imageIrl;

    private long durationMillis = -1;
    private Set<RecipeInput> inputSet = new LinkedHashSet<>(8);
    private long outputAmountMillis = -1;

//    // No-arg constructor required for Jackson
//    public Recipe() {}

    public Recipe(String id, String name, String wikiUrl, String imageIrl) {
        this.id = id;
        this.name = name;
        this.wikiUrl = wikiUrl;
        this.imageIrl = imageIrl;
    }

    @Override
    public String toString() {
        return name;
    }

    // ************************************************************************
    // Getters and setters
    // ************************************************************************

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getWikiUrl() {
        return wikiUrl;
    }

    public String getImageIrl() {
        return imageIrl;
    }

    public long getDurationMillis() {
        return durationMillis;
    }

    public void setDurationMillis(long durationMillis) {
        this.durationMillis = durationMillis;
    }

    public Set<RecipeInput> getInputSet() {
        return inputSet;
    }

    public long getOutputAmountMillis() {
        return outputAmountMillis;
    }

    public void setOutputAmountMillis(long outputAmountMillis) {
        this.outputAmountMillis = outputAmountMillis;
    }
}
