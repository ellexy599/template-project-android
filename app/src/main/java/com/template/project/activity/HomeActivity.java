package com.template.project.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;

import com.template.project.R;
import com.template.project.base.BaseActivity;
import com.template.project.fragment.HomeFragment;

public class
        HomeActivity extends BaseActivity {

    private static final String TAG = HomeActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        HomeFragment homeFragment = new HomeFragment();
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .detach(homeFragment)
                .setTransition(FragmentTransaction.TRANSIT_ENTER_MASK)
                .replace(R.id.activity_home_container, homeFragment)
                .attach(homeFragment)
                .add(homeFragment, HomeFragment.class.getSimpleName())
                .commit();

        /*getSupportFragmentManager().beginTransaction()
                .replace(R.id.activity_home_container, homeFragment, HomeFragment.class.getSimpleName())
                .commit();*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

}
