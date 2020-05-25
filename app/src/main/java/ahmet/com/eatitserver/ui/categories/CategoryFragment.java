package ahmet.com.eatitserver.ui.categories;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import ahmet.com.eatitserver.adapter.CategoriesAdapter;
import ahmet.com.eatitserver.common.Common;
import ahmet.com.eatitserver.common.SwipeRecyclerHelper;
import ahmet.com.eatitserver.eventBus.ToastEvent;
import ahmet.com.eatitserver.model.Category;
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

public class CategoryFragment extends Fragment {

    @BindView(R.id.recycler_categoeies)
    RecyclerView mRecyclerCategories;
    @BindView(R.id.shimmer_layout_category)
    ShimmerLayout mShimmerLayout;

    private ImageView mImgCategory;

    private CategoriesAdapter categoriesAdapter;

    private LayoutAnimationController mAnimationController;

    private CategoryViewModel categoryViewModel;

    private List<Category> mListUpdateCategory;

    private Uri mImageUri;

    private CompositeDisposable mDisposable;
    private IFCMService mIfcmService;

    private android.app.AlertDialog mDialog;

    @OnClick(R.id.fab_add_new_category)
    void onClickAddCategory() {
        showAddCategoryDialog();
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        categoryViewModel = new ViewModelProvider(this).get(CategoryViewModel.class);

        View layoutView = inflater.inflate(R.layout.fragment_category, container, false);

        ButterKnife.bind(this, layoutView);

        initViews();

        categoryViewModel.getMutableLMessageError()
                .observe(getViewLifecycleOwner(), s -> {
                    Toast.makeText(getActivity(), s, Toast.LENGTH_SHORT).show();
                });

        categoryViewModel.getMutableListCategory()
                .observe(getViewLifecycleOwner(), mListCategory -> {

                    if (mListCategory != null) {

                        mShimmerLayout.stopShimmerAnimation();
                        mShimmerLayout.setVisibility(View.GONE);

                        mListUpdateCategory = mListCategory;
                        categoriesAdapter = new CategoriesAdapter(getActivity(), mListUpdateCategory);
                        mRecyclerCategories.setAdapter(categoriesAdapter);
                        mRecyclerCategories.setLayoutAnimation(mAnimationController);

                    }
                });

        return layoutView;
    }

    private void initViews() {

        mDisposable = new CompositeDisposable();
        mIfcmService = RetrofitFCMClient.getRetrofit().create(IFCMService.class);

        mShimmerLayout.startShimmerAnimation();

        mAnimationController = AnimationUtils.loadLayoutAnimation(getActivity(), R.anim.raw_item_from_left);

        mRecyclerCategories.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerCategories.setLayoutManager(layoutManager);
        mRecyclerCategories.addItemDecoration(new DividerItemDecoration(getActivity(), layoutManager.getOrientation()));

        mDialog = new SpotsDialog.Builder().setContext(getActivity()).build();

        SwipeRecyclerHelper swipeRecycler = new SwipeRecyclerHelper(
                getActivity(), mRecyclerCategories, 150
        ) {
            @Override
            public void instantiateButton(RecyclerView.ViewHolder viewHolder, List<MButton> mListMButton) {
                MButton mButton = new MButton(getActivity(), getString(R.string.update), 30, 0,
                        Color.parseColor("#158652"), position -> {

                    Common.currentCategory = mListUpdateCategory.get(position);
                    showUpdateDialog();

                });
                mListMButton.add(mButton);

                MButton mButton1 = new MButton(getActivity(), getString(R.string.delete), 30, 0,
                        Color.parseColor("#F44336"), position -> {
                    if (mListUpdateCategory != null)
                        Common.currentCategory = mListUpdateCategory.get(position);
                    deleteCategory();
                });
                mListMButton.add(mButton1);
            }
        };

    }


    private void deleteCategory() {

        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.delete_category)
                .setMessage(R.string.delete_category_message)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    FirebaseDatabase.getInstance().getReference()
                            .child(Common.KEY_CAEGORIES_REFERANCE)
                            .child(Common.currentCategory.getMenu_id())
                            .removeValue()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {

                                    String notifTitle = getString(R.string.delete_category);
                                    String notifContent = Common.currentServer.getName() + " "+
                                            getString(R.string.deleted_category);

                                    Common.sendNoification(getActivity(), notifTitle, notifContent,
                                            mDisposable, mIfcmService);
                                    categoryViewModel.loadCategorise();
                                    Toast.makeText(getActivity(), "Delete Category success", Toast.LENGTH_SHORT).show();
                                    EventBus.getDefault().postSticky(new ToastEvent(false,false));
                                }
                            });
                }).setNegativeButton(R.string.cancel, (dialog, which) -> {
            dialog.dismiss();
        }).show();


    }

    private void showUpdateDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.update_category);
        View layoutView = LayoutInflater.from(getActivity())
                .inflate(R.layout.dialog_update_category, null);
        MaterialEditText mInputCategoryName = layoutView.findViewById(R.id.input_update_category_name);
        mImgCategory = layoutView.findViewById(R.id.img_update_category_image);

        // Set Data

        mInputCategoryName.setText(new StringBuilder("").append(Common.currentCategory.getName()));
        Picasso.get().load(Common.currentCategory.getImage()).into(mImgCategory);

        // Set event
        mImgCategory.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent, getString(R.string.select_image)),
                    Common.CODE_REQUEST_UPDATE_CATEGORY_IMAGE);
        });

        builder.setNegativeButton(R.string.cancel, (dialog, which) -> {
            dialog.dismiss();
        }).setPositiveButton(R.string.update_category, ((dialog, which) -> {

            String categoryName = mInputCategoryName.getText().toString();
            if (TextUtils.isEmpty(categoryName)) {
                mInputCategoryName.setError(getString(R.string.please_enter_category_name));
                return;
            }
            Map<String, Object> mMapUpdateCategory = new HashMap<>();
            mMapUpdateCategory.put("name", categoryName);

            if (mImageUri != null)
                // In this we will use firebase Storage to upload image
                uploadImage(mMapUpdateCategory);
            else
                updateCategory(mMapUpdateCategory);

        }));

        builder.setView(layoutView);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showAddCategoryDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.add_category);
        View layoutView = LayoutInflater.from(getActivity())
                .inflate(R.layout.dialog_update_category, null);
        MaterialEditText mInputCategoryName = layoutView.findViewById(R.id.input_update_category_name);
        mImgCategory = layoutView.findViewById(R.id.img_update_category_image);

        mImgCategory.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
        mImgCategory.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;

        Picasso.get().load(Common.URL_IMAGE_DEFAULT).into(mImgCategory);

        // Set event
        mImgCategory.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent, getString(R.string.select_image)),
                    Common.CODE_REQUEST_UPDATE_CATEGORY_IMAGE);
        });

        builder.setNegativeButton(R.string.cancel, (dialog, which) -> {
            dialog.dismiss();
        }).setPositiveButton(R.string.add_category, ((dialog, which) -> {

            String categoryName = mInputCategoryName.getText().toString();
            if (TextUtils.isEmpty(categoryName)) {
                mInputCategoryName.setError(getString(R.string.please_enter_category_name));
                return;
            }
            Map<String, Object> mMapUpdateCategory = new HashMap<>();
            mMapUpdateCategory.put("name", categoryName);
            mMapUpdateCategory.put("image", Common.URL_IMAGE_DEFAULT);

            if (mImageUri != null)
                // In this we will use firebase Storage to upload image
                uploadAddImage(mMapUpdateCategory);
            else
                addCategory(mMapUpdateCategory);

        }));

        builder.setView(layoutView);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void uploadImage(Map<String, Object> mMapUpdateCategory) {

        mDialog.setMessage(getString(R.string.uploading));
        mDialog.show();
        String uniqueImageName = UUID.randomUUID().toString();

        StorageReference storageImageFolder = FirebaseStorage.getInstance()
                .getReference()
                .child(Common.KEY_IMAGES_CATEGORY_PATH + uniqueImageName);
        storageImageFolder.putFile(mImageUri)
                .addOnFailureListener(e -> {
                    mDialog.dismiss();
                    Log.e("UPLOAD_IMAGE_ERROR", e.getMessage());
                }).addOnCompleteListener(task -> {
            mDialog.dismiss();
            storageImageFolder.getDownloadUrl()
                    .addOnSuccessListener(uri -> {
                        mMapUpdateCategory.put("image", uri.toString());
                        updateCategory(mMapUpdateCategory);

                    });
        }).addOnProgressListener(taskSnapshot -> {
            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
            mDialog.setMessage(new StringBuilder(getString(R.string.uploading_done) + " ").append(progress).append("%"));
        });


    }

    private void uploadAddImage(Map<String, Object> mMapUpdateCategory) {

        mDialog.setMessage(getString(R.string.uploading));
        mDialog.show();
        String uniqueImageName = UUID.randomUUID().toString();

        StorageReference storageImageFolder = FirebaseStorage.getInstance()
                .getReference()
                .child(Common.KEY_IMAGES_CATEGORY_PATH + uniqueImageName);
        storageImageFolder.putFile(mImageUri)
                .addOnFailureListener(e -> {
                    mDialog.dismiss();
                    Log.e("UPLOAD_IMAGE_ERROR", e.getMessage());
                }).addOnCompleteListener(task -> {
            mDialog.dismiss();
            storageImageFolder.getDownloadUrl()
                    .addOnSuccessListener(uri -> {
                        mMapUpdateCategory.put("image", uri.toString());
                        addCategory(mMapUpdateCategory);

                    });
        }).addOnProgressListener(taskSnapshot -> {
            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
            mDialog.setMessage(new StringBuilder(getString(R.string.uploading_done) + " ").append(progress).append("%"));
        });


    }

    private void updateCategory(Map<String, Object> mMapUpdateCategory) {

        FirebaseDatabase.getInstance().getReference()
                .child(Common.KEY_CAEGORIES_REFERANCE)
                .child(Common.currentCategory.getMenu_id())
                .updateChildren(mMapUpdateCategory)
                .addOnFailureListener(e -> {
                    Log.e("UPDATE_CATEGORY_ERROR", e.getMessage());
                }).addOnCompleteListener(task -> {

            String notifTitle = getString(R.string.update_category);
            String notifContent = Common.currentServer.getName() + " "+
                    getString(R.string.updated_category);

            Common.sendNoification(getActivity(), notifTitle, notifContent,
                    mDisposable, mIfcmService);

            categoryViewModel.loadCategorise();
            EventBus.getDefault().postSticky(new ToastEvent(true, false));
        });
    }

    private void addCategory(Map<String, Object> mMapUpdateCategory) {

        FirebaseDatabase.getInstance().getReference()
                .child(Common.KEY_CAEGORIES_REFERANCE)
                .push()
                .setValue(mMapUpdateCategory)
                .addOnFailureListener(e -> {
                    Log.e("UPDATE_CATEGORY_ERROR", e.getMessage());
                }).addOnCompleteListener(task -> {

            String notifTitle = getString(R.string.add_category);
            String notifContent = Common.currentServer.getName() + " "+
                    getString(R.string.added_new_category);

                    Common.sendNoification(getActivity(), notifTitle, notifContent, mDisposable, mIfcmService);

                    categoryViewModel.loadCategorise();

        });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Common.CODE_REQUEST_UPDATE_CATEGORY_IMAGE) {
            if (resultCode == RESULT_OK) {
                mImageUri = data.getData();
                mImgCategory.setImageURI(mImageUri);
            }
        }
    }

    @Override
    public void onDestroy() {
        mDisposable.clear();
        super.onDestroy();
    }
}
