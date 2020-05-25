package ahmet.com.eatitserver.eventBus;

import java.util.List;

import ahmet.com.eatitserver.model.Food.Food;
import ahmet.com.eatitserver.model.Food.FoodSize;

public class UpdateFood {

    private List<Food> food;

    public UpdateFood() {
    }

    public List<Food> getFood() {
        return food;
    }

    public void setFood(List<Food> food) {
        this.food = food;
    }
}
