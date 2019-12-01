package net.dm73.plainpress.adapter;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import net.dm73.plainpress.CharaterClickListener;
import net.dm73.plainpress.FaceDetail;
import net.dm73.plainpress.R;
import net.dm73.plainpress.model.FaceCharacter;

import java.util.List;


public class CharacterRecyclerAdapter extends RecyclerView.Adapter<CharacterRecyclerAdapter.CharacterHolder> {


    private Context context;
    private int typeDetail;
    private boolean gender;
    private List<FaceCharacter> charatcerRessources;
    private String color;

    private CharaterClickListener mCharacterClickListener;

    public CharacterRecyclerAdapter(Context context, List<FaceCharacter> charatcerRessources, int typeDetail, boolean gender, String color) {
        this.context = context;
        this.charatcerRessources = charatcerRessources;
        this.typeDetail = typeDetail;
        this.gender = gender;
        this.color = color;
    }

    @Override
    public CharacterHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_character, parent, false);
        return new CharacterHolder(view);
    }

    @Override
    public void onBindViewHolder(CharacterHolder holder, int position) {

        Uri uri = getUri(typeDetail, charatcerRessources.get(position).getRessourceName(), gender);
        Glide.with(context)
                .loadFromMediaStore(uri)
                .fitCenter()
                .into(holder.characterImageView);

        if(charatcerRessources.get(position).isSelected()){
            holder.setSelected(View.VISIBLE);
        }else{
            holder.setSelected(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return (charatcerRessources!=null)? charatcerRessources.size() : 0;
    }

    private Uri getUri(int type, String resName, boolean gender){
        String root = "file:///android_asset/preview_avatar/";
        if(gender){
            root+="male/";
            return Uri.parse(root+ FaceDetail.listTypes.get(type)+"/"+color+"/"+resName);
        }else{
            root+="female/";
            return Uri.parse(root+ FaceDetail.listTypes.get(type)+"/"+color+"/"+resName);
        }
    }

    public void setCharacterClickListener(CharaterClickListener charatcerClickListner){
        this.mCharacterClickListener = charatcerClickListner;
    }

    public void itemSlected(int position){

        for(int i =  0 ; i < charatcerRessources.size() ; i++){
            boolean selected = false;
            if(i == position)
                selected = true;
            charatcerRessources.set(i, new FaceCharacter(charatcerRessources.get(i).getRessourceName(), selected));

        }
        notifyDataSetChanged();
    }


    public class CharacterHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView characterImageView;
        private ImageView checkedMarck;

        public CharacterHolder(View itemView) {
            super(itemView);
            characterImageView = (ImageView) itemView.findViewById(R.id.characterImageView);
            checkedMarck = (ImageView) itemView.findViewById(R.id.checkedMarck);
            characterImageView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if(mCharacterClickListener != null){
                mCharacterClickListener.onCharacterClickListner(typeDetail, getAdapterPosition());
            }
        }

        public void setSelected(int visibility){
            checkedMarck.setVisibility(visibility);
        }

    }

}
