package net.dm73.plainpress;


import android.view.View;
import android.widget.ImageView;


public interface DetailClickListner {

    void ontDetailClick(int position, ImageView v);
    void onProfilClick(int position, View view);
}
