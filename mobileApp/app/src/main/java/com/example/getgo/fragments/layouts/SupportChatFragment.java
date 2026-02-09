package com.example.getgo.fragments.layouts;

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


public class SupportChatFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextInputEditText inputMessage;
    private MaterialButton sendButton;


    public SupportChatFragment() {
        // Required empty public constructor
    }



    public static SupportChatFragment newInstance(String param1, String param2) {
        SupportChatFragment fragment = new SupportChatFragment();
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
        View view = inflater.inflate(R.layout.fragment_support_chat, container, false);

        recyclerView = view.findViewById(R.id.support_chat_recycler);
        inputMessage = view.findViewById(R.id.support_chat_input);
        sendButton = view.findViewById(R.id.support_chat_send);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        // TODO: recyclerView.setAdapter(chatAdapter);

        sendButton.setOnClickListener(v -> {
            String message = inputMessage.getText() != null
                    ? inputMessage.getText().toString().trim()
                    : "";

            if (!message.isEmpty()) {
                // TODO: send message to backend / websocket
                inputMessage.setText("");
            }
        });

        return view;
    }
}
