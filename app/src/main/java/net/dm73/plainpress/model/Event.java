package net.dm73.plainpress.model;


import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.ServerValue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@IgnoreExtraProperties
public class Event {

    @Exclude
    private String eventId;
    @Exclude
    private boolean viewed = false;
    private String title;
    private String description;
    private long views;
    private Object publishedAt;
    private Object updatedAt;
    private long eventAt;
    private Object createdAt;
    private boolean isAnonymous;
    private Location location;
    private Category category;
    private HashMap<String, Object> user;
    private Media media;
    private Detail detail;
    private long signaled;
    private long status;
    private boolean enabled;
    private long comments;
    private List<String> hashtags;


    public Event() {
    }

    public Event(String title, String description, Location location) {
        this.title = title;
        this.location = location;
        this.description = description;
    }

    //provisoire
    public Event(String title, String desc) {
        this.title = title;
        this.description = desc;
    }

    public Event(String title, Category category, String description, Long publishedAt) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.publishedAt = publishedAt;
    }


    public Event(String title, String description, Long views, Long publishedAt, Location location, Media eventMedia, List<String> hashtags) {
        this.title = title;
        this.description = description;
        this.views = views;
        this.publishedAt = publishedAt;
        this.location = location;
        this.media = eventMedia;
        this.hashtags = hashtags;
    }

    public Event(String title, String description, long eventAt, boolean isAnonymous, Location location, Category category, HashMap<String, Object> user, List<String> hashtags, Detail detail) {
        this.title = title;
        this.description = description;
        this.views = 0;
        this.createdAt = ServerValue.TIMESTAMP;
        this.publishedAt = 0;
        this.updatedAt = 0;
        this.eventAt = eventAt;
        this.isAnonymous = isAnonymous;
        this.location = location;
        this.category = category;
        this.user = user;
        this.signaled = 0;
        this.status = 1;
        this.enabled = true;
        this.comments = 0;
        this.hashtags = hashtags;
        this.detail = detail;
    }


    public Event(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Long getViews() {
        return views;
    }

    public Object getPublishedAt() {
        return publishedAt;
    }

    public boolean isAnonymous() {
        return isAnonymous;
    }

    public Location getLocation() {
        return location;
    }

    public HashMap<String, Object> getUser() {
        return user;
    }

    @Exclude
    public User getUserInstance() {
        return new User((String) user.get("id"), (String) user.get("nickName"), (String) user.get("photoUrl"));
    }

    @Exclude
    public String getEventID() {
        return eventId;
    }

    @Exclude
    public boolean isViewed() {
        return viewed;
    }

    public Category getCategory() {
        return category;
    }

    public Object getUpdatedAt() {
        return updatedAt;
    }

    public long getEventAt() {
        return eventAt;
    }

    public Object getCreatedAt() {
        return createdAt;
    }

    public long getSignaled() {
        return signaled;
    }

    public long getStatus() {
        return status;
    }

    public Detail getDetail() {
        return detail;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public long getComments() {
        return comments;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public void setUser(HashMap<String, Object> user) {
        this.user = user;
    }

    public void setViews(long views) {
        this.views = views;
    }

    public void setPublishedAt(long publishedAt) {
        this.publishedAt = publishedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setEventAt(long eventAt) {
        this.eventAt = eventAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public void setMedia(Media media) {
        this.media = media;
    }

    public void setSignaled(long signaled) {
        this.signaled = signaled;
    }

    public void setStatus(long status) {
        this.status = status;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setComments(long comments) {
        this.comments = comments;
    }

    public Media getMedia() {
        return this.media;
    }

    @Exclude
    public String getFirstMediaImage() {
        if (media != null && media.getImages() != null) {
            return media.getFirstImage();
        } else {
            return null;
        }
    }

    @Exclude
    public boolean isMediaEmpty() {
        if (media == null)
            return true;
        else if(media.getImages() == null || media.getImages().size() == 0)
            return true;
        else
            return false;
    }

    @Exclude
    public boolean isPhotoProfilEmpty() {
        if (user.containsKey("photoUrl") && user.get("photoUrl") != null && !user.get("photoUrl").toString().isEmpty()) {
            return false;
        }
        return true;
    }

    @Exclude
    public boolean isNickNameEmpty() {
        if (user.containsKey("nickName") && user.get("nickName") != null && !user.get("nickName").toString().isEmpty()) {
            return false;
        }
        return true;
    }


    public void setHashtags(List<String> hashtags) {
        this.hashtags = hashtags;
    }

    @Exclude
    public boolean isHashTagsEmpty() {
        if (hashtags == null)
            return true;
        else if (hashtags.removeAll(Arrays.asList("", null)) && hashtags.size() == 0) {
            return true;
        } else {
            return false;
        }
    }

    public void setAnonymous(boolean anonymous) {
        this.isAnonymous = anonymous;
    }

    public void setCategory(Category category) {
        this.category = category;

    }

    public void setDetail(Detail detail) {
        this.detail = detail;
    }

    @Exclude
    public boolean isDetailEmpty() {
        if (detail == null)
            return true;
        else if (detail.getPicture() == null || detail.getPicture().isEmpty()) {
            return true;
        } else {
            return false;
        }
    }

    @Exclude
    public boolean isDetailUrlEmpty() {

        if (detail != null && !detail.getPicture().isEmpty())
            return false;

        return true;
    }

    @Exclude
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    @Exclude
    public void setViewed(boolean viewed) {
        this.viewed = viewed;
    }

    public List<String> getHashtags() {
        return this.hashtags;
    }
}
