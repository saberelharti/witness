package net.dm73.plainpress.adapter;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import net.dm73.plainpress.MenuEventClickListner;
import net.dm73.plainpress.R;
import net.dm73.plainpress.model.Event;
import net.dm73.plainpress.util.EventUtil;

import java.util.List;

import static net.dm73.plainpress.util.EventUtil.getDifferenceTime;
import static net.dm73.plainpress.util.EventUtil.getLocationName;

public class UserEventRecyclerAdapter extends RecyclerView.Adapter<UserEventRecyclerAdapter.UserEventHolder>{

    private List<String> eventKeys;
    private Context context;
    private DatabaseReference mRef;
    private boolean hideMenu;

    public static MenuEventClickListner menuEventClickListner;


    public UserEventRecyclerAdapter(List<String> eventKeys, Context context, DatabaseReference mRef, boolean hideMenu) {
        this.eventKeys = eventKeys;
        this.context = context;
        this.mRef = mRef;
        this.hideMenu = hideMenu;
    }

    @Override
    public UserEventHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_event_user, null);
        return new UserEventHolder(view);
    }

    @Override
    public void onBindViewHolder(final UserEventHolder holder, final int position) {

        final UserEventHolder finalHolder = holder;

        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Event event = dataSnapshot.getValue(Event.class);

                if (hideMenu)
                    holder.hideMenu();

                finalHolder.setTitle(event.getTitle());
                finalHolder.setLocation(getLocationName(event.getLocation()));
                finalHolder.setTime((long) event.getPublishedAt() == 0 ? "0" : (getDifferenceTime((long) event.getPublishedAt())));
                Glide.with(context)
                        .load(event.getFirstMediaImage() == null ? R.drawable.image_holder : event.getFirstMediaImage())
                        .centerCrop()
                        .error(R.drawable.image_holder)
                        .into(finalHolder.getEventImage());//todo:add the preview image
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        mRef.child(eventKeys.get(position)).addListenerForSingleValueEvent(eventListener);

    }

    @Override
    public int getItemCount() {
        return (eventKeys!=null)?eventKeys.size():0;
    }

    public void setMenuEventClickListner(MenuEventClickListner clickListener) {
        this.menuEventClickListner = clickListener;
    }


    class UserEventHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private ImageView eventImage;
        private TextView eventTitle;
        private TextView eventTime;
        private TextView eventLocation;
        private ImageView menuEvent;


        public UserEventHolder(View itemView) {
            super(itemView);

            eventImage = (ImageView) itemView.findViewById(R.id.cardEventMedia);
            eventTitle = (TextView) itemView.findViewById(R.id.CardEventTitle);
            eventTime = (TextView) itemView.findViewById(R.id.cardEventTime);
            eventLocation = (TextView) itemView.findViewById(R.id.cardEventLocation);
            menuEvent = (ImageView) itemView.findViewById(R.id.menuEvent);
            eventImage.setOnClickListener(this);
            menuEvent.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

            if(v != null){
                switch(v.getId()){
                    case R.id.cardEventMedia:
                        menuEventClickListner.onCardEventClick(eventKeys.get(getAdapterPosition()));
                        break;
                    case R.id.menuEvent:
                        menuEventClickListner.onMenuEventClick(menuEvent, eventKeys.get(getAdapterPosition()));
                        break;
                }

            }

        }

        public ImageView getEventImage() {
            return eventImage;
        }

        public void setTitle(String title){
            eventTitle.setText(title);
        }

        public void setTime(String time){
            eventTime.setText(time);
        }

        public void setLocation(String location){
            eventLocation.setText(location);
        }

        public void hideMenu(){
            menuEvent.setVisibility(View.GONE);
        }
    }

}
