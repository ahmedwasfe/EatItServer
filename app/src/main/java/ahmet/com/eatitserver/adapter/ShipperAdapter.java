package ahmet.com.eatitserver.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import ahmet.com.eatitserver.common.Common;
import ahmet.com.eatitserver.eventBus.UpdateShipperEvent;
import ahmet.com.eatitserver.model.Shipper;
import ahmet.com.eatitserver.R;
import butterknife.BindView;
import butterknife.ButterKnife;


public class ShipperAdapter extends RecyclerView.Adapter<ShipperAdapter.ShipperHolder> {

    private Context mContext;
    private List<Shipper> mListShipper;
    private LayoutInflater inflater;

    public ShipperAdapter(Context mContext, List<Shipper> mListShipper) {
        this.mContext = mContext;
        this.mListShipper = mListShipper;

        inflater = LayoutInflater.from(mContext);
    }

    @NonNull
    @Override
    public ShipperHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutView = inflater.inflate(R.layout.raw_shipper, parent, false);
        return new ShipperHolder(layoutView);
    }

    @Override
    public void onBindViewHolder(@NonNull ShipperHolder holder, int position) {

        Common.setSpanStringColor("",mListShipper.get(position).getName(),
                holder.mTxtShipperName, mContext.getColor(R.color.colorBlue1));
        Common.setSpanStringColor("",mListShipper.get(position).getPhone(),
                holder.mTxtShipperPhone, mContext.getColor(R.color.colorBlue));

        holder.mSwitchEnableShipper.setChecked(mListShipper.get(position).isActive());
        holder.mSwitchEnableShipper.setOnCheckedChangeListener(((buttonView, isChecked) -> {
            EventBus.getDefault().postSticky(new UpdateShipperEvent(mListShipper.get(position), isChecked));
        }));
    }

    @Override
    public int getItemCount() {
        return mListShipper.size();
    }

    public List<Shipper> getListShipper() {
        return mListShipper;
    }

    class ShipperHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.txt_shipper_name)
        TextView mTxtShipperName;
        @BindView(R.id.txt_shipper_phone)
        TextView mTxtShipperPhone;
        @BindView(R.id.awitch_enable_shipper)
        SwitchCompat mSwitchEnableShipper;

        public ShipperHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}