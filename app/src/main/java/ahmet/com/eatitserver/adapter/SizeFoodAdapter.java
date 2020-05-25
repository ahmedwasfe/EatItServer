package ahmet.com.eatitserver.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import ahmet.com.eatitserver.callback.IRecyclerItemClickLitener;
import ahmet.com.eatitserver.eventBus.SelectFoodSize;
import ahmet.com.eatitserver.eventBus.UpdateFoodSize;
import ahmet.com.eatitserver.model.Food.FoodSize;
import ahmet.com.eatitserver.R;
import butterknife.BindView;
import butterknife.ButterKnife;


public class SizeFoodAdapter extends RecyclerView.Adapter<SizeFoodAdapter.SizeFoodHolder> {

    private Context mContext;
    private List<FoodSize> mListSizeFood;
    private LayoutInflater inflater;

    private UpdateFoodSize updateFoodSize;

    private int mEditPosition;

    public SizeFoodAdapter(Context mContext, List<FoodSize> mListSizeFood) {
        this.mContext = mContext;
        this.mListSizeFood = mListSizeFood;

        inflater = LayoutInflater.from(mContext);

        updateFoodSize = new UpdateFoodSize();

        mEditPosition = -1;
    }

    @NonNull
    @Override
    public SizeFoodHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutView = inflater.inflate(R.layout.raw_addon_size_food, parent, false);
        return new SizeFoodHolder(layoutView);
    }

    @Override
    public void onBindViewHolder(@NonNull SizeFoodHolder holder, int position) {

        holder.mTxtFoodSize.setText(mListSizeFood.get(position).getName());
        holder.mTxtFoodPrice.setText(new StringBuilder("$").append(mListSizeFood.get(position).getPrice()).toString());

        holder.mImgDeleteFoodSize.setOnClickListener(view -> {
            mListSizeFood.remove(position);
            notifyItemRemoved(position);
            updateFoodSize.setFoodSizeList(mListSizeFood); // Set for event
            EventBus.getDefault().postSticky(mListSizeFood);  // Send evenr

        });

        holder.setItemClickLitener((view, position1) -> {
            mEditPosition = position;
            EventBus.getDefault().postSticky(new SelectFoodSize(mListSizeFood.get(position1)));
        });
    }

    @Override
    public int getItemCount() {
        return mListSizeFood.size();
    }

    public void addNewFoodSize(FoodSize foodSize) {

        mListSizeFood.add(foodSize);
        notifyItemInserted(mListSizeFood.size() - 1);
        updateFoodSize.setFoodSizeList(mListSizeFood);
        EventBus.getDefault().postSticky(updateFoodSize);
    }

    public void editFoodSize(FoodSize foodSize) {

        if (mEditPosition != -1){
            mListSizeFood.set(mEditPosition, foodSize);
            notifyItemChanged(mEditPosition);
            // Reset variabul after success
            mEditPosition = -1;
            // Send update
            updateFoodSize.setFoodSizeList(mListSizeFood);
            EventBus.getDefault().postSticky(updateFoodSize);
        }
    }

    class SizeFoodHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.txt_food_addon_size_name)
        TextView mTxtFoodSize;
        @BindView(R.id.txt_food_addon_size_price)
        TextView mTxtFoodPrice;
        @BindView(R.id.img_delete_food_addon_size)
        ImageView mImgDeleteFoodSize;

        private IRecyclerItemClickLitener itemClickLitener;

        public SizeFoodHolder(@NonNull View itemView) {
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