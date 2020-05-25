package ahmet.com.eatitserver.eventBus;

import java.util.List;

import ahmet.com.eatitserver.model.Food.Addon;

public class UpdateFoodAddon {

    private List<Addon> foodAddonList;

    public UpdateFoodAddon() {
    }

    public UpdateFoodAddon(List<Addon> foodAddonList) {
        this.foodAddonList = foodAddonList;
    }

    public List<Addon> getFoodAddonList() {
        return foodAddonList;
    }

    public void setFoodAddonList(List<Addon> foodAddonList) {
        this.foodAddonList = foodAddonList;
    }
}
