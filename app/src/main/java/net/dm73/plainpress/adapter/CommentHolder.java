package net.dm73.plainpress.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import net.dm73.plainpress.R;

import de.hdodenhof.circleimageview.CircleImageView;


public class CommentHolder extends RecyclerView.ViewHolder {

    private TextView nickName;
    private TextView message;
    private TextView creatAt;
    private CircleImageView imageProfile;

    public CommentHolder(View itemView) {
        super(itemView);
        nickName = (TextView) itemView.findViewById(R.id.userNickName);
        message = (TextView) itemView.findViewById(R.id.userMessage);
        creatAt = (TextView) itemView.findViewById(R.id.timerComment);
        imageProfile = (CircleImageView) itemView.findViewById(R.id.userImage);
    }

    public void setNickName(String nickName) {
        this.nickName.setText(nickName);
    }

    public void setMessage(String message) {
        this.message.setText(message);
    }

    public void setCreatAt(String creatAt) {
        this.creatAt.setText(creatAt);
    }

    public CircleImageView getImageProfile(){
        return  imageProfile;
    }
}
