package ahmet.com.eatitserver.callback;

import java.util.List;

import ahmet.com.eatitserver.model.Order;

public interface IOrderCallBackListener {

    void onLoadOrderSuccess(List<Order> mListOrders);
    void onLoadOrderFaield(String error);

}
