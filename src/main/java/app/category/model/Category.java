package app.category.model;

import app.recipe.model.Recipe;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    @Enumerated(EnumType.STRING)
    private CategoryName name;

    @Column(nullable = false)
    private String imageUrl;

    @Column(length = 800)
    private String description;

    @ManyToMany(mappedBy = "categories")
    private List<Recipe> recipes;

    public Category(UUID id, CategoryName name) {
        this.id = id;
        this.name = name;
    }
}
