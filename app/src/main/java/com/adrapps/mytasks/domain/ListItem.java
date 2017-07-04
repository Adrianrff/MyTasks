package com.adrapps.mytasks.domain;

/**
 * Created by adria on 03/07/2017.
 */

public abstract class ListItem {

   public static final int TYPE_TASK = 1;
   public static final int TYPE_HEADER = 0;

   abstract public int getType();
}
