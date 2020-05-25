package ahmet.com.eatitserver.eventBus;

public class ChangeMenuClick {

    private boolean isFromFood;

    public ChangeMenuClick(boolean isFromFood) {
        this.isFromFood = isFromFood;
    }

    public boolean isFromFood() {
        return isFromFood;
    }

    public void setFromFood(boolean fromFood) {
        isFromFood = fromFood;
    }
}
