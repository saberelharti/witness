package net.dm73.plainpress.util;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.TimePicker;

import java.util.Calendar;


public class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener{

    private TimeSelectedListner mListener;

    public TimePickerFragment() {
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current time as the default values for the picker
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        // Create a new instance of TimePickerDialog and return it
        return new TimePickerDialog(getActivity(), this, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        // Do something with the time chosen by the user
        mListener.onTimeSelected(String.format("%s%02d:%02d","  ",hourOfDay,minute));
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof TimeSelectedListner) {
            mListener = (TimeSelectedListner) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof TimeSelectedListner) {
            mListener = (TimeSelectedListner) activity;
        } else {
            throw new RuntimeException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    public interface TimeSelectedListner {
        void onTimeSelected(String time);
    }
}