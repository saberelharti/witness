package net.dm73.plainpress.model;

import java.io.Serializable;
import java.util.HashMap;

public class Detail implements Serializable {

    private HashMap<String, String> face;
    private HashMap<String, String> eyes;
    private HashMap<String, String> nose;
    private HashMap<String, String> hair;
    private HashMap<String, String> mouth;
    private HashMap<String, String> eyebrows;
    private HashMap<String, String> beard;
    private HashMap<String, String> moustache;
    private HashMap<String, String> ear;
    private boolean gender;
    private String picture;

    public Detail() {

    }

    public Detail(HashMap<String, String> face, HashMap<String, String> eyes, HashMap<String, String> nose, HashMap<String, String> hair, HashMap<String, String> mouth, HashMap<String, String> eyebrows, HashMap<String, String> beard, HashMap<String, String> moustache, boolean gender, String picture) {
        this.face = face;
        this.eyes = eyes;
        this.nose = nose;
        this.hair = hair;
        this.mouth = mouth;
        this.eyebrows = eyebrows;
        this.beard = beard;
        this.moustache = moustache;
        this.gender = gender;
        this.picture = picture;
    }

    public HashMap<String, String> getBeard() {
        return beard;
    }

    public void setBeard(HashMap<String, String> beard) {
        this.beard = beard;
    }

    public HashMap<String, String> getMoustache() {
        return moustache;
    }

    public void setMoustache(HashMap<String, String> moustache) {
        this.moustache = moustache;
    }

    public HashMap<String, String> getFace() {
        return face;
    }

    public void setFace(HashMap<String, String> face) {
        this.face = face;
    }

    public HashMap<String, String> getEyes() {
        return eyes;
    }

    public void setEyes(HashMap<String, String> eyes) {
        this.eyes = eyes;
    }

    public HashMap<String, String> getNose() {
        return nose;
    }

    public void setNose(HashMap<String, String> nose) {
        this.nose = nose;
    }

    public HashMap<String, String> getHair() {
        return hair;
    }

    public void setHair(HashMap<String, String> hair) {
        this.hair = hair;
    }

    public HashMap<String, String> getMouth() {
        return mouth;
    }

    public void setMouth(HashMap<String, String> mouth) {
        this.mouth = mouth;
    }

    public HashMap<String, String> getEyebrows() {
        return eyebrows;
    }

    public void setEyebrows(HashMap<String, String> eyebrows) {
        this.eyebrows = eyebrows;
    }

    public boolean isGender() {
        return gender;
    }

    public void setGender(boolean gender) {
        this.gender = gender;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public void setEar(HashMap<String, String> ear) {
        this.ear = ear;
    }

    public HashMap<String, String> getEar() {
        return ear;
    }
}
