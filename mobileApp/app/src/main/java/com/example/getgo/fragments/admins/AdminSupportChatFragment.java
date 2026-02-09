package com.example.getgo.fragments.admins;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.getgo.R;
import com.example.getgo.adapters.SupportChatAdapter;
import com.example.getgo.dtos.supportChat.CreateMessageRequestDTO;
import com.example.getgo.model.ChatMessage;
import com.example.getgo.repositories.SupportChatRepository;
import com.example.getgo.utils.WebSocketManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;


public class AdminSupportChatFragment extends Fragment {

    private RecyclerView rvMessages;
    private EditText etMessage;
    private ImageButton btnSend;
    private TextView tvChatTitle;
    private SupportChatAdapter adapter;
    private SupportChatRepository repository;

    private WebSocketManager webSocketManager;


    private int chatId;
    private String username;

    public AdminSupportChatFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static AdminSupportChatFragment newInstance(String param1, String param2) {
        AdminSupportChatFragment fragment = new AdminSupportChatFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_support_chat, container, false);

        tvChatTitle = view.findViewById(R.id.tvChatTitle);
        rvMessages = view.findViewById(R.id.rvMessages);
        etMessage = view.findViewById(R.id.etMessage);
        btnSend = view.findViewById(R.id.btnSend);

        chatId = getArguments() != null ? getArguments().getInt("CHAT_ID") : -1;
        username = getArguments() != null ? getArguments().getString("CHAT_USER_NAME") : "noName";

        repository = SupportChatRepository.getInstance();

        tvChatTitle.setText(username);

        adapter = new SupportChatAdapter(new ArrayList<>());
        rvMessages.setLayoutManager(new LinearLayoutManager(getContext()));
        rvMessages.setAdapter(adapter);

        loadMessages();

        webSocketManager = new WebSocketManager();
        webSocketManager.connect();

        webSocketManager.subscribeToChat(
                (long) chatId,
                "ADMIN",
                message -> {
                    if (getActivity() == null) return;

                    getActivity().runOnUiThread(() -> {
                        adapter.addMessage(message);
                        rvMessages.scrollToPosition(adapter.getItemCount() - 1);
                    });
                }
        );

        btnSend.setOnClickListener(v -> {
            String text = etMessage.getText().toString().trim();
            if (!text.isEmpty()) sendMessage(text);
        });

        return view;
    }

    private void loadMessages() {
        new Thread(() -> {
            try {
                List<ChatMessage> messages = repository.getMessages(chatId);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        adapter.setMessages(messages);
                        rvMessages.scrollToPosition(messages.size() - 1);
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void sendMessage(String text) {
        new Thread(() -> {
            try {
                repository.sendMessageAdmin(chatId, text);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        etMessage.setText("");
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Failed to send message", Toast.LENGTH_SHORT).show()
                    );
                }
            }
        }).start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (webSocketManager != null) {
            webSocketManager.disconnect();
        }
    }

}