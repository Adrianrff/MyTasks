package com.adrapps.mytasks.Interfaces;

import android.view.View;

/**
 * Created by Adrian Flores on 5/4/2017.
 */

public interface RecyclerItemClickListener extends View.OnClickListener {

    @Override
    void onClick(View v);

    void onItemCilck();
}
