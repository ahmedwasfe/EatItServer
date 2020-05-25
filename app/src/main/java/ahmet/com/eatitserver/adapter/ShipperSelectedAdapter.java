package ahmet.com.eatitserver.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ahmet.com.eatitserver.callback.IRecyclerItemClickLitener;
import ahmet.com.eatitserver.model.Shipper;
import ahmet.com.eatitserver.R;
import butterknife.BindView;
import butterknife.ButterKnife;


public class ShipperSelectedAdapter extends RecyclerView.Adapter<ShipperSelectedAdapter.ShipperSelectedHolder> {

    private Context mContext;
    private List<Shipper> mListShipperSelected;
    private LayoutInflater inflater;

    private ImageView lastcheckedImage = null;
    private Shipper mSelectedShipper = null;

    public ShipperSelectedAdapter(Context mContext, List<Shipper> mListShipperSelected) {
        this.mContext = mContext;
        this.mListShipperSelected = mListShipperSelected;

        inflater = LayoutInflater.from(mContext);
    }

    @NonNull
    @Override
    public ShipperSelectedHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutView = inflater.inflate(R.layout.raw_shipper_selected, parent, false);
        return new ShipperSelectedHolder(layoutView);
    }

    @Override
    public void onBindViewHolder(@NonNull ShipperSelectedHolder holder, int position) {

        holder.mTxtShipperName.setText(mListShipperSelected.get(position).getName());
        holder.mTxtShipperPhone.setText(mListShipperSelected.get(position).getPhone());

        holder.setRecyclerItemClickLitener((view, position1) -> {

            if (lastcheckedImage != null)
                lastcheckedImage.setImageResource(0);
            holder.mImgShipperSelected.setImageResource(R.drawable.ic_done_shipped);
            lastcheckedImage = holder.mImgShipperSelected;
            mSelectedShipper = mListShipperSelected.get(position1);
        });
    }

    public Shipper getShipperSelected() {
        return mSelectedShipper;
    }

    @Override
    public int getItemCount() {
        return mListShipperSelected.size();
    }

    class ShipperSelectedHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.txt_order_shipper_name_selected)
        TextView mTxtShipperName;
        @BindView(R.id.txt_order_shipper_phone_selected)
        TextView mTxtShipperPhone;
        @BindView(R.id.img_order_shipper_selected)
        ImageView mImgShipperSelected;

        private IRecyclerItemClickLitener recyclerItemClickLitener;

        public ShipperSelectedHolder(@NonNull View itemView) {
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