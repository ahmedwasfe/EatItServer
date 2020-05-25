package ahmet.com.eatitserver.callback;

import java.util.List;

import ahmet.com.eatitserver.model.BestDeals;

public interface IBestDealsCallbaclListener {

    void onLoadBestDealsSuccess(List<BestDeals> mListBestDeals);
    void onLoadBestDealsFailed(String error);
}
