package net.dm73.plainpress.model;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;

@IgnoreExtraProperties
public class Comments {

    private Long createdAt;
    private String message;
    private HashMap<String, String> user;


    public Comments() {

    }

    public Comments(Long ceatedAt, String message, HashMap<String, String> user) {
        this.createdAt = ceatedAt;
        this.message = message;
        this.user = user;
    }


    public String getMessage() {
        return message;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public HashMap<String, String> getUser() {
        return user;
    }

    public String getNickName(){
       return (String) user.get("nickName");
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public void setUser(HashMap<String, String> user) {
        this.user = user;
    }

    @Exclude
    public boolean isNicknameUserEmpty(){
        if(user.containsKey("nickName") && !user.get("nickName").isEmpty())
            return false;
        return false;
    }

    @Exclude
    public boolean isPhotoProfileEmpty(){
        if(user.containsKey("photoUrl") && !user.get("photoUrl").isEmpty())
            return false;
        return false;
    }
}
