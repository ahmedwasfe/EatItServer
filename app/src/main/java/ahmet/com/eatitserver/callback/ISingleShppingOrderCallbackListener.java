package ahmet.com.eatitserver.callback;

import ahmet.com.eatitserver.model.ShippingOrder;

public interface ISingleShppingOrderCallbackListener {

    void onLoadShippingOrderSuccess(ShippingOrder shippingOrder);
}
