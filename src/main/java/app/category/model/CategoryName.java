package app.category.model;

import lombok.Getter;

@Getter
public enum CategoryName {
    VEGAN("Vegan"),
    DESSERTS("Desserts"),
    APPETIZERS("Appetizers"),
    MAIN_COURSE("Main Course"),
    BEVERAGES("Beverages"),
    SNACKS("Snacks"),
    SOUPS("Soups"),
    SALADS("Salads"),
    ;

    private final String value;

    CategoryName(String value) {
        this.value = value;
    }
}
