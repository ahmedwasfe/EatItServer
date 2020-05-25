package ahmet.com.eatitserver.eventBus;

import ahmet.com.eatitserver.model.Food.Food;

public class FoodClick {

    private boolean success;
    private Food food;

    public FoodClick(boolean success, Food food) {
        this.success = success;
        this.food = food;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Food getFood() {
        return food;
    }

    public void setFood(Food food) {
        this.food = food;
    }
}
