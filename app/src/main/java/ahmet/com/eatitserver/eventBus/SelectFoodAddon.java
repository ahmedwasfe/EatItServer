package ahmet.com.eatitserver.eventBus;

import ahmet.com.eatitserver.model.Food.Addon;

public class SelectFoodAddon {

    private Addon addon;

    public SelectFoodAddon(Addon addon) {
        this.addon = addon;
    }

    public Addon getAddon() {
        return addon;
    }

    public void setAddon(Addon addon) {
        this.addon = addon;
    }
}
