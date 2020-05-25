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
import ahmet.com.eatitserver.eventBus.CategoryClick;
import ahmet.com.eatitserver.model.Category;
import ahmet.com.eatitserver.R;
import butterknife.BindView;
import butterknife.ButterKnife;


public class CategoriesAdapter extends RecyclerView.Adapter<CategoriesAdapter.CategoriesHolder> {

    private Context mContext;
    private List<Category> mLIstCategory;
    private LayoutInflater inflater;

    public CategoriesAdapter(Context mContext, List<Category> mLIstCategory) {
        this.mContext = mContext;
        this.mLIstCategory = mLIstCategory;

        inflater = LayoutInflater.from(mContext);
    }

    @NonNull
    @Override
    public CategoriesHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutView = inflater.inflate(R.layout.raw_category, parent, false);
        return new CategoriesHolder(layoutView);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoriesHolder holder, int position) {

        Picasso.get()
                .load(mLIstCategory.get(position).getImage())
                .into(holder.mImgCategory);
        holder.mTxtCategroyName.setText(new StringBuilder(mLIstCategory.get(position).getName()));

        holder.setRecyclerItemClickLitener((view, poistion1) -> {
            Common.currentCategory = mLIstCategory.get(poistion1);
            EventBus.getDefault().postSticky(new CategoryClick(true, mLIstCategory.get(poistion1)));
        });

    }

    @Override
    public int getItemCount() {
        return mLIstCategory.size();
    }

    class CategoriesHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.img_category_image)
        ImageView mImgCategory;
        @BindView(R.id.txt_category_name)
        TextView mTxtCategroyName;

        private IRecyclerItemClickLitener recyclerItemLitener;

        public CategoriesHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(this);
        }

        public void setRecyclerItemClickLitener(IRecyclerItemClickLitener recyclerItemLitener) {
            this.recyclerItemLitener = recyclerItemLitener;
        }

        @Override
        public void onClick(View v) {
            recyclerItemLitener.onItemClick(v, getAdapterPosition());
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (mLIstCategory.size() == 1)
            return Common.DEFAULT_COLUMN_COUNT;
        else{
            if (mLIstCategory.size() % 2 == 0)
                return Common.DEFAULT_COLUMN_COUNT;
            else
                return (position > 1 && position == mLIstCategory.size() - 1) ? Common.FULL_WIDTH_COLUMN : Common.DEFAULT_COLUMN_COUNT;
        }
    }
}