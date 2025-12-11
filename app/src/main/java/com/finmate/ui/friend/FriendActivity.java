package com.finmate.ui.friend;


import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;
import com.finmate.R;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class FriendActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FriendAdapter adapter;
    private ImageView btnBack;
    private ImageView btnMore;

    private FriendViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend);

        viewModel = new ViewModelProvider(this).get(FriendViewModel.class);

        recyclerView = findViewById(R.id.rvFriends);
        btnBack = findViewById(R.id.btnBack);
        btnMore = findViewById(R.id.btnMore);

        adapter = new FriendAdapter(new ArrayList<>());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());
        btnMore.setOnClickListener(v -> {
            // TODO: Show menu options
        });

        observeViewModel();
        setupActions();

        viewModel.loadFriends();
    }

    private void observeViewModel() {
        viewModel.friends.observe(this, list -> {
            adapter.updateData(list);  // anh thêm hàm này trong FriendAdapter
        });

        viewModel.loading.observe(this, isLoading -> {
            // show/hide progress bar nếu anh có
        });

        viewModel.error.observe(this, msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupActions() {
        // nếu FriendAdapter có callback accept/reject/remove thì pass vào đây
        adapter.setOnActionListener(new FriendAdapter.OnActionListener() {
            @Override
            public void onRemove(FriendUIModel item) {
                viewModel.remove(item.getFriendshipId());
            }

            @Override
            public void onAccept(FriendUIModel item) {
                viewModel.accept(item.getFriendshipId());
            }

            @Override
            public void onReject(FriendUIModel item) {
                viewModel.reject(item.getFriendshipId());
            }
        });
    }
}
