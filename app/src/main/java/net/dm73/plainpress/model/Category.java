package net.dm73.plainpress.model;


import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Category {

    private long id;
    private String name;
    private String type;
    private String description;
    private boolean enabled;
    @Exclude
    private boolean checked;
    private String picture;


    public Category() {
    }

    public Category(String name, String type, String description, boolean enabled, String picture) {
        this.name = name;
        this.type = type;
        this.description = description;
        this.enabled = enabled;
        this.picture = picture;
    }

    public Category(long id, String name){
        this.id = id;
        this.name = name;
    }

    public Category(long id, String name, boolean checked){
        this.id = id;
        this.name = name;
        this.checked = checked;
    }
    public Category(long id, String name, String picture){
        this.id = id;
        this.name = name;
        this.picture = picture;
    }

    public Category(String name, String type, String description, boolean enabled, String picture, long id, boolean checked) {
        this.name = name;
        this.type = type;
        this.description = description;
        this.enabled = enabled;
        this.picture = picture;
        this.id = id;
        this.checked = checked;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public boolean isEnabled() {
        return enabled;
    }

    @Exclude
    public boolean isChecked() {
        return checked;
    }

    public String getPicture() {
        return picture;
    }

    public long getId(){
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setId(long id){
        this.id =id;
    }

    @Exclude
    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }
}
