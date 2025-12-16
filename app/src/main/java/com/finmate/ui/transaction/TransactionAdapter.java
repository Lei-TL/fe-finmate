package com.finmate.ui.transaction;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.finmate.R;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransactionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_TRANSACTION = 1;

    private List<TransactionGroupedItem> groupedItems;
    // ✅ Map để cache category name -> icon name
    private Map<String, String> categoryIconMap;

    public TransactionAdapter(List<TransactionGroupedItem> groupedItems) {
        this.groupedItems = groupedItems;
        this.categoryIconMap = new HashMap<>();
    }

    // ✅ Set category icon map từ ViewModel/Activity
    public void setCategoryIconMap(Map<String, String> map) {
        this.categoryIconMap = map != null ? new HashMap<>(map) : new HashMap<>();
        // ✅ Notify để refresh tất cả items khi map thay đổi
        notifyDataSetChanged();
    }

    // === PHƯƠNG THỨC ĐỂ CẬP NHẬT DỮ LIỆU ===
    public void updateList(List<TransactionGroupedItem> newList) {
        this.groupedItems.clear();
        this.groupedItems.addAll(newList);
        notifyDataSetChanged(); // Báo cho RecyclerView cập nhật lại
    }

    public TransactionGroupedItem getItem(int position) {
        return groupedItems.get(position);
    }

    @Override
    public int getItemViewType(int position) {
        return groupedItems.get(position).getType() == TransactionGroupedItem.ItemType.HEADER 
                ? TYPE_HEADER 
                : TYPE_TRANSACTION;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction_date_header, parent, false);
            return new DateHeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);
            return new TransactionViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        TransactionGroupedItem item = groupedItems.get(position);
        
        if (item.getType() == TransactionGroupedItem.ItemType.HEADER) {
            DateHeaderViewHolder headerHolder = (DateHeaderViewHolder) holder;
            headerHolder.tvDate.setText(item.getDateHeader());
            headerHolder.tvDayOfWeek.setText(item.getDayOfWeek());
        } else {
            TransactionViewHolder transactionHolder = (TransactionViewHolder) holder;
            TransactionUIModel transaction = item.getTransaction();
            Context context = holder.itemView.getContext();
            
            transactionHolder.tvName.setText(transaction.name);
            // ✅ Hiển thị category name (không hiển thị "Unknown" nữa)
            String categoryName = transaction.category;
            if (categoryName == null || categoryName.isEmpty()) {
                transactionHolder.tvGroup.setText("");
            } else {
                transactionHolder.tvGroup.setText(categoryName);
            }
            
            // ✅ Lấy icon theo category
            // Logic: categoryName (từ transaction) -> iconName (từ map) -> iconResId (từ drawable)
            String iconName = null;
            if (categoryName != null && !categoryName.isEmpty() && categoryIconMap != null && !categoryIconMap.isEmpty()) {
                // ✅ Thử tìm với tên gốc trước
                iconName = categoryIconMap.get(categoryName);
                // ✅ Nếu không tìm thấy, thử với trimmed
                if (iconName == null) {
                    iconName = categoryIconMap.get(categoryName.trim());
                }
                // ✅ Nếu vẫn không tìm thấy, thử với lowercase
                if (iconName == null) {
                    iconName = categoryIconMap.get(categoryName.toLowerCase());
                }
                // ✅ Nếu vẫn không tìm thấy, thử với trimmed lowercase
                if (iconName == null) {
                    iconName = categoryIconMap.get(categoryName.trim().toLowerCase());
                }
                
                if (iconName == null) {
                    android.util.Log.d("TransactionAdapter", "Icon not found for category: " + categoryName + ", map size: " + categoryIconMap.size());
                } else {
                    android.util.Log.d("TransactionAdapter", "Found icon for category: " + categoryName + " -> " + iconName);
                }
            } else {
                if (categoryName == null || categoryName.isEmpty()) {
                    android.util.Log.d("TransactionAdapter", "Category name is null or empty: " + categoryName);
                }
                if (categoryIconMap == null || categoryIconMap.isEmpty()) {
                    android.util.Log.w("TransactionAdapter", "Category icon map is null or empty");
                }
            }
            // ✅ iconName bây giờ là "ic_food", "ic_salary", etc. (hoặc null)
            int iconResId = getIconResourceId(context, iconName);
            if (iconResId != 0) {
                transactionHolder.imgCategory.setImageResource(iconResId);
                // ✅ Clear tint để hiển thị màu icon gốc
                transactionHolder.imgCategory.setColorFilter(null);
            } else {
                // ✅ Fallback về default icon nếu không tìm thấy
                android.util.Log.d("TransactionAdapter", "Using default icon for category: " + categoryName);
                transactionHolder.imgCategory.setImageResource(R.drawable.ic_default_category);
                transactionHolder.imgCategory.setColorFilter(null);
            }
            
            // ✅ Format amount với dấu + hoặc - và màu sắc dựa trên type
            String type = transaction.type;
            String amountText = transaction.amount;
            
            if ("INCOME".equals(type)) {
                // Thu nhập: màu xanh + dấu +
                // Loại bỏ dấu - nếu có, thêm dấu + nếu chưa có
                amountText = amountText.trim();
                if (amountText.startsWith("-")) {
                    amountText = amountText.substring(1); // Bỏ dấu -
                }
                if (!amountText.startsWith("+")) {
                    amountText = "+" + amountText;
                }
                transactionHolder.tvMoney.setText(amountText);
                transactionHolder.tvMoney.setTextColor(ContextCompat.getColor(context, R.color.green_500));
            } else if ("EXPENSE".equals(type)) {
                // Chi tiêu: màu đỏ + dấu -
                // Loại bỏ dấu + nếu có, thêm dấu - nếu chưa có
                amountText = amountText.trim();
                if (amountText.startsWith("+")) {
                    amountText = amountText.substring(1); // Bỏ dấu +
                }
                if (!amountText.startsWith("-")) {
                    amountText = "-" + amountText;
                }
                transactionHolder.tvMoney.setText(amountText);
                transactionHolder.tvMoney.setTextColor(ContextCompat.getColor(context, R.color.red_500));
            } else {
                // TRANSFER hoặc loại khác: giữ nguyên màu mặc định
                transactionHolder.tvMoney.setText(amountText);
                // Sử dụng màu từ layout (textColorPrimary)
                transactionHolder.tvMoney.setTextColor(transactionHolder.tvName.getCurrentTextColor());
            }
            
            transactionHolder.tvWallet.setText(transaction.wallet);
        }
    }

    @Override
    public int getItemCount() {
        return groupedItems.size();
    }

    // ViewHolder cho Date Header
    public static class DateHeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvDayOfWeek;

        public DateHeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvDayOfWeek = itemView.findViewById(R.id.tvDayOfWeek);
        }
    }

    // ✅ Helper method để lấy icon resource ID từ icon name
    // Input: "ic_food", "ic_salary", etc. (từ categoryIconMap)
    // Output: R.drawable.ic_food, R.drawable.ic_salary, etc.
    private int getIconResourceId(Context context, String iconName) {
        if (iconName == null || iconName.isEmpty()) {
            return 0; // Trả về 0 để adapter dùng default
        }
        
        Resources resources = context.getResources();
        String packageName = context.getPackageName();
        
        // ✅ Icon name từ backend đã là "ic_food", chỉ cần trim và tìm
        String trimmedName = iconName.trim();
        int resId = resources.getIdentifier(trimmedName, "drawable", packageName);
        
        // ✅ Nếu không tìm thấy (có thể do case), thử lowercase
        if (resId == 0) {
            resId = resources.getIdentifier(trimmedName.toLowerCase(), "drawable", packageName);
        }
        
        // Nếu không tìm thấy, trả về 0 để adapter xử lý fallback
        return resId;
    }

    // ViewHolder cho Transaction
    public static class TransactionViewHolder extends RecyclerView.ViewHolder {
        ImageView imgCategory;
        TextView tvName, tvGroup, tvMoney, tvWallet;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCategory = itemView.findViewById(R.id.imgCategory);
            tvName = itemView.findViewById(R.id.tvName);
            tvGroup = itemView.findViewById(R.id.tvGroup);
            tvMoney = itemView.findViewById(R.id.tvMoney);
            tvWallet = itemView.findViewById(R.id.tvWallet);
        }
    }
}
