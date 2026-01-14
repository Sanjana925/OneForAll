package com.sanjana.oneforall;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import com.sanjana.oneforall.ui.backup.BackupFragment;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.sanjana.oneforall.ui.category.CategoryFragment;
import com.sanjana.oneforall.ui.category.AddEditCategoryActivity;
import com.sanjana.oneforall.ui.home.AddEditItemActivity;
import com.sanjana.oneforall.ui.home.HomeFragment;
import com.sanjana.oneforall.ui.list.AddEditListActivity;
import com.sanjana.oneforall.ui.list.ListFragment;
import com.sanjana.oneforall.ui.search.SearchFragment;
import com.sanjana.oneforall.ui.calendar.CalendarFragment;
import com.sanjana.oneforall.ui.calendar.AddCalendarEventActivity;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private BottomNavigationView bottomNavigationView;
    private NavigationView navigationView;
    private MaterialToolbar toolbar;
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawerLayout = findViewById(R.id.drawerLayout);
        bottomNavigationView = findViewById(R.id.bottomNavigation);
        navigationView = findViewById(R.id.navigationView);
        toolbar = findViewById(R.id.topAppBar);
        fab = findViewById(R.id.fabAdd);

        toolbar.setNavigationOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        // Load default fragment
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
            fab.show();
        }

        // Bottom navigation selection
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                fragment = new HomeFragment();
                fab.show();
            } else if (id == R.id.nav_category) {
                fragment = new CategoryFragment();
                fab.show();
            } else if (id == R.id.nav_search) {
                fragment = new SearchFragment();
                fab.hide();
            } else if (id == R.id.nav_calendar) {
                fragment = new CalendarFragment();
                fab.show(); // show FAB for Calendar
            } else if (id == R.id.nav_list) {
                fragment = new ListFragment();
                fab.show();
            }

            if (fragment != null) {
                loadFragment(fragment);
                return true;
            }
            return false;
        });

        // Drawer menu click
        navigationView.setNavigationItemSelectedListener(item -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            int id = item.getItemId();

            if (id == R.id.drawer_profile) {
                // Open Profile
            } else if (id == R.id.drawer_settings) {
                // Open Settings
            } else if (id == R.id.drawer_backup) {
                // Open a new BackupFragment
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainer, new BackupFragment())
                        .addToBackStack(null)
                        .commit();
            }
            return true;
        });

        // FAB click listener
        fab.setOnClickListener(v -> {
            Fragment current = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);

            if (current instanceof HomeFragment) {
                startActivity(new Intent(this, AddEditItemActivity.class));
            } else if (current instanceof CategoryFragment) {
                startActivity(new Intent(this, AddEditCategoryActivity.class));
            } else if (current instanceof ListFragment) {
                startActivity(new Intent(this, AddEditListActivity.class));
            } else if (current instanceof CalendarFragment) {
                startActivity(new Intent(this, AddCalendarEventActivity.class));
            }
        });
    }

    private void loadFragment(@NonNull Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }
}
