package com.finmate.data.remote.api;

import com.finmate.data.dto.CategoryRequest;
import com.finmate.data.dto.CategoryResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface CategoryService {

    @GET("categories")
    Call<List<CategoryResponse>> getCategoriesByType(
            @Query("type") String type
    );

    @POST("categories")
    Call<CategoryResponse> createCategory(@Body CategoryRequest request);

    @PUT("categories/{id}")
    Call<CategoryResponse> updateCategory(
            @Path("id") String id,
            @Body CategoryRequest request
    );

    @DELETE("categories/{id}")
    Call<Void> deleteCategory(@Path("id") String id);
}
