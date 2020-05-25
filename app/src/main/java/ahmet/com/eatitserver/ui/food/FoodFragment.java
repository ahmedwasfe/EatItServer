package ahmet.com.eatitserver.ui.food;

import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import ahmet.com.eatitserver.adapter.FoodAdapter;
import ahmet.com.eatitserver.common.Common;
import ahmet.com.eatitserver.common.SwipeRecyclerHelper;
import ahmet.com.eatitserver.EditSizeAddonActivity;
import ahmet.com.eatitserver.eventBus.ChangeMenuClick;
import ahmet.com.eatitserver.eventBus.EditSizeAddonEvent;
import ahmet.com.eatitserver.eventBus.ToastEvent;
import ahmet.com.eatitserver.eventBus.UpdateFood;
import ahmet.com.eatitserver.eventBus.UpdateFoodSize;
import ahmet.com.eatitserver.model.Food.Addon;
import ahmet.com.eatitserver.model.Food.Food;
import ahmet.com.eatitserver.model.Food.FoodSize;
import ahmet.com.eatitserver.R;
import ahmet.com.eatitserver.services.IFCMService;
import ahmet.com.eatitserver.services.RetrofitFCMClient;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dmax.dialog.SpotsDialog;
import io.reactivex.disposables.CompositeDisposable;
import io.supercharge.shimmerlayout.ShimmerLayout;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.SEARCH_SERVICE;

public class FoodFragment extends Fragment {

    @BindView(R.id.recycler_food)
    RecyclerView mRecyclerFoods;
    @BindView(R.id.shimmer_layout_food)
    ShimmerLayout mShimmerLayout;

    private ImageView mImgFood;

    private FoodViewModel foodViewModel;

    private LayoutAnimationController mAnimationController;

    private FoodAdapter mFoodAdapter;
    private List<Food> mListFoods;

    private Uri mImageUri;

    private CompositeDisposable mDisposable;
    private IFCMService mIfcmService;

    private android.app.AlertDialog mDialog;

    @OnClick(R.id.fab_add_new_food) void onClickAddFood(){
        showAddFoodDialog();
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        foodViewModel = new ViewModelProvider(this).get(FoodViewModel.class);

        View layoutView = inflater.inflate(R.layout.fragment_food, container, false);

        ButterKnife.bind(this, layoutView);

        initViews();

        foodViewModel.getmMutabMessageError()
                .observe(getViewLifecycleOwner(), s -> {
                    Toast.makeText(getActivity(), s, Toast.LENGTH_SHORT).show();
                });

        foodViewModel.getMutabListFoods()
                .observe(getViewLifecycleOwner(), mListFood -> {

                    if (mListFood != null) {

                        mListFoods = mListFood;
                        mFoodAdapter = new FoodAdapter(getActivity(), mListFoods);

                        mRecyclerFoods.setAdapter(mFoodAdapter);
                        mRecyclerFoods.setLayoutAnimation(mAnimationController);

                        mShimmerLayout.stopShimmerAnimation();
                        mShimmerLayout.setVisibility(View.GONE);
                    }else{

                        mShimmerLayout.stopShimmerAnimation();
                        mShimmerLayout.setVisibility(View.GONE);
                    }

                });

        return layoutView;
    }

    private void initViews() {

        // Enable menu in food fragment
        setHasOptionsMenu(true);

        if (Common.currentCategory == null)
            showAddFoodDialog();
          else
            ((AppCompatActivity)getActivity())
                    .getSupportActionBar()
                    .setTitle(Common.currentCategory.getName());

          mDisposable = new CompositeDisposable();
          mIfcmService = RetrofitFCMClient.getRetrofit().create(IFCMService.class);

        mShimmerLayout.startShimmerAnimation();

        mAnimationController = AnimationUtils.loadLayoutAnimation(getActivity(), R.anim.raw_item_from_left);

        mRecyclerFoods.setHasFixedSize(true);
        mRecyclerFoods.setLayoutManager(new StaggeredGridLayoutManager(1, LinearLayout.VERTICAL));

        mDialog = new SpotsDialog.Builder().setContext(getActivity()).build();


        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;

        SwipeRecyclerHelper swipeRecyclerHelper = new SwipeRecyclerHelper(
                getActivity(), mRecyclerFoods, width/6) {
            @Override
            public void instantiateButton(RecyclerView.ViewHolder viewHolder, List<MButton> mListMButton) {

                mListMButton.add(new MButton(getActivity(), getString(R.string.update), 30, 0,
                        getActivity().getColor(R.color.colorGreen), position -> {
                    Food food = mFoodAdapter.getItemAtPosition(position);
                    if (food.getPositionInList() == -1)
                        showUpdateDialog(position, food);
                    else
                        showUpdateDialog(food.getPositionInList(), food);
                }));


                mListMButton.add(new MButton(getActivity(), getString(R.string.delete), 30, 0,
                        getActivity().getColor(R.color.colorAccent),position -> {

                    if (mListFoods != null)
                        Common.currentFood = mListFoods.get(position);
                        deleteFood(position);

                }));


                mListMButton.add(new MButton(getActivity(), getString(R.string.size), 30,0,
                       getActivity().getColor(R.color.colorBlue), position -> {

                    Food food = mFoodAdapter.getItemAtPosition(position);
                    if (food.getPositionInList() == -1)
                        Common.currentFood = mListFoods.get(position);
                    else
                        Common.currentFood = food;

                    startActivity(new Intent(getActivity(), EditSizeAddonActivity.class));

                    if (food.getPositionInList() == -1)
                        EventBus.getDefault().postSticky(new EditSizeAddonEvent(false, position));
                    else
                        EventBus.getDefault().postSticky(new EditSizeAddonEvent(false, food.getPositionInList()));

                }));

                mListMButton.add(new MButton(getActivity(), getString(R.string.addon), 30,0,
                        getActivity().getColor(R.color.colorYellow), position -> {

                    Food food = mFoodAdapter.getItemAtPosition(position);
                    if (food.getPositionInList() == -1)
                        Common.currentFood = mListFoods.get(position);
                    else
                        Common.currentFood = food;
                    startActivity(new Intent(getActivity(), EditSizeAddonActivity.class));

                    if (food.getPositionInList() == -1)
                        EventBus.getDefault().postSticky(new EditSizeAddonEvent(true, position));
                    else
                        EventBus.getDefault().postSticky(new EditSizeAddonEvent(true, food.getPositionInList()));

                }));

            }
        };
    }

    private void showUpdateDialog(int position, Food food) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.update_food);
        View layoutView = LayoutInflater.from(getActivity())
                .inflate(R.layout.dialog_update_food, null);
        MaterialEditText mInputFoodName = layoutView.findViewById(R.id.input_update_food_name);
        MaterialEditText mInputFoodPrice = layoutView.findViewById(R.id.input_update_food_price);
        MaterialEditText mInputDescription = layoutView.findViewById(R.id.input_update_food_description);
        mImgFood = layoutView.findViewById(R.id.img_update_food_image);

        mImgFood.setImageURI(Uri.parse(Common.URL_IMAGE_DEFAULT));

        // Set Data
        mInputFoodName.setText(new StringBuilder("").append(food.getName()));
        mInputFoodPrice.setText(new StringBuilder("").append(food.getPrice()));
        mInputDescription.setText(new StringBuilder("").append(food.getDescription()));
        Picasso.get().load(food.getImage()).into(mImgFood);

        // Set event
        mImgFood.setOnClickListener(v -> {

            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent, getString(R.string.select_image)),
                    Common.CODE_REQUEST_UPDATE_FOOD_IMAGE);
        });

        builder.setNegativeButton(R.string.cancel, (dialog, which) -> {
           dialog.dismiss();
        }).setPositiveButton(R.string.update_food, (dialog, which) -> {

            String foodName = mInputFoodName.getText().toString();
            String foodPrice = mInputFoodPrice.getText().toString();
            String foodDescription = mInputDescription.getText().toString();

            if (TextUtils.isEmpty(foodName)){
                mInputFoodName.setError(getString(R.string.please_enter_food_name));
                return;
            }

            Food foods = food;
            foods.setName(foodName);
            foods.setDescription(foodDescription);
            foods.setPrice(TextUtils.isEmpty(foodPrice) ? 0 : Long.parseLong(foodPrice));

            if (mImageUri != null){
                uploadImage(foods, position);
            }else{
                updateFood(Common.currentCategory.getFoods(), false);
            }
        });

        builder.setView(layoutView);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showAddFoodDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.add_food);
        View layoutView = LayoutInflater.from(getActivity())
                .inflate(R.layout.dialog_update_food, null);

        MaterialEditText mInputFoodName = layoutView.findViewById(R.id.input_update_food_name);
        MaterialEditText mInputFoodPrice = layoutView.findViewById(R.id.input_update_food_price);
        MaterialEditText mInputDescription = layoutView.findViewById(R.id.input_update_food_description);
        mImgFood = layoutView.findViewById(R.id.img_update_food_image);

        mImgFood.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
        mImgFood.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
        mImgFood.setImageURI(Uri.parse(Common.URL_IMAGE_DEFAULT));

        Picasso.get().load(Common.URL_IMAGE_DEFAULT).into(mImgFood);


        // Set event
        mImgFood.setOnClickListener(v -> {

            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent, getString(R.string.select_image)),
                    Common.CODE_REQUEST_UPDATE_FOOD_IMAGE);
        });

        builder.setNegativeButton(R.string.cancel, (dialog, which) -> {
            dialog.dismiss();
        }).setPositiveButton(R.string.add_food, (dialog, which) -> {

            String foodName = mInputFoodName.getText().toString();
            long foodPrice = Long.valueOf(mInputFoodPrice.getText().toString());
            String foodDescription = mInputDescription.getText().toString();

            if (TextUtils.isEmpty(foodName)){
                mInputFoodName.setError(getString(R.string.please_enter_food_name));
                return;
            }

            List<Food> mListAddNewFood = new ArrayList<>();
            List<Addon> mListAddAddon = new ArrayList<>();
            List<FoodSize> mListAddSize = new ArrayList<>();

            // set data default
            Addon addon = new Addon();
            addon.setName("Oil Olives");
            addon.setPrice(1L);
            mListAddAddon.add(addon);

            FoodSize foodSize = new FoodSize();
            foodSize.setName("Small");
            foodSize.setPrice(1L);
            mListAddSize.add(foodSize);

            Food food = new Food();
            food.setId(new StringBuilder(Common.currentCategory.getName())
                    .append("_").append(UUID.randomUUID()).toString());
            food.setName(foodName);
            food.setPrice(foodPrice);
            food.setDescription(foodDescription);
            food.setImage(Common.URL_IMAGE_DEFAULT);
            food.setAddon(mListAddAddon);
            food.setSize(mListAddSize);


            mListAddNewFood.add(food);

            if (mImageUri != null){
                uploadAddImage(food);
            }else{

                addNewFood(mListAddNewFood);
            }
        });

        builder.setView(layoutView);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void deleteFood(int position) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.delete_food)
                .setMessage(R.string.delete_food_message)
                .setPositiveButton(R.string.delete, (dialog, which) -> {

                    // get item in adapter
                    Food food = mFoodAdapter.getItemAtPosition(position);
                    // If = -1 default do nothing
                    if (food.getPositionInList() == -1)
                        Common.currentCategory.getFoods().remove(position);
                    else
                        // Remove by index we was save
                        Common.currentCategory.getFoods().remove(food.getPositionInList());
                        updateFood(Common.currentCategory.getFoods(), true);

                    String notifTitle = getString(R.string.delete_food);
                    String notifContent = Common.currentServer.getName() + " "+
                            getString(R.string.deleted_food);

                    Common.sendNoification(getActivity(), notifTitle, notifContent, mDisposable, mIfcmService);

                }).setNegativeButton(R.string.cancel, (dialog, which) -> {
                    dialog.dismiss();
                });

        AlertDialog dialog = builder.create();
        dialog.show();

        Button negativeBtn = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        negativeBtn.setTextColor(Color.GRAY);
        Button PositiveBtn = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        PositiveBtn.setTextColor(Color.RED);


    }

    private void uploadImage(Food food, int position) {

        mDialog.setMessage(getString(R.string.uploading));
        mDialog.show();
        String uniqueImageName = UUID.randomUUID().toString();

        StorageReference storageImageFolder = FirebaseStorage.getInstance()
                .getReference()
                .child(Common.KEY_IMAGES_FOOD_PATH+uniqueImageName);
        storageImageFolder.putFile(mImageUri)
                .addOnFailureListener(e -> {
                    mDialog.dismiss();
                    Log.e("UPLOAD_IMAGE_ERROR", e.getMessage());
                }).addOnCompleteListener(task -> {
            mDialog.dismiss();
            storageImageFolder.getDownloadUrl()
                    .addOnSuccessListener(uri -> {
                        food.setImage(uri.toString());
                        Common.currentCategory.getFoods().set(position, food);
                        updateFood(Common.currentCategory.getFoods(), false);
                    });
        }).addOnProgressListener(taskSnapshot -> {
            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
            mDialog.setMessage(new StringBuilder(getString(R.string.uploading_done)+" ").append(progress).append("%"));
        });


    }

    private void uploadAddImage(Food food) {

        mDialog.setMessage(getString(R.string.uploading));
        mDialog.show();
        String uniqueImageName = UUID.randomUUID().toString();

        StorageReference storageImageFolder = FirebaseStorage.getInstance()
                .getReference()
                .child(Common.KEY_IMAGES_FOOD_PATH+uniqueImageName);
        storageImageFolder.putFile(mImageUri)
                .addOnFailureListener(e -> {
                    mDialog.dismiss();
                    Log.e("UPLOAD_IMAGE_ERROR", e.getMessage());
                }).addOnCompleteListener(task -> {
            mDialog.dismiss();
            storageImageFolder.getDownloadUrl()
                    .addOnSuccessListener(uri -> {
                        List<Food> mListAddFood = new ArrayList<>();
                        food.setImage(uri.toString());
//                        mListAddFood.add(food);
//                        addNewFood(mListAddFood);
                        mFoodAdapter.addNewFood(food);
                    });
        }).addOnProgressListener(taskSnapshot -> {
            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
            mDialog.setMessage(new StringBuilder(getString(R.string.uploading_done)+" ").append(progress).append("%"));
        });


    }

    private void updateFood(List<Food> foods, boolean isDelete) {

        Map<String, Object> mMapFoods = new HashMap<>();
        mMapFoods.put(Common.KEY_FOOD_CHILD, foods);

        FirebaseDatabase.getInstance().getReference()
                .child(Common.KEY_CAEGORIES_REFERANCE)
                .child(Common.currentCategory.getMenu_id())
                .updateChildren(mMapFoods)
                .addOnFailureListener(e -> {
                    Log.e("UPDATE_FOOD_ERROR", e.getMessage());
                }).addOnCompleteListener(task -> {
                    if (task.isSuccessful()){

                        String notifTitle = getString(R.string.update_food);
                        String notifContent = Common.currentServer.getName() + " "+
                                getString(R.string.updated_food);

                        Common.sendNoification(getActivity(), notifTitle, notifContent, mDisposable, mIfcmService);

                        foodViewModel.getMutabListFoods();
                        EventBus.getDefault().postSticky(new ToastEvent(!isDelete, true));
                    }
                });
    }

    private void addNewFood(List<Food> foods) {

        FirebaseDatabase.getInstance().getReference()
                .child(Common.KEY_CAEGORIES_REFERANCE)
                .child(Common.currentCategory.getMenu_id())
                .child(Common.KEY_FOOD_CHILD)
                .setValue(foods)
                .addOnFailureListener(e -> {
                    Log.e("DELETE_FOOD_ERROR", e.getMessage());
                }).addOnCompleteListener(task -> {
            if (task.isSuccessful()){

                String notifTitle = getString(R.string.add_food);
                String notifContent = Common.currentServer.getName() + " "+
                        getString(R.string.added_new_food);

                Common.sendNoification(getActivity(), notifTitle, notifContent, mDisposable, mIfcmService);

                foodViewModel.getMutabListFoods();
                Toast.makeText(getActivity(), "Add food success", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveFood(int itemPosition, Food food){

        Common.currentCategory.getFoods().set(itemPosition, food);
        Map<String, Object> mapAddFood = new HashMap<>();
        mapAddFood.put(Common.currentCategory.getMenu_id(), Common.currentCategory.getFoods());
        FirebaseDatabase.getInstance().getReference()
                .child(Common.KEY_CAEGORIES_REFERANCE)
                .child(Common.currentCategory.getMenu_id())
                .updateChildren(mapAddFood)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful())
                        Toast.makeText(getActivity(), "Add food suuceee", Toast.LENGTH_SHORT).show();
                }).addOnFailureListener(e -> {
            Log.e("ADD_FOOD_ERROR", e.getMessage());
        });
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {

        inflater.inflate(R.menu.food_menu, menu);

        MenuItem menuItem = menu.findItem(R.id.action_search);

        SearchManager searchManager = (SearchManager) getActivity().getSystemService(SEARCH_SERVICE);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));

        // Event
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                startSearchInFood(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        // Clear text when click to clear button on SearchView
        ImageView closeBtnSearch = searchView.findViewById(R.id.search_close_btn);
        closeBtnSearch.setOnClickListener(view -> {

            EditText inputTextSearch = searchView.findViewById(R.id.search_src_text);
            // Clear text
            inputTextSearch.setText("");
            // Clear query
            searchView.setQuery("", false);
            // Collapse the action view
            searchView.onActionViewCollapsed();
            // Collapse the action width
            menuItem.collapseActionView();
            // Restore result to original
            foodViewModel.getMutabListFoods()
                    .setValue(Common.currentCategory.getFoods());
        });

    }

    private void startSearchInFood(String query) {

        List<Food> mListFoodResult = new ArrayList<>();
        for (int i = 0; i < Common.currentCategory.getFoods().size(); i++) {
            Food food = Common.currentCategory.getFoods().get(i);
            if (food.getName().toLowerCase().contains(query.toLowerCase())) {
                // Save index
                food.setPositionInList(i);
                mListFoodResult.add(food);
            }
        }

        // Get search result
        foodViewModel.getMutabListFoods()
                .setValue(mListFoodResult);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Common.CODE_REQUEST_UPDATE_FOOD_IMAGE)
            if (resultCode == RESULT_OK){
                mImageUri = data.getData();
                mImgFood.setImageURI(mImageUri);
            }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onAddFood(UpdateFood event){
        if (event.getFood() != null)
            Common.currentCategory.setFoods(event.getFood());
    }


    // Register event
    @Override
    public void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
    }

    // unRegister event
    @Override
    public void onStop() {
        EventBus.getDefault().removeStickyEvent(UpdateFoodSize.class);
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().postSticky(new ChangeMenuClick(true));
        mDisposable.clear();
        super.onDestroy();
    }
}
