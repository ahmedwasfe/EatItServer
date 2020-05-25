package ahmet.com.eatitserver.services;

import android.content.Intent;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;
import java.util.Random;

import ahmet.com.eatitserver.MainActivity;
import ahmet.com.eatitserver.R;
import ahmet.com.eatitserver.common.Common;

public class FCMServices extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {

        Map<String, String> dataRecv = remoteMessage.getData();

        if (dataRecv != null) {
            if (dataRecv.get(Common.KEY_NOTFI_TITLE).equals(getString(R.string.title_order))){
                // Here we need call with MainActivity because we must assign value for Common.currentUser
                // So we must call with MainActivity to do that, if you direct call HomeActivity you will be crash
                // Because Common.currentUser only assign in MainActivity AFTER LOGIN
                Intent intent = new Intent(this, MainActivity.class);
                // Use extra to detect is app open from notification
                intent.putExtra(Common.IS_OPRN_ACTIVITY_NEW_ORDER, true);
                Common.showNotification(this, new Random().nextInt(),
                        dataRecv.get(Common.KEY_NOTFI_TITLE),
                        dataRecv.get(Common.KEY_NOTFI_CONTENT),
                        intent);
            }else {
                Common.showNotification(this, new Random().nextInt(),
                        dataRecv.get(Common.KEY_NOTFI_TITLE),
                        dataRecv.get(Common.KEY_NOTFI_CONTENT),
                        null);
            }
        }

    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        // we are in server app so server = true
        Common.updateToken(this, token, true, false);
    }
}
