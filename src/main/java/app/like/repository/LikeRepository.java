package app.like.repository;

import app.like.model.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface LikeRepository extends JpaRepository<Like, UUID> {
    boolean existsByUser_IdAndRecipe_Id(UUID userId, UUID recipeId);
}
