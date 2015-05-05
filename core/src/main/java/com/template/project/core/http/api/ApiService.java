package com.template.project.core.http.api;

import com.template.project.core.http.json.JsonName;

import retrofit.Callback;
import retrofit.client.Response;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.Multipart;
import retrofit.http.Part;
import retrofit.http.Path;
import retrofit.mime.TypedFile;
import retrofit.mime.TypedString;

public interface ApiService {

    @FormUrlEncoded
    @POST("/user")
    public Response register(
            @Field(JsonName.USER_EMAIL) String email,
            @Field(JsonName.USER_PASSWORD) String password);

    @Multipart
    @POST("/updateUser")
    public Response updateUser(
            @Part(JsonName.USER_EMAIL) TypedString email,
            @Part(JsonName.USER_PASSWORD) TypedString password,
            @Part(JsonName.USER_IMAGE) TypedFile image);

    @GET("/user/{"+ JsonName.USER_EMAIL +"}")
    public Response getUserDetails(
            @Path(JsonName.USER_EMAIL) String email);


    // Interface for asynchronous retrofit

    @FormUrlEncoded
    @POST("/user")
    public void register(
            @Field(JsonName.USER_EMAIL) String email,
            @Field(JsonName.USER_PASSWORD) String password,
            Callback callback);

    @Multipart
    @POST("/updateUser")
    public void updateUser(
            @Part(JsonName.USER_EMAIL) TypedString email,
            @Part(JsonName.USER_PASSWORD) TypedString password,
            @Part(JsonName.USER_IMAGE) TypedFile image,
            Callback callback);

    @GET("/user/{"+ JsonName.USER_EMAIL +"}")
    public void getUserDetails(
            @Path(JsonName.USER_EMAIL) String email,
            Callback callback);
}