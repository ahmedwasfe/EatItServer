package ahmet.com.eatitserver.ui.orderDetails;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.text.SimpleDateFormat;

import ahmet.com.eatitserver.adapter.OrderDetailsAdapter;
import ahmet.com.eatitserver.common.Common;
import ahmet.com.eatitserver.R;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.supercharge.shimmerlayout.ShimmerLayout;

public class OrderDetailsActivity extends AppCompatActivity {

    @BindView(R.id.toolbar_order_details)
    Toolbar mToolbar;

    @BindView(R.id.card_order_details)
    CardView mCardOrderDetails;

    @BindView(R.id.txt_order_detail_number)
    TextView mTxtOrderId;
    @BindView(R.id.txt_order_detail_user_name)
    TextView mTxtOrderUserName;
    @BindView(R.id.txt_order_detail_status)
    TextView mTxtOrderStatus;
    @BindView(R.id.txt_order_details_date)
    TextView mTxtOrderDate;
    @BindView(R.id.txt_order_detail_comment)
    TextView mTxtOrderComment;
    @BindView(R.id.txt_order_detail_address)
    TextView mTxtOrderAddress;

    @BindView(R.id.recycler_order_details)
    RecyclerView mRecyclerOrderDetails;
    @BindView(R.id.shimmer_layout_orders_details)
    ShimmerLayout mShimmerLayout;

    private OrderDetailsViewModel orderDetailsViewModel;
    private SimpleDateFormat dateFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);

        orderDetailsViewModel = new ViewModelProvider(this).get(OrderDetailsViewModel.class);
        ButterKnife.bind(this);

        initViews();
        loadOrdersData();
        loadOrderDetails();
    }

    private void loadOrderDetails() {

        orderDetailsViewModel.getMutableListOrderDetails()
                .observe(this, mListOrderDetails -> {
                    OrderDetailsAdapter detailsAdapter = new OrderDetailsAdapter(this, mListOrderDetails);
                    mRecyclerOrderDetails.setAdapter(detailsAdapter);
                });
    }

    private void initViews() {

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(R.string.order_details);

        mShimmerLayout.startShimmerAnimation();

        dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

        mRecyclerOrderDetails.setHasFixedSize(true);
        mRecyclerOrderDetails.setLayoutManager(new LinearLayoutManager(this));
    }

    private void loadOrdersData() {

        mTxtOrderId.setText(Common.currentOrder.getKey());
        mTxtOrderComment.setText(Common.currentOrder.getComment());

        Common.setSpanStringColor(getString(R.string.order_status)+" ", Common.convertStatus(Common.currentOrder.getOrderStatus()),
                mTxtOrderStatus, getColor(R.color.colorBlue1));

        Common.setSpanStringColor(getString(R.string.name)+" ", Common.currentOrder.getUserName(),
                mTxtOrderUserName, getColor(R.color.colorGreen1));

        Common.setSpanStringColor(getString(R.string.address)+" ", Common.currentOrder.getShippingAddress(),
                mTxtOrderAddress, getColor(R.color.colorGreen1));

        Common.setSpanStringColor(getString(R.string.order_date)+" ", dateFormat.format(Common.currentOrder.getDate()),
                mTxtOrderDate, Color.parseColor("#333639"));

        mShimmerLayout.stopShimmerAnimation();
        mShimmerLayout.setVisibility(View.GONE);
    }
}
