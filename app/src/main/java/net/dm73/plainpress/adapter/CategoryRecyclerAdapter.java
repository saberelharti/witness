package net.dm73.plainpress.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.dm73.plainpress.CategoryChooser;
import net.dm73.plainpress.R;
import net.dm73.plainpress.model.Category;

import java.util.ArrayList;
import java.util.List;


public class CategoryRecyclerAdapter extends RecyclerView.Adapter<CategoryRecyclerAdapter.CategoryHolder> {

    private List<Category> listCategories;
    private Context context;

    private CategoryChooser categoryChooserListener;
    private Typeface typeface;


    public CategoryRecyclerAdapter(List<Category> listCategories, Context context, Typeface typeface) {
        this.listCategories = listCategories;
        this.context = context;
        this.typeface = typeface;
    }

    @Override
    public CategoryHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View categoryView = LayoutInflater.from(context).inflate(R.layout.item_category, null);
        return new CategoryHolder(categoryView);
    }

    @Override
    public void onBindViewHolder(CategoryHolder holder, int position) {

        holder.setText(listCategories.get(position).getName());
        holder.setTypeFace();
        holder.updtaeUI(listCategories.get(position).isChecked());
    }

    @Override
    public int getItemCount() {
        return (listCategories!=null)? listCategories.size() : 0;
    }

    public void setCategoryChooserListener(CategoryChooser categoryChooserListener){
        this.categoryChooserListener = categoryChooserListener;
    }

    public void notifyCategoryClicked(int position){

        Category category = listCategories.get(position);

        if(category.isChecked()){
            category.setChecked(false);
        }else{
            category.setChecked(true);
        }

        listCategories.set(position, category);
        notifyItemChanged(position);

    }

    public List<Long> getListCategoiesChoosed(){

        List<Long> listChoosedCategories = new ArrayList<>();
        for(Category category : listCategories){
            if(category.isChecked())
                listChoosedCategories.add(category.getId());
        }
        return listChoosedCategories;
    }


    class CategoryHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private TextView category;

        public CategoryHolder(View itemView) {
            super(itemView);
            category = (TextView) itemView.findViewById(R.id.categoryName);
        }

        public void setText(String categoryName){
            category.setText(categoryName);
            category.setOnClickListener(this);
        }

        public void setTypeFace(){
            category.setTypeface(typeface);
        }

        public void updtaeUI(boolean isChecked){
            if(isChecked){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    category.setCompoundDrawablesWithIntrinsicBounds(null, null, context.getDrawable(R.drawable.ic_category_checked), null);
                }else{
                    category.setCompoundDrawablesWithIntrinsicBounds(null, null, context.getResources().getDrawable(R.drawable.ic_category_checked), null);
                }
                category.setBackgroundResource(R.drawable.background_button_category_ac);
                category.setCompoundDrawablePadding(10);
            }else{
                category.setCompoundDrawables(null, null, null, null);
                category.setBackgroundResource(R.drawable.background_button_category_desac);
                category.setCompoundDrawablePadding(0);
            }
        }

        @Override
        public void onClick(View v) {
            categoryChooserListener.onCategoryChoosed(getAdapterPosition());
        }
    }
}
