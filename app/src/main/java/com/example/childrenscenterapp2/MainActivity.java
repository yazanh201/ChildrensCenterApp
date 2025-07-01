package com.example.childrenscenterapp2;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.childrenscenterapp2.ui.home.HomeFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // טען את HomeFragment ברגע פתיחת האפליקציה
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }
    }

    /**
     * טוען Fragment חדש לתוך container.
     * @param fragment הפרגמנט שברצונך להציג
     */
    public void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment); // ID של FrameLayout מ־activity_main.xml
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
