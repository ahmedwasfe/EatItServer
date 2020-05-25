package ahmet.com.eatitserver.ui.food;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import ahmet.com.eatitserver.common.Common;
import ahmet.com.eatitserver.model.Food.Food;

public class FoodViewModel extends ViewModel {

    private MutableLiveData<List<Food>> mutabListFoods;
    private MutableLiveData<String> mMutabMessageError;

    public FoodViewModel() {

        if (mutabListFoods == null){
            mutabListFoods = new MutableLiveData<>();
            mMutabMessageError = new MutableLiveData<>();
        }
    }

    public MutableLiveData<String> getmMutabMessageError() {
        return mMutabMessageError;
    }

    public MutableLiveData<List<Food>> getMutabListFoods() {

        mutabListFoods.setValue(Common.currentCategory.getFoods());
        return mutabListFoods;
    }
}