/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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

package org.acme.factoriolayout.rest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.acme.factoriolayout.domain.Recipe;
import org.acme.factoriolayout.domain.RecipeInput;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * To update the recipes, first call http://localhost:8080/wikiConnector/scrape
 * Then copy the recipes.json file from target/... to src/main/resources/...
 */
@Path("wikiConnector")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class WikiConnectorResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(WikiConnectorResource.class);
    private static final List<String> IGNORE_RECIPE_IDS = Arrays.asList(
            // Not materials
            "Blueprint", "Deconstruction_planner", "Upgrade_planner", "Blueprint_book",
            // No use of barrels: fluid pipes only
            "Crude_oil_barrel", "Heavy_oil_barrel", "Light_oil_barrel", "Lubricant_barrel",
            "Petroleum_gas_barrel", "Sulfuric_acid_barrel", "Water_barrel",
            "Empty_barrel",
            // Replaced by Uranium_processing
            "Uranium-235", "Uranium-238",
            // No exotic nuclear cyclic recipes
            "Kovarex_enrichment_process", "Nuclear_fuel_reprocessing",
            // Requires launching a rocket
            "Space_science_pack");

    private static final String WIKI_URL = "https://wiki.factorio.com";

    @GET
    @Path("scrape")
    public String scrape() {
        List<Recipe> recipeList = fetchRecipes();
        calculateLevel(recipeList);
        ObjectMapper mapper = new ObjectMapper();
        File jsonFile = Paths.get("recipes.json").toFile();
        try {
            mapper.writeValue(jsonFile, recipeList);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot write jsonFile (" + jsonFile + ").", e);
        }
        return "Updated recipes json stored to " + jsonFile.getAbsolutePath() + ".\n"
        + "Copy it from target to src/main/resources/org/acme/factoriolayout/bootstrap/recipes.json";
    }

    private List<Recipe> fetchRecipes() {
        List<Recipe> recipeList = parseRecipesTOC();
        Map<String, Recipe> recipeMap = recipeList.stream().collect(Collectors.toMap(Recipe::getId, recipe -> recipe));
        for (Recipe recipe : recipeList) {
            parseRecipe(recipeMap, recipe);
        }
        return recipeList;
    }

    private List<Recipe> parseRecipesTOC() {
        Document document;
        String recipesUrl = WIKI_URL + "/Materials_and_recipes";
        try {
            document = Jsoup.connect(recipesUrl).get();
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read Wiki URL (" + recipesUrl + ").", e);
        }
        Elements materialElements = document.select(".inventory .factorio-icon a");
        List<Recipe> recipeList = new ArrayList<>(materialElements.size());
        for (Element materialElement : materialElements) {
            String id = materialElement.attr("href").replaceFirst("^/", "");
            if (IGNORE_RECIPE_IDS.contains(id)) {
                continue;
            }
            String name = materialElement.attr("title");
            String imageUrl = WIKI_URL + materialElement.selectFirst("img").attr("src");
            String wikiUrl = WIKI_URL + materialElement.attr("href");
            recipeList.add(new Recipe(id, name, wikiUrl, imageUrl));
        }
        return recipeList;
    }

    private void parseRecipe(Map<String, Recipe> recipeMap, Recipe recipe) {
        Document document;
        try {
            document = Jsoup.connect(recipe.getWikiUrl()).get();
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read Wiki URL (" + recipe.getWikiUrl() + ").", e);
        }
        // First is "Normal mode", second is "Expensive mode"
        Element recipeElement = document.selectFirst(".infobox tr:contains(Recipe) + tr");
        if (recipeElement == null) {
            // Mined element
            recipe.setDurationMillis(1000L);
            recipe.setOutputAmountMillis(1_000_000_000_000L);
        } else {
            Elements materialElements = recipeElement.select(".factorio-icon");
            boolean processed = false;
            for (Element materialElement : materialElements) {
                String id = materialElement.selectFirst("a").attr("href").replaceFirst("^/", "");
                String amountString = materialElement.select(".factorio-icon-text").text()
                        .replaceFirst("k", "000");
                long amountMillis = (long) (Double.parseDouble(amountString) * 1000.0);
                if (!processed) {
                    if (id.equals("Time")) {
                        if (recipe.getDurationMillis() >= 0) {
                            throw new IllegalStateException(
                                    "Recipe (" + recipe.getId() + ") has multiple time inputs.");
                        }
                        recipe.setDurationMillis(amountMillis);
                    } else {
                        if (id.matches("Uranium-23\\d")) {
                            // TODO fix the model to allow to deliver both (now one output is thrown away)
                            if (id.equals("Uranium-235")) {
                                amountMillis = (long) ((double) amountMillis / 0.993);
                            } else if (id.equals("Uranium-238")) {
                                amountMillis = (long) ((double) amountMillis / 0.007);
                            } else {
                                throw new IllegalStateException(
                                        "Recipe (" + recipe.getId() + ") has an invalid input (" + id + ").");
                            }
                            id = "Uranium_processing";
                        }
                        Recipe inputRecipe = recipeMap.get(id);
                        if (inputRecipe == null) {
                            LOGGER.warn("Wiki parsing of recipe ({}) has non-existing ingredient ({}).", recipe.getId(), id);
                        } else {
                            recipe.getInputSet().add(new RecipeInput(inputRecipe, amountMillis));
                        }
                    }
                    processed = materialElement.nextSibling().toString().matches("\\s+→\\s+");
                } else {
                    if (id.equals(recipe.getId())) {
                        recipe.setOutputAmountMillis(amountMillis);
                    } else if (recipe.getId().equals("Uranium_processing")
                            && id.matches("Uranium-23\\d")) {
                        recipe.setOutputAmountMillis(1000L);
                    } else {
                        throw new IllegalStateException(
                                "Recipe (" + recipe.getId() + ") has an invalid output (" + id + ").");
                    }
                }
            }
        }
        if (recipe.getOutputAmountMillis() < 0) {
            throw new IllegalStateException(
                    "Recipe (" + recipe.getId() + ") has no output amount.");
        }
        LOGGER.info("Parsed recipe ({}): {}ms + {} -> ?.", recipe.getId(),
                recipe.getDurationMillis(),
                recipe.getInputSet().isEmpty() ? "mining"
                : recipe.getInputSet().stream()
                        .map(recipeInput ->  (recipeInput.getAmountMillis() / 1000.0) + " " + recipeInput.getRecipe().getId())
                        .collect(Collectors.joining(" + ")));
    }

    private void calculateLevel(List<Recipe> recipeList) {
        int nextLevel = 0;
        List<Recipe> unprocessedRecipeList = new ArrayList<>(recipeList);
        Predicate<Recipe> predicate = (recipe) -> recipe.getLevel() < 0
                && recipe.getInputSet().stream().allMatch(
                recipeInput -> recipeInput.getRecipe().getLevel() >= 0);
        Optional<Recipe> recipeRef = unprocessedRecipeList.stream().filter(predicate).findFirst();
        while (recipeRef.isPresent()) {
            Recipe recipe = recipeRef.get();
            recipe.setLevel(nextLevel++);
            unprocessedRecipeList.remove(recipe);
            recipeRef = unprocessedRecipeList.stream().filter(predicate).findFirst();
        }
        if (!unprocessedRecipeList.isEmpty()) {
            throw new IllegalArgumentException("The unprocessedRecipeList (" + unprocessedRecipeList
                    + ") should be empty by now.");
        }
    }

}
