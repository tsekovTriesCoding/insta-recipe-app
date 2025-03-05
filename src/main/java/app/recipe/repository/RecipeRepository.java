package app.recipe.repository;

import app.recipe.model.Recipe;
import app.user.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, UUID> {
    List<Recipe> findAllByCreatedBy(User createdBy);

    Page<Recipe> findAllByTitleContainingIgnoreCase(String query, Pageable pageable);

    List<Recipe> findAllByIdIn(Collection<UUID> ids);
}
