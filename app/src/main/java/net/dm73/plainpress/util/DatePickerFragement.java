package net.dm73.plainpress.util;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.icu.text.DecimalFormat;
import android.os.Bundle;
import android.util.Log;
import android.widget.DatePicker;

import java.util.Calendar;


public class DatePickerFragement extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    private DateSelectedListner mListner;

    public DatePickerFragement() {

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current time as the default values for the picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(getActivity(), this, year, month, day);
        dialog.getDatePicker().setMaxDate(c.getTimeInMillis());
        dialog.setTitle("");

        // Create a new instance of TimePickerDialog and return it
        return dialog;
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Log.e("date piker", year+" y,"+month+" m,"+dayOfMonth+" ,");
        mListner.onDateSelected(String.format("%02d/%02d/%02d", dayOfMonth, ++month, year));
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof DateSelectedListner) {
            mListner = (DateSelectedListner) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (activity instanceof DateSelectedListner) {
            mListner = (DateSelectedListner) activity;
        } else {
            throw new RuntimeException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    public interface DateSelectedListner {
        void onDateSelected(String date);
    }
}