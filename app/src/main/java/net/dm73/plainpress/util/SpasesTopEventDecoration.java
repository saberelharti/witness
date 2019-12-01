package net.dm73.plainpress.util;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class SpasesTopEventDecoration extends RecyclerView.ItemDecoration {

        private final int mSpace;

        public SpasesTopEventDecoration(int space) {
            this.mSpace = space;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            outRect.top = mSpace;
            outRect.left = mSpace/2;
            outRect.right = mSpace/2;
            outRect.bottom = mSpace;
        }

}
