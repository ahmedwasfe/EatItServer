package ahmet.com.eatitserver.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;


import org.greenrobot.eventbus.EventBus;

import java.util.List;

import ahmet.com.eatitserver.callback.IRecyclerItemClickLitener;
import ahmet.com.eatitserver.common.Common;
import ahmet.com.eatitserver.eventBus.FoodClick;
import ahmet.com.eatitserver.eventBus.UpdateFood;
import ahmet.com.eatitserver.model.Food.Food;
import ahmet.com.eatitserver.R;
import butterknife.BindView;
import butterknife.ButterKnife;


public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.FoodHolder> {

    private Context mContext;
    private List<Food> mListFood;
    private LayoutInflater inflater;

    private UpdateFood updateFood;


    public FoodAdapter(Context mContext, List<Food> mListFood) {
        this.mContext = mContext;
        this.mListFood = mListFood;

        inflater = LayoutInflater.from(mContext);

        updateFood = new UpdateFood();
    }

    @NonNull
    @Override
    public FoodHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutView = inflater.inflate(R.layout.raw_food, parent, false);
        return new FoodHolder(layoutView);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodHolder holder, int position) {

        Picasso.get()
                .load(mListFood.get(position).getImage())
                .into(holder.mImgFood);
        holder.mTxtFoodPrice.setText(new StringBuilder("$")
                                    .append(mListFood.get(position).getPrice()));
        holder.mTxtFoodName.setText(new StringBuilder("")
                                    .append(mListFood.get(position).getName()));

        holder.setRecyclerItemClickLitener((view, position1) -> {
            Common.currentFood = mListFood.get(position1);
            Common.currentFood.setKey(String.valueOf(position1));
            EventBus.getDefault().postSticky(new FoodClick(true, mListFood.get(position)));
        });


    }

    @Override
    public int getItemCount() {
        return mListFood.size();
    }

    public Food getItemAtPosition(int position){
        return mListFood.get(position);
    }



    public void addNewFood(Food food) {
        mListFood.add(food);
        notifyItemInserted(mListFood.size() - 1);
        updateFood.setFood(mListFood);
        EventBus.getDefault().postSticky(updateFood);

    }

    class FoodHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.txt_food_name)
        TextView mTxtFoodName;
        @BindView(R.id.txt_food_price)
        TextView mTxtFoodPrice;

        @BindView(R.id.img_food_image)
        ImageView mImgFood;

        private IRecyclerItemClickLitener recyclerItemClickLitener;


        public FoodHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(this);
        }

        public void setRecyclerItemClickLitener(IRecyclerItemClickLitener recyclerItemClickLitener) {
            this.recyclerItemClickLitener = recyclerItemClickLitener;
        }

        @Override
        public void onClick(View v) {
            recyclerItemClickLitener.onItemClick(v, getAdapterPosition());
        }
    }
}