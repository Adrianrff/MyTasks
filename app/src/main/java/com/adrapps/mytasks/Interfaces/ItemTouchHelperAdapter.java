package com.adrapps.mytasks.Interfaces;

/**
 * Created by Adrian Flores on 9/4/2017.
 */

public interface ItemTouchHelperAdapter {

    boolean onItemMove(int fromPosition, int toPosition);

    void onItemSwiped(int position, int direction);


}
