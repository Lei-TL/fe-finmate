package com.finmate.data.repository;

import com.finmate.core.network.ApiCallExecutor;
import com.finmate.data.dto.CategoryRequest;
import com.finmate.data.dto.CategoryResponse;
import com.finmate.data.remote.api.ApiCallback;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Call;

@Singleton
public class CategoryRemoteRepository {

    private final com.finmate.data.remote.api.CategoryService categoryApi;
    private final ApiCallExecutor apiCallExecutor;

    @Inject
    public CategoryRemoteRepository(com.finmate.data.remote.api.CategoryService categoryApi,
                                    ApiCallExecutor apiCallExecutor) {
        this.categoryApi = categoryApi;
        this.apiCallExecutor = apiCallExecutor;
    }

    public void fetchCategoriesByType(String type,
                                      ApiCallback<List<CategoryResponse>> callback) {
        Call<List<CategoryResponse>> call = categoryApi.getCategoriesByType(type);
        apiCallExecutor.execute(call, callback);
    }

    public void createCategory(CategoryRequest request,
                               ApiCallback<CategoryResponse> callback) {
        Call<CategoryResponse> call = categoryApi.createCategory(request);
        apiCallExecutor.execute(call, callback);
    }

    public void updateCategory(String id,
                               CategoryRequest request,
                               ApiCallback<CategoryResponse> callback) {
        Call<CategoryResponse> call = categoryApi.updateCategory(id, request);
        apiCallExecutor.execute(call, callback);
    }

    public void deleteCategory(String id,
                               ApiCallback<Void> callback) {
        Call<Void> call = categoryApi.deleteCategory(id);
        apiCallExecutor.execute(call, callback);
    }
}
