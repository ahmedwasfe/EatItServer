package ahmet.com.eatitserver.ui.orders;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ahmet.com.eatitserver.adapter.OrdersAdapter;
import ahmet.com.eatitserver.adapter.ShipperSelectedAdapter;
import ahmet.com.eatitserver.callback.IShipperCallBackListener;
import ahmet.com.eatitserver.common.Common;
import ahmet.com.eatitserver.common.SwipeRecyclerHelper;
import ahmet.com.eatitserver.eventBus.ChangeMenuClick;
import ahmet.com.eatitserver.eventBus.LoadEventOrder;
import ahmet.com.eatitserver.model.Order;
import ahmet.com.eatitserver.model.ServiceModel.FCMSendData;
import ahmet.com.eatitserver.model.Shipper;
import ahmet.com.eatitserver.model.ShippingOrder;
import ahmet.com.eatitserver.model.Token;
import ahmet.com.eatitserver.R;
import ahmet.com.eatitserver.services.IFCMService;
import ahmet.com.eatitserver.services.RetrofitFCMClient;
import ahmet.com.eatitserver.TrackingOrderActivity;
import ahmet.com.eatitserver.ui.ordersFilter.BottomSheetOrderFragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import io.supercharge.shimmerlayout.ShimmerLayout;

public class OrdersFragment extends Fragment implements IShipperCallBackListener {

    @BindView(R.id.recycler_orders)
    RecyclerView mRecyclerOrders;
    @BindView(R.id.shimmer_layout_orders)
    ShimmerLayout mShimmerLayout;
    @BindView(R.id.txt_order_filter)
    TextView mTxtOrderFilter;

    private RecyclerView mRecyclerShipperSelected;

    private OrdersAdapter ordersAdapter;
    private ShipperSelectedAdapter shipperSelectedAdapter;

    private LayoutAnimationController mAnimationController;

    private OrdersViewModel ordersViewModel;

    private CompositeDisposable mDisposable;
    private IFCMService mIfcmService;

    private IShipperCallBackListener shipperCallBackListener;

    private android.app.AlertDialog mDialog;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        ordersViewModel = new ViewModelProvider(this).get(OrdersViewModel.class);
        View layoutView = inflater.inflate(R.layout.fragment_orders, container, false);

        ButterKnife.bind(this, layoutView);

        initViews();

        ordersViewModel.getMutabMessageError()
                .observe(getViewLifecycleOwner(), error -> {
                    Log.e("LOAD_ORDERS_ERROR", error);
                });

        ordersViewModel.getMutabListOrders()
                .observe(getViewLifecycleOwner(), mLIstOrders -> {

                    if (mLIstOrders != null) {

                        ordersAdapter = new OrdersAdapter(getActivity(), mLIstOrders);
                        mRecyclerOrders.setAdapter(ordersAdapter);
                        mRecyclerOrders.setLayoutAnimation(mAnimationController);

                        mTxtOrderFilter.setVisibility(View.VISIBLE);
                        updateOrdersCount();

                        mShimmerLayout.stopShimmerAnimation();
                        mShimmerLayout.setVisibility(View.GONE);
                    }
                });

        return layoutView;
    }

    private void initViews() {

        setHasOptionsMenu(true);

        shipperCallBackListener = this;

        mDisposable = new CompositeDisposable();
        mIfcmService = RetrofitFCMClient.getRetrofit().create(IFCMService.class);

        mShimmerLayout.startShimmerAnimation();
        mTxtOrderFilter.setVisibility(View.GONE);

        mRecyclerOrders.setHasFixedSize(true);
        mRecyclerOrders.setLayoutManager(new LinearLayoutManager(getActivity()));

        mAnimationController = AnimationUtils.loadLayoutAnimation(getActivity(), R.anim.raw_item_from_left);

        mDialog = new SpotsDialog.Builder().setContext(getActivity()).build();

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;

        SwipeRecyclerHelper swipeRecyclerHelper = new SwipeRecyclerHelper(
                getActivity(), mRecyclerOrders, width / 6) {
            @Override
            public void instantiateButton(RecyclerView.ViewHolder viewHolder, List<MButton> mListMButton) {

                mListMButton.add(new MButton(getActivity(), getString(R.string.directions), 30, 0,
                        getActivity().getColor(R.color.colorYellow), position -> {
                    Order order = ((OrdersAdapter) mRecyclerOrders.getAdapter()).getItemAtPosition(position);
                    // if be Shipping
                    if (order.getOrderStatus() == 1){
                        Common.currentOrderSelected = order;
                        startActivity(new Intent(getActivity(), TrackingOrderActivity.class));
                    }else{
                        Toast.makeText(getActivity(), new StringBuilder(getString(R.string.your_order_is)+" ")
                                .append(Common.convertStatus(order.getOrderStatus()))
                                .append(" "+getString(R.string.track_directions)), Toast.LENGTH_SHORT).show();
                    }
                }));

                mListMButton.add(new MButton(getActivity(), getString(R.string.edit), 30, 0,
                        getActivity().getColor(R.color.colorGreen), position -> {

                    showEditDialog(ordersAdapter.getItemAtPosition(position), position);

                }));

                mListMButton.add(new MButton(getActivity(), getString(R.string.remove), 30, 0,
                        getActivity().getColor(R.color.colorAccent), position -> {

                    Order order = ordersAdapter.getItemAtPosition(position);
                    removeOrder(position, order);
                }));

                mListMButton.add(new MButton(getActivity(), getString(R.string.call), 30, 0,
                        getActivity().getColor(R.color.colorBlue), position -> {
                    contactForUser(position);
                }));

            }
        };


    }

    private void showEditDialog(Order order, int position) {

        View dialogView;
        AlertDialog.Builder builder;

        // Shipping
        if (order.getOrderStatus() == 0) {
            dialogView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_shipping, null);
            // ReyclerView
            mRecyclerShipperSelected = dialogView.findViewById(R.id.recycler_shipper_selected);

            builder = new AlertDialog.Builder(getActivity(), android.R.style.Theme_Material_Light_NoActionBar_Fullscreen)
                    .setView(dialogView);

        }
        // cancelled
        else if (order.getOrderStatus() == -1) {
            dialogView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_cancelled, null);
            builder = new AlertDialog.Builder(getActivity())
                    .setView(dialogView);

        }
        // Shipped
        else {
            dialogView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_shipped, null);
            builder = new AlertDialog.Builder(getActivity())
                    .setView(dialogView);
        }

        // Text
        TextView txtStatus = dialogView.findViewById(R.id.txt_status);
        // button
        Button btnOk = dialogView.findViewById(R.id.btn_ok);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);
        // Radio
        RadioButton radioShipping = dialogView.findViewById(R.id.btn_radio_shipping);

        RadioButton radioShipped = dialogView.findViewById(R.id.btn_radio_shipped);
        RadioButton radioCancelled = dialogView.findViewById(R.id.btn_radio_cancelled);

        RadioButton radioRestorePlaced = dialogView.findViewById(R.id.btn_radio_restore_placed);
        RadioButton radioDelete = dialogView.findViewById(R.id.btn_radio_delete);


        // Set data
        Common.setSpanStringColor(getString(R.string.order_status),
                "( "+Common.convertStatus(order.getOrderStatus())+" )",
                txtStatus, getActivity().getColor(R.color.colorBlue1));


        // Create dialog
        AlertDialog dialog = builder.create();

        if (order.getOrderStatus() == 0)
            loadAllShippers(position, order, dialog, btnOk, btnCancel,
                    radioShipping, radioShipped, radioCancelled, radioDelete, radioRestorePlaced);
        else
            showDialog(position, order, dialog, btnOk, btnCancel,
                    radioShipping, radioShipped, radioCancelled, radioDelete, radioRestorePlaced);

        dialog.show();

    }

    private void loadAllShippers(int position, Order order, AlertDialog dialog, Button btnOk, Button btnCancel, RadioButton radioShipping, RadioButton radioShipped, RadioButton radioCancelled, RadioButton radioDelete, RadioButton radioRestorePlaced) {

        DatabaseReference mReference = FirebaseDatabase.getInstance().getReference(Common.KEY_SHIPPER_REFERANCE);
       // Load only shippers active by server app
        Query queryShipperState = mReference.orderByChild("active").equalTo(true);
        queryShipperState.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                List<Shipper> mLIstShipperSelected = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Shipper shipper = snapshot.getValue(Shipper.class);
                    shipper.setKey(snapshot.getKey());
                    mLIstShipperSelected.add(shipper);
                }

                shipperCallBackListener.onLoadOrderSuccess(position, order, mLIstShipperSelected,
                        dialog, btnOk, btnCancel, radioShipping, radioShipped,
                        radioCancelled, radioDelete, radioRestorePlaced);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                shipperCallBackListener.onLoadOrderFaield(databaseError.getMessage());
            }
        });

    }

    private void showDialog(int position, Order order, AlertDialog dialog, Button btnOk, Button btnCancel, RadioButton radioShipping, RadioButton radioShipped, RadioButton radioCancelled, RadioButton radioDelete, RadioButton radioRestorePlaced) {


        // custom dialog
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setGravity(Gravity.CENTER);

        // event
        btnCancel.setOnClickListener(view -> {
            dialog.dismiss();
        });

        btnOk.setOnClickListener(view -> {

            if (radioCancelled != null && radioCancelled.isChecked()) {
                updateOrder(position, order, -1);
                dialog.dismiss();
            }else if (radioShipping != null && radioShipping.isChecked()) {
                //updateOrder(position, order, 1);
                Shipper shipper = null;
                if (shipperSelectedAdapter != null){
                    shipper = shipperSelectedAdapter.getShipperSelected();
                    if (shipper != null){
                        createShippingOrder(position, shipper, order, dialog);
                    }else{
                        Toast.makeText(getActivity(), getString(R.string.please_select_shipper), Toast.LENGTH_SHORT).show();
                    }
                }
            }else if (radioShipped != null && radioShipped.isChecked()) {
                updateOrder(position, order, 2);
                dialog.dismiss();
            }else if (radioRestorePlaced != null && radioRestorePlaced.isChecked()) {
                updateOrder(position, order, 0);
                dialog.dismiss();
            }else if (radioDelete != null && radioDelete.isChecked()) {
                removeOrder(position, order);
                dialog.dismiss();
            }
        });
    }

    private void createShippingOrder(int position, Shipper shipper, Order order, AlertDialog dialog) {

        ShippingOrder shippingOrder = new ShippingOrder();

        shippingOrder.setShipperPhone(shipper.getPhone());
        shippingOrder.setShipperName(shipper.getName());
        shippingOrder.setOrder(order);
        shippingOrder.setStartTrip(false);
        shippingOrder.setCurrentLat(-1.0);
        shippingOrder.setCurrentLng(-1.0);

        FirebaseDatabase.getInstance().getReference()
                .child(Common.KEY_SHIPPING_ORDER_REFERANCE)
                .child(order.getKey()) // change sipping order push to order key
                .setValue(shippingOrder)
                .addOnFailureListener(e -> {
                    dialog.dismiss();
                    Toast.makeText(getActivity(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("SHIPPING_ORDER_ERROR",e.getMessage());
                }).addOnCompleteListener(task -> {
                    if (task.isSuccessful()){

                        dialog.dismiss();
                        // First get token of user
                        Log.d("SHIPPER_KEY", shipper.getKey());
                        FirebaseDatabase.getInstance().getReference()
                                .child(Common.KEY_TOKEN_REFERANCE)
                                .child(shipper.getKey())
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                        if (dataSnapshot.exists()) {
                                            Token token = dataSnapshot.getValue(Token.class);
                                            Map<String, String> mMapNotificationData = new HashMap<>();

                                            mMapNotificationData.put(Common.KEY_NOTFI_TITLE, getString(R.string.order_ship));
                                            mMapNotificationData.put(Common.KEY_NOTFI_CONTENT, new StringBuilder(getString(R.string.new_order_ship))
                                                    .append(" ").append(order.getUserPhone()).toString());

                                            FCMSendData fcmSendData = new FCMSendData(token.getToken(), mMapNotificationData);

                                            mDisposable.add(mIfcmService.sendNotification(fcmSendData)
                                                    .subscribeOn(Schedulers.io())
                                                    .observeOn(AndroidSchedulers.mainThread())
                                                    .subscribe(fcmResponse -> {

                                                        mDialog.dismiss();
                                                        if (fcmResponse.getSuccess() == 1) {
                                                            updateOrder(position, order,1);
                                                            //Toast.makeText(getActivity(), getString(R.string.update_order_success), Toast.LENGTH_SHORT).show();
                                                        } else {
                                                            Toast.makeText(getActivity(), getString(R.string.failed_send_notfi_to_shipper), Toast.LENGTH_SHORT).show();
                                                        }
                                                    }, throwable -> {
                                                        mDialog.dismiss();
                                                        Toast.makeText(getActivity(), "Failure to send notification", Toast.LENGTH_SHORT).show();
                                                    }));

                                        } else {
                                            mDialog.dismiss();
                                            Toast.makeText(getActivity(), "Token not fiund", Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                        mDialog.dismiss();
                                        Log.e("GET_TOKEN_ERROR", databaseError.getMessage());
                                    }
                                });
                        Toast.makeText(getActivity(), getString(R.string.order_sent_to_shipper), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateOrder(int position, Order order, int status) {

        if (!TextUtils.isEmpty(order.getKey())) {

            Map<String, Object> mMapUpdateOrder = new HashMap<>();
            mMapUpdateOrder.put("orderStatus", status);

            FirebaseDatabase.getInstance().getReference()
                    .child(Common.KEY_ORDER_REFERANCE)
                    .child(order.getKey())
                    .updateChildren(mMapUpdateOrder)
                    .addOnFailureListener(e -> {
                        Log.e("EDIT_ORDER_STATUS_ERROR", e.getMessage());
                    }).addOnCompleteListener(task -> {

                mDialog.show();
                // First get token of user
                FirebaseDatabase.getInstance().getReference()
                        .child(Common.KEY_TOKEN_REFERANCE)
                        .child(order.getUserId())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                if (dataSnapshot.exists()) {
                                    Token token = dataSnapshot.getValue(Token.class);
                                    Map<String, String> mMapNotificationData = new HashMap<>();
                                    mMapNotificationData.put(Common.KEY_NOTFI_TITLE, getString(R.string.order_update));
                                    mMapNotificationData.put(Common.KEY_NOTFI_CONTENT, new StringBuilder(getString(R.string.your_order))
                                            .append(" ").append(order.getKey()).append(" ")
                                            .append(getString(R.string.was_update)).append(" ")
                                            .append(Common.convertStatus(status)).toString());

                                    FCMSendData fcmSendData = new FCMSendData(token.getToken(), mMapNotificationData);

                                    mDisposable.add(mIfcmService.sendNotification(fcmSendData)
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe(fcmResponse -> {

                                                mDialog.dismiss();
                                                if (fcmResponse.getSuccess() == 1) {
                                                    Toast.makeText(getActivity(), getString(R.string.update_order_success), Toast.LENGTH_SHORT).show();
                                                } else {
                                                    Toast.makeText(getActivity(), getString(R.string.update_order_success) + " but failed to send notification", Toast.LENGTH_SHORT).show();
                                                }
                                            }, throwable -> {
                                                mDialog.dismiss();
                                                Toast.makeText(getActivity(), "Failure to send notification", Toast.LENGTH_SHORT).show();
                                            }));

                                } else {
                                    mDialog.dismiss();
                                    Toast.makeText(getActivity(), "Token not fiund", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                mDialog.dismiss();
                                Log.e("GET_TOKEN_ERROR", databaseError.getMessage());
                            }
                        });

                ordersAdapter.removeItem(position);
                ordersAdapter.notifyItemRemoved(position);
                updateOrdersCount();
                Toast.makeText(getActivity(), getString(R.string.update_order_success), Toast.LENGTH_SHORT).show();
            });
        } else
            Toast.makeText(getActivity(), getString(R.string.message_check_order_number), Toast.LENGTH_SHORT).show();
    }

    private void updateOrdersCount() {

        mTxtOrderFilter.setText(new StringBuilder("Orders (")
                .append(ordersAdapter.getItemCount())
                .append(")"));
    }

    private void removeOrder(int position, Order order) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.delete_order)
                .setMessage(R.string.delete_order_message)
                .setNegativeButton(R.string.cancel, (dialog, which) -> {
                    dialog.dismiss();
                }).setPositiveButton(R.string.delete_order, (dialog, which) -> {
                    // Order order = ordersAdapter.getItemAtPosition(position);
                    FirebaseDatabase.getInstance().getReference()
                            .child(Common.KEY_ORDER_REFERANCE)
                            .child(order.getKey())
                            .removeValue()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    ordersAdapter.removeItem(position);
                                    mTxtOrderFilter.setVisibility(View.VISIBLE);
                                    updateOrdersCount();
                                    dialog.dismiss();
                                    Toast.makeText(getActivity(), getString(R.string.delete_order_success), Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(e -> {
                        Log.e("DELETE_ORDER_ERROR", e.getMessage());
                    });
                });
        AlertDialog dialog = builder.create();
        dialog.show();

        Button negativeBtn = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        negativeBtn.setTextColor(Color.GRAY);
        Button PositiveBtn = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        PositiveBtn.setTextColor(Color.RED);

    }

    private void contactForUser(int position) {

        Dexter.withActivity(getActivity())
                .withPermission(Manifest.permission.CALL_PHONE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {

                        Order order = ordersAdapter.getItemAtPosition(position);
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_DIAL);
                        intent.setData(Uri.parse(new StringBuilder("tel: ")
                                .append(order.getUserPhone()).toString()));
                        startActivity(intent);
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Toast.makeText(getActivity(), "You must accept " + response.getPermissionName(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                    }
                }).check();
        ;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.order_filter_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.action_filter) {
            BottomSheetOrderFragment sheetOrderFragment = BottomSheetOrderFragment.getInstance();
            sheetOrderFragment.show(getActivity().getSupportFragmentManager(), "OrderFilter");
            return true;
        } else
            return super.onOptionsItemSelected(item);
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onLoadOrdersEvent(LoadEventOrder event) {

        ordersViewModel.loadOrdersByStatus(event.getStatus());
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        if (EventBus.getDefault().hasSubscriberForEvent(LoadEventOrder.class))
            EventBus.getDefault().removeStickyEvent(LoadEventOrder.class);
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().postSticky(new ChangeMenuClick(true));
        mDisposable.dispose();
        super.onDestroy();
    }

    @Override
    public void onLoadOrderSuccess(List<Shipper> mListShippers) {

    }

    @Override
    public void onLoadOrderSuccess(int position, Order order, List<Shipper> mListShippers, AlertDialog dialog, Button btnOk, Button btnCancel, RadioButton radioShipping, RadioButton radioShipped, RadioButton radioCancelled, RadioButton radioDelete, RadioButton radiorestorePlaced) {

        if (mRecyclerShipperSelected != null){

            mRecyclerShipperSelected.setHasFixedSize(true);
            LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
            mRecyclerShipperSelected.setLayoutManager(layoutManager);
            mRecyclerShipperSelected.addItemDecoration(new DividerItemDecoration(getActivity(), layoutManager.getOrientation()));

            shipperSelectedAdapter = new ShipperSelectedAdapter(getActivity(), mListShippers);
            mRecyclerShipperSelected.setAdapter(shipperSelectedAdapter);
        }

        showDialog(position, order, dialog, btnOk, btnCancel, radioShipping, radioShipped, radioCancelled, radioDelete, radiorestorePlaced);
    }

    @Override
    public void onLoadOrderFaield(String error) {
        Toast.makeText(getActivity(), error, Toast.LENGTH_SHORT).show();
    }
}
