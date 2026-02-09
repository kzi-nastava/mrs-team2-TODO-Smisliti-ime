package com.example.getgo.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.getgo.R;
import com.example.getgo.model.ChatMessage;

import java.util.List;

public class SupportChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final List<ChatMessage> messages;

    private static final int TYPE_MINE = 1;
    private static final int TYPE_OTHER = 2;

    public SupportChatAdapter(List<ChatMessage> messages) {
        this.messages = messages;
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage message = messages.get(position);
        return message.isMine() ? TYPE_MINE : TYPE_OTHER;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_MINE) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_chat_mine, parent, false);
            return new MineViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_chat_other, parent, false);
            return new OtherViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
        if (holder instanceof MineViewHolder) {
            ((MineViewHolder) holder).bind(message);
        } else if (holder instanceof OtherViewHolder) {
            ((OtherViewHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public void addMessage(ChatMessage message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    public void setMessages(List<ChatMessage> messages) {
        this.messages.clear();
        this.messages.addAll(messages);
        notifyDataSetChanged();
    }

    // --- ViewHolders ---

    static class MineViewHolder extends RecyclerView.ViewHolder {
        TextView textMessage, textTime;

        public MineViewHolder(@NonNull View itemView) {
            super(itemView);
            textMessage = itemView.findViewById(R.id.text_message);
            textTime = itemView.findViewById(R.id.text_time);
        }

        void bind(ChatMessage message) {
            textMessage.setText(message.getText());
            textTime.setText(message.getTime());
        }
    }

    static class OtherViewHolder extends RecyclerView.ViewHolder {
        TextView textMessage, textTime;

        public OtherViewHolder(@NonNull View itemView) {
            super(itemView);
            textMessage = itemView.findViewById(R.id.text_message);
            textTime = itemView.findViewById(R.id.text_time);
        }

        void bind(ChatMessage message) {
            textMessage.setText(message.getText());
            textTime.setText(message.getTime());
        }
    }
}
