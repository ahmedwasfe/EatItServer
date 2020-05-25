package ahmet.com.eatitserver.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import java.util.List;

import ahmet.com.eatitserver.model.Cart;
import ahmet.com.eatitserver.model.Food.Addon;
import ahmet.com.eatitserver.model.Food.FoodSize;
import ahmet.com.eatitserver.R;
import butterknife.BindView;
import butterknife.ButterKnife;


public class OrderDetailsAdapter extends RecyclerView.Adapter<OrderDetailsAdapter.OrderDetailsHolder> {

    private Context mContext;
    private List<Cart> mListOrderDetails;
    private LayoutInflater inflater;

    private Gson gson;

    public OrderDetailsAdapter(Context mContext, List<Cart> mListOrderDetails) {
        this.mContext = mContext;
        this.mListOrderDetails = mListOrderDetails;

        inflater = LayoutInflater.from(mContext);

        gson = new Gson();
    }

    @NonNull
    @Override
    public OrderDetailsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutView = inflater.inflate(R.layout.raw_order_details, parent, false);
        return new OrderDetailsHolder(layoutView);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderDetailsHolder holder, int position) {

        Picasso.get().load(mListOrderDetails.get(position).getFoodImage()).into(holder.mImgOrderFood);

        holder.mTxtOrderFoodName.setText(mListOrderDetails.get(position).getFoodName());
        holder.mTxtOrderFoodQuantity.setText(new StringBuilder(mContext.getString(R.string.quantitiy)+" ")
                                    .append(mListOrderDetails.get(position).getFoodQuantity()));
        FoodSize foodSize = gson.fromJson(mListOrderDetails.get(position).getFoodSize(),
                new TypeToken<FoodSize>(){}.getType());
        if (foodSize != null)
            holder.mTxtOrderFoodSize.setText(new StringBuilder(mContext.getString(R.string.size))
                                    .append(": ")
                                    .append(foodSize.getName()));
        if (!mListOrderDetails.get(position).getFoodAddon().equals("Default")){
            List<Addon> listAddon = gson.fromJson(mListOrderDetails.get(position).getFoodAddon(),
                    new TypeToken<List<Addon>>(){}.getType());
            StringBuilder addonStr = new StringBuilder();
            if (listAddon != null){
                for (Addon addon : listAddon)
                    addonStr.append(addon.getName()).append(",");
                // Remove last "," character
                addonStr.delete(addonStr.length()-1, addonStr.length());
                holder.mTxtOrderFoodAddon.setText(new StringBuilder(mContext.getString(R.string.addon))
                                        .append(": ").append(addonStr));
            }
        }else
            holder.mTxtOrderFoodAddon.setText(new StringBuilder("Addon Default"));

    }

    @Override
    public int getItemCount() {
        return mListOrderDetails.size();
    }

    class OrderDetailsHolder extends RecyclerView.ViewHolder {


        @BindView(R.id.img_order_details_food)
        ImageView mImgOrderFood;

        @BindView(R.id.txt_order_details_food_name)
        TextView mTxtOrderFoodName;
        @BindView(R.id.txt_order_details_food_addon)
        TextView mTxtOrderFoodAddon;
        @BindView(R.id.txt_order_details_food_size)
        TextView mTxtOrderFoodSize;
        @BindView(R.id.txt_order_details_food_quantity)
        TextView mTxtOrderFoodQuantity;

        public OrderDetailsHolder(@NonNull View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }
    }
}