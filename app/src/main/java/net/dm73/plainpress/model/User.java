package net.dm73.plainpress.model;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@IgnoreExtraProperties
public class User {

    private String id;
    private String accessToken;
    private String firstName;
    private String LastName;
    private String photoUrl;
    private String email;
    private String nickname;
    private String provider;
    private boolean gender;
    private String description;
    private String adress;
    private String age;
    private String phone;
    private HashMap<String,Object> events;
    private HashMap<String, Object> location;
    private Object createdAt;
    private Object updatedAt;
    private boolean isAdmin;
    private boolean isDeleted;
    private boolean enabled;
    private Object lastLogin;
    private HashMap<String, Object> settings;
    private List<String> viewedEvents;

    public User(){

    }

    public User(String id, String nickName, String photoUrl){
        this.id= id;
        this.photoUrl = photoUrl;
        this.nickname = nickName;
    }

    public User(String nickName, String email, String provider, HashMap<String, Object> settings, String accessToken) {
        this.nickname = nickName;
        this.email = email;
        this.provider = provider;
        this.createdAt = ServerValue.TIMESTAMP;
        this.settings = settings;
        this.isAdmin = false;
        this.isDeleted = false;
        this.enabled = true;
        this.accessToken = accessToken;
    }

    public User(String firstName, String lastName, String nickName, String description, boolean gender, String adress, String age, String phone, HashMap<String, Object> location) {
        this.firstName = firstName;
        LastName = lastName;
        this.nickname = nickName;
        this.description = description;
        this.gender = gender;
        this.adress = adress;
        this.age = age;
        this.phone = phone;
        this.isAdmin = false;
        this.updatedAt = ServerValue.TIMESTAMP;
        this.location = location;
    }

    public String getId() {
        return id;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return LastName;
    }

    public String getEmail() {
        return email;
    }

    public String getNickName() {
        return nickname;
    }

    public String getProvider() {
        return provider;
    }

    public boolean getGender() {
        return gender;
    }

    public String getDescription() {
        return description;
    }

    public String getAdress() {
        return adress;
    }

    public String getAge() {
        return age;
    }

    public String getPhone() {
        return phone;
    }

    public Object getCreatedAt() {
        return createdAt;
    }

    public Object getUpdatedAt() {
        return updatedAt;
    }

    public HashMap<String, Object> getEvents() {
        return events;
    }

    public boolean isIsAdmin() {
        return isAdmin;
    }

    public boolean isIsDeleted() {
        return isDeleted;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public HashMap<String, Object> getLocation() {
        return location;
    }

    public Object getLastLogin() {
        return lastLogin;
    }

    public HashMap<String, Object> getSettings() {
        return settings;
    }

    public List<String> getViewedEvents() {
        return viewedEvents;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        LastName = lastName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setNickName(String nickName) {
        this.nickname = nickName;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public void setGender(boolean gender) {
        this.gender = gender;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setAdress(String adress) {
        this.adress = adress;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setCreatedAt(Object createdAt) {
        this.createdAt = createdAt;
    }

    public void setIsAdmin(boolean isAdmin) {
        isAdmin = isAdmin;
    }

    public void setIsDeleted(boolean isDeleted) {
        isDeleted = isDeleted;
    }

    public void setDisabled(boolean isDisabled) {
        isDisabled = isDisabled;
    }

    public void setLastLogin(Object lastLogin) {
        this.lastLogin = lastLogin;
    }

    public void setSettings(HashMap<String, Object> settings) {
        this.settings = settings;
    }

    public void setUpdatedAt(Object updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setLocation(HashMap<String, Object> location) {
        this.location = location;
    }

    public void setViewedEvents(List<String> viewedEvents) {
        this.viewedEvents = viewedEvents;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public void setEvents(HashMap<String, Object> events) {
        this.events = events;
    }

    @Exclude
    public Map<String, Object> toMap(){

        HashMap<String, Object> user = new HashMap<>();

        if(!getFirstName().isEmpty())
            user.put("firstName", getFirstName());
        if(!getLastName().isEmpty())
            user.put("lastName", getLastName());
        if(!getNickName().isEmpty())
            user.put("nickName", getNickName());
        if(!getDescription().isEmpty())
            user.put("description", getDescription());
        if(!getAge().isEmpty())
            user.put("age", getAge());
        if(!getAdress().isEmpty())
            user.put("adress", getAdress());
        if(!getPhone().isEmpty())
            user.put("phone", getPhone());

        return user;
    }

    @Exclude
    public boolean isUserNickNameEmpty(){
        if(nickname != null && !nickname.isEmpty())
            return false;
        return true;
    }

    @Exclude
    public boolean isUserPhotoProfilEmpty(){
        if(photoUrl != null && !photoUrl.isEmpty())
            return false;
        return true;

    }
}
