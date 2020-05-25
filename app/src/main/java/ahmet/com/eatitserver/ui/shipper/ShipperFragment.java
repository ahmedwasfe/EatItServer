package ahmet.com.eatitserver.ui.shipper;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.FirebaseDatabase;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ahmet.com.eatitserver.adapter.ShipperAdapter;
import ahmet.com.eatitserver.common.Common;
import ahmet.com.eatitserver.eventBus.ChangeMenuClick;
import ahmet.com.eatitserver.eventBus.UpdateShipperEvent;
import ahmet.com.eatitserver.model.Shipper;
import ahmet.com.eatitserver.R;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.supercharge.shimmerlayout.ShimmerLayout;

public class ShipperFragment extends Fragment {

    @BindView(R.id.recycler_shippers)
    RecyclerView mRecyclerShipper;
    @BindView(R.id.shimmer_layout_shipper)
    ShimmerLayout mShimmerLayout;

    private ShipperAdapter shipperAdapter;
    private List<Shipper> listShipper, listSaveShipperBeforeSearch;
    private ShipperViewModel shipperViewModel;

    private LayoutAnimationController mAnimationController;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        shipperViewModel = new ViewModelProvider(this).get(ShipperViewModel.class);

        View layoutView = inflater.inflate(R.layout.fragment_shippers, container, false);

        ButterKnife.bind(this, layoutView);

        initViews();

        shipperViewModel.getMutableListShipper()
                .observe(getViewLifecycleOwner(), mListShippers -> {

                    listShipper = mListShippers;
                    listSaveShipperBeforeSearch = mListShippers;

                    shipperAdapter = new ShipperAdapter(getActivity(), listShipper);
                    mRecyclerShipper.setAdapter(shipperAdapter);
                    mRecyclerShipper.setLayoutAnimation(mAnimationController);

                    mShimmerLayout.setVisibility(View.GONE);
                    mShimmerLayout.stopShimmerAnimation();

                });

        shipperViewModel.getMutabMessageError()
                .observe(getViewLifecycleOwner(), messageError -> {
                    Toast.makeText(getActivity(), "" + messageError, Toast.LENGTH_SHORT).show();
                    Log.e("SHIPPER_LOAD_ERROR", messageError);
                });

        return layoutView;
    }

    private void initViews() {

        setHasOptionsMenu(true);

        mShimmerLayout.startShimmerAnimation();

        listShipper = new ArrayList<>();
        listSaveShipperBeforeSearch = new ArrayList<>();

        mAnimationController = AnimationUtils.loadLayoutAnimation(getActivity(), R.anim.raw_item_from_left);

        mRecyclerShipper.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerShipper.setLayoutManager(layoutManager);
        mRecyclerShipper.addItemDecoration(new DividerItemDecoration(getActivity(), layoutManager.getOrientation()));
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {

        inflater.inflate(R.menu.food_menu, menu);

        MenuItem menuItem = menu.findItem(R.id.action_search);

        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));

        // Event
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                startSearchInShipper(query);
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
            shipperViewModel.loadAllShippers();
        });
    }

    private void startSearchInShipper(String query) {

        List<Shipper> mListSipperResult = new ArrayList<>();
        for (int i = 0; i < shipperAdapter.getListShipper().size(); i++){
            Shipper shipper = shipperAdapter.getListShipper().get(i);
            if (shipper.getName().toLowerCase().contains(query.toLowerCase()) ||
                shipper.getPhone().contains(query))
                mListSipperResult.add(shipper);
        }

        // get searchresult
        shipperViewModel.getMutableListShipper()
                .setValue(mListSipperResult);
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onUpdateShipperActive(UpdateShipperEvent event){

        Map<String, Object> mapActivrShipper = new HashMap<>();
        // get state of button not shipper
        mapActivrShipper.put("active", event.isActive());

        FirebaseDatabase.getInstance().getReference()
                .child(Common.KEY_SHIPPER_REFERANCE)
                .child(event.getShipper().getKey())
                .updateChildren(mapActivrShipper)
                .addOnFailureListener(e -> {
                    Log.e("ShIPPER_STATE_ERROR", e.getMessage());
                }).addOnCompleteListener(task -> {
                    Toast.makeText(getActivity(), getString(R.string.update_status_to)+" "+Common.convertShipperState(event.isActive()), Toast.LENGTH_SHORT).show();
                });

    }

    @Override
    public void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        if (EventBus.getDefault().hasSubscriberForEvent(UpdateShipperEvent.class))
            EventBus.getDefault().removeStickyEvent(UpdateShipperEvent.class);
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().postSticky(new ChangeMenuClick(true));
        super.onDestroy();
    }
}
