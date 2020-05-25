package ahmet.com.eatitserver.common;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ahmet.com.eatitserver.model.BestDeals;
import ahmet.com.eatitserver.model.Category;
import ahmet.com.eatitserver.model.Food.Food;
import ahmet.com.eatitserver.model.Mostpopular;
import ahmet.com.eatitserver.model.Order;
import ahmet.com.eatitserver.model.ServerUser;
import ahmet.com.eatitserver.model.ServiceModel.FCMSendData;
import ahmet.com.eatitserver.model.Token;
import ahmet.com.eatitserver.R;
import ahmet.com.eatitserver.services.IFCMService;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class Common {

    public static final String URL_IMAGE_DEFAULT = "https://i.postimg.cc/XXMGNTNy/cropped-Getty-Images-643764514.jpg";

    public static final int CODE_REQUEST_PHONE = 1880;
    public static final int CODE_REQUEST_UPDATE_CATEGORY_IMAGE = 1881;
    public static final int CODE_REQUEST_UPDATE_FOOD_IMAGE = 1882;
    public static final int CODE_REQUEST_UPDATE_BEST_DEALS_IMAGE = 1883;
    public static final int CODE_REQUEST_UPDATE_MOST_POPULAR_IMAGE = 1884;
    public static final int CODE_REQUEST_NEWS_SYSTEM_IMAGE = 1885;

    // Database
    public static final String KEY_SERVER_REFERANCE = "Servers";
    public static final String KEY_CAEGORIES_REFERANCE = "Category";
    public static final String KEY_ORDER_REFERANCE = "Orders";
    public static final String KEY_TOKEN_REFERANCE = "Tokens";
    public static final String KEY_SHIPPER_REFERANCE = "Shippers";
    public static final String KEY_SHIPPING_ORDER_REFERANCE = "ShippingOrders";
    public static final String KEY_POPULAR_REFERANCE = "MostPopular";
    public static final String KEY_BEST_DEALS_REFERANCE = "BestDeals";
    public static final String KEY_FOOD_CHILD = "foods";
    // Storage
    public static final String KEY_IMAGES_CATEGORY_PATH = "CategoriesImages/";
    public static final String KEY_IMAGES_FOOD_PATH = "FoodsImages/";
    public static final String KEY_IMAGES_BEST_DEALS_PATH = "BestDealsImages/";
    public static final String KEY_IMAGES_MOST_POPULAR_PATH = "MostPopularImages/";
    public static final String KEY_IMAGES_NEWS_PATH = "NewsImages/";

    // Notification
    public static final String KEY_NOTFI_TITLE = "title";
    public static final String KEY_NOTFI_CONTENT = "content";
    public static final String KEY_IS_SEND_IMAGE = "IS_SEND_IMAGE";
    public static final String KEY_NOTFI_IMAGE_LINK = "Image_Link";
    public static final String IS_OPRN_ACTIVITY_NEW_ORDER = "IsOpenActivityNewOrder";

    public static final int DEFAULT_COLUMN_COUNT = 0;
    public static final int FULL_WIDTH_COLUMN = 1;



    public static ServerUser currentServer;
    public static Category currentCategory;
    public static Food currentFood;
    public static Order currentOrder;
    public static Order currentOrderSelected;
    public static BestDeals bestDealsSelected;
    public static Mostpopular mostPopularSelected;

    public static void setSpanString(String welcome, String name, TextView textView){

        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(welcome);
        SpannableString spannableString = new SpannableString(name);
        StyleSpan boldSpan = new StyleSpan(Typeface.BOLD);
        spannableString.setSpan(boldSpan, 0, name.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.append(spannableString);
        textView.setText(builder, TextView.BufferType.SPANNABLE);

    }

    public static void setSpanStringColor(String welcome, String text, TextView textView, int color) {

        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(welcome);
        SpannableString spannableString = new SpannableString(text);
        StyleSpan boldSpan = new StyleSpan(Typeface.BOLD);
        spannableString.setSpan(boldSpan, 0, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new ForegroundColorSpan(color), 0, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.append(spannableString);
        textView.setText(builder, TextView.BufferType.SPANNABLE);
    }

    public static String convertStatus(int orderStatus) {
        switch (orderStatus){
            case 0:
                return "Placed";
            case 1:
                return "Shipping";
            case 2:
                return "Shipped";
            case -1:
                return "Cancelled";
            default:
                return "Unknown";
        }
    }

    public static void showNotification(Context mContext, int id, String title, String content, Intent intent) {

        PendingIntent pendingIntent = null;
        if (intent != null)
            pendingIntent = PendingIntent.getActivity(mContext, id, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        String NOTIFICATION_CHANNEL_ID = "ocean_for_it_eat_it";
        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(mContext.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                    "Eat It", NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setDescription("Eat It");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.BLUE);
            notificationChannel.setVibrationPattern(new long[]{0,1000,500,1000});
            notificationChannel.enableVibration(true);

            notificationManager.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext, NOTIFICATION_CHANNEL_ID);
        builder.setContentTitle(title)
                .setContentText(content)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setStyle(new NotificationCompat.BigTextStyle())
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_restaurant_menu));
        if (pendingIntent != null)
            builder.setContentIntent(pendingIntent);
        Notification notification = builder.build();
        notificationManager.notify(id, notification);
    }

    public static void sendNoification(Context mContext, String title, String content, CompositeDisposable disposable, IFCMService ifcmService) {

        Map<String, String> mapNotfi = new HashMap<>();
        mapNotfi.put(Common.KEY_NOTFI_TITLE, title);
        mapNotfi.put(Common.KEY_NOTFI_CONTENT, content);

        FCMSendData fcmSendData = new FCMSendData(Common.getNewsTopic(), mapNotfi);
        disposable.add(ifcmService.sendNotification(fcmSendData)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(fcmResponse -> {
                    if (fcmResponse.getSuccess() == 1) {
                        Log.e("SEND_NOTIFI_SUCCESS", fcmResponse.getResult().toString());
                        Toast.makeText(mContext, "Send notification success", Toast.LENGTH_SHORT).show();
                    }else {
                        Log.e("FCM_RESPONSE_ERROR", fcmResponse.getMessage_id() + "");
                        Toast.makeText(mContext, "failed to send notification", Toast.LENGTH_SHORT).show();
                    }
                }, throwable -> {
                    Log.e("NOtification_ERROR", throwable.getMessage());
                    Toast.makeText(mContext, ""+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                }));


    }

    public static void updateToken(Context mContext, String newToken, boolean isServer, boolean isShipper) {

        if (Common.currentServer != null){
            FirebaseDatabase.getInstance().getReference()
                    .child(Common.KEY_TOKEN_REFERANCE)
                    .child(Common.currentServer.getUid())
                    .setValue(new Token(Common.currentServer.getPhone(), newToken, isServer, isShipper))
                    .addOnCompleteListener(task -> {

                    }).addOnFailureListener(e -> {
                Log.e("GET_TOKEN_ERROR", e.getMessage());
                Toast.makeText(mContext, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }
    }

    public static String createTopicOrder() {
        return new StringBuilder("/topics/new_order").toString();
    }

    public static String convertShipperState(boolean active){
        if (active == true)
            return "Active";
        else
            return "Not active";
    }

    public static float getBearing(LatLng start, LatLng end) {

        double lat = Math.abs(start.latitude - end.latitude);
        double lng = Math.abs(start.longitude - end.longitude);

        if (start.latitude < end.latitude && start.longitude < end.longitude)
            return (float) (Math.toDegrees(Math.atan(lng/lat)));
        else if (start.latitude >= end.latitude && start.longitude < end.longitude)
            return (float) ((90 - Math.toDegrees(Math.atan(lng/lat)))+90);
        else if (start.latitude >= end.latitude && start.longitude >= end.longitude)
            return (float) (Math.toDegrees(Math.atan(lng/lat))+180);
        else if (start.latitude < end.latitude && start.longitude >= end.longitude)
            return (float) ((90 - Math.toDegrees(Math.atan(lng/lat)))+270);

        return -1;
    }

    public static List<LatLng> decodePoly(String encoded) {

        List poly = new ArrayList();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len){
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++)-63;
                result |= (b & 0x1f) << shift;
                shift+=5;
            }while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat +=dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++)-63;
                result |= (b & 0x1f) << shift;
                shift+=5;
            }while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng +=dlng;

            LatLng p = new LatLng((((double)lat / 1E5)),
                    (((double)lng / 1E5)));
            poly.add(p);
        }

        return poly;

    }

    public static String getNewsTopic() {
        return new StringBuilder("/topics/news").toString();
    }
}
