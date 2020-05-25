package ahmet.com.eatitserver.ui.most_popular;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import ahmet.com.eatitserver.callback.IMostPopularCallbaclListener;
import ahmet.com.eatitserver.common.Common;
import ahmet.com.eatitserver.model.Mostpopular;

public class MostPopularViewModel extends ViewModel implements IMostPopularCallbaclListener {

    private MutableLiveData<List<Mostpopular>> mutabListPopular;
    private MutableLiveData<String> mutabMessageError;

    private IMostPopularCallbaclListener mostPopularCallbaclListener;

    public MostPopularViewModel() {

        if (mutabListPopular == null){
            mutabListPopular = new MutableLiveData<>();
            mutabMessageError = new MutableLiveData<>();
        }

        mostPopularCallbaclListener = this;
    }

    public MutableLiveData<String> getMutabMessageError() {
        return mutabMessageError;
    }

    public MutableLiveData<List<Mostpopular>> getMutabListPopular() {
        loadMostPopular();
        return mutabListPopular;
    }

    public void loadMostPopular() {

        FirebaseDatabase.getInstance().getReference()
                .child(Common.KEY_POPULAR_REFERANCE)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        List<Mostpopular> litsMostpopulars = new ArrayList<>();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                            Mostpopular mostpopular = snapshot.getValue(Mostpopular.class);
                            mostpopular.setKey(snapshot.getKey());
                            litsMostpopulars.add(mostpopular);
                        }

                        mostPopularCallbaclListener.onLoadMostPopularSuccess(litsMostpopulars);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        mostPopularCallbaclListener.onLoadMostPopularsFailed(databaseError.getMessage());
                    }
                });
    }

    @Override
    public void onLoadMostPopularSuccess(List<Mostpopular> mListMostPopular) {
        mutabListPopular.setValue(mListMostPopular);
    }

    @Override
    public void onLoadMostPopularsFailed(String error) {
        mutabMessageError.setValue(error);
    }
}
