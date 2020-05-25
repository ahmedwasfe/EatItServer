package ahmet.com.eatitserver.ui.categories;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import ahmet.com.eatitserver.callback.ICategoriesCallBackListener;
import ahmet.com.eatitserver.common.Common;
import ahmet.com.eatitserver.model.Category;

public class CategoryViewModel extends ViewModel implements ICategoriesCallBackListener {

    private MutableLiveData<List<Category>> mutableListCategory;
    private MutableLiveData<String> mutableLMessageError;
    private ICategoriesCallBackListener categoriesCallBackListener;

    public CategoryViewModel() {
        if (mutableListCategory == null){
            mutableListCategory = new MutableLiveData<>();
            mutableLMessageError = new MutableLiveData<>();
        }
        categoriesCallBackListener = this;
    }

    public MutableLiveData<String> getMutableLMessageError() {
        return mutableLMessageError;
    }

    public MutableLiveData<List<Category>> getMutableListCategory() {
        loadCategorise();
        return mutableListCategory;
    }

    public void loadCategorise() {

        FirebaseDatabase.getInstance().getReference()
                .child(Common.KEY_CAEGORIES_REFERANCE)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        List<Category> mListcategories = new ArrayList<>();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                            Category category = snapshot.getValue(Category.class);
                            category.setMenu_id(snapshot.getKey());
                            mListcategories.add(category);
                        }
                        categoriesCallBackListener.onLoadCategoriseSuccess(mListcategories);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        categoriesCallBackListener.onLoadCategoriseFaield(databaseError.getMessage());
                    }
                });
    }

    @Override
    public void onLoadCategoriseSuccess(List<Category> mListCategory) {
        mutableListCategory.setValue(mListCategory);
    }

    @Override
    public void onLoadCategoriseFaield(String error) {
        mutableLMessageError.setValue(error);
    }
}