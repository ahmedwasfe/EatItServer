package ahmet.com.eatitserver;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import ahmet.com.eatitserver.common.Common;
import ahmet.com.eatitserver.eventBus.CategoryClick;
import ahmet.com.eatitserver.eventBus.ChangeMenuClick;
import ahmet.com.eatitserver.eventBus.ToastEvent;
import ahmet.com.eatitserver.eventBus.UpdateFood;
import ahmet.com.eatitserver.model.ServiceModel.FCMSendData;
import ahmet.com.eatitserver.remote.RetrofitClient;
import ahmet.com.eatitserver.services.IFCMService;
import ahmet.com.eatitserver.services.RetrofitFCMClient;
import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import dmax.dialog.SpotsDialog;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawer;
    @BindView(R.id.nav_view)
    NavigationView mNavigationView;


    private NavController mNavController;

    private AppBarConfiguration mAppBarConfiguration;

    private int menuClickId = -1;

    // News System
    private Uri mImageUri;
    private ImageView mImgUpload;
    private CompositeDisposable mDisposable;
    private IFCMService mIfcmServicel;

    private android.app.AlertDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        ButterKnife.bind(this);

        init();

        subscribeToTopic(Common.createTopicOrder());

        updateToken();

        initNavigation();

        checkIsOpenNotificationFromActivity();
    }

    private void init() {

        mDisposable = new CompositeDisposable();
        mIfcmServicel = RetrofitFCMClient.getRetrofit().create(IFCMService.class);
        mDialog = new SpotsDialog.Builder().setContext(this).build();
    }

    private void checkIsOpenNotificationFromActivity() {

        boolean isOpenFromNewOrder = getIntent().getBooleanExtra(Common.IS_OPRN_ACTIVITY_NEW_ORDER, false);
        if (isOpenFromNewOrder){
            mNavController.popBackStack();
            mNavController.navigate(R.id.nav_orders);
            menuClickId = R.id.nav_orders;
        }
    }

    private void updateToken() {

        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnFailureListener(e -> {
                    Log.e("GET_TOKEN_ERROR", e.getMessage());
                }).addOnCompleteListener(task -> {
                    Common.updateToken(this, task.getResult().getToken(), true,false);
                });
    }

    private void subscribeToTopic(String topicOrder) {

        FirebaseMessaging.getInstance()
                .subscribeToTopic(topicOrder)
                .addOnFailureListener(e -> {
                    Log.e("TOPIC_ERROR", e.getMessage());
                }).addOnCompleteListener(task -> {
                    if (!task.isSuccessful())
                        Toast.makeText(this, "Failed "+ task.isSuccessful(), Toast.LENGTH_SHORT).show();
                });
    }

    private void initNavigation() {

        requestPermissions();

        setSupportActionBar(mToolbar);

        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_category, R.id.nav_food,
                R.id.nav_orders, R.id.nav_shipper)
                .setDrawerLayout(mDrawer)
                .build();

        mNavController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, mNavController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(mNavigationView, mNavController);

        mNavigationView.setNavigationItemSelectedListener(this);
        mNavigationView.bringToFront();

        View headerView = mNavigationView.getHeaderView(0);
        TextView mTxtServerName = headerView.findViewById(R.id.txt_header_server_name);
        TextView mTxtServerPhone = headerView.findViewById(R.id.txt_header_server_phone);
        CircleImageView mImgServer = headerView.findViewById(R.id.img_heder_server_user_avater);

        Common.setSpanString(getString(R.string.welcome)+" ", Common.currentServer.getName(), mTxtServerName);
        mTxtServerPhone.setText(Common.currentServer.getPhone());
        Picasso.get()
                .load("https://i.postimg.cc/ZqMZ3KJ5/man-character-face-avatar-in-glasses-vector-17074986.jpg")
                .into(mImgServer);

        // Default
        menuClickId = R.id.nav_category;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
       return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    private void requestPermissions(){

        Dexter.withActivity(this)
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {

                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                    }
                }).check();
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onCategorySelected(CategoryClick event){
        if (event.isSuccess()){

            if (menuClickId != R.id.nav_food){

                mNavController.navigate(R.id.nav_food);
                menuClickId = R.id.nav_food;
            }
        }

    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onToastEvent(ToastEvent event){

        if (event.isUpdate()) {
            Toast.makeText(this, getString(R.string.update_success), Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(this, getString(R.string.delete_success), Toast.LENGTH_SHORT).show();
        }
            EventBus.getDefault().postSticky(new ChangeMenuClick(event.isFromFood()));
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onChangeMenuClick(ChangeMenuClick event){

        if (event.isFromFood()){
            // Clear
            mNavController.popBackStack(R.id.nav_category, true);
            mNavController.navigate(R.id.nav_category);
        }else{
            // Clear
            mNavController.popBackStack(R.id.nav_food, true);
            mNavController.navigate(R.id.nav_food);
        }

        menuClickId = -1;
    }


    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        mDisposable.clear();
        super.onStop();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        item.setChecked(true);
        mDrawer.closeDrawers();

        switch (item.getItemId()){

            case R.id.nav_category:
                if (item.getItemId() != menuClickId){
                    mNavController.popBackStack(); // Remove all back stack
                    mNavController.navigate(R.id.nav_category);
                }
                break;
            case R.id.nav_orders:
                if (item.getItemId() != menuClickId) {
                    mNavController.popBackStack(); // Remove all back stack
                    mNavController.navigate(R.id.nav_orders);
                }
                break;
            case R.id.nav_shipper:
                if (item.getItemId() != menuClickId) {
                    mNavController.popBackStack(); // Remove all back stack
                    mNavController.navigate(R.id.nav_shipper);
                }
                break;
            case R.id.nav_best_deal:
                if (item.getItemId() != menuClickId) {
                    mNavController.popBackStack(); // Remove all back stack
                    mNavController.navigate(R.id.nav_best_deal);
                }
                break;
            case R.id.nav_most_popular:
                if (item.getItemId() != menuClickId) {
                    mNavController.popBackStack(); // Remove all back stack
                    mNavController.navigate(R.id.nav_most_popular);
                }
                break;
            case R.id.nav_send_new:
                showNewsSystemDialog();
              //  menuClickId = -1;
                mDrawer.closeDrawers();
                break;
            case R.id.nav_sign_out:
                signOut();
               // menuClickId = -1;
                mDrawer.closeDrawers();
                break;
            default:
                menuClickId = -1;
                break;
        }

        menuClickId = item.getItemId();
        return true;
    }

    private void showNewsSystemDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.news_system);

        View itenView = LayoutInflater.from(this)
                .inflate(R.layout.layout_news_system, null);

        MaterialEditText mInputTitle = itenView.findViewById(R.id.input_news_title);
        MaterialEditText mInputContet = itenView.findViewById(R.id.input_news_content);
        MaterialEditText mInputNewsLink = itenView.findViewById(R.id.input_news_type_link);

        mImgUpload = itenView.findViewById(R.id.img_upload);
        RadioButton mRdiNone = itenView.findViewById(R.id.rdi_none);
        RadioButton mRdiLink = itenView.findViewById(R.id.rdi_link);
        RadioButton mRdiImage = itenView.findViewById(R.id.rdi_image);

        // Events
        mRdiNone.setOnClickListener(v -> {
            mInputNewsLink.setVisibility(View.GONE);
            mImgUpload.setVisibility(View.GONE);
        });
        mRdiLink.setOnClickListener(v -> {
            mInputNewsLink.setVisibility(View.VISIBLE);
            mImgUpload.setVisibility(View.GONE);
        });
        mRdiImage.setOnClickListener(v -> {
            mInputNewsLink.setVisibility(View.GONE);
            mImgUpload.setVisibility(View.VISIBLE);
        });

        mImgUpload.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent,getString(R.string.select_image)),
                    Common.CODE_REQUEST_NEWS_SYSTEM_IMAGE);
        });

        builder.setView(itenView);

        builder.setNegativeButton(R.string.cancel, (dialog, which) -> {
            dialog.dismiss();
        }).setPositiveButton(R.string.send, (dialog, which) -> {

            String title = mInputTitle.getText().toString();
            String content = mInputContet.getText().toString();
            String link = mInputNewsLink.getText().toString();

            if (mRdiNone.isChecked())
                sendNews(title, content);
            else if (mRdiLink.isChecked())
                sendNews(title, content, link);
            else if (mRdiImage.isChecked()){
                if (mImageUri != null)
                    sendNewsImage(title, content);
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();

    }

    private void sendNewsImage(String title, String conten) {

        mDialog.setMessage(getString(R.string.waiting));
        mDialog.show();

        String fileName = UUID.randomUUID().toString();

        StorageReference storageImageFolder = FirebaseStorage.getInstance().getReference()
                .child(Common.KEY_IMAGES_NEWS_PATH + fileName);
        storageImageFolder.putFile(mImageUri)
                .addOnFailureListener(e -> {
                    mDialog.dismiss();
                    Log.e("UPLOAD_IMAGE_ERROR", e.getMessage());
                }).addOnCompleteListener(task -> {
                    mDialog.dismiss();
                    storageImageFolder.getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                sendNews(title, conten, uri.toString());
                            });
        }).addOnProgressListener(taskSnapshot -> {
            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
            mDialog.setMessage(new StringBuilder(getString(R.string.uploading_done) + " ").append(progress).append("%"));
        });


    }

    private void sendNews(String title, String content, String link) {

        Map<String, String> mapNotificaionData = new HashMap<>();
        mapNotificaionData.put(Common.KEY_NOTFI_TITLE, title);
        mapNotificaionData.put(Common.KEY_NOTFI_CONTENT, content);
        mapNotificaionData.put(Common.KEY_IS_SEND_IMAGE, "true");
        mapNotificaionData.put(Common.KEY_NOTFI_IMAGE_LINK, link);

        FCMSendData fcmSendData = new FCMSendData(Common.getNewsTopic(), mapNotificaionData);
        mDialog.setMessage(getString(R.string.waiting));
        mDialog.show();

        mDisposable.add(mIfcmServicel.sendNotification(fcmSendData)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(fcmResponse -> {
                    mDialog.dismiss();
                    if (fcmResponse.getMessage_id() != 0)
                        Toast.makeText(this, getString(R.string.news_sent), Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(this, "News sent failed", Toast.LENGTH_SHORT).show();
                }, throwable -> {
                    mDialog.dismiss();
                    Log.e("SEND_NEWS_ERROR", throwable.getMessage());
                }));

    }

    private void sendNews(String title, String content) {

        Map<String, String> mapNotificaionData = new HashMap<>();
        mapNotificaionData.put(Common.KEY_NOTFI_TITLE, title);
        mapNotificaionData.put(Common.KEY_NOTFI_CONTENT, content);
        mapNotificaionData.put(Common.KEY_IS_SEND_IMAGE, "false");

        FCMSendData fcmSendData = new FCMSendData(Common.getNewsTopic(), mapNotificaionData);
        mDialog.setMessage(getString(R.string.waiting));
        mDialog.show();

        mDisposable.add(mIfcmServicel.sendNotification(fcmSendData)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(fcmResponse -> {
                        mDialog.dismiss();
                        if (fcmResponse.getMessage_id() != 0)
                            Toast.makeText(this, getString(R.string.news_sent), Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(this, "News sent failed", Toast.LENGTH_SHORT).show();
                    }, throwable -> {
                        mDialog.dismiss();
                        Log.e("SEND_NEWS_ERROR", throwable.getMessage());
                    }));

    }


    private void signOut() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.sign_out)
                .setMessage(R.string.message_sign_out)
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).setPositiveButton("SIGN OUT", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Common.currentFood = null;
                Common.currentCategory = null;
                Common.currentServer = null;

                FirebaseAuth.getInstance().signOut();;
                Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Common.CODE_REQUEST_NEWS_SYSTEM_IMAGE) {
            if (resultCode == RESULT_OK) {
                mImageUri = data.getData();
                mImgUpload.setImageURI(mImageUri);
            }
        }
    }
}
