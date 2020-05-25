package ahmet.com.eatitserver.callback;

import android.widget.Button;
import android.widget.RadioButton;

import androidx.appcompat.app.AlertDialog;

import java.util.List;

import ahmet.com.eatitserver.model.Order;
import ahmet.com.eatitserver.model.Shipper;

public interface IShipperCallBackListener {

    void onLoadOrderSuccess(List<Shipper> mListShippers);
    void onLoadOrderSuccess(int position, Order order, List<Shipper> mListShippers,
                            AlertDialog dialog,
                            Button btnOk, Button btnCancel,
                            RadioButton radioShipping, RadioButton radioShipped, RadioButton radioCancelled,
                            RadioButton radioDelete, RadioButton radiorestorePlaced);
    void onLoadOrderFaield(String error);

}
