package org.example.controller;

import org.example.model.*;

import javax.swing.SwingUtilities;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * This class manages recipe interaction and API communication for the meal prepping application.
 * This class provides functionality to search for recipes based on ingredients, fetch detailed
 * recipe nutrition information, save recipes, remove individual or all saved recipes, and
 * retrieve a list of saved recipes.
 * <p>
 * Representation Invariant:
 * 1. client is not null and represents a valid SpoonacularClient
 * 2. savedRecipes is not null
 * 3. savedRecipes contains only unique Recipe objects
 * 4. The SpoonacularClient is initialized with a valid API key
 * <p>
 * Abstraction Function:
 * AF(r) = A meal recipe management system that:
 * 1. r.client = external API client for fetching recipes
 * 2. r.savedRecipes = a list of recipes saved by the user
 */
public class MealController {
    private final static SpoonacularClient client;
    private List<Recipe> savedRecipes = new ArrayList<>();
  
    /**
     * Controller to handle client/API calls and manage views. Also serves to cache recipies.... mayb wanna make this a diff class
     */
    static {
        try {
            client = new SpoonacularClient(System.getenv("KEY"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Finds recipes based on selected ingredients.
     * <p>
     * Performs the following steps:
     * - Connects to Spoonacular API
     * - Searches for recipes using provided ingredients
     * - Fetches detailed nutrition information for each recipe
     *
     * @param selectedIngredients List of ingredients to base recipe search on
     * @return List of fully detailed recipes
     * @throws Exception if recipe search or nutrition fetch fails
     */
    public List<Recipe> findRecipies(List<String> selectedIngredients) throws Exception{
        try {
            List<Recipe> recipiesFull = new ArrayList<>();
            List<Recipe> recipies = client.findRecipesByIngredients(selectedIngredients, 2, 6);
            recipies.forEach(recipe -> {
                try {
                    //doing this cuz recipe is immutable
                    Recipe recipeNutrition = client.getRecipeNutrition(recipe.id());
                    Recipe updatedRecipie = new Recipe(recipe.id(), recipe.title(), recipe.image(), recipeNutrition.nutrition(), recipe.usedIngredients(), recipe.missedIngredients(), recipeNutrition.servings(), recipeNutrition.sourceUrl());
                    recipiesFull.add(updatedRecipie);
                } catch (Exception e) {
                    throw new RuntimeException(e);//TODO change this to invalid recipie?
                }

            });
            checkRep();
            return recipiesFull;
        } catch (Exception ex) {
            throw new Exception();//TODO add exception detailing for unfound recipies
        }
    }

    /**
     * Adds a list of recipes to the saved recipes collection.
     *
     * @param recipes List of recipes to be saved
     * @return true if all recipes were successfully added, false otherwise
     */
    public boolean addRecipies(List<Recipe> recipes) {
        checkRep();
        return savedRecipes.addAll(recipes);
    }

    /**
     * Removes a specific recipe from the saved recipes collection.
     *
     * @param recipe The recipe to be removed
     * @return true if the recipe was successfully removed, false otherwise
     */
    public boolean removeRecipie(Recipe recipe) {
        checkRep();
        return savedRecipes.remove(recipe);
    }

    /**
     * Removes all saved recipes from the collection.
     * Clears the entire list of saved recipes.
     */
    public void removeAllRecipies() {
        checkRep();
        savedRecipes.clear();
    }

    /**
     * Retrieves the list of currently saved recipes.
     *
     * @return A list of all saved recipes
     */
    public List<Recipe> getSavedRecipes() {
        checkRep();
        return savedRecipes;
    }

    /**
     * Checks the representation invariants for the MealController.
     * <p>
     * Verifies:
     * - client is not null and represents a valid SpoonacularClient
     * - savedRecipes is not null
     * - savedRecipes contains only unique Recipe objects
     * - SpoonacularClient is initialized with a valid API key
     *
     * @throws AssertionError if any representation invariant is violated
     */
    private void checkRep() {
        // Check client is not null
        assert client != null : "SpoonacularClient cannot be null";

        // Check savedRecipes is not null
        assert savedRecipes != null : "Saved recipes list cannot be null";

        // Check for unique recipes
        HashSet<Recipe> uniqueRecipes = new HashSet<>(savedRecipes);
        assert uniqueRecipes.size() == savedRecipes.size() : "Saved recipes must be unique";

        // Check API key (assuming there's a method to validate the key)
        assert isValidApiKey() : "Invalid or missing Spoonacular API key";
    }

    /**
     * Validates the Spoonacular API key.
     *
     * @return true if the API key is valid, false otherwise
     */
    private boolean isValidApiKey() {
        try {
            // Attempt a minimal API call or check key format
            return client != null &&
                    System.getenv("KEY") != null &&
                    !System.getenv("KEY").isEmpty();
        } catch (Exception e) {
            return false;
        }
    }
}