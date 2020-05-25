package ahmet.com.eatitserver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.database.FirebaseDatabase;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.syd.oden.circleprogressdialog.view.RotateLoading;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ahmet.com.eatitserver.adapter.AddonFoodAdapter;
import ahmet.com.eatitserver.adapter.SizeFoodAdapter;
import ahmet.com.eatitserver.common.Common;
import ahmet.com.eatitserver.common.SwipeRecyclerHelper;
import ahmet.com.eatitserver.eventBus.EditSizeAddonEvent;
import ahmet.com.eatitserver.eventBus.SelectFoodAddon;
import ahmet.com.eatitserver.eventBus.SelectFoodSize;
import ahmet.com.eatitserver.eventBus.UpdateFoodAddon;
import ahmet.com.eatitserver.eventBus.UpdateFoodSize;
import ahmet.com.eatitserver.model.Food.Addon;
import ahmet.com.eatitserver.model.Food.FoodSize;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class EditSizeAddonActivity extends AppCompatActivity {

    @BindView(R.id.toolbar_addon_size)
    Toolbar mToolbar;
    @BindView(R.id.input_addon_size_name)
    MaterialEditText mInputAddonSizeName;
    @BindView(R.id.input_addon_size_price)
    MaterialEditText mInputAddonSizePrice;
    @BindView(R.id.recycler_addon_size)
    RecyclerView mRecyclerAddonSize;
    @BindView(R.id.btn_create_addon_size)
    Button mBtnCreate;
    @BindView(R.id.btn_edit_addon_size)
    Button mBtnEdit;
    @BindView(R.id.progress_loading_edit_addon_size)
    RotateLoading mRotateLoading;

    private SizeFoodAdapter sizeFoodAdapter;
    private AddonFoodAdapter addonFoodAdapter;

    private int foodEditPosition = -1;
    private boolean isNeedSave = false;
    private boolean isAddon = false;

    @OnClick(R.id.btn_create_addon_size) void onCreateAddonSize(){

        mRotateLoading.start();

        String foodSizeName = mInputAddonSizeName.getText().toString();
        long foodSizePrice = Long.valueOf(mInputAddonSizePrice.getText().toString());

        // Create size
        if (!isAddon){
            if (sizeFoodAdapter != null){
                FoodSize foodSize = new FoodSize();
                foodSize.setName(foodSizeName);
                foodSize.setPrice(foodSizePrice);

                sizeFoodAdapter.addNewFoodSize(foodSize);
                mRotateLoading.stop();
            }
        // Create addon
        }else{
            if (addonFoodAdapter != null){
                Addon addon = new Addon();
                addon.setName(foodSizeName);
                addon.setPrice(foodSizePrice);

                addonFoodAdapter.addNewFoodAddon(addon);
                mRotateLoading.stop();
            }
        }
    }

    @OnClick(R.id.btn_edit_addon_size) void onEditAddonSize(){

        mRotateLoading.start();
        String foodSizeName = mInputAddonSizeName.getText().toString();
        long foodSizePrice = Long.valueOf(mInputAddonSizePrice.getText().toString());
        // Edit size
        if (!isAddon){
            if (sizeFoodAdapter != null){
                FoodSize foodSize = new FoodSize();
                foodSize.setName(foodSizeName);
                foodSize.setPrice(foodSizePrice);

                sizeFoodAdapter.editFoodSize(foodSize);
                mRotateLoading.stop();
            }
        // Edit addon
        }else{
            Addon addon = new Addon();
            addon.setName(foodSizeName);
            addon.setPrice(foodSizePrice);

            addonFoodAdapter.editFoodAddon(addon);
            mRotateLoading.stop();

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_size_addon);

        ButterKnife.bind(this);

        initViews();
    }

    private void initViews() {

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mRotateLoading.stop();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerAddonSize.setHasFixedSize(true);
        mRecyclerAddonSize.setLayoutManager(layoutManager);
        mRecyclerAddonSize.addItemDecoration(new DividerItemDecoration(this, layoutManager.getOrientation()));

        SwipeRecyclerHelper swipeRecyclerHelper = new SwipeRecyclerHelper(
                this, mRecyclerAddonSize,150) {
            @Override
            public void instantiateButton(RecyclerView.ViewHolder viewHolder, List<MButton> mListMButton) {
                mListMButton.add(new MButton(EditSizeAddonActivity.this, getString(R.string.delete),
                        30,0, Color.parseColor("#F44336"), position -> {

                    // Delete size
                    if (!isAddon) {
                        Common.currentFood.getSize().remove(position);
                        sizeFoodAdapter.notifyItemRemoved(position);
                        UpdateFoodSize updateFoodSize = new UpdateFoodSize();
                        updateFoodSize.setFoodSizeList(Common.currentFood.getSize());
                        EventBus.getDefault().postSticky(Common.currentFood.getSize());
                    // Delete addon
                    }else{
                        Common.currentFood.getAddon().remove(position);
                        addonFoodAdapter.notifyItemRemoved(position);
                        UpdateFoodAddon updateFoodAddon = new UpdateFoodAddon();
                        updateFoodAddon.setFoodAddonList(Common.currentFood.getAddon());
                        EventBus.getDefault().postSticky(Common.currentFood.getAddon());
                    }
                }));
            }
        };
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.addon_size_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){

            case R.id.action_save_addon_size:
                saveAddonSize();
                break;
            case android.R.id.home:
                unSaveAddonSize();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void saveAddonSize() {

        mRotateLoading.start();

        if (foodEditPosition != -1){
            // Save food to category
            Common.currentCategory.getFoods().set(foodEditPosition, Common.currentFood);
            Map<String, Object> mMapUpdateData = new HashMap<>();
            mMapUpdateData.put(Common.KEY_FOOD_CHILD, Common.currentCategory.getFoods());

            FirebaseDatabase.getInstance().getReference()
                    .child(Common.KEY_CAEGORIES_REFERANCE)
                    .child(Common.currentCategory.getMenu_id())
                    .updateChildren(mMapUpdateData)
                    .addOnFailureListener(e -> {
                        Log.e("UPDATE_FOOD_SIZE_ERROR", e.getMessage());
                        mRotateLoading.stop();
                    })
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            mRotateLoading.stop();
                            Toast.makeText(this, getString(R.string.reload_success), Toast.LENGTH_SHORT).show();
                            isNeedSave = false;
                            mInputAddonSizeName.setText("");
                            mInputAddonSizePrice.setText("0");
                        }
                    });
        }
    }

    private void unSaveAddonSize() {

        if (isNeedSave){

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.cancel)
                    .setMessage(R.string.cancel_save_addon_size)
                    .setNegativeButton(R.string.cancel, (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .setPositiveButton(R.string.ok, (dialog, which) -> {
                        isNeedSave = false;
                        closeActivity();
                    });
            AlertDialog dialog = builder.create();
            dialog.show();;
        }else {
            closeActivity();
        }
    }

    private void closeActivity() {
        mInputAddonSizeName.setText("");
        mInputAddonSizePrice.setText("0");
        finish();
    }

    // Receive event
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onAddonSizeReceived(EditSizeAddonEvent event){
        // if event is size
        if (!event.isAddon()){
            // if size is not empty
            if (Common.currentFood.getSize() != null){
                // set data in adapter
                sizeFoodAdapter = new SizeFoodAdapter(this, Common.currentFood.getSize());
                // Save food edit to update
                foodEditPosition = event.getPosition();
                // set Adapter in recyclerview
                mRecyclerAddonSize.setAdapter(sizeFoodAdapter);

                isAddon = event.isAddon();
            }
        }else{ // is addon
            // if addon is not empty
            if (Common.currentFood.getAddon() != null){
                // set data in adapter
                addonFoodAdapter = new AddonFoodAdapter(this, Common.currentFood.getAddon());
                // Save food edit to update
                foodEditPosition = event.getPosition();
                // set Adapter in recyclerview
                mRecyclerAddonSize.setAdapter(addonFoodAdapter);

                isAddon = event.isAddon();
            }
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onUpdateFoodSize(UpdateFoodSize event){

        if (event.getFoodSizeList() != null){
            isNeedSave = true;
            Common.currentFood.setSize(event.getFoodSizeList());
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onUpdateFoodAddon(UpdateFoodAddon event){

        if (event.getFoodAddonList() != null){
            isNeedSave = true;
            Common.currentFood.setAddon(event.getFoodAddonList());
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onSelectFoodSize(SelectFoodSize event){

        if (event.getFoodSize() != null){
            mInputAddonSizeName.setText(event.getFoodSize().getName());
            mInputAddonSizePrice.setText(String.valueOf(event.getFoodSize().getPrice()));

            mBtnEdit.setEnabled(true);

        }else{
            mBtnEdit.setEnabled(false);
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onSelectFoodAddon(SelectFoodAddon event){

        if (event.getAddon() != null){
            mInputAddonSizeName.setText(event.getAddon().getName());
            mInputAddonSizePrice.setText(String.valueOf(event.getAddon().getPrice()));

            mBtnEdit.setEnabled(true);

        }else{
            mBtnEdit.setEnabled(false);
        }
    }

    // Register event
    @Override
    protected void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
    }

    // unRegister event
    @Override
    protected void onStop() {
        EventBus.getDefault().removeStickyEvent(UpdateFoodSize.class);
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
        super.onStop();
    }
}
