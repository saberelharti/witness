package net.dm73.plainpress.fragment;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;

import java.util.Calendar;


public class DatePickerFragement extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    private TextView dateView;

    public DatePickerFragement(View dateView) {
        this.dateView = (TextView) dateView;
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

        dateView.setText(String.format("%02d", dayOfMonth) + "/" + String.format("%02d",++month) + "/" + year);
        SearchDisplay.dateFilterActive = true;

    }
}