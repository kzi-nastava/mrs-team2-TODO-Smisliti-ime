package com.example.getgo.fragments.admins;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.getgo.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;


public class AdminSupportChatFragment extends Fragment {

    private static final String ARG_CHAT_ID = "chat_id";

    private RecyclerView recyclerView;
    private TextInputEditText inputMessage;
    private MaterialButton sendButton;

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

        recyclerView = view.findViewById(R.id.admin_support_chat_recycler);
        inputMessage = view.findViewById(R.id.admin_support_chat_input);
        sendButton = view.findViewById(R.id.admin_support_chat_send);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        // TODO: recyclerView.setAdapter(adminChatAdapter);

        sendButton.setOnClickListener(v -> {
            String message = inputMessage.getText() != null
                    ? inputMessage.getText().toString().trim()
                    : "";

            if (!message.isEmpty()) {
                // TODO: send admin message
                inputMessage.setText("");
            }
        });

        return view;
    }
}