package com.example.getgo.fragments.admins;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.getgo.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AdminChatListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AdminChatListFragment extends Fragment {

    private RecyclerView recyclerView;

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

        recyclerView = view.findViewById(R.id.admin_chat_list_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // TODO: recyclerView.setAdapter(adminChatListAdapter);

        return view;
    }
}