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

package org.acme.factoriolayout.bootstrap;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.runtime.StartupEvent;
import org.acme.factoriolayout.domain.Area;
import org.acme.factoriolayout.domain.Assembly;
import org.acme.factoriolayout.domain.FactorioLayout;
import org.acme.factoriolayout.domain.Recipe;
import org.acme.factoriolayout.domain.RecipeInput;
import org.acme.factoriolayout.domain.Requirement;
import org.acme.factoriolayout.persistence.FactorioLayoutRepository;
import org.apache.commons.lang3.tuple.Pair;

@ApplicationScoped
public class FactorioDataGenerator {

    private static final String RECIPES_JSON_RESOURCE = "org/acme/factoriolayout/bootstrap/recipes.json";

    @Inject
    FactorioLayoutRepository factorioLayoutRepository;

    public void generateDemoData(@Observes StartupEvent startupEvent) {
        List<Recipe> recipeList = readRecipes();
        Map<String, Recipe> recipeMap = recipeList.stream().collect(Collectors.toMap(Recipe::getId, recipe -> recipe));
        List<Requirement> requirementList = buildRequirementList(recipeMap);
        List<Assembly> assemblyList = buildAssemblyList(recipeList, requirementList);
        List<Area > areaList = buildAreaList(assemblyList);
        factorioLayoutRepository.set(new FactorioLayout(recipeList, requirementList, areaList, assemblyList));
    }

    private List<Recipe> readRecipes() {
        List<Recipe> recipeList;
        ObjectMapper mapper = new ObjectMapper();
        try {
            recipeList = mapper.readValue(
                    Thread.currentThread().getContextClassLoader().getResource(RECIPES_JSON_RESOURCE),
                    mapper.getTypeFactory().constructCollectionType(List.class, Recipe.class));
        } catch (IOException e) {
            throw new IllegalStateException("Failed reading recipes JSON resource (" + RECIPES_JSON_RESOURCE + ").", e);
        }
        return recipeList;
    }

    private List<Requirement> buildRequirementList(Map<String, Recipe> recipeMap) {
        List<Requirement> requirementList = new ArrayList<>();
        requirementList.add(buildRequirement(recipeMap, "Automation_science_pack", 1));
        return requirementList;
    }

    private Requirement buildRequirement(Map<String, Recipe> recipeMap, String recipeId, long amount) {
        Recipe recipe = recipeMap.get(recipeId);
        if (recipe == null) {
            throw new IllegalArgumentException("The recipe ID (" + recipeId + ") is invalid.");
        }
        return new Requirement(recipe, amount * 1000L);
    }

    private List<Assembly> buildAssemblyList(List<Recipe> recipeList, List<Requirement> requirementList) {
        List<Assembly> globalAssemblyList = new ArrayList<>(recipeList.size() * 10);

        Map<Recipe, List<Assembly>> unsuppliedAssemblyMap = new HashMap<>(recipeList.size());
        for (Requirement requirement : requirementList) {
            Recipe recipe = requirement.getRecipe();
            long requiredMillis = requirement.getAmountMillis();
            if (requiredMillis <= 0L) {
                throw new IllegalArgumentException("The requirement (" + requirement + ") has an invalid amount.");
            }
            // As long as downstreams don't link to upstreams, these dummies won't escape this method
            Recipe dummyDownstreamRecipe = new Recipe("dummy", null, null, null);
            dummyDownstreamRecipe.getInputSet().add(new RecipeInput(recipe, requiredMillis));
            Assembly dummyDownstreamAssembly = new Assembly("dummy", dummyDownstreamRecipe);
            unsuppliedAssemblyMap.put(recipe, Collections.singletonList(dummyDownstreamAssembly));
        }
        List<Recipe> sortedRecipeList = recipeList.stream()
                .sorted(Comparator.comparing(Recipe::getLevel).reversed())
                .collect(Collectors.toList());

        long nextAssemblyId = 0L;
        for (Recipe recipe : sortedRecipeList) {
            // All higher recipes already have assemblies. This and all lower don't have assemblies yet.
            List<Assembly> downstreamAssemblyList = unsuppliedAssemblyMap.remove(recipe);
            if (downstreamAssemblyList != null) {
                long leftOverMillis = 0L;
                Assembly lastAssembly = null;
                for (Assembly downstreamAssembly : downstreamAssemblyList) {
                    RecipeInput downstreamInput = downstreamAssembly.getRecipe().getInputSet().stream()
                            .filter(recipeInput_ -> recipeInput_.getRecipe() == recipe).findFirst().get();
                    long requiredMillis = downstreamInput.getAmountMillis();
                    if (leftOverMillis > 0) {
                        downstreamAssembly.getInputAssemblyList().add(lastAssembly);
                        requiredMillis -= leftOverMillis;
                    }
                    while (requiredMillis > 0L) {
                        lastAssembly = new Assembly(recipe.getId() + "-" + (nextAssemblyId++), recipe);
                        globalAssemblyList.add(lastAssembly);
                        // Connect downstream assemblies
                        downstreamAssembly.getInputAssemblyList().add(lastAssembly);
                        requiredMillis -= recipe.getOutputAmountMillis();
                        // Request upstream assemblies
                        for (RecipeInput upstreamInput : recipe.getInputSet()) {
                            unsuppliedAssemblyMap.computeIfAbsent(upstreamInput.getRecipe(),
                                    (upstreamInput_) -> new ArrayList<>())
                                    .add(lastAssembly);
                        }
                    }
                    leftOverMillis = -requiredMillis;
                }
            }
        }
        return globalAssemblyList;
    }

    private List<Area> buildAreaList(List<Assembly> assemblyList) {
        List<Assembly> sourceAssemblyList = assemblyList.stream()
                .filter(assembly -> assembly.getInputAssemblyList().isEmpty())
                .collect(Collectors.toList());
        int halfWidth = 9;
        int height = (int) ((assemblyList.size() - sourceAssemblyList.size()) * 2.0 / 9.0) + 1;
        List<Area> areaList = new ArrayList<>(halfWidth * 2 * height + sourceAssemblyList.size());
        int nextSourceX = 1;
        for (Assembly sourceAssembly : sourceAssemblyList) {
            // Pin mined resources to the beginning of the layout
            Area area = new Area(nextSourceX, -1);
            nextSourceX = (nextSourceX > 0) ? -nextSourceX : (-nextSourceX) + 1;
            areaList.add(area);
            sourceAssembly.setArea(area);
            sourceAssembly.setPinned(true);
        }
        // x 0 is reserved for the main bus
        for (int x = 1; x <= halfWidth; x++) {
            for (int y = 0; y < height; y++) {
                areaList.add(new Area(-x, y));
                areaList.add(new Area(x, y));
            }
        }
        return areaList;
    }

}
