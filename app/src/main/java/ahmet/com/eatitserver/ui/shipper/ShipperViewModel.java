package ahmet.com.eatitserver.ui.shipper;

import android.widget.Button;
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import ahmet.com.eatitserver.callback.IShipperCallBackListener;
import ahmet.com.eatitserver.common.Common;
import ahmet.com.eatitserver.model.Order;
import ahmet.com.eatitserver.model.Shipper;

public class ShipperViewModel extends ViewModel implements IShipperCallBackListener {

    private MutableLiveData<List<Shipper>> mutableListShipper;
    private MutableLiveData<String> mutabMessageError;

    private IShipperCallBackListener shipperListener;

    public ShipperViewModel() {
        if (mutableListShipper == null){
            mutableListShipper = new MutableLiveData<>();
            mutabMessageError = new MutableLiveData<>();
        }

        shipperListener = this;
    }

    public MutableLiveData<List<Shipper>> getMutableListShipper() {

        return mutableListShipper;
    }

    public MutableLiveData<String> getMutabMessageError() {
        loadAllShippers();
        return mutabMessageError;
    }

    public void loadAllShippers() {

        FirebaseDatabase.getInstance().getReference()
                .child(Common.KEY_SHIPPER_REFERANCE)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        List<Shipper> mListShipper = new ArrayList<>();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                            Shipper shipper = snapshot.getValue(Shipper.class);
                            shipper.setKey(snapshot.getKey());
                            mListShipper.add(shipper);
                        }
                        shipperListener.onLoadOrderSuccess(mListShipper);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        shipperListener.onLoadOrderFaield(databaseError.getMessage());
                    }
                });
    }

    @Override
    public void onLoadOrderSuccess(List<Shipper> mListShippers) {

        if (mutableListShipper != null)
            mutableListShipper.setValue(mListShippers);
    }

    @Override
    public void onLoadOrderSuccess(int position, Order order, List<Shipper> mListShippers, AlertDialog dialog, Button btnOk, Button btnCancel, RadioButton radioShipping, RadioButton radioShipped, RadioButton radioCancelled, RadioButton radioDelete, RadioButton radiorestorePlaced) {

    }

    @Override
    public void onLoadOrderFaield(String error) {
        mutabMessageError.setValue(error);
    }
}
