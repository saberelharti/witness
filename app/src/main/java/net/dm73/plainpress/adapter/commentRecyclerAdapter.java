package net.dm73.plainpress.adapter;

import android.content.Context;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.Query;
import net.dm73.plainpress.R;
import net.dm73.plainpress.model.Comments;
import net.dm73.plainpress.util.EventUtil;


public class commentRecyclerAdapter extends FirebaseRecyclerAdapter<Comments, CommentHolder> {

    private Context context;

    public commentRecyclerAdapter(Class<Comments> modelClass, int modelLayout, Class<CommentHolder> viewHolderClass, Query ref, Context context) {
        super(modelClass, modelLayout, viewHolderClass, ref);
        this.context = context;
    }


    @Override
    protected void populateViewHolder(CommentHolder viewHolder, Comments model, int position) {
        viewHolder.setNickName(model.isNicknameUserEmpty() ? "User" : model.getNickName());
        viewHolder.setMessage(model.getMessage());
        viewHolder.setCreatAt(EventUtil.getDifferenceTime(model.getCreatedAt()));
        Glide.with(context)
                .load(model.isPhotoProfileEmpty() ? R.drawable.image_holder : model.getUser().get("photoUrl"))
                .centerCrop()
                .placeholder(R.drawable.ic_avatar_m)
                .error(R.drawable.ic_avatar_m)
                .into(viewHolder.getImageProfile());
    }


    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public Comments getItem(int position) {
        return super.getItem(getItemCount()- position - 1);
    }

}
