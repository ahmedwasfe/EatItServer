package ahmet.com.eatitserver.eventBus;

public class EditSizeAddonEvent {

    private boolean addon;
    private int position;

    public EditSizeAddonEvent(boolean addon, int position) {
        this.addon = addon;
        this.position = position;
    }

    public boolean isAddon() {
        return addon;
    }

    public void setAddon(boolean addon) {
        this.addon = addon;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
