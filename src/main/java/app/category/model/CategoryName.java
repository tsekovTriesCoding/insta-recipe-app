package app.category.model;

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

    private final String name;

    CategoryName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
