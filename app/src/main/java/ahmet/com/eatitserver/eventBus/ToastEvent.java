package ahmet.com.eatitserver.eventBus;

public class ToastEvent {

    private boolean isUpdate;
    private boolean isFromFood;


    public ToastEvent(boolean isUpdate, boolean isFromFood) {
        this.isUpdate = isUpdate;
        this.isFromFood = isFromFood;
    }

    public boolean isUpdate() {
        return isUpdate;
    }

    public void setUpdate(boolean update) {
        isUpdate = update;
    }

    public boolean isFromFood() {
        return isFromFood;
    }

    public void setFromFood(boolean fromFood) {
        isFromFood = fromFood;
    }
}
