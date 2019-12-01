package net.dm73.plainpress.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.dm73.plainpress.R;

import static net.dm73.plainpress.adapter.SearchRecyclerAdapter.clickListenerResult;


public class SearchEventHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

    private ImageView eventImage;
    private TextView eventTitle;
    private TextView eventDescp;
    private TextView eventTimer;
    private TextView eventLocation;
    private RelativeLayout container;
    private TextView eventViews;
    private TextView eventCategory;

    public SearchEventHolder(View itemView) {
        super(itemView);

        eventImage = (ImageView) itemView.findViewById(R.id.eventSearchImage);
        eventTitle = (TextView) itemView.findViewById(R.id.eventSearchTitle);
        eventDescp = (TextView) itemView.findViewById(R.id.eventSearchDesc);
        eventTimer = (TextView) itemView.findViewById(R.id.cardEventTime);
        eventLocation = (TextView) itemView.findViewById(R.id.cardEventLocation);
        eventViews = (TextView) itemView.findViewById(R.id.eventViews);
        eventCategory = (TextView) itemView.findViewById(R.id.gridCategoryTitle);
        container = (RelativeLayout) itemView.findViewById(R.id.searchEventContainer);

        container.setOnClickListener(this);
        itemView.setTag(this);

    }

    public void setEventTitle(String title){
        eventTitle.setText(title);
    }

    public void setEventDescription(String dsec){
        eventDescp.setText(dsec);
    }

    public void setEventTimer(String title){
        eventTimer.setText(title);
    }

    public void setEventLocation(String title){
        eventLocation.setText(title);
    }

    public void setEventViews(String title){
        eventViews.setText(title);
    }

    public void setEventCategory(String title){
        eventCategory.setText(title);
    }

    public ImageView getEventImageView(){
        return eventImage;
    }

    public RelativeLayout getContainer(){
        return container;
    }

    public void hotEventUpdate(){
        eventViews.setBackgroundResource(R.drawable.background_round_view_red);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.searchEventContainer:
                clickListenerResult.onClickResult(getAdapterPosition());
                break;
        }
    }
}
