package net.dm73.plainpress.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.dm73.plainpress.CharaterClickListener;
import net.dm73.plainpress.FaceDetail;
import net.dm73.plainpress.R;
import net.dm73.plainpress.adapter.CharacterRecyclerAdapter;
import net.dm73.plainpress.model.FaceCharacter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;



public class AvatarCharacters extends Fragment implements CharaterClickListener {


    private static String CHARATER_TYPR = "charater_type";
    private static String SELECTED_ITEM = "selected_item";
    private static String GENDER_AVATR = "gender_avatar";
    private static String COLOR_AVATR = "color_avatar";

    private int characterType;
    private int selectedItem;
    private boolean gender;
    private String color;
    private List<FaceCharacter> listCharacterRessources;
    private CharacterRecyclerAdapter mAdapter;
    private CharacterChoosed characterChoosedListner;


    public static AvatarCharacters newInstance(int characterType, int selectedItem, boolean gender, String color){
        AvatarCharacters avatarCharacters = new AvatarCharacters();
        Bundle args = new Bundle();
        args.putInt(CHARATER_TYPR, characterType);
        args.putInt(SELECTED_ITEM, selectedItem);
        args.putBoolean(GENDER_AVATR, gender);
        args.putString(COLOR_AVATR, color);
        avatarCharacters.setArguments(args);
        return avatarCharacters;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments()!=null){
            characterType = getArguments().getInt(CHARATER_TYPR);
            selectedItem = getArguments().getInt(SELECTED_ITEM);
            gender = getArguments().getBoolean(GENDER_AVATR);
            color = getArguments().getString(COLOR_AVATR);
            loadCharatersData(characterType, selectedItem, gender);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        RecyclerView charatecrRecyclerView = (RecyclerView) inflater.inflate(R.layout.fragment_face_characters, null);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, gender);
        layoutManager.setStackFromEnd(false);
        mAdapter = new CharacterRecyclerAdapter(getActivity(), listCharacterRessources, characterType, gender, color);
        charatecrRecyclerView.setLayoutManager(layoutManager);
        charatecrRecyclerView.setAdapter(mAdapter);
        mAdapter.setCharacterClickListener(this);

        return charatecrRecyclerView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof AvatarCharacters.CharacterChoosed) {
            characterChoosedListner = (AvatarCharacters.CharacterChoosed) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onCharacterClickListner(int type, int position) {

        characterChoosedListner.onCharacterChoosed(type, listCharacterRessources.get(position).getRessourceName());
        mAdapter.itemSlected(position);

        Log.e("item","clicked position: "+position+" and type:"+type);
    }


    @Override
    public void onDetach() {
        super.onDetach();
        characterChoosedListner = null;
    }


    private void loadCharatersData(int type, int selectedItem, boolean gender){

        listCharacterRessources = new ArrayList<>();
        List<String> listRessources = (gender) ? Arrays.asList(getResources().getStringArray(FaceDetail.listArrayRessourcesMale.get(type))) : Arrays.asList(getResources().getStringArray(FaceDetail.listArrayRessourcesFemale.get(type)));
        for(int i=0; i<listRessources.size(); i++)
            if(i==selectedItem)
                listCharacterRessources.add(new FaceCharacter(listRessources.get(i), true));
            else{
                listCharacterRessources.add(new FaceCharacter(listRessources.get(i), false));
            }
    }


    public interface CharacterChoosed {
        void onCharacterChoosed(int type, String res);
    }

}
