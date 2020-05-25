package ahmet.com.eatitserver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.syd.oden.circleprogressdialog.view.RotateLoading;

import java.util.Arrays;
import java.util.List;

import ahmet.com.eatitserver.common.Common;
import ahmet.com.eatitserver.model.ServerUser;
import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.progress_loading_sign_in)
    RotateLoading mRotateLoading;

    private List<AuthUI.IdpConfig> mListProviderPhone;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        initViews();
    }

    private void initViews() {

        mListProviderPhone = Arrays.asList(new AuthUI.IdpConfig.PhoneBuilder().build());

        mFirebaseAuth = FirebaseAuth.getInstance();

        mAuthStateListener = firebaseAuth -> {

            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null)
                // Check user from Firebase
                checkServerUserFromDatabase(user);
            else
                showUIPhonenumber();
        };
    }

    private void checkServerUserFromDatabase(FirebaseUser user) {

        mRotateLoading.start();

        FirebaseDatabase.getInstance().getReference()
                .child(Common.KEY_SERVER_REFERANCE)
                .child(user.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            ServerUser serverUser = dataSnapshot.getValue(ServerUser.class);
                            if (serverUser.isActive()){
                                goToHomeActivity(serverUser);
                            }else{
                                mRotateLoading.stop();
                                Toast.makeText(MainActivity.this, "You must be allowed from Admin to access this app", Toast.LENGTH_SHORT).show();

                            }
                        }else{
                            // User not exist in database
                            mRotateLoading.stop();
                            showDialogSignUp(user);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        mRotateLoading.stop();
                        Log.e("ERROR_TO_CHECK_USER", databaseError.getMessage());
                    }
                });
    }

    private void showDialogSignUp(FirebaseUser user) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.sign_up);
               // .setMessage(R.string.sign_up_message);

        View layoutView = LayoutInflater.from(this)
                .inflate(R.layout.dialog_sign_up, null);
        MaterialEditText mInputName = layoutView.findViewById(R.id.input_server_name);
        MaterialEditText mInputPhone = layoutView.findViewById(R.id.input_server_phone);
        RotateLoading rotateLoading = layoutView.findViewById(R.id.progress_loading_sign_up);

        rotateLoading.stop();

        mInputPhone.setText(user.getPhoneNumber());

        builder.setNegativeButton(R.string.cancel, (dialog, which) -> {
            dialog.dismiss();
        }).setPositiveButton(R.string.sign_up, (dialog, which) -> {
            String serverName = mInputName.getText().toString();
            String serverPhone = mInputPhone.getText().toString();

            if (TextUtils.isEmpty(serverName)){
                mInputName.setError(getString(R.string.please_enter_name));
                return;
            }

            if (TextUtils.isEmpty(serverPhone)){
                mInputPhone.setError(getString(R.string.please_enter_phone));
                return;
            }

            signUp(user, serverName, serverPhone, rotateLoading);
        });

        builder.setView(layoutView);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void signUp(FirebaseUser user, String serverName, String serverPhone, RotateLoading rotateLoading) {

        rotateLoading.start();

        ServerUser serverUser = new ServerUser();
        serverUser.setUid(user.getUid());
        serverUser.setName(serverName);
        serverUser.setPhone(serverPhone);
        serverUser.setActive(false); // Default failed we must active user by manual in databse

        FirebaseDatabase.getInstance().getReference()
                .child(Common.KEY_SERVER_REFERANCE)
                .child(serverUser.getUid())
                .setValue(serverUser)
                .addOnFailureListener(e -> {
                    rotateLoading.stop();
                    Log.e("ERROR_ADD_SERVER", e.getMessage());
                }).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                       rotateLoading.stop();
                       Toast.makeText(this, getString(R.string.sign_up_success), Toast.LENGTH_SHORT).show();
                       // goToHomeActivity(serverUser);
                    }

        });
    }

    private void goToHomeActivity(ServerUser serverUser) {

        mRotateLoading.stop();
        Common.currentServer = serverUser;
        Intent intent = new Intent(this, HomeActivity.class);
        intent.putExtra(Common.IS_OPRN_ACTIVITY_NEW_ORDER,
                getIntent().getBooleanExtra(Common.IS_OPRN_ACTIVITY_NEW_ORDER, false));
        startActivity(intent);
        finish();
    }

    private void showUIPhonenumber() {
        startActivityForResult(AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(mListProviderPhone)
                .build(), Common.CODE_REQUEST_PHONE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Common.CODE_REQUEST_PHONE){
            IdpResponse idpResponse = IdpResponse.fromResultIntent(data);
            if (resultCode == RESULT_OK){
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            }else{
                Toast.makeText(this, "Failed to sign in", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onStop() {
        if (mAuthStateListener != null)
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        super.onStop();
    }
}
