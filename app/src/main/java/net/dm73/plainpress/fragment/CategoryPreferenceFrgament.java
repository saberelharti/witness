package net.dm73.plainpress.fragment;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import net.dm73.plainpress.CategoryChooser;
import net.dm73.plainpress.R;
import net.dm73.plainpress.adapter.CategoryRecyclerAdapter;
import net.dm73.plainpress.model.Category;
import net.dm73.plainpress.util.SpasesTopEventDecoration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class CategoryPreferenceFrgament extends Fragment implements CategoryChooser {

    private RecyclerView categoryRecyclerView;
    private CategoryRecyclerAdapter mAdapter;
    private OnCategoriesChoosedListner mListener;
    private Typeface typeface;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        typeface = Typeface.createFromAsset(getContext().getAssets(), "fonts/titilliumweb_regular.ttf");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_category_preference, container, false);

        categoryRecyclerView = (RecyclerView) rootView.findViewById(R.id.categoryRecyclerView);
        ((TextView) rootView.findViewById(R.id.catgoryFragmentTitle)).setTypeface(typeface);

        Glide.with(getActivity())
                .load(R.drawable.global_view)
                .centerCrop()
                .into((ImageView) rootView.findViewById(R.id.catgoryFragmentHeader));

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        ValueEventListener categoryListner = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                List<HashMap<String, Object>> categories = (ArrayList<HashMap<String, Object>>)dataSnapshot.getValue();

                List<Category> listCategories = new ArrayList<>();
                for(int i=0; i<categories.size(); i++) {
                    if((boolean)categories.get(i).get("enabled"))
                        listCategories.add(new Category((long)categories.get(i).get("id"), (String)categories.get(i).get("name")));
                }

                categoryRecyclerView.setHasFixedSize(true);
                mAdapter = new CategoryRecyclerAdapter(listCategories, getActivity(), typeface);
                categoryRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
                SpasesTopEventDecoration decoration = new SpasesTopEventDecoration(16);
                categoryRecyclerView.addItemDecoration(decoration);
                categoryRecyclerView.setAdapter(mAdapter);
                mAdapter.setCategoryChooserListener(CategoryPreferenceFrgament.this);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        FirebaseDatabase.getInstance().getReference().child("categories").addListenerForSingleValueEvent(categoryListner);

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof CategoryPreferenceFrgament.OnCategoriesChoosedListner) {
            mListener = (CategoryPreferenceFrgament.OnCategoriesChoosedListner) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onCategoryChoosed(int position) {
        mAdapter.notifyCategoryClicked(position);
        mListener.categoriesChoosed(mAdapter.getListCategoiesChoosed());
    }

    public interface OnCategoriesChoosedListner{
        void categoriesChoosed(List<Long> preferedcategories);
    }

}
