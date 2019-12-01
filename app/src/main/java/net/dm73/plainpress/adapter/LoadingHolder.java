package net.dm73.plainpress.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;

import net.dm73.plainpress.R;


public class LoadingHolder extends RecyclerView.ViewHolder {

    public ProgressBar progressBar;

    public LoadingHolder(View itemView) {
        super(itemView);
        progressBar = (ProgressBar) itemView.findViewById(R.id.progressBar);

    }

}
