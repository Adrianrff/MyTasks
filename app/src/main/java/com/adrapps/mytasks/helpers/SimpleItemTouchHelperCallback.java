package com.adrapps.mytasks.helpers;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.TypedValue;
import android.view.View;

import com.adrapps.mytasks.R;
import com.adrapps.mytasks.interfaces.ItemTouchHelperAdapter;
import com.adrapps.mytasks.interfaces.ItemTouchHelperViewHolder;

public class SimpleItemTouchHelperCallback extends ItemTouchHelper.Callback {

   private final ItemTouchHelperAdapter mAdapter;
   private Context context;
   Paint p;
   TypedValue typedValue;
   private TypedArray typedArray;
   private int color;

   public SimpleItemTouchHelperCallback(ItemTouchHelperAdapter adapter, Context context) {
      mAdapter = adapter;
      this.context = context;
      p = new Paint();
      typedValue = new TypedValue();
      typedArray = context.obtainStyledAttributes(typedValue.data, new int[]{R.attr.colorAccent});
      color = typedArray.getColor(0, 0);
      typedArray.recycle();

   }

   @Override
   public boolean isLongPressDragEnabled() {
      //TODO changed
      return true;
   }

   @Override
   public boolean isItemViewSwipeEnabled() {
      return false;
   }

   @Override
   public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
      int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
      int swipeFlags = ItemTouchHelper.END;
      return makeMovementFlags(dragFlags, swipeFlags);
   }

   @Override
   public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                         RecyclerView.ViewHolder target) {
      mAdapter.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
      return true;
   }

   @Override
   public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
      mAdapter.onItemSwiped(viewHolder.getAdapterPosition(), direction);
   }

   @Override
   public void onSelectedChanged(RecyclerView.ViewHolder viewHolder,
                                 int actionState) {

      // We only want the active item
      if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
         if (viewHolder instanceof ItemTouchHelperViewHolder) {
            ItemTouchHelperViewHolder itemViewHolder =
                  (ItemTouchHelperViewHolder) viewHolder;
            itemViewHolder.onItemSelected();
         }
      }

      super.onSelectedChanged(viewHolder, actionState);
   }

   @Override
   public void clearView(RecyclerView recyclerView,
                         RecyclerView.ViewHolder viewHolder) {
      super.clearView(recyclerView, viewHolder);

      if (viewHolder instanceof ItemTouchHelperViewHolder) {
         ItemTouchHelperViewHolder itemViewHolder =
               (ItemTouchHelperViewHolder) viewHolder;
         itemViewHolder.onItemClear();
      }
   }

   @Override
   public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder
         viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
      Bitmap icon;
      if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {

         View itemView = viewHolder.itemView;
         float height = (float) itemView.getBottom() - (float) itemView.getTop();
         float width = height / 3;

         if (dX > 0) {
            p.setColor(color);
            RectF background = new RectF((float) itemView.getLeft(),
                  (float) itemView.getTop(), dX, (float) itemView.getBottom());
            c.drawRect(background, p);
            icon = BitmapFactory.decodeResource(context.getResources(), R.mipmap.indent_white);
            RectF icon_dest = new RectF((float) itemView.getLeft() + width,
                  (float) itemView.getTop() + width,
                  (float) itemView.getLeft() + 2 * width, (float) itemView.getBottom() - width);
            c.drawBitmap(icon, null, icon_dest, p);
         } else {
            p.setColor(Color.parseColor("#D32F2F"));
            RectF background = new RectF((float) itemView.getRight() + dX,
                  (float) itemView.getTop(), (float) itemView.getRight(),
                  (float) itemView.getBottom());
            c.drawRect(background, p);
            icon = BitmapFactory.decodeResource(context.getResources(), R.mipmap.delete_white);
            RectF icon_dest = new RectF((float) itemView.getRight() - 2 * width,
                  (float) itemView.getTop() + width, (float) itemView.getRight() -
                  width, (float) itemView.getBottom() - width);
            c.drawBitmap(icon, null, icon_dest, p);
         }
      }
      super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

   }


}
