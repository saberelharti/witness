package net.dm73.plainpress.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;

import net.dm73.plainpress.ClickListenerResult;
import net.dm73.plainpress.R;
import net.dm73.plainpress.model.Event;

import java.util.List;

import static net.dm73.plainpress.util.EventUtil.customViewNumber;
import static net.dm73.plainpress.util.EventUtil.getDifferenceTime;
import static net.dm73.plainpress.util.EventUtil.getLocationName;


public class SearchRecyclerAdapter extends RecyclerView.Adapter<SearchEventHolder> {

    private List<Event> listEvent;
    private Context context;

    public static ClickListenerResult clickListenerResult;


    public SearchRecyclerAdapter(List<Event> listEvent, Context context) {
        this.listEvent = listEvent;
        this.context = context;
    }

    @Override
    public SearchEventHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View rootView = LayoutInflater.from(context).inflate(R.layout.item_event_search, null);
        return new SearchEventHolder(rootView);
    }

    @Override
    public void onBindViewHolder(SearchEventHolder holder, int position) {

//        if(position == getItemCount()-1){
//            holder.getContainer().setPadding(0, 0, 0, 50);
//        }

        Event event = listEvent.get(position);
        holder.setEventTitle(event.getTitle());
        holder.setEventDescription(event.getDescription());
        holder.setEventLocation(getLocationName(event.getLocation()));
        holder.setEventTimer(getDifferenceTime((long) event.getPublishedAt()));
        holder.setEventCategory(event.getCategory().getName());
        if (event.getViews() > 1000) {
            holder.hotEventUpdate();
        }
        holder.setEventViews(customViewNumber(event.getViews()));
        Glide.with(context)
                .load(event.getFirstMediaImage() == null ? R.drawable.image_holder_1 : event.getFirstMediaImage())
                .centerCrop()
                .error(R.drawable.image_holder_1)
                .into(holder.getEventImageView());

    }

    @Override
    public int getItemCount() {
        return (listEvent != null) ? listEvent.size() : 0;
    }

    public void setDataAdapter(List<Event> newList) {
        listEvent = newList;
        notifyDataSetChanged();
    }

    public void setCilckResultListener(ClickListenerResult clickListener) {
        this.clickListenerResult = clickListener;
    }

}
