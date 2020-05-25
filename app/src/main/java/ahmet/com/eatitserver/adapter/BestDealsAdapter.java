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

import ahmet.com.eatitserver.R;
import ahmet.com.eatitserver.callback.IRecyclerItemClickLitener;
import ahmet.com.eatitserver.model.BestDeals;
import butterknife.BindView;
import butterknife.ButterKnife;


public class BestDealsAdapter extends RecyclerView.Adapter<BestDealsAdapter.BestDealsHolder> {

    private Context mContext;
    private List<BestDeals> mListBestDeals;
    private LayoutInflater inflater;

    public BestDealsAdapter(Context mContext, List<BestDeals> mListBestDeals) {
        this.mContext = mContext;
        this.mListBestDeals = mListBestDeals;

        inflater = LayoutInflater.from(mContext);
    }

    @NonNull
    @Override
    public BestDealsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutView = inflater.inflate(R.layout.raw_best_deals, parent, false);
        return new BestDealsHolder(layoutView);
    }

    @Override
    public void onBindViewHolder(@NonNull BestDealsHolder holder, int position) {

        Picasso.get()
                .load(mListBestDeals.get(position).getImage())
                .into(holder.mImgBestDeals);
        holder.mTxtBestDealsName.setText(mListBestDeals.get(position).getName());

        holder.setRecyclerItemClickLitener((view, position1) -> {

        });

    }

    @Override
    public int getItemCount() {
        return mListBestDeals.size();
    }

    class BestDealsHolder extends RecyclerView.ViewHolder implements View.OnClickListener {


        @BindView(R.id.img_best_deals)
        ImageView mImgBestDeals;
        @BindView(R.id.txt_best_deals_name)
        TextView mTxtBestDealsName;

        private IRecyclerItemClickLitener recyclerItemClickLitener;;

        public BestDealsHolder(@NonNull View itemView) {
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