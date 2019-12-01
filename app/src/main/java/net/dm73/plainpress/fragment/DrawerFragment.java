package net.dm73.plainpress.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class DrawerFragment extends Fragment {


    public static String USER_ID = "user_id";

    private String userId;


    public static DrawerFragment newInstance(String userId) {

        Bundle args = new Bundle();

        DrawerFragment fragment = new DrawerFragment();
        args.putString(USER_ID, userId);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null){
            userId = getArguments().getString(USER_ID);
        }
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }
}
