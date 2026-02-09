package com.example.getgo.fragments.layouts;

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

import com.example.getgo.R;
import com.example.getgo.adapters.SupportChatAdapter;
import com.example.getgo.callbacks.SupportChatMessageListener;
import com.example.getgo.dtos.supportChat.CreateMessageRequestDTO;
import com.example.getgo.model.ChatMessage;
import com.example.getgo.repositories.SupportChatRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;


public class SupportChatFragment extends Fragment {

    private RecyclerView rvMessages;
    private EditText etMessage;
    private ImageButton btnSend;
    private TextView tvChatTitle;
    private SupportChatAdapter adapter;
    private SupportChatRepository repository;
    private String userType;



    public SupportChatFragment() {
        // Required empty public constructor
    }



    public static SupportChatFragment newInstance(String userType) {
        SupportChatFragment fragment = new SupportChatFragment();
        Bundle args = new Bundle();
        args.putString("USER_TYPE", userType); // "PASSENGER" ili "DRIVER"
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userType = getArguments().getString("USER_TYPE", "PASSENGER"); // default
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_support_chat, container, false);

        tvChatTitle = view.findViewById(R.id.tvChatTitle);
        rvMessages = view.findViewById(R.id.rvMessages);
        etMessage = view.findViewById(R.id.etMessage);
        btnSend = view.findViewById(R.id.btnSend);

        tvChatTitle.setText("Chat with Admin");

        repository = SupportChatRepository.getInstance();

        adapter = new SupportChatAdapter(new ArrayList<>());
        rvMessages.setLayoutManager(new LinearLayoutManager(getContext()));
        rvMessages.setAdapter(adapter);

        loadMessages();

        btnSend.setOnClickListener(v -> {
            String text = etMessage.getText().toString().trim();
            if (!text.isEmpty()) sendMessage(text);
        });

        return view;
    }

    private void loadMessages() {
        new Thread(() -> {
            try {
                List<ChatMessage> messages = repository.getMyMessages(userType);
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
                repository.sendMessage(new CreateMessageRequestDTO(text));
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        etMessage.setText("");
                        loadMessages();
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
