package ahmet.com.eatitserver.ui.best_deals;

import android.app.AlertDialog;
import android.content.Intent;
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
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
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

import ahmet.com.eatitserver.R;
import ahmet.com.eatitserver.adapter.BestDealsAdapter;
import ahmet.com.eatitserver.common.Common;
import ahmet.com.eatitserver.common.SwipeRecyclerHelper;
import ahmet.com.eatitserver.eventBus.ToastEvent;
import ahmet.com.eatitserver.model.BestDeals;
import ahmet.com.eatitserver.services.IFCMService;
import ahmet.com.eatitserver.services.RetrofitFCMClient;
import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;
import io.reactivex.disposables.CompositeDisposable;
import io.supercharge.shimmerlayout.ShimmerLayout;

import static android.app.Activity.RESULT_OK;

public class BestDealsFragment extends Fragment {

    @BindView(R.id.recycler_best_deals)
    RecyclerView mRecyclerBestDeals;
    @BindView(R.id.shimmer_layout_best_deals)
    ShimmerLayout mShimmerLayout;

    private LayoutAnimationController mAnimationController;

    private BestDealsAdapter mBestDealsAdapter;
    private List<BestDeals> bestDealsList;

    private BestDealsViewModel bestDealsViewModel;

    private ImageView mImgBestDeals;
    private Uri mImageUri;

    private AlertDialog mDialog;

    private CompositeDisposable mDisposable;
    private IFCMService mIfcmService;

    private static BestDealsFragment instance;
    public static BestDealsFragment getInstance(){
        return instance == null ? new BestDealsFragment() : instance;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        bestDealsViewModel = new ViewModelProvider(this).get(BestDealsViewModel.class);

        View layoutView = inflater.inflate(R.layout.fragment_best_deals, container, false);

        ButterKnife.bind(this, layoutView);

        initViews();

        bestDealsViewModel.getMutableMessageError()
                .observe(getViewLifecycleOwner(), s -> {
                    Toast.makeText(getActivity(), s, Toast.LENGTH_SHORT).show();
                });

        bestDealsViewModel.getMutableListBestDeals()
                .observe(getViewLifecycleOwner(), bestDeals -> {

                    mShimmerLayout.stopShimmerAnimation();
                    mShimmerLayout.setVisibility(View.GONE);

                    bestDealsList = bestDeals;
                    mBestDealsAdapter = new BestDealsAdapter(getActivity(), bestDealsList);
                    mRecyclerBestDeals.setAdapter(mBestDealsAdapter);
                    mRecyclerBestDeals.setLayoutAnimation(mAnimationController);

                });

        return layoutView;
    }

    private void initViews() {

        mDisposable = new CompositeDisposable();
        mIfcmService = RetrofitFCMClient.getRetrofit().create(IFCMService.class);

        mShimmerLayout.startShimmerAnimation();

        mAnimationController = AnimationUtils.loadLayoutAnimation(getActivity(),R.anim.raw_item_from_left);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerBestDeals.setHasFixedSize(true);
        mRecyclerBestDeals.setLayoutManager(layoutManager);
        mRecyclerBestDeals.addItemDecoration(new DividerItemDecoration(getActivity(), layoutManager.getOrientation()));

        mDialog = new SpotsDialog.Builder().setContext(getActivity()).build();

        SwipeRecyclerHelper recyclerHelper = new SwipeRecyclerHelper(
                getActivity(), mRecyclerBestDeals, 150) {
            @Override
            public void instantiateButton(RecyclerView.ViewHolder viewHolder, List<MButton> mListMButton) {

                mListMButton.add(new MButton(getActivity(), getString(R.string.update),30,0,
                        getActivity().getColor(R.color.colorGreen), position -> {

                    Common.bestDealsSelected = bestDealsList.get(position);
                    showUpdateDialog();
                }));

                mListMButton.add(new MButton(getActivity(), getString(R.string.delete),30,0,
                        getActivity().getColor(R.color.colorAccent), position -> {

                    Common.bestDealsSelected = bestDealsList.get(position);
                    showDeleteDialog();
                }));
            }
        };
    }

    private void showDeleteDialog() {

        new androidx.appcompat.app.AlertDialog.Builder(getActivity())
                .setTitle(R.string.delete_best_deals)
                .setMessage(R.string.delete_best_deals_message)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    FirebaseDatabase.getInstance().getReference()
                            .child(Common.KEY_BEST_DEALS_REFERANCE)
                            .child(Common.bestDealsSelected.getKey())
                            .removeValue()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {

                                    String notifTitle = getString(R.string.delete_best_deals);
                                    String notifContent = Common.currentServer.getName() + " "+
                                            getString(R.string.deleted_best_deals);

                                    Common.sendNoification(getActivity(), notifTitle, notifContent,
                                            mDisposable, mIfcmService);

                                    bestDealsViewModel.loadBestDeals();
                                    EventBus.getDefault().postSticky(new ToastEvent(false,true));
                                }
                            });
                }).setNegativeButton(R.string.cancel, (dialog, which) -> {
            dialog.dismiss();
        }).show();
    }

    private void showUpdateDialog() {

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.update_best_deals);
        View layoutView = LayoutInflater.from(getActivity())
                .inflate(R.layout.dialog_update_category, null);
        MaterialEditText mInputCategoryName = layoutView.findViewById(R.id.input_update_category_name);
        mImgBestDeals = layoutView.findViewById(R.id.img_update_category_image);

        // Set Data

        mInputCategoryName.setText(new StringBuilder("").append(Common.bestDealsSelected.getName()));
        Picasso.get().load(Common.bestDealsSelected.getImage()).into(mImgBestDeals);

        // Set event
        mImgBestDeals.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent, getString(R.string.select_image)),
                    Common.CODE_REQUEST_UPDATE_BEST_DEALS_IMAGE);
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
                updateBestDeals(mMapUpdateCategory);

        }));

        builder.setView(layoutView);
        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void uploadImage(Map<String, Object> mMapUpdateCategory) {

        mDialog.setMessage(getString(R.string.uploading));
        mDialog.show();
        String uniqeImageName = UUID.randomUUID().toString();

        StorageReference storageImageFolder = FirebaseStorage.getInstance().getReference()
                .child(Common.KEY_IMAGES_BEST_DEALS_PATH + uniqeImageName);
        storageImageFolder.putFile(mImageUri)
                .addOnFailureListener(e -> {
                    mDialog.dismiss();
                    Log.e("UPLOAD_IMAGE_ERROR", e.getMessage());
                }).addOnCompleteListener(task -> {
                    mDialog.dismiss();
                    storageImageFolder.getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                mMapUpdateCategory.put("image", uri.toString());
                                updateBestDeals(mMapUpdateCategory);
                            });
        }).addOnProgressListener(taskSnapshot -> {
            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
            mDialog.setMessage(new StringBuilder(getString(R.string.uploading_done) + " ").append(progress).append("%"));
        });
    }

    private void updateBestDeals(Map<String, Object> mMapUpdateCategory) {

        FirebaseDatabase.getInstance().getReference()
                .child(Common.KEY_BEST_DEALS_REFERANCE)
                .child(Common.bestDealsSelected.getKey())
                .updateChildren(mMapUpdateCategory)
                .addOnFailureListener(e -> {
                    Log.e("UPDATE_CATEGORY_ERROR", e.getMessage());
                }).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        String notifTitle = getString(R.string.update_best_deals);
                        String notifContent = Common.currentServer.getName() + " "+
                                getString(R.string.updated_best_deals);

                        Common.sendNoification(getActivity(), notifTitle, notifContent,
                                mDisposable, mIfcmService);

                        bestDealsViewModel.loadBestDeals();
                        EventBus.getDefault().postSticky(new ToastEvent(true, true));
                    }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Common.CODE_REQUEST_UPDATE_BEST_DEALS_IMAGE) {
            if (resultCode == RESULT_OK) {
                mImageUri = data.getData();
                mImgBestDeals.setImageURI(mImageUri);
            }
        }
    }

    @Override
    public void onStop() {
        mDisposable.clear();
        super.onStop();
    }
}
