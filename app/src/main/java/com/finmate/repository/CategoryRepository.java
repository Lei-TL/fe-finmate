package com.finmate.repository;

import android.content.Context;

import com.finmate.database.AppDatabase;
import com.finmate.database.dao.CategoryDao;
import com.finmate.entities.CategoryEntity;

import java.util.List;

public class CategoryRepository {

    private final CategoryDao dao;

    public CategoryRepository(Context ctx) {
        dao = AppDatabase.getInstance(ctx).categoryDao();
    }

    public void insert(CategoryEntity category) {
        new Thread(() -> dao.insert(category)).start();
    }

    public void update(CategoryEntity category) {
        new Thread(() -> dao.update(category)).start();
    }

    public void delete(CategoryEntity category) {
        new Thread(() -> dao.delete(category)).start();
    }

    public void getByType(String type, Callback<List<CategoryEntity>> callback) {
        new Thread(() -> callback.onResult(dao.getByType(type))).start();
    }

    public void getAll(Callback<List<CategoryEntity>> callback) {
        new Thread(() -> callback.onResult(dao.getAll())).start();
    }

    public interface Callback<T> { void onResult(T data); }
}
