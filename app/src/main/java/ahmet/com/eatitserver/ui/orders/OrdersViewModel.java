package ahmet.com.eatitserver.ui.orders;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ahmet.com.eatitserver.callback.IOrderCallBackListener;
import ahmet.com.eatitserver.common.Common;
import ahmet.com.eatitserver.model.Order;

public class OrdersViewModel extends ViewModel implements IOrderCallBackListener {

    private MutableLiveData<List<Order>> mutabListOrders;
    private MutableLiveData<String> mutabMessageError;

    private IOrderCallBackListener orderListener;

    public OrdersViewModel() {

        mutabListOrders = new MutableLiveData<>();
        mutabMessageError = new MutableLiveData<>();

        orderListener = this;
    }

    public MutableLiveData<List<Order>> getMutabListOrders() {
        loadOrdersByStatus(0);
        return mutabListOrders;
    }

    public void loadOrdersByStatus(int status) {

        Query query = FirebaseDatabase.getInstance().getReference()
                .child(Common.KEY_ORDER_REFERANCE)
                .orderByChild("orderStatus")
                .equalTo(status);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                List<Order> mListOrders = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Order order = snapshot.getValue(Order.class);
                    order.setKey(snapshot.getKey());
                    order.setOrderNumber(snapshot.getKey());
                    mListOrders.add(order);
                }

                orderListener.onLoadOrderSuccess(mListOrders);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                orderListener.onLoadOrderFaield(databaseError.getMessage());
            }
        });

    }

    public MutableLiveData<String> getMutabMessageError() {
        return mutabMessageError;
    }

    @Override
    public void onLoadOrderSuccess(List<Order> mListOrders) {
        if (mListOrders.size() > 0){
            Collections.sort(mListOrders, (order,  t1) -> {
                if (order.getDate() < t1.getDate())
                    return -1;
                return order.getDate() == t1.getDate() ? 0 : 1;
            });
        }

        mutabListOrders.setValue(mListOrders);
    }

    @Override
    public void onLoadOrderFaield(String error) {
        mutabMessageError.setValue(error);
    }
}