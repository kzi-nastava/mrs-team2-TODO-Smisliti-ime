package com.example.getgo.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.getgo.R;
import com.example.getgo.dtos.supportChat.ChatSummary;

import java.util.List;

public class AdminChatListAdapter extends RecyclerView.Adapter<AdminChatListAdapter.ChatViewHolder>{

    private List<ChatSummary> chats;
    private final ChatClickListener listener;

    public interface ChatClickListener {
        void onChatClick(int chatId);
    }

    public AdminChatListAdapter(List<ChatSummary> chats, ChatClickListener listener) {
        this.chats = chats;
        this.listener = listener;
    }

    public void setChats(List<ChatSummary> chats) {
        this.chats = chats;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_summary, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ChatViewHolder holder,
            int position
    ) {
        ChatSummary chat = chats.get(position);
        holder.tvName.setText(chat.getUserName());
        holder.itemView.setOnClickListener(
                v -> listener.onChatClick(chat.getId())
        );
    }

    @Override
    public int getItemCount() {
        return chats == null ? 0 : chats.size();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;

        ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
        }
    }
}
