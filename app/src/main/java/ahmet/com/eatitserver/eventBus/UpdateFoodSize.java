package ahmet.com.eatitserver.eventBus;

import java.util.List;

import ahmet.com.eatitserver.model.Food.FoodSize;

public class UpdateFoodSize {

    private List<FoodSize> foodSizeList;

    public UpdateFoodSize() {
    }

    public UpdateFoodSize(List<FoodSize> foodSizeList) {
        this.foodSizeList = foodSizeList;
    }

    public List<FoodSize> getFoodSizeList() {
        return foodSizeList;
    }

    public void setFoodSizeList(List<FoodSize> foodSizeList) {
        this.foodSizeList = foodSizeList;
    }
}
