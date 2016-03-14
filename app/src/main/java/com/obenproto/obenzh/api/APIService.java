package com.obenproto.obenzh.api;

import com.obenproto.obenzh.api.response.GetAllUserAvatarsResponse;
import com.obenproto.obenzh.api.response.GetAvatarResponse;
import com.obenproto.obenzh.api.response.GetPhrasesResponse;
import com.obenproto.obenzh.api.response.LoginResponse;
import com.obenproto.obenzh.api.response.SaveUserAvatarResponse;
import com.squareup.okhttp.RequestBody;

import retrofit.Call;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.Part;
import retrofit.http.Path;

public interface APIService {

    @FormUrlEncoded
    @POST("morphing/ws/MorphingService/loginUser")
    Call<LoginResponse> userLogin(@Field("userEmail") String userEmail,
                                  @Field("userPassword") String userPassword,
                                  @Field("userDisplayName") String userDisplayName);

    /**
     * Get all user avatars.
     *
     * @param userId User ID.
     * @return All user avatars as ArrayList object.
     */
    @GET("morphing/ws/MorphingService/getAllUserAvatars/{userId}")
    Call<GetAllUserAvatarsResponse> getAllUserAvatars(@Path("userId") Integer userId);

    @Multipart
    @POST("morphing/ws/MorphingService/saveUserAvatar/mode/{mode}")
    Call<SaveUserAvatarResponse> saveUserAvatar(@Path("mode") Integer mode,
                                                @Part("userId") Integer userId,
                                                @Part("recordId") Integer recordId,
                                                @Part("audioFile") RequestBody audioFile,
                                                @Part("avatarId") Integer avatarId);

    /**
     * Get all phrases.
     *
     * @param mode User mode.
     * @return All phrases as list object.
     */
    @GET("morphing/ws/MorphingService/getPhrases/mode/{mode}")
    Call<GetPhrasesResponse> getPhrases(@Path("mode") Integer mode);

    /**
     * Get avatar data from avatar ID.
     *
     * @param avatarId Avatar ID.
     * @return Recorded sentences of avatar ID.
     */
    @GET("morphing/ws/MorphingService/getAvatar/{avatarId}")
    Call<GetAvatarResponse> getAvatar(@Path("avatarId") Integer avatarId);
}