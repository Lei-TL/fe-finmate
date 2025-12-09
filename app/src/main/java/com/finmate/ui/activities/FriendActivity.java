package com.finmate.ui.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.finmate.R;
import com.finmate.adapters.FriendAdapter;
import com.finmate.models.Friend;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class FriendActivity extends BaseActivity {

    private RecyclerView rvFriends;
    private FriendAdapter friendAdapter;
    private List<Friend> activeFriends;
    private List<Friend> pausedFriends;
    private TabLayout tabLayout;
    private boolean isGridView = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend);

        // Initialize views
        rvFriends = findViewById(R.id.rvFriends);
        tabLayout = findViewById(R.id.tabLayout);

        // Prepare data
        prepareFriendLists();

        // Setup RecyclerView
        setupRecyclerView(false); // Default to list view

        // Setup Tabs
        setupTabLayout();

        // Setup other buttons
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnConnectNewFriend).setOnClickListener(v -> showConnectFriendDialog());
        findViewById(R.id.btnMore).setOnClickListener(v -> showFriendOptionsBottomSheet());
    }

    private void prepareFriendLists() {
        // Sample Active Friends
        activeFriends = new ArrayList<>();
        activeFriends.add(new Friend(R.drawable.ic_avatar, "Phan Hữu Linh Phong"));
        activeFriends.add(new Friend(R.drawable.ic_avatar, "Lý Xuân An"));
        activeFriends.add(new Friend(R.drawable.ic_avatar, "Mai Đỗ Xuân Long"));
        activeFriends.add(new Friend(R.drawable.ic_avatar, "Nguyễn Ân"));
        activeFriends.add(new Friend(R.drawable.ic_avatar, "Lý Tuấn An"));
        activeFriends.add(new Friend(R.drawable.ic_avatar, "Nguyễn Văn Ngạn"));

        // Sample Paused Friends
        pausedFriends = new ArrayList<>();
        pausedFriends.add(new Friend(R.drawable.ic_avatar, "Phan Khánh Ân"));
        pausedFriends.add(new Friend(R.drawable.ic_avatar, "Lý Xuân An (Paused)"));
        pausedFriends.add(new Friend(R.drawable.ic_avatar, "Mai Nguyễn"));
    }

    private void setupRecyclerView(boolean isGrid) {
        this.isGridView = isGrid;
        if (isGrid) {
            rvFriends.setLayoutManager(new GridLayoutManager(this, 3));
        } else {
            rvFriends.setLayoutManager(new LinearLayoutManager(this));
        }
        friendAdapter = new FriendAdapter(this, new ArrayList<>(activeFriends));
        rvFriends.setAdapter(friendAdapter);
    }

    private void setupTabLayout() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    friendAdapter.updateData(activeFriends);
                } else {
                    friendAdapter.updateData(pausedFriends);
                }
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void showFriendOptionsBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_friend_options);

        LinearLayout deleteOption = bottomSheetDialog.findViewById(R.id.option_delete);
        LinearLayout viewModeOption = bottomSheetDialog.findViewById(R.id.option_view_mode);
        TextView viewModeText = bottomSheetDialog.findViewById(R.id.tv_view_mode_text);

        // Set initial view mode text
        viewModeText.setText(isGridView ? getString(R.string.show_as_list) : getString(R.string.show_as_grid));

        deleteOption.setOnClickListener(v -> {
            showDeleteConfirmationDialog();
            bottomSheetDialog.dismiss();
        });

        viewModeOption.setOnClickListener(v -> {
            setupRecyclerView(!isGridView);
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.show();
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_delete_friend_title)
                .setMessage(R.string.confirm_delete_friend_message)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    // TODO: Implement friend deletion logic
                    Toast.makeText(this, "Friend deleted (simulation)", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showConnectFriendDialog() {
        // TODO: Implement the dialog from the previous step
        Toast.makeText(this, "Connect friend dialog will be shown here", Toast.LENGTH_SHORT).show();
    }
}
