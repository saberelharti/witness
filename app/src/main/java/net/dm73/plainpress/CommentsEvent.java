package net.dm73.plainpress;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import net.dm73.plainpress.adapter.CommentHolder;
import net.dm73.plainpress.adapter.commentRecyclerAdapter;
import net.dm73.plainpress.model.Comments;
import net.dm73.plainpress.util.ActivityConfig;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CommentsEvent extends AppCompatActivity {


    @BindView(R.id.commentList)
    RecyclerView commentRecyclerView;
    @BindView(R.id.back_comment)
    ImageView backButton;

    private Query mQuery;
    private commentRecyclerAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments_event);
        ButterKnife.bind(this);
        ActivityConfig.setStatusBarTranslucent(getWindow());
        Bundle extras = getIntent().getExtras();
        mQuery = FirebaseDatabase.getInstance().getReference("comments").child("items").child(extras.getString("EVENT_KEY")).orderByChild("createdAt");

    }

    @Override
    protected void onStart() {
        super.onStart();

        LinearLayoutManager mManager = new LinearLayoutManager(getApplicationContext());
        commentRecyclerView.setHasFixedSize(false);
        commentRecyclerView.setLayoutManager(mManager);
        mAdapter = new commentRecyclerAdapter(Comments.class, R.layout.comment, CommentHolder.class, mQuery, getApplicationContext());
        commentRecyclerView.setAdapter(mAdapter);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }


    @Override
    public void onStop() {
        super.onStop();
        if (mAdapter != null) {
            mAdapter.cleanup();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
