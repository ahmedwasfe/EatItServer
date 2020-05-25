package ahmet.com.eatitserver.ui.best_deals;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import ahmet.com.eatitserver.callback.IBestDealsCallbaclListener;
import ahmet.com.eatitserver.common.Common;
import ahmet.com.eatitserver.model.BestDeals;

public class BestDealsViewModel extends ViewModel implements IBestDealsCallbaclListener {

    private MutableLiveData<List<BestDeals>> mutableListBestDeals;
    private MutableLiveData<String> mutableMessageError;

    private IBestDealsCallbaclListener bestDealsCallbaclListener;

    public BestDealsViewModel() {
        if (mutableListBestDeals == null) {
            mutableListBestDeals = new MutableLiveData<>();
            mutableMessageError = new MutableLiveData<>();
        }
        bestDealsCallbaclListener = this;
    }

    public MutableLiveData<String> getMutableMessageError() {
        return mutableMessageError;
    }

    public MutableLiveData<List<BestDeals>> getMutableListBestDeals() {
        loadBestDeals();
        return mutableListBestDeals;
    }

    public void loadBestDeals() {

        FirebaseDatabase.getInstance().getReference()
                .child(Common.KEY_BEST_DEALS_REFERANCE)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        List<BestDeals> listBestDeals = new ArrayList<>();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                            BestDeals bestDeals = snapshot.getValue(BestDeals.class);
                            bestDeals.setKey(snapshot.getKey());
                            listBestDeals.add(bestDeals);
                        }
                        bestDealsCallbaclListener.onLoadBestDealsSuccess(listBestDeals);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        bestDealsCallbaclListener.onLoadBestDealsFailed(databaseError.getMessage());
                    }
                });
    }

    @Override
    public void onLoadBestDealsSuccess(List<BestDeals> mListBestDeals) {
        mutableListBestDeals.setValue(mListBestDeals);
    }

    @Override
    public void onLoadBestDealsFailed(String error) {
        mutableMessageError.setValue(error);
    }
}
