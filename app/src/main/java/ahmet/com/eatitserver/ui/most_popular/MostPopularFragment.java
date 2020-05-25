package ahmet.com.eatitserver.ui.most_popular;

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
import ahmet.com.eatitserver.adapter.MostPopularAdapter;
import ahmet.com.eatitserver.common.Common;
import ahmet.com.eatitserver.common.SwipeRecyclerHelper;
import ahmet.com.eatitserver.eventBus.ToastEvent;
import ahmet.com.eatitserver.model.Mostpopular;
import ahmet.com.eatitserver.services.IFCMService;
import ahmet.com.eatitserver.services.RetrofitFCMClient;
import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;
import io.reactivex.disposables.CompositeDisposable;
import io.supercharge.shimmerlayout.ShimmerLayout;

import static android.app.Activity.RESULT_OK;

public class MostPopularFragment extends Fragment {

    @BindView(R.id.recycler_most_popular)
    RecyclerView mRecyclerMostpopular;
    @BindView(R.id.shimmer_layout_most_popular)
    ShimmerLayout mShimmerLayout;

    private LayoutAnimationController mAnimationController;

    private MostPopularViewModel mostPopularViewModel;

    private MostPopularAdapter mMostPopularAdapter;
    private List<Mostpopular> listMostPopular;


    private ImageView mImgMostPopular;
    private Uri mImageUri;

    private AlertDialog mDialog;

    private CompositeDisposable mDisposable;
    private IFCMService mIfcmService;

    private static MostPopularFragment instance;
    public static MostPopularFragment getInstance(){
        return instance == null ? new MostPopularFragment() : instance;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        mostPopularViewModel = new ViewModelProvider(this).get(MostPopularViewModel.class);

        View layoutView = inflater.inflate(R.layout.fragment_most_popular, container, false);

        ButterKnife.bind(this, layoutView);

        initViews();

        mostPopularViewModel.getMutabMessageError()
                .observe(getViewLifecycleOwner(), s -> {
                    Toast.makeText(getActivity(), s, Toast.LENGTH_SHORT).show();
                });

        mostPopularViewModel.getMutabListPopular()
                .observe(getViewLifecycleOwner(), mostpopulars -> {

                    mShimmerLayout.stopShimmerAnimation();
                    mShimmerLayout.setVisibility(View.GONE);

                    listMostPopular = mostpopulars;
                    mMostPopularAdapter = new MostPopularAdapter(getActivity(), listMostPopular);
                    mRecyclerMostpopular.setAdapter(mMostPopularAdapter);
                    mRecyclerMostpopular.setLayoutAnimation(mAnimationController);
                });



        return layoutView;
    }

    private void initViews() {

        mDisposable = new CompositeDisposable();
        mIfcmService = RetrofitFCMClient.getRetrofit().create(IFCMService.class);

        mShimmerLayout.startShimmerAnimation();

        mAnimationController = AnimationUtils.loadLayoutAnimation(getActivity(), R.anim.raw_item_from_left);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerMostpopular.setHasFixedSize(true);
        mRecyclerMostpopular.setLayoutManager(layoutManager);;
        mRecyclerMostpopular.addItemDecoration(new DividerItemDecoration(getActivity(), layoutManager.getOrientation()));

        mDialog = new SpotsDialog.Builder().setContext(getActivity()).build();

        SwipeRecyclerHelper recyclerHelper = new SwipeRecyclerHelper(
                getActivity(), mRecyclerMostpopular,150) {
            @Override
            public void instantiateButton(RecyclerView.ViewHolder viewHolder, List<MButton> mListMButton) {

                mListMButton.add(new MButton(getActivity(), getString(R.string.update),30,0,
                        getActivity().getColor(R.color.colorGreen), position -> {

                    Common.mostPopularSelected = listMostPopular.get(position);
                    showUpdateDialog();
                }));

                mListMButton.add(new MButton(getActivity(), getString(R.string.delete),30,0,
                        getActivity().getColor(R.color.colorAccent), position -> {

                    Common.mostPopularSelected = listMostPopular.get(position);
                    showDeleteDialog();
                }));
            }
        };
    }

    private void showDeleteDialog() {

        new androidx.appcompat.app.AlertDialog.Builder(getActivity())
                .setTitle(R.string.delete_most_popular)
                .setMessage(R.string.delete_best_most_popular)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    FirebaseDatabase.getInstance().getReference()
                            .child(Common.KEY_POPULAR_REFERANCE)
                            .child(Common.mostPopularSelected.getKey())
                            .removeValue()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {

                                    String notifTitle = getString(R.string.delete_most_popular);
                                    String notifContent = Common.currentServer.getName() + " "+
                                            getString(R.string.deleted_popular);

                                    Common.sendNoification(getActivity(), notifTitle, notifContent,
                                            mDisposable, mIfcmService);

                                    mostPopularViewModel.loadMostPopular();
                                    EventBus.getDefault().postSticky(new ToastEvent(false,true));
                                }
                            });
                }).setNegativeButton(R.string.cancel, (dialog, which) -> {
            dialog.dismiss();
        }).show();
    }

    private void showUpdateDialog() {

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.update_most_popular);
        View layoutView = LayoutInflater.from(getActivity())
                .inflate(R.layout.dialog_update_category, null);
        MaterialEditText mInputCategoryName = layoutView.findViewById(R.id.input_update_category_name);
        mImgMostPopular = layoutView.findViewById(R.id.img_update_category_image);

        // Set Data

        mInputCategoryName.setText(new StringBuilder("").append(Common.mostPopularSelected.getName()));
        Picasso.get().load(Common.mostPopularSelected.getImage()).into(mImgMostPopular);

        // Set event
        mImgMostPopular.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent, getString(R.string.select_image)),
                    Common.CODE_REQUEST_UPDATE_MOST_POPULAR_IMAGE);
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
                updateMostPopular(mMapUpdateCategory);

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
                .child(Common.KEY_IMAGES_MOST_POPULAR_PATH + uniqeImageName);
        storageImageFolder.putFile(mImageUri)
                .addOnFailureListener(e -> {
                    mDialog.dismiss();
                    Log.e("UPLOAD_IMAGE_ERROR", e.getMessage());
                }).addOnCompleteListener(task -> {
            mDialog.dismiss();
            storageImageFolder.getDownloadUrl()
                    .addOnSuccessListener(uri -> {
                        mMapUpdateCategory.put("image", uri.toString());
                        updateMostPopular(mMapUpdateCategory);
                    });
        }).addOnProgressListener(taskSnapshot -> {
            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
            mDialog.setMessage(new StringBuilder(getString(R.string.uploading_done) + " ").append(progress).append("%"));
        });
    }

    private void updateMostPopular(Map<String, Object> mMapUpdateCategory) {

        FirebaseDatabase.getInstance().getReference()
                .child(Common.KEY_POPULAR_REFERANCE)
                .child(Common.mostPopularSelected.getKey())
                .updateChildren(mMapUpdateCategory)
                .addOnFailureListener(e -> {
                    Log.e("UPDATE_CATEGORY_ERROR", e.getMessage());
                }).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {

                String notifTitle = getString(R.string.update_most_popular);
                String notifContent = Common.currentServer.getName() + " "+
                        getString(R.string.updated_popular);

                Common.sendNoification(getActivity(), notifTitle, notifContent,
                        mDisposable, mIfcmService);
                mostPopularViewModel.loadMostPopular();
                EventBus.getDefault().postSticky(new ToastEvent(true, true));
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Common.CODE_REQUEST_UPDATE_MOST_POPULAR_IMAGE) {
            if (resultCode == RESULT_OK) {
                mImageUri = data.getData();
                mImgMostPopular.setImageURI(mImageUri);
            }
        }
    }

    @Override
    public void onStop() {
        mDisposable.clear();
        super.onStop();
    }
}
