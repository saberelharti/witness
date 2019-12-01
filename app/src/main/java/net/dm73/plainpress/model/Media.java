package net.dm73.plainpress.model;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.List;

@IgnoreExtraProperties
public class Media {

    private List<String> images;
    private List<String> video;
    private List<Object> sizes;

    public Media() {
    }

    public Media(List<String> images) {
        this.images = images;
    }

    public Media(List<String> images, List<String> video, List<Object> sizes) {
        this.images = images;
        this.video = video;
        this.sizes = sizes;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public List<String> getVideo() {
        return video;
    }

    public void setVideo(List<String> video) {
        this.video = video;
    }

    public List<Object> getSizes() {
        return sizes;
    }

    public void setSizes(List<Object> sizes) {
        this.sizes = sizes;
    }

    @Exclude
    public String getFirstImage(){
        if(images !=null){
            return images.get(0);
        }
        return null;
    }
}
