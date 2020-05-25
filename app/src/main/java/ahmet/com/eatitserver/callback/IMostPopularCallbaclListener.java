package ahmet.com.eatitserver.callback;

import java.util.List;

import ahmet.com.eatitserver.model.BestDeals;
import ahmet.com.eatitserver.model.Mostpopular;

public interface IMostPopularCallbaclListener {

    void onLoadMostPopularSuccess(List<Mostpopular> mListMostPopular);
    void onLoadMostPopularsFailed(String error);
}
