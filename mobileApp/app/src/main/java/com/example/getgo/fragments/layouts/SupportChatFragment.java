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
import com.example.getgo.dtos.supportChat.GetMessageDTO;
import com.example.getgo.model.ChatMessage;
import com.example.getgo.model.MessageType;
import com.example.getgo.repositories.SupportChatRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class SupportChatFragment extends Fragment {

    private RecyclerView rvMessages;
    private EditText etMessage;
    private MaterialButton btnSend;
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

        rvMessages = view.findViewById(R.id.support_chat_recycler);
        etMessage = view.findViewById(R.id.support_chat_input);
        btnSend   = view.findViewById(R.id.support_chat_send);
        tvChatTitle = view.findViewById(R.id.tvChatTitle);

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
                List<GetMessageDTO> dtos = repository.getMessagesDTO();
                List<ChatMessage> messages = new ArrayList<>();

                SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

                for (GetMessageDTO dto : dtos) {
                    boolean isMine = dto.getSenderType().equalsIgnoreCase(
                            userType.equals("PASSENGER") ? "USER" : userType.equals("DRIVER") ? "DRIVER" : "ADMIN"
                    );

                    String timestamp = dto.getTimestamp();
                    if (timestamp.contains(".")) {
                        timestamp = timestamp.substring(0, timestamp.indexOf("."));
                    }

                    Date date = isoFormat.parse(timestamp);
                    String time = sdf.format(date);

                    messages.add(new ChatMessage(dto.getText(), isMine, time, MessageType.TEXT));
                }

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
