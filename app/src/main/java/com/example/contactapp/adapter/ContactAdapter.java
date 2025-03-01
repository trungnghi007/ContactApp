package com.example.contactapp.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.contactapp.R;
import com.example.contactapp.model.Contact;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ContactAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Filterable {

    private List<Object> contactList;
    private List<Object> contactListFiltered;
    private Context context;
    private OnContactClickListener listener;
    private  OnFavoriteClickListener favoriteClickListener;
    private String currentCategory;
    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_ITEM = 1;


    public ContactAdapter(Context context, List<Object> contactList, OnContactClickListener listener, OnFavoriteClickListener favoriteClickListener) {
        this.context = context;
        this.contactList = contactList;
        this.contactListFiltered = contactList;
        this.listener = listener;
        this.favoriteClickListener = favoriteClickListener;
        this.currentCategory = "Tất cả";
    }

    // Cập nhật danh sách liên hệ và làm mới giao diện
    public void setContactList(List<Object> contactList) {
        this.contactList = contactList;
        this.contactListFiltered = contactList;
        notifyDataSetChanged();
    }
    // Cập nhật danh mục hiển thị hiện tại và làm mới giao diện
    public void setCurrentCategory(String currentCategory) {
        this.currentCategory = currentCategory;
        notifyDataSetChanged();
    }


    @NonNull
    @Override
    // Tạo ViewHolder phù hợp với loại dữ liệu (header hoặc contact)
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact, parent, false);
            return new ContactViewHolder(view);
        }
    }
    // Gắn dữ liệu vào ViewHolder
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == VIEW_TYPE_HEADER) {
            HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;
            if (contactListFiltered.get(position) instanceof String){
                String header = (String) contactListFiltered.get(position);
                headerViewHolder.tvHeader.setText(header);
            }
        } else {
            ContactViewHolder contactViewHolder = (ContactViewHolder) holder;
            if (contactListFiltered.get(position) instanceof Contact){
                Contact contact = (Contact) contactListFiltered.get(position);
                contactViewHolder.tvName.setText(contact.getName());
                contactViewHolder.tvPhoneNumber.setText(contact.getPhoneNumber());
                if (contact.getProfilePictureUri() != null && !contact.getProfilePictureUri().isEmpty()) {
                    Uri imageUri = Uri.parse(contact.getProfilePictureUri());
                    Glide.with(context)
                            .load(imageUri)
                            .placeholder(R.drawable.ic_person)
                            .into(contactViewHolder.ivProfile);
                } else {
                    contactViewHolder.ivProfile.setImageResource(R.drawable.ic_person);
                }
                contactViewHolder.ivFavorite.setImageResource(contact.isFavorite() ? R.drawable.ic_favorite : R.drawable.ic_unfavorite);
                contactViewHolder.itemView.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onContactClick(contact);
                    }
                });
                contactViewHolder.ivFavorite.setOnClickListener(v -> {
                    if (favoriteClickListener != null) {
                        favoriteClickListener.onFavoriteClick(contact, position);
                    }
                });
            }


        }
    }
    public void updateItem(Contact updatedContact, int position) {
        if (position >= 0 && position < contactListFiltered.size()) {
            if (contactListFiltered.get(position) instanceof Contact) {
                contactListFiltered.set(position, updatedContact);
                notifyItemChanged(position);
            }

        }
    }
    // Xác định loại dữ liệu tại vị trí chỉ định
    @Override
    public int getItemViewType(int position) {
        if (contactListFiltered.get(position) instanceof String){
            return VIEW_TYPE_HEADER;
        }
        return VIEW_TYPE_ITEM;
    }

    // Trả về tổng số phần tử trong danh sách
    @Override
    public int getItemCount() {
        if (contactListFiltered == null) return 0;
        return contactListFiltered.size();
    }
    @Override
    public Filter getFilter() {
        return new Filter() {
            // Thực hiện lọc danh sách liên hệ theo chuỗi tìm kiếm và phân nhóm kết quả.
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String filterString = constraint.toString().toLowerCase();
                FilterResults results = new FilterResults();
                List<Object> filteredList = new ArrayList<>();
                Set<Contact> uniqueContacts = new HashSet<>(); // Sử dụng HashSet để loại bỏ trùng lặp

                if (filterString.isEmpty()) {
                    results.values = contactList;
                    results.count = contactList.size();
                } else {
                    for (Object object : contactList) {
                        if (object instanceof Contact) {
                            Contact contact = (Contact) object;
                            if (contact.getName().toLowerCase().contains(filterString) ||
                                    contact.getPhoneNumber().toLowerCase().contains(filterString)) {
                                uniqueContacts.add(contact); // Chỉ thêm liên hệ vào Set
                            }
                        }
                    }


                    Map<String, List<Contact>> groupedContacts = new LinkedHashMap<>();
                    for (Contact contact : uniqueContacts) {
                        String firstChar = contact.getName().substring(0, 1).toUpperCase();
                        if (!groupedContacts.containsKey(firstChar)) {
                            groupedContacts.put(firstChar, new ArrayList<>());
                        }
                        groupedContacts.get(firstChar).add(contact);
                    }


                    for (Map.Entry<String, List<Contact>> entry : groupedContacts.entrySet()) {
                        filteredList.add(entry.getKey());
                        filteredList.addAll(entry.getValue());
                    }

                    results.values = filteredList;
                    results.count = filteredList.size();
                }
                return results;
            }


            @Override
            // Cập nhật danh sách hiển thị sau khi lọc và làm mới giao diện
            protected void publishResults(CharSequence constraint, FilterResults results) {
                contactListFiltered = (List<Object>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    // Quản lý giao diện của item liên hệ
    public static class ContactViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        TextView tvPhoneNumber;
        ImageView ivProfile;
        ImageView ivFavorite;

        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvPhoneNumber = itemView.findViewById(R.id.tvPhoneNumber);
            ivProfile = itemView.findViewById(R.id.ivProfile);
            ivFavorite = itemView.findViewById(R.id.ivFavorite);

        }
    }
    // Quản lý giao diện của item header
    public static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvHeader;
        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHeader = itemView.findViewById(R.id.tvHeader);
        }
    }

    // Xử lý sự kiện nhấn vào một liên hệ
    public interface OnContactClickListener {
        void onContactClick(Contact contact);
    }
    // Xử lý sự kiện nhấn vào biểu tượng yêu thích của một liên hệ
    public interface OnFavoriteClickListener {
        void onFavoriteClick(Contact contact, int position);
    }
}