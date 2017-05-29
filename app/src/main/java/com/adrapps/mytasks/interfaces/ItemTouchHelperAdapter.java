package com.adrapps.mytasks.interfaces;


public interface ItemTouchHelperAdapter {

   boolean onItemMove(int fromPosition, int toPosition);

   void onItemSwiped(int position, int direction);

}
