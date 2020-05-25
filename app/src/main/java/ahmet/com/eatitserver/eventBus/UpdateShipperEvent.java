package ahmet.com.eatitserver.eventBus;

import ahmet.com.eatitserver.model.Shipper;

public class UpdateShipperEvent {

    private Shipper shipper;
    private boolean active;

    public UpdateShipperEvent(Shipper shipper, boolean active) {
        this.shipper = shipper;
        this.active = active;
    }

    public Shipper getShipper() {
        return shipper;
    }

    public void setShipper(Shipper shipper) {
        this.shipper = shipper;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
