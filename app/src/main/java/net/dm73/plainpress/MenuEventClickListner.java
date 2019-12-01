package net.dm73.plainpress;


import android.widget.ImageView;

public interface MenuEventClickListner {

    void onMenuEventClick(ImageView menuEvent, String keyEvent);
    void onCardEventClick(String keyEvent);
}
