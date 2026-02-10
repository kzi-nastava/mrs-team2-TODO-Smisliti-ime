package com.example.getgo.fragments.admins;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.getgo.R;
import com.example.getgo.adapters.AdminChatListAdapter;
import com.example.getgo.dtos.supportChat.ChatSummary;
import com.example.getgo.repositories.SupportChatRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AdminChatListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AdminChatListFragment extends Fragment {

    private RecyclerView rvChats;
    private AdminChatListAdapter adapter;
    private SupportChatRepository repository;

    public AdminChatListFragment() {
        // Required empty public constructor
    }


    public static AdminChatListFragment newInstance(String param1, String param2) {
        AdminChatListFragment fragment = new AdminChatListFragment();
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
        View view = inflater.inflate(R.layout.fragment_admin_chat_list, container, false);

        rvChats = view.findViewById(R.id.rvChats);
        repository = SupportChatRepository.getInstance();

        adapter = new AdminChatListAdapter(new ArrayList<>(), (chatId, userName) -> openChat(chatId, userName));
        rvChats.setLayoutManager(new LinearLayoutManager(getContext()));
        rvChats.setAdapter(adapter);

        loadChats();
        return view;
    }

    private void loadChats() {
        new Thread(() -> {
            try {
                List<ChatSummary> chats = repository.getAllChats();
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> adapter.setChats(chats));
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Failed to load chats", Toast.LENGTH_SHORT).show()
                    );
                }
            }
        }).start();
    }


    private void openChat(int chatId, String userName) {
        AdminSupportChatFragment fragment = new AdminSupportChatFragment();
        Bundle args = new Bundle();
        args.putInt("CHAT_ID", chatId);
        args.putString("CHAT_USER_NAME", userName);
        fragment.setArguments(args);

        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit();
    }
}