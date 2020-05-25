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


import java.util.List;

import ahmet.com.eatitserver.R;
import ahmet.com.eatitserver.callback.IRecyclerItemClickLitener;
import ahmet.com.eatitserver.model.Mostpopular;
import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;


public class MostPopularAdapter extends RecyclerView.Adapter<MostPopularAdapter.MostPopularHolder> {

    private Context mContext;
    private List<Mostpopular> mLIstMostPopular;
    private LayoutInflater inflater;

    public MostPopularAdapter(Context mContext, List<Mostpopular> mLIstMostPopular) {
        this.mContext = mContext;
        this.mLIstMostPopular = mLIstMostPopular;

        inflater = LayoutInflater.from(mContext);
    }

    @NonNull
    @Override
    public MostPopularHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutView = inflater.inflate(R.layout.raw_most_popular, parent, false);
        return new MostPopularHolder(layoutView);
    }

    @Override
    public void onBindViewHolder(@NonNull MostPopularHolder holder, int position) {

        Picasso.get()
                .load(mLIstMostPopular.get(position).getImage())
                .into(holder.mImgPopular);
        holder.mTxtPopularName.setText(new StringBuilder(mLIstMostPopular.get(position).getName()));

        holder.setRecyclerItemClickLitener((view, position1) -> {

        });


    }

    @Override
    public int getItemCount() {
        return mLIstMostPopular.size();
    }

    class MostPopularHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.img_populer)
        ImageView mImgPopular;
        @BindView(R.id.txt_populer_name)
        TextView mTxtPopularName;

        private IRecyclerItemClickLitener recyclerItemClickLitener;

        public MostPopularHolder(@NonNull View itemView) {
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