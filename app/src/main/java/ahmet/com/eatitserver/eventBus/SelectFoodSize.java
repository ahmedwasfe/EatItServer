package ahmet.com.eatitserver.eventBus;

import ahmet.com.eatitserver.model.Food.FoodSize;

public class SelectFoodSize {

    private FoodSize foodSize;

    public SelectFoodSize(FoodSize foodSize) {
        this.foodSize = foodSize;
    }

    public FoodSize getFoodSize() {
        return foodSize;
    }

    public void setFoodSize(FoodSize foodSize) {
        this.foodSize = foodSize;
    }
}
