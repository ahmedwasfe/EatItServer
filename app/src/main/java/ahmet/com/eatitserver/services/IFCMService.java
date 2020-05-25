package ahmet.com.eatitserver.services;

import ahmet.com.eatitserver.model.ServiceModel.FCMResponse;
import ahmet.com.eatitserver.model.ServiceModel.FCMSendData;
import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IFCMService {

    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAkGIj1FE:APA91bFYKPgrgeMVPw8Xp9rFoymXMfTaMp7NXXOon_JgYpjeMDBXy5XKGHfpqTxcq5qXwjUyeCwweWjYTUAzv1WZT1fRAWGgJYd13McWILrzDe8YSq06KmJgY0BwJVBUyZDd3rkPHeUs"
    })
    @POST("fcm/send")
    Observable<FCMResponse> sendNotification(@Body FCMSendData body);
}
