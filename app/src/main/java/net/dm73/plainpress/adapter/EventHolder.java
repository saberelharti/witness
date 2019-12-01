package net.dm73.plainpress.adapter;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.shimmer.ShimmerFrameLayout;
import net.dm73.plainpress.R;

import de.hdodenhof.circleimageview.CircleImageView;

import static net.dm73.plainpress.adapter.EventRecyclerAdapter.detailClickListner;


public class EventHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private CircleImageView eventUserImage;
    private TextView userNickname;
    private TextView eventTitle;
    private TextView eventViews;
    private TextView eventDatePublishing;
    private TextView eventLocation;
    private ImageView eventTrend;
    private ImageView eventImageMedia;
    private TextView eventViewed;


    public EventHolder(View itemView) {
        super(itemView);
        eventUserImage = (CircleImageView) itemView.findViewById(R.id.profilImage);
        userNickname = (TextView) itemView.findViewById(R.id.userNikname);
        eventTitle = (TextView) itemView.findViewById(R.id.eventTitle);
        eventViews = (TextView) itemView.findViewById(R.id.viewsNumber);
        eventDatePublishing = (TextView) itemView.findViewById(R.id.timer);
        eventLocation = (TextView) itemView.findViewById(R.id.locationEvent);
        eventTrend = (ImageView) itemView.findViewById(R.id.trendImage);
        eventImageMedia = (ImageView) itemView.findViewById(R.id.mediaImageEvent);
        eventViewed = (TextView) itemView.findViewById(R.id.eventViewed);

        eventImageMedia.setOnClickListener(this);
        eventUserImage.setOnClickListener(this);
        itemView.setTag(this);

    }

    @Override
    public void onClick(View v) {

        if (detailClickListner != null) {
            switch (v.getId()) {
                case R.id.profilImage:
                    detailClickListner.onProfilClick(getAdapterPosition(), eventUserImage);
                    break;
                case R.id.mediaImageEvent:
                    detailClickListner.ontDetailClick(getAdapterPosition(), eventImageMedia);
                    break;
            }
        }

    }

    public void setUserNickname(String nickname) {
        userNickname.setText(nickname);
    }

    public void setEventTitle(String mEventTitle) {
        eventTitle.setText(mEventTitle);
    }

    public void setEventViews(String mEventViews) {
        this.eventViews.setText(mEventViews);
    }

    public void setEventViewsColor(int color) {
        this.eventViews.setTextColor(color);
    }

    public void setEventDatePublishing(String mEventDatePublishing) {
        this.eventDatePublishing.setText(mEventDatePublishing);
    }

    public void setEventTrendImage(int resIdImage) {
        this.eventTrend.setImageResource(resIdImage);
    }

    public void setEventLocation(String mEventLocation) {
        this.eventLocation.setText(mEventLocation);
    }

    public void setTextFonts(Typeface typeFace) {
        this.eventTitle.setTypeface(typeFace);
        this.eventViews.setTypeface(typeFace);
        this.eventDatePublishing.setTypeface(typeFace);
        this.eventLocation.setTypeface(typeFace);
        this.eventViewed.setTypeface(typeFace);
    }

    public void setCardEventViewed() {
        final int semiTransparentGrey = Color.argb(100, 0, 0, 0);
        eventImageMedia.setColorFilter(semiTransparentGrey, PorterDuff.Mode.SRC_ATOP);
    }

    public ImageView getEventImageMedia() {
        return eventImageMedia;
    }

    public CircleImageView getEventUserImage() {
        return eventUserImage;
    }

    public void setViewedEventVisible(){
        eventViewed.setVisibility(View.VISIBLE);
    }


}
