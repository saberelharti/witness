
package net.dm73.plainpress.adapter;

import android.app.Activity;
import android.graphics.Typeface;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseReference;
import net.dm73.plainpress.DetailClickListner;
import net.dm73.plainpress.OnLoadMoreListener;
import net.dm73.plainpress.R;
import net.dm73.plainpress.model.Event;
import net.dm73.plainpress.model.Location;
import net.dm73.plainpress.util.EventUtil;

import java.text.DecimalFormat;
import java.util.List;

import static net.dm73.plainpress.util.EventUtil.customViewNumber;
import static net.dm73.plainpress.util.EventUtil.getDifferenceTime;
import static net.dm73.plainpress.util.EventUtil.getLocationName;


public class EventRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;
    private OnLoadMoreListener onLoadMoreListener;
    private boolean isLoading;
    private Activity activity;
    private List<Event> listEvent;
    private int visibleThreshold = 2;
    private int lastVisibleItem, totalItemCount;
    private DatabaseReference mRef;
    private EventHolder eventHolder;

    public static DetailClickListner detailClickListner;

    public EventRecyclerAdapter(RecyclerView recyclerView, List<Event> listEvent, Activity activity, DatabaseReference mRef) {
        this.listEvent = listEvent;
        this.activity = activity;
        this.mRef = mRef;

        final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                totalItemCount = linearLayoutManager.getItemCount();
                lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
                if (!isLoading && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                    if (onLoadMoreListener != null) {
                        onLoadMoreListener.onLoadMore();
                    }
                    isLoading = true;
                }
            }
        });
    }

    public void setOnLoadMoreListener(OnLoadMoreListener mOnLoadMoreListener) {
        this.onLoadMoreListener = mOnLoadMoreListener;
    }

    @Override
    public int getItemViewType(int position) {
        return listEvent.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
            View view = LayoutInflater.from(activity).inflate(R.layout.item_event, parent, false);
            return new EventHolder(view);
        } else if (viewType == VIEW_TYPE_LOADING) {
            View view = LayoutInflater.from(activity).inflate(R.layout.item_progrees_view, parent, false);
            return new LoadingHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof EventHolder) {
            eventHolder = (EventHolder) holder;

            Event model = listEvent.get(position);

            if(model.isViewed()){
                eventHolder.setCardEventViewed();
                eventHolder.setViewedEventVisible();
            }

            ViewCompat.setTransitionName(eventHolder.getEventImageMedia(), "proifle");

            eventHolder.setEventViews(customViewNumber(model.getViews()));
            eventHolder.setEventDatePublishing(getDifferenceTime((long) model.getPublishedAt()));
            eventHolder.setEventLocation(getLocationName(model.getLocation()));
            eventHolder.setEventTitle(model.getTitle());
            eventHolder.setTextFonts(Typeface.createFromAsset(activity.getAssets(), "fonts/titilliumweb_regular.ttf"));

            if (model.getViews() > 1000) {
                eventHolder.setEventViewsColor(0xffe05751);
                eventHolder.setEventTrendImage(R.drawable.ic_trend_red);
            }

            // Load the image using Glide
            Glide.with(activity)
                    .load(model.getFirstMediaImage())
                    .centerCrop()
                    .error(R.drawable.image_holder)
                    .into(eventHolder.getEventImageMedia());

            if(model.isAnonymous()) {
                eventHolder.setUserNickname("Anonymous");
                Glide.with(activity)
                        .load(R.drawable.ic_ghost_profile)
                        .centerCrop()
                        .into(eventHolder.getEventUserImage());
            }else{
                eventHolder.setUserNickname((model.isNickNameEmpty()) ? "User" : model.getUserInstance().getNickName());
                Glide.with(activity)
                        .load((model.isPhotoProfilEmpty())? R.drawable.image_holder : model.getUserInstance().getPhotoUrl())
                        .centerCrop()
                        .placeholder(R.drawable.ic_avatar_m)
                        .error(R.drawable.ic_avatar_m)
                        .into(eventHolder.getEventUserImage());
            }

        } else if (holder instanceof LoadingHolder) {
            LoadingHolder loadingViewHolder = (LoadingHolder) holder;
            loadingViewHolder.progressBar.setIndeterminate(true);
        }
    }

    @Override
    public int getItemCount() {
        return listEvent == null ? 0 : listEvent.size();
    }

    public void setDetailClickListner(DetailClickListner clickListener) {
        this.detailClickListner = clickListener;
    }

    public void setLoaded() {
        isLoading = false;
    }

}
