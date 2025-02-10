package app.favorite;

import app.web.dto.FavoriteRecipe;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FavoriteServiceClient {
    //TODO: I can use REstClient instead of the WebClient,because I use it synchronously
    private final WebClient webClient;
    private final String FAVORITES_SERVICE_URL = "http://localhost:8081/api/favorites";

    public FavoriteServiceClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl(FAVORITES_SERVICE_URL).build();
    }

    public List<UUID> getFavoriteRecipeIds(UUID userId) {
        try {
            List<FavoriteRecipe> response = webClient.get()
                    .uri("/{userId}", userId)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<FavoriteRecipe>>() {})
                    .block();

            return response.stream()
                    .map(FavoriteRecipe::getRecipeId)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error fetching favorite recipes: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public boolean addFavorite(UUID userId, UUID recipeId) {
        try {
            webClient.post()
                    .uri("/{userId}/{recipeId}", userId, recipeId)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
            return true;
        } catch (Exception e) {
            System.err.println("Error adding favorite: " + e.getMessage());
            return false;
        }
    }

    public boolean removeFavorite(UUID userId, UUID recipeId) {
        try {
            webClient.delete()
                    .uri("/{userId}/{recipeId}", userId, recipeId)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
            return true;
        } catch (Exception e) {
            System.err.println("Error removing favorite: " + e.getMessage());
            return false;
        }
    }
}