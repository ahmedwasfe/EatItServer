package ahmet.com.eatitserver.eventBus;

public class LoadEventOrder {

    private int status;

    public LoadEventOrder(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
