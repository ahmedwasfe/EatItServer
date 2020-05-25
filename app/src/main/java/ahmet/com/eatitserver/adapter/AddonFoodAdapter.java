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
import ahmet.com.eatitserver.eventBus.SelectFoodAddon;
import ahmet.com.eatitserver.eventBus.UpdateFoodAddon;
import ahmet.com.eatitserver.model.Food.Addon;
import ahmet.com.eatitserver.R;
import butterknife.BindView;
import butterknife.ButterKnife;


public class AddonFoodAdapter extends RecyclerView.Adapter<AddonFoodAdapter.AddonFoodHolder> {

    private Context mContext;
    private List<Addon> mListFoodAddon;
    private LayoutInflater inflater;

    private UpdateFoodAddon updateFoodAddon;

    private int mEditPosition;

    public AddonFoodAdapter(Context mContext, List<Addon> mListFoodAddon) {
        this.mContext = mContext;
        this.mListFoodAddon = mListFoodAddon;

        inflater = LayoutInflater.from(mContext);

        updateFoodAddon = new UpdateFoodAddon();

        mEditPosition = -1;

    }

    @NonNull
    @Override
    public AddonFoodHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutView = inflater.inflate(R.layout.raw_addon_size_food, parent, false);
        return new AddonFoodHolder(layoutView);
    }

    @Override
    public void onBindViewHolder(@NonNull AddonFoodHolder holder, int position) {

        holder.mTxtFoodAddon.setText(mListFoodAddon.get(position).getName());
        holder.mTxtFoodPrice.setText(new StringBuilder("$").append(mListFoodAddon.get(position).getPrice()).toString());

        holder.mImgDeleteFoodAddon.setOnClickListener(view -> {
            mListFoodAddon.remove(position);
            notifyItemRemoved(position);
            updateFoodAddon.setFoodAddonList(mListFoodAddon); // Set for event
            EventBus.getDefault().postSticky(mListFoodAddon);  // Send evenr
        });

        holder.setItemClickLitener((view, position1) -> {
            mEditPosition = position;
            EventBus.getDefault().postSticky(new SelectFoodAddon(mListFoodAddon.get(position1)));
        });
    }

    @Override
    public int getItemCount() {
        return mListFoodAddon.size();
    }

    public void addNewFoodAddon(Addon addon) {

        mListFoodAddon.add(addon);
        notifyItemInserted(mListFoodAddon.size() - 1);
        updateFoodAddon.setFoodAddonList(mListFoodAddon);
        EventBus.getDefault().postSticky(updateFoodAddon);
    }

    public void editFoodAddon(Addon addon) {

        if (mEditPosition != -1){
            mListFoodAddon.set(mEditPosition, addon);
            notifyItemChanged(mEditPosition);
            // Reset variabul after success
            mEditPosition = -1;
            // Send update
            updateFoodAddon.setFoodAddonList(mListFoodAddon);
            EventBus.getDefault().postSticky(updateFoodAddon);
        }
    }

    class AddonFoodHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.txt_food_addon_size_name)
        TextView mTxtFoodAddon;
        @BindView(R.id.txt_food_addon_size_price)
        TextView mTxtFoodPrice;
        @BindView(R.id.img_delete_food_addon_size)
        ImageView mImgDeleteFoodAddon;

        private IRecyclerItemClickLitener itemClickLitener;

        public AddonFoodHolder(@NonNull View itemView) {
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