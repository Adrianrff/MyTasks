package com.adrapps.mytasks.preferences;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.RadioButton;

import com.adrapps.mytasks.R;


public class NumberPickerPreference extends DialogPreference {

   // allowed range
   private static final int MAX_VALUE = 12;
   private static final int MIN_VALUE = 1;
   // enable or disable the 'circular behavior'
   private static final boolean WRAP_SELECTOR_WHEEL = true;

   private NumberPicker picker;
   private int value, pickerValue;
   private RadioButton amRadioButton, pmRadioButton;

   public NumberPickerPreference(Context context, AttributeSet attrs) {
      super(context, attrs);
   }

   public NumberPickerPreference(Context context, AttributeSet attrs, int defStyleAttr) {
      super(context, attrs, defStyleAttr);
   }

   @Override
   protected View onCreateDialogView() {
      LayoutInflater inflater = LayoutInflater.from(getContext());
      View dialogView = inflater.inflate(R.layout.number_picker_dialog, null);
//      FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
//            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//      layoutParams.gravity = Gravity.CENTER;
//
      picker = (NumberPicker) dialogView.findViewById(R.id.numberPicker);
      amRadioButton = (RadioButton) dialogView.findViewById(R.id.amRB);
      pmRadioButton = (RadioButton) dialogView.findViewById(R.id.pmRB);

//      picker.setLayoutParams(layoutParams);
//
//      FrameLayout dialogView = new FrameLayout(getContext());
//      dialogView.addView(picker);
      return dialogView;
   }

   @Override
   protected void onBindDialogView(View view) {
      super.onBindDialogView(view);
      picker.setMinValue(MIN_VALUE);
      picker.setMaxValue(MAX_VALUE);
      picker.setWrapSelectorWheel(WRAP_SELECTOR_WHEEL);
      if (getValue() >= 12) {
         if (getValue() == 12){
            pickerValue = 12;
         } else {
            pickerValue = getValue() - 12;
         }
         pmRadioButton.setChecked(true);
      } else {
         if (getValue() == 0) {
            picker.setValue(12);
         } else {
            pickerValue = getValue();
         }
         amRadioButton.setChecked(true);
      }
      picker.setValue(pickerValue);
   }

   @Override
   protected void onDialogClosed(boolean positiveResult) {
      if (positiveResult) {
         picker.clearFocus();
         int value;
         if (amRadioButton.isChecked()) {
            if (picker.getValue() == 12){
               value = 0;
            } else {
               value = picker.getValue();
            }
         } else {
            if (picker.getValue() == 12){
               value = 12;
            } else {
               value = picker.getValue() + 12;
            }
         }
         setValue(value);
         callChangeListener(value);
      }
   }

   @Override
   protected Object onGetDefaultValue(TypedArray a, int index) {
      return a.getInt(index, MIN_VALUE);
   }

   @Override
   protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
      setValue(restorePersistedValue ? getPersistedInt(MIN_VALUE) : (Integer) defaultValue);
   }

   private void setValue(int value) {
      this.value = value;
      persistInt(this.value);
   }

   private int getValue() {
      return this.value;
   }
}