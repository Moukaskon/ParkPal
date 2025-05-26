package com.example.parkpal;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

public class AdminChangesFragment extends Fragment {
    String code;
    public AdminChangesFragment(){
        // To do
    }
    public AdminChangesFragment(String areaCode){
        code = areaCode;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_changes, container, false);
        return view;
    }
}
