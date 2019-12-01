package net.dm73.plainpress;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import net.dm73.plainpress.fragment.AvatarCharacters;
import net.dm73.plainpress.model.Detail;
import net.dm73.plainpress.util.ActivityConfig;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;

public class FaceDetail extends AppCompatActivity implements AvatarCharacters.CharacterChoosed{

    @BindView(R.id.charaterViewPager)
    ViewPager characterViewPager;
    @BindView(R.id.tabTypeDetail)
    TabLayout mTabLayout;
    @BindViews({ R.id.faceCharater,  R.id.noseCharater, R.id.earCharater, R.id.mouthCharater, R.id.eyesCharater, R.id.eyebrowsCharater, R.id.hairCharater,  R.id.beardCharater, R.id.moustahceCharater })
    List<ImageView> avatarViews;
    @BindViews({ R.id.colorPalette1, R.id.colorPalette2, R.id.colorPalette3, R.id.colorPalette4, R.id.colorPalette5,  R.id.colorPalette6 })
    List<CardView> paletteViews;
    @BindView(R.id.avatarDone)
    ImageView avatarDone;
    @BindView(R.id.back_comment)
    ImageView backActivity;
    @BindView(R.id.manAvatar)
    ImageView manAvatar;
    @BindView(R.id.womanAvatar)
    ImageView womanAvatar;
    @BindView(R.id.imageResult)
    ImageView imageRes;
    @BindView(R.id.cardPreview)
    CardView cardPreview;


    private static final int NO_ITEM = -1;
    public static List<Integer> listArrayRessourcesMale = Arrays.asList(R.array.face_male, R.array.nose_male, R.array.ear_male,R.array.mouth_male, R.array.eyes_male,  R.array.eyebrows_male, R.array.hair_male,  R.array.beard_male, R.array.mustache_male);
    public static List<Integer> listArrayRessourcesFemale = Arrays.asList(R.array.face_female, R.array.nose_female, R.array.ear_female,R.array.mouth_female, R.array.eyes_female,  R.array.eyebrows_female, R.array.hair_female);
    public static List<String> listTypes = Arrays.asList("face", "nose", "ear", "mouth", "eyes",  "eyebrows", "hair", "beard", "moustache");
    public static List<String> listTypesFemale = Arrays.asList("face", "nose", "ear", "mouth", "eyes",  "eyebrows", "hair");
    public static List<String> skinColors = Arrays.asList("White", "MistyRose", "Black", "SandyBrown", "LightYellow");
    public static List<String> hairColors = Arrays.asList("Black", "Beige", "Gray", "Gold", "DarkRed", "Maroon");
    public static List<String> eyesColors = Arrays.asList("Black", "Brown", "Green", "Blue", "Gray");
    private static HashMap<String,Integer> colorPalette;
    private String skinColor = "White";
    private String hairColor = "Black";
    private String eyesColor = "Black";

    private FaceDetailPagerAdapter mAdapter;
    private HashMap<String,Object> avatarComponents;
    private boolean gender = true;
    private HashMap<CardView,String> colorPaletteView;

    private Vibrator vibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_datail);
        ButterKnife.bind(this);

        ActivityConfig.setStatusBarTranslucent(getWindow());

        if(getIntent().getSerializableExtra("UPDATE") != null){
            HashMap<String,Object> detail = (HashMap<String, Object>) getIntent().getSerializableExtra("UPDATE");
            detail.remove("picture");
            updateColor(detail);
            updateUi(detail);
        }else
            updateUi(null);

        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        colorPalette = loadPalette();


        colorPaletteView = new HashMap<>();
        for(int i=0; i<6; i++)
            colorPaletteView.put(paletteViews.get(i), "");

        updateColorPalette(0);
    }

    @Override
    protected void onStart() {
        super.onStart();

        avatarDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MergeChoosedDrawables mergeChoosedDrawables = new MergeChoosedDrawables(FaceDetail.this);
                mergeChoosedDrawables.execute(avatarComponents);
                Log.e("avatarComponents", avatarComponents.toString());
            }
        });

        backActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        womanAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vibrator.vibrate(30);
                womanAvatar.setBackgroundResource(R.drawable.background_gender_active);
                manAvatar.setBackgroundColor(0x00ffffff);
                gender = false;
                avatarComponents = new HashMap();
                clearAvatarView();
                avatarDone.setVisibility(View.GONE);
                updateUi(null);
            }
        });

        manAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vibrator.vibrate(30);
                manAvatar.setBackgroundResource(R.drawable.background_gender_active);
                womanAvatar.setBackgroundColor(0x00ffffff);
                gender = true;
                avatarComponents = new HashMap();
                clearAvatarView();
                avatarDone.setVisibility(View.GONE);
                updateUi(null);
            }
        });

        paletteViews.get(0).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("color name", colorPaletteView.get(paletteViews.get(0)));
                changeCorrespondingDefaultColor(mTabLayout.getSelectedTabPosition(), colorPaletteView.get(paletteViews.get(0)));

                mAdapter.notifyColorChange(mTabLayout.getSelectedTabPosition());

                changeImageTintColor(mTabLayout.getSelectedTabPosition());

            }
        });

        paletteViews.get(1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("color name", colorPaletteView.get(paletteViews.get(1)));
                changeCorrespondingDefaultColor(mTabLayout.getSelectedTabPosition(), colorPaletteView.get(paletteViews.get(1)));
                mAdapter.notifyColorChange(mTabLayout.getSelectedTabPosition());

                changeImageTintColor(mTabLayout.getSelectedTabPosition());
            }
        });

        paletteViews.get(2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("color name", colorPaletteView.get(paletteViews.get(2)));
                changeCorrespondingDefaultColor(mTabLayout.getSelectedTabPosition(), colorPaletteView.get(paletteViews.get(2)));
                mAdapter.notifyColorChange(mTabLayout.getSelectedTabPosition());

                changeImageTintColor(mTabLayout.getSelectedTabPosition());
            }
        });

        paletteViews.get(3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("color name", colorPaletteView.get(paletteViews.get(3)));
                changeCorrespondingDefaultColor(mTabLayout.getSelectedTabPosition(), colorPaletteView.get(paletteViews.get(3)));
                mAdapter.notifyColorChange(mTabLayout.getSelectedTabPosition());

                changeImageTintColor(mTabLayout.getSelectedTabPosition());
            }
        });

        paletteViews.get(4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("color name", colorPaletteView.get(paletteViews.get(4)));
                changeCorrespondingDefaultColor(mTabLayout.getSelectedTabPosition(), colorPaletteView.get(paletteViews.get(4)));
                mAdapter.notifyColorChange(mTabLayout.getSelectedTabPosition());

                changeImageTintColor(mTabLayout.getSelectedTabPosition());
            }
        });

        paletteViews.get(5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("color name", colorPaletteView.get(paletteViews.get(5)));
                changeCorrespondingDefaultColor(mTabLayout.getSelectedTabPosition(), colorPaletteView.get(paletteViews.get(5)));
                mAdapter.notifyColorChange(mTabLayout.getSelectedTabPosition());

                changeImageTintColor(mTabLayout.getSelectedTabPosition());
            }
        });

    }

    @Override
    public void onCharacterChoosed(int type, String res) {

        //load image avatar into Avatar view
        Uri uri = getUri(type, res.replace(".png",""));
        Glide.with(this)
                .loadFromMediaStore(uri)
                .fitCenter()
                .into(avatarViews.get(type));

        //save type detail
        HashMap<String, String> detailType = new HashMap<>();
        detailType.put("color", getCorrespondingColor(type));
        detailType.put("item", res.replace(".png",""));

        if(gender)
            avatarComponents.put(listTypes.get(type), detailType);
        else
            avatarComponents.put(listTypesFemale.get(type), detailType);

        if(avatarIsReady())
            avatarDone.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private HashMap<String,Integer> loadPalette() {

        HashMap<String,Integer> color = new HashMap<>();
        color.put("White", 0xffffffff);
        color.put("Black", 0xff000000);
        color.put("Gray", 0xffbcbec0);
        color.put("Beige", 0xfff7e8c9);
        color.put("DarkRed", 0xffd61f26);
        color.put("Gold", 0xfffddb00);
        color.put("Maroon", 0xff7c513c);
        color.put("Gold", 0xfffddb00);
        color.put("Blue", 0xff4a7474);
        color.put("Green", 0xff3c804f);
        color.put("Brown", 0xff845a39);
        color.put("LightYellow", 0xfff5e0cd);
        color.put("MistyRose", 0xfff0beb4);
        color.put("SandyBrown", 0xffcc997f);

        return color;
    }

    private Uri getUri(int type, String resName){
        String root = "file:///android_asset/avatar/";
        if(gender){
            root+="male/";
            return Uri.parse(root+ listTypes.get(type)+"/"+getCorrespondingColor(type)+"/"+resName+".png");
        }else{
            root+="female/";
            return Uri.parse(root+ listTypesFemale.get(type)+"/"+getCorrespondingColor(type)+"/"+resName+".png");
        }
    }

    private String getCorrespondingColor(int type){

        //todo:add gender case

        switch(type){
            case 0:
                return skinColor.toLowerCase();
            case 1:
                return skinColor.toLowerCase();
            case 2:
                return skinColor.toLowerCase();
            case 3:
                return skinColor.toLowerCase();
            case 4:
                return eyesColor.toLowerCase();
            case 5:
                return hairColor.toLowerCase();
            case 6:
                return hairColor.toLowerCase();
            case 7:
                return hairColor.toLowerCase();
            case 8:
                return hairColor.toLowerCase();
            default:
                return "df";
        }
    }

    public void updateColorPalette(int type){

        //todo:add gender case

        List<String> listColors = new ArrayList<>();

        switch (type) {
            case 0:
                listColors = skinColors;
                break;
            case 1:
                listColors = skinColors;
                break;
            case 2:
                listColors = skinColors;
                break;
            case 3:
                listColors = skinColors;
                break;
            case 4:
                listColors = eyesColors;
                break;
            case 5:
                listColors = hairColors;
                break;
            case 6:
                listColors = hairColors;
                break;
            case 7:
                listColors = hairColors;
                break;
            case 8:
                listColors = hairColors;
                break;
        }

        for(int i=0; i<6; i++){
            if(i<listColors.size()) {
                paletteViews.get(i).setCardBackgroundColor(colorPalette.get(listColors.get(i)));
                paletteViews.get(i).setVisibility(View.VISIBLE);
                colorPaletteView.put(paletteViews.get(i), listColors.get(i));
            }else {
                paletteViews.get(i).setVisibility(View.GONE);
                colorPaletteView.put(paletteViews.get(i), "");
            }
        }

    }

    public void changeCorrespondingDefaultColor(int type, String color){

        switch (type) {
            case 0:
                skinColor = color;
                break;
            case 1:
                skinColor = color;
                break;
            case 2:
                skinColor = color;
                break;
            case 3:
                skinColor = color;
                break;
            case 4:
                eyesColor = color;
                break;
            case 5:
                hairColor = color;
                break;
            case 6:
                hairColor = color;
                break;
            case 7:
                hairColor = color;
                break;
            case 8:
                hairColor = color;
                break;
        }
    }

    /**
     * change the corresponding image by selected color
     */
    public void changeImageTintColor(int type){

        List<String> listTypes;
        if(gender)
            listTypes = FaceDetail.listTypes;
        else
            listTypes = listTypesFemale;

        switch (type){
            case 0:
                changeSkinViewsColor(listTypes);
                break;
            case 1:
                changeSkinViewsColor(listTypes);
                break;
            case 2:
                changeSkinViewsColor(listTypes);
                break;
            case 3:
                changeSkinViewsColor(listTypes);
                break;
            case 4:
                changeEyesViewColor(listTypes);
                break;
            case 5:
                changeHairViewsColor(listTypes);
                break;
            case 6:
                changeHairViewsColor(listTypes);
                break;
            case 7:
                changeHairViewsColor(listTypes);
                break;
            case 8:
                changeHairViewsColor(listTypes);
                break;
        }
    }

    public void changeSkinViewsColor(List<String> listTypes){
        Uri uri;

        if(avatarComponents.containsKey(listTypes.get(0))) {
            uri = getUri(0, ((HashMap<String, String>)avatarComponents.get(listTypes.get(0))).get("item"));
            Log.e("uri face", uri.toString());
            Glide.with(getApplicationContext())
                    .loadFromMediaStore(uri)
                    .fitCenter()
                    .into(avatarViews.get(0));
            ((HashMap<String, String>)avatarComponents.get(listTypes.get(0))).put("color", skinColor);
        }

        if(avatarComponents.containsKey(listTypes.get(1))) {
            uri = getUri(1, ((HashMap<String, String>)avatarComponents.get(listTypes.get(1))).get("item"));
            Glide.with(getApplicationContext())
                    .loadFromMediaStore(uri)
                    .fitCenter()
                    .into(avatarViews.get(1));

            ((HashMap<String, String>)avatarComponents.get(listTypes.get(1))).put("color", skinColor);
        }

        if(avatarComponents.containsKey(listTypes.get(2))) {
            uri = getUri(2, ((HashMap<String, String>)avatarComponents.get(listTypes.get(2))).get("item"));
            Glide.with(getApplicationContext())
                    .loadFromMediaStore(uri)
                    .fitCenter()
                    .into(avatarViews.get(2));

            ((HashMap<String, String>)avatarComponents.get(listTypes.get(2))).put("color", skinColor);
        }

        if(avatarComponents.containsKey(listTypes.get(3))) {
            uri = getUri(3, ((HashMap<String, String>)avatarComponents.get(listTypes.get(3))).get("item"));
            Glide.with(getApplicationContext())
                    .loadFromMediaStore(uri)
                    .fitCenter()
                    .into(avatarViews.get(3));

            ((HashMap<String, String>)avatarComponents.get(listTypes.get(3))).put("color", skinColor);
        }
    }

    public void changeHairViewsColor(List<String> listTypes){
        Uri uri;

        if(avatarComponents.containsKey(listTypes.get(5))) {
            uri = getUri(5, ((HashMap<String, String>)avatarComponents.get(listTypes.get(5))).get("item"));
            Glide.with(getApplicationContext())
                    .loadFromMediaStore(uri)
                    .fitCenter()
                    .into(avatarViews.get(5));

            ((HashMap<String, String>)avatarComponents.get(listTypes.get(5))).put("color", hairColor);
        }

        if(avatarComponents.containsKey(listTypes.get(6))) {
            uri = getUri(6, ((HashMap<String, String>)avatarComponents.get(listTypes.get(6))).get("item"));
            Glide.with(getApplicationContext())
                    .loadFromMediaStore(uri)
                    .fitCenter()
                    .into(avatarViews.get(6));

            ((HashMap<String, String>)avatarComponents.get(listTypes.get(6))).put("color", hairColor);
        }

        if(gender) {
            if (avatarComponents.containsKey(listTypes.get(7))) {
                uri = getUri(7, ((HashMap<String, String>) avatarComponents.get(listTypes.get(7))).get("item"));
                Glide.with(getApplicationContext())
                        .loadFromMediaStore(uri)
                        .fitCenter()
                        .into(avatarViews.get(7));

                ((HashMap<String, String>) avatarComponents.get(listTypes.get(7))).put("color", hairColor);
            }

            if (avatarComponents.containsKey(listTypes.get(8))) {
                uri = getUri(8, ((HashMap<String, String>) avatarComponents.get(listTypes.get(8))).get("item"));
                Glide.with(getApplicationContext())
                        .loadFromMediaStore(uri)
                        .fitCenter()
                        .into(avatarViews.get(8));

                ((HashMap<String, String>) avatarComponents.get(listTypes.get(8))).put("color", hairColor);
            }
        }
    }

    public void changeEyesViewColor(List<String> listTypes){
        if(avatarComponents.containsKey(listTypes.get(4))) {
            Uri uri = getUri(4, ((HashMap<String, String>)avatarComponents.get(listTypes.get(4))).get("item"));
            Glide.with(getApplicationContext())
                    .loadFromMediaStore(uri)
                    .fitCenter()
                    .into(avatarViews.get(4));


            ((HashMap<String, String>)avatarComponents.get(listTypes.get(4))).put("color", eyesColor);
        }
    }

    private void clearAvatarView(){
        for(int i=0; i<avatarViews.size(); i++){
            avatarViews.get(i).setImageBitmap(null);
        }
    }

    private boolean avatarIsReady(){

        for(int i=0; i<avatarComponents.size(); i++){
            if(avatarComponents.get(listTypes.get(0)) == null){
                return false;
            }

            if(avatarComponents.get(listTypes.get(1)) == null){
                return false;
            }

            if(avatarComponents.get(listTypes.get(3)) == null){
                return false;
            }

            if(avatarComponents.get(listTypes.get(4)) == null){
                return false;
            }
        }

        return true;
    }

    private HashMap<String,Object> detailToHashmap(Detail detail){
        HashMap<String, Object> data = new HashMap<>();

            data.put("gender", detail.isGender());

            if (detail.getFace() != null) {
                data.put(listTypes.get(0), detail.getFace());
            }

            if (detail.getNose() != null) {
                data.put(listTypes.get(1), detail.getNose());
            }

            if (detail.getEar() != null) {
                data.put(listTypes.get(2), detail.getEar());
            }

            if (detail.getMouth() != null) {
                data.put(listTypes.get(3), detail.getMouth());
            }

            if (detail.getEyes() != null) {
                data.put(listTypes.get(4), detail.getEyes());
            }

            if (detail.getEyebrows() != null) {
                data.put(listTypes.get(5), detail.getEyebrows());
            }

            if (detail.getHair() != null) {
                data.put(listTypes.get(6), detail.getHair());
            }

            if (detail.getBeard() != null) {
                data.put(listTypes.get(7), detail.getBeard());
            }

            if (detail.getMoustache() != null) {
                data.put(listTypes.get(8), detail.getMoustache());
            }

        return data;
    }

    private void updateUi(HashMap<String,Object> detail){

        if(detail!=null){
            gender = (Boolean) detail.get("gender");
            List<Integer> data = hashMapToList(detail, gender);
            mAdapter = new FaceDetailPagerAdapter(getSupportFragmentManager(), data, gender);
            characterViewPager.setAdapter(mAdapter);
            characterViewPager.setOffscreenPageLimit(7);
            mTabLayout.setupWithViewPager(characterViewPager);
            mTabLayout.setSelectedTabIndicatorHeight(0);
            avatarComponents = detail;
            drawAvatar(data, gender);
            if(gender){
                manAvatar.setBackgroundResource(R.drawable.background_gender_active);
                womanAvatar.setBackgroundColor(0x00ffffff);
            }else{
                womanAvatar.setBackgroundResource(R.drawable.background_gender_active);
                manAvatar.setBackgroundColor(0x00ffffff);
            }
        }else{
            mAdapter = new FaceDetailPagerAdapter(getSupportFragmentManager(), null, gender);
            characterViewPager.setAdapter(mAdapter);
            characterViewPager.setOffscreenPageLimit(7);
            mTabLayout.setupWithViewPager(characterViewPager);
            mTabLayout.setSelectedTabIndicatorHeight(0);
            avatarComponents = new HashMap<>();
        }

        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener(){

            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                updateColorPalette(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

    }

    private List<Integer> hashMapToList(HashMap<String,Object> listSelectedItem, boolean gender){

        List<Integer> list = new ArrayList<>();
        if(gender)
            for(int i = 0; i< listTypes.size(); i++){
                if(listSelectedItem.get(listTypes.get(i))!=null && !((HashMap<String,String>) listSelectedItem.get(listTypes.get(i))).get("item").toString().isEmpty())
                    list.add(Integer.parseInt(((HashMap<String,String>) listSelectedItem.get(listTypes.get(i))).get("item").toString())-1);
                else
                    list.add(-1);
            }
        else
            for(int i = 0; i< listTypesFemale.size(); i++){
                if(listSelectedItem.get(listTypesFemale.get(i))!=null && !((HashMap<String,String>) listSelectedItem.get(listTypesFemale.get(i))).get("item").toString().isEmpty())
                    list.add(Integer.parseInt(((HashMap<String,String>) listSelectedItem.get(listTypesFemale.get(i))).get("item").toString())-1);
                else
                    list.add(-1);
            }
        Log.e("list", list.toString());
        return  list;
    }

    private void updateColor(HashMap<String,Object> detail){
        eyesColor = ((HashMap<String,String>)detail.get("eyes")).get("color").toLowerCase();
        skinColor = ((HashMap<String,String>)detail.get("face")).get("color").toLowerCase();
        if(detail.get("eyebrows") !=null){
            hairColor = ((HashMap<String,String>)detail.get("eyebrows")).get("color");
        }else if(detail.get("hair") !=null){
            hairColor = ((HashMap<String,String>)detail.get("hair")).get("color");
        }else if(detail.get("moustache") !=null){
            hairColor = ((HashMap<String,String>)detail.get("moustache")).get("color");
        }else if(detail.get("beard") !=null){
            hairColor = ((HashMap<String,String>)detail.get("beard")).get("color");
        }
    }


    private void drawAvatar(List<Integer> listSelectedItem, boolean gender){
        for(int i=0; i<listSelectedItem.size(); i++)
            if(listSelectedItem.get(i)>=0) {
                Log.e("size ressource",getResources().getStringArray(listArrayRessourcesMale.get(i)).length+"");
                Log.e("item selected",listSelectedItem.get(i)+"");
                if(gender)
                    onCharacterChoosed(i, Arrays.asList(getResources().getStringArray(listArrayRessourcesMale.get(i))).get(listSelectedItem.get(i)));
                else
                    onCharacterChoosed(i, Arrays.asList(getResources().getStringArray(listArrayRessourcesFemale.get(i))).get(listSelectedItem.get(i)));
            }

    }


    public class FaceDetailPagerAdapter extends FragmentStatePagerAdapter {

        private List<AvatarCharacters> listFragments;
        private boolean gender;

        public FaceDetailPagerAdapter(FragmentManager fm, List<Integer> listItemSelected, boolean gender) {
            super(fm);

            this.gender = gender;

            if(listItemSelected != null && listItemSelected.size()>0)
                loadFragment(listItemSelected, gender);
            else
                loadFragment();

        }

        @Override
        public Fragment getItem(int position) {
            return listFragments.get(position);
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public int getCount() {
            return listFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return (gender)? listTypes.get(position):listTypesFemale.get(position);
        }

        private void loadFragment(){
            listFragments = new ArrayList<>();
            if(gender)
                for(int i = 0; i< listTypes.size(); i++)
                    listFragments.add(AvatarCharacters.newInstance(i, NO_ITEM, gender, getCorrespondingColor(i)));
            else
                for(int i = 0; i< listTypesFemale.size(); i++)
                    listFragments.add(AvatarCharacters.newInstance(i, NO_ITEM, gender, getCorrespondingColor(i)));
        }

        private void loadFragment(List<Integer> listItemSelected, boolean gender){
            listFragments = new ArrayList<>();
            if(gender)
                for(int i = 0; i< listTypes.size(); i++)
                    listFragments.add(AvatarCharacters.newInstance(i, listItemSelected.get(i), gender, getCorrespondingColor(i)));
            else
                for(int i = 0; i< listTypesFemale.size(); i++)
                    listFragments.add(AvatarCharacters.newInstance(i, listItemSelected.get(i), gender, getCorrespondingColor(i)));
        }

        public void notifyColorChange(int type){
            switch(type){
                case 0:
                    setSkinFragment();
                    break;
                case 1:
                    setHairFragment();
                    break;
                case 2:
                    setEyeFragment();
                    break;
                case 3:
                    setSkinFragment();
                    break;
                case 4:
                    setSkinFragment();
                    break;
                case 5:
                    setHairFragment();
                    break;
                case 6:
                    setHairFragment();
                    break;
                case 7:
                    setSkinFragment();
                    break;
                case 8:
                    setHairFragment();
                    break;
            }
            notifyDataSetChanged();
        }

        private void setSkinFragment(){
            if(gender) {
                listFragments.set(0, AvatarCharacters.newInstance(0, avatarComponents.containsKey(listTypes.get(0)) ? Integer.parseInt(((HashMap<String, String>)avatarComponents.get(listTypes.get(0))).get("item").toString().substring(0,2))-1 : NO_ITEM, gender, getCorrespondingColor(0)));
                listFragments.set(1, AvatarCharacters.newInstance(1, avatarComponents.containsKey(listTypes.get(1)) ? Integer.parseInt(((HashMap<String, String>)avatarComponents.get(listTypes.get(1))).get("item").toString().substring(0,2))-1 : NO_ITEM, gender, getCorrespondingColor(1)));
                listFragments.set(2, AvatarCharacters.newInstance(2, avatarComponents.containsKey(listTypes.get(2)) ? Integer.parseInt(((HashMap<String, String>)avatarComponents.get(listTypes.get(2))).get("item").toString().substring(0,2))-1 : NO_ITEM, gender, getCorrespondingColor(2)));
                listFragments.set(3, AvatarCharacters.newInstance(3, avatarComponents.containsKey(listTypes.get(3)) ? Integer.parseInt(((HashMap<String, String>)avatarComponents.get(listTypes.get(3))).get("item").toString().substring(0,2))-1 : NO_ITEM, gender, getCorrespondingColor(3)));
            }else{
                listFragments.set(0, AvatarCharacters.newInstance(0, avatarComponents.containsKey(listTypesFemale.get(0)) ? Integer.parseInt(((HashMap<String, String>)avatarComponents.get(listTypesFemale.get(0))).get("item").toString().substring(0,2))-1 : NO_ITEM, gender, getCorrespondingColor(0)));
                listFragments.set(1, AvatarCharacters.newInstance(1, avatarComponents.containsKey(listTypesFemale.get(1)) ? Integer.parseInt(((HashMap<String, String>)avatarComponents.get(listTypesFemale.get(1))).get("item").toString().substring(0,2))-1 : NO_ITEM, gender, getCorrespondingColor(1)));
                listFragments.set(2, AvatarCharacters.newInstance(2, avatarComponents.containsKey(listTypesFemale.get(2)) ? Integer.parseInt(((HashMap<String, String>)avatarComponents.get(listTypesFemale.get(2))).get("item").toString().substring(0,2))-1 : NO_ITEM, gender, getCorrespondingColor(2)));
                listFragments.set(3, AvatarCharacters.newInstance(3, avatarComponents.containsKey(listTypesFemale.get(3)) ? Integer.parseInt(((HashMap<String, String>)avatarComponents.get(listTypesFemale.get(3))).get("item").toString().substring(0,2))-1 : NO_ITEM, gender, getCorrespondingColor(3)));
            }
        }

        private void setHairFragment(){
            if(gender) {
                listFragments.set(5, AvatarCharacters.newInstance(5, avatarComponents.containsKey(listTypes.get(5)) ? Integer.parseInt(((HashMap<String, String>)avatarComponents.get(listTypes.get(5))).get("item").toString().substring(0,2))-1 : NO_ITEM, gender, getCorrespondingColor(5)));
                listFragments.set(6, AvatarCharacters.newInstance(6, avatarComponents.containsKey(listTypes.get(6)) ? Integer.parseInt(((HashMap<String, String>)avatarComponents.get(listTypes.get(6))).get("item").toString().substring(0,2))-1 : NO_ITEM, gender, getCorrespondingColor(6)));
                listFragments.set(7, AvatarCharacters.newInstance(7, avatarComponents.containsKey(listTypes.get(7)) ? Integer.parseInt(((HashMap<String, String>)avatarComponents.get(listTypes.get(1))).get("item").toString().substring(0,2))-1 : NO_ITEM, gender, getCorrespondingColor(7)));
                listFragments.set(8, AvatarCharacters.newInstance(8, avatarComponents.containsKey(listTypes.get(8)) ? Integer.parseInt(((HashMap<String, String>)avatarComponents.get(listTypes.get(6))).get("item").toString().substring(0,2))-1 : NO_ITEM, gender, getCorrespondingColor(8)));
            }else{
                listFragments.set(5, AvatarCharacters.newInstance(5, avatarComponents.containsKey(listTypesFemale.get(5)) ? Integer.parseInt(((HashMap<String, String>)avatarComponents.get(listTypesFemale.get(5))).get("item").toString().substring(0,2))-1 : NO_ITEM, gender, getCorrespondingColor(5)));
                listFragments.set(6, AvatarCharacters.newInstance(6, avatarComponents.containsKey(listTypesFemale.get(6)) ? Integer.parseInt(((HashMap<String, String>)avatarComponents.get(listTypesFemale.get(6))).get("item").toString().substring(0,2))-1 : NO_ITEM, gender, getCorrespondingColor(6)));
            }
        }

        private void setEyeFragment(){
            if(gender){
                listFragments.set(4, AvatarCharacters.newInstance(4, avatarComponents.containsKey(listTypes.get(4)) ? Integer.parseInt(((HashMap<String, String>)avatarComponents.get(listTypes.get(4))).get("item").toString().substring(0,2))-1 : NO_ITEM, gender, getCorrespondingColor(4)));
            }else{
                listFragments.set(4, AvatarCharacters.newInstance(4, avatarComponents.containsKey(listTypesFemale.get(4)) ? Integer.parseInt(((HashMap<String, String>)avatarComponents.get(listTypesFemale.get(4))).get("item").toString().substring(0,2))-1 : NO_ITEM, gender, getCorrespondingColor(4)));
            }
        }
    }

    class MergeChoosedDrawables extends AsyncTask<HashMap<String, Object>, Void, Bitmap> {

        private AssetManager assetManager;
        private Activity mActivity;

        public MergeChoosedDrawables(Activity mActivity) {
            super();
            this.mActivity = mActivity;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            assetManager = getAssets();
            //show progress bar
        }

        @Override
        protected Bitmap doInBackground(HashMap<String,Object>... params) {

            List<Bitmap> listBimaps = new ArrayList<>();
            InputStream istr;

            if(gender)
                for(int i = 0; i< listTypes.size(); i++) {
                    try {
                        if(params[0].containsKey(listTypes.get(i))) {
                            //not all list case
                            istr = assetManager.open("avatar/male/" + listTypes.get(i) + "/" + ((HashMap<String, String>) params[0].get(listTypes.get(i).toString())).get("color").toLowerCase() + "/" + ((HashMap<String, String>) params[0].get(listTypes.get(i).toString())).get("item")+".png");
                            listBimaps.add(BitmapFactory.decodeStream(istr));
                        }
                    } catch (IOException e) {
                        // handle exception
                        Log.e("error",e.getMessage());
                    }

            }else
                for(int i = 0; i< listTypesFemale.size(); i++) {
                    try {
                        if(params[0].containsKey(listTypesFemale.get(i))) {
                            istr = assetManager.open("avatar/female/" + listTypesFemale.get(i) + "/" + ((HashMap<String, String>) params[0].get(listTypes.get(i).toString())).get("color").toLowerCase() + "/" + ((HashMap<String, String>) params[0].get(listTypesFemale.get(i).toString())).get("item")+".png");
                            listBimaps.add(BitmapFactory.decodeStream(istr));
                        }
                    } catch (IOException e) {
                        // handle exception
                        Log.e("error",e.getMessage());
                    }

                }

            return combineImages(listBimaps);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            Log.e("bitmap created", "true succesfuly");

            //hide the progress bar and move to the publish activity
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] data = baos.toByteArray();

            Intent resultIntent = new Intent();
            resultIntent.putExtra(Camera.AVATAR_BYTE, data);
            resultIntent.putExtra(Camera.AVATAR_DETAIL, detailEvent(avatarComponents));
            mActivity.setResult(Camera.RESULT_OK, resultIntent);
            mActivity.finish();

        }

        private Bitmap combineImages(List<Bitmap> listBitmaps) {
            int width = 500, height = 500;
            Bitmap cs = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas comboImage = new Canvas(cs);

            for(Bitmap bitmap : listBitmaps) {
                comboImage.drawBitmap(bitmap, new Matrix(), null);
            }

            return cs;
        }

        private HashMap<String,Object> detailEvent(HashMap<String,Object> data){
            data.put("gender", gender);
            return data;
        }

    }
}
