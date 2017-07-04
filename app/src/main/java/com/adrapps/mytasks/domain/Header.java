package com.adrapps.mytasks.domain;

/**
 * Created by adria on 03/07/2017.
 */

public class Header extends ListItem {

   private String text;

   public String getText() {
      return text;
   }
   public void setText(String text) {
      this.text = text;
   }
   @Override
   public int getType() {
      return ListItem.TYPE_HEADER;
   }
}
