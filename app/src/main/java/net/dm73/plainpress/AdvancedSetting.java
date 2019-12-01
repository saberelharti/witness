package net.dm73.plainpress;


import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import net.dm73.plainpress.fragment.UpdateEmail;
import net.dm73.plainpress.fragment.UpdatePassword;


public class AdvancedSetting extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advanced_setting);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

        Bundle extras = getIntent().getExtras();
        if(extras.getInt("fragmentId")==0){
            fragmentTransaction.replace(R.id.fragment_container, new UpdatePassword());
            fragmentTransaction.commit();
        }else{
            fragmentTransaction.replace(R.id.fragment_container, new UpdateEmail());
            fragmentTransaction.commit();
        }

    }
}
