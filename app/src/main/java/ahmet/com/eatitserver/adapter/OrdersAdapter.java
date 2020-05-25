package ahmet.com.eatitserver.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.List;

import ahmet.com.eatitserver.callback.IRecyclerItemClickLitener;
import ahmet.com.eatitserver.common.Common;
import ahmet.com.eatitserver.model.Order;
import ahmet.com.eatitserver.R;
import ahmet.com.eatitserver.ui.orderDetails.OrderDetailsActivity;
import butterknife.BindView;
import butterknife.ButterKnife;


public class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.OrdersHolder> {

    private Context mContext;
    private List<Order> mListOrders;
    private LayoutInflater inflater;

    private SimpleDateFormat dateFormat;

    public OrdersAdapter(Context mContext, List<Order> mListOrders) {
        this.mContext = mContext;
        this.mListOrders = mListOrders;

        inflater = LayoutInflater.from(mContext);

        dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss a");
    }

    @NonNull
    @Override
    public OrdersHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutView = inflater.inflate(R.layout.raw_order, parent, false);
        return new OrdersHolder(layoutView);
    }

    @Override
    public void onBindViewHolder(@NonNull OrdersHolder holder, int position) {

        Picasso.get()
                .load(mListOrders.get(position).getCarts().get(0).getFoodImage())
                .into(holder.mImgOrder);

        holder.mTxtOrderNumber.setText(mListOrders.get(position).getKey());

        Common.setSpanStringColor(mContext.getString(R.string.order_date)+" ", dateFormat.format(mListOrders.get(position).getDate()),
                holder.mTxtOrderDate, Color.parseColor("#333639"));

        Common.setSpanStringColor(mContext.getString(R.string.order_status)+" ", Common.convertStatus(mListOrders.get(position).getOrderStatus()),
                holder.mTxtOrderStatus, mContext.getColor(R.color.colorBlue1));

        Common.setSpanStringColor(mContext.getString(R.string.name)+" ", mListOrders.get(position).getUserName(),
                holder.mTxtOrderUserName, mContext.getColor(R.color.colorGreen1));

        Common.setSpanStringColor(mContext.getString(R.string.num_of_item)+" ",
                mListOrders.get(position).getCarts() == null ? "0" :
                String.valueOf(mListOrders.get(position).getCarts().size()),
                holder.mTxtOrderNumItem, mContext.getColor(R.color.colorDarkGray1));

        holder.setItemClickLitener(((view, position1) -> {
            Common.currentOrder = mListOrders.get(position1);
            mContext.startActivity(new Intent(mContext, OrderDetailsActivity.class));
        }));

    }

    @Override
    public int getItemCount() {
        return mListOrders.size();
    }

    public Order getItemAtPosition(int position) {
        return mListOrders.get(position);
    }

    public void removeItem(int position) {
        mListOrders.remove(position);
        notifyItemRemoved(position);
    }

    class OrdersHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.txt_order_food_number)
        TextView mTxtOrderNumber;
        @BindView(R.id.txt_order_food_date)
        TextView mTxtOrderDate;
        @BindView(R.id.txt_order_user_name)
        TextView mTxtOrderUserName;
        @BindView(R.id.txt_order_food_status)
        TextView mTxtOrderStatus;
        @BindView(R.id.txt_order_num_item)
        TextView mTxtOrderNumItem;
        @BindView(R.id.img_order_food)
        ImageView mImgOrder;

        private IRecyclerItemClickLitener itemClickLitener;

        public OrdersHolder(@NonNull View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(this);
        }

        public void setItemClickLitener(IRecyclerItemClickLitener itemClickLitener) {
            this.itemClickLitener = itemClickLitener;
        }

        @Override
        public void onClick(View v) {
            itemClickLitener.onItemClick(v, getAdapterPosition());
        }
    }
}