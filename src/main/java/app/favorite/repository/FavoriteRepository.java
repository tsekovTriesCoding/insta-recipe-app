package app.favorite.repository;

import app.favorite.model.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, UUID> {
    Optional<Favorite> findByUserIdAndRecipeId(UUID userId, UUID recipeId);

    List<Favorite> findAllByUser_Id(UUID userId);
}
