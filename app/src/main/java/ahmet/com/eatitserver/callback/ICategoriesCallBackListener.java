package ahmet.com.eatitserver.callback;

import java.util.List;

import ahmet.com.eatitserver.model.Category;

public interface ICategoriesCallBackListener {

    void onLoadCategoriseSuccess(List<Category> mListCategory);
    void onLoadCategoriseFaield(String error);

}
