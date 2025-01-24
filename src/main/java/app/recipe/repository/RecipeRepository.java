package app.recipe.repository;

import app.recipe.model.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, UUID> {
    boolean existsByTitle(String title);

    List<Recipe> getAllByTitle(String title);
}
