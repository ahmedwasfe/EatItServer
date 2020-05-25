package ahmet.com.eatitserver.ui.ordersFilter;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.greenrobot.eventbus.EventBus;

import ahmet.com.eatitserver.eventBus.LoadEventOrder;
import ahmet.com.eatitserver.R;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class BottomSheetOrderFragment extends BottomSheetDialogFragment {

    @OnClick(R.id.linear_placed_order) void onPlacedFilterClick(){
        EventBus.getDefault().postSticky(new LoadEventOrder(0));
        dismiss();
    }

    @OnClick(R.id.linear_shipping_order) void onShippingFilterClick(){
        EventBus.getDefault().postSticky(new LoadEventOrder(1));
        dismiss();
    }

    @OnClick(R.id.linear_shipped_order) void onShippedFilterClick(){
        EventBus.getDefault().postSticky(new LoadEventOrder(2));
        dismiss();
    }

    @OnClick(R.id.linear_cancelled_order) void onCancelledFilterClick(){
        EventBus.getDefault().postSticky(new LoadEventOrder(-1));
        dismiss();
    }

    private static BottomSheetOrderFragment instance;

    public static BottomSheetOrderFragment getInstance(){
        return instance == null ? new BottomSheetOrderFragment() : instance;
    }

    public BottomSheetOrderFragment(){}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View layoutView = inflater.inflate(R.layout.fragment_order_filter, container, false);

        ButterKnife.bind(this, layoutView);

        return layoutView;
    }
}
