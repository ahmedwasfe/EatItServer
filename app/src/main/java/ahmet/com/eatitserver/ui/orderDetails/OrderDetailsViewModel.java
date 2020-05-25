package ahmet.com.eatitserver.ui.orderDetails;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import ahmet.com.eatitserver.common.Common;
import ahmet.com.eatitserver.model.Cart;

public class OrderDetailsViewModel extends ViewModel {

    private MutableLiveData<List<Cart>> mutableListOrderDetails;

    public OrderDetailsViewModel() {
        if (mutableListOrderDetails == null)
            mutableListOrderDetails = new MutableLiveData<>();
    }

    public MutableLiveData<List<Cart>> getMutableListOrderDetails() {
        mutableListOrderDetails.setValue(Common.currentOrder.getCarts());
        return mutableListOrderDetails;
    }
}
