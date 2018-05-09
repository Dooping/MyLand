package com.gago.david.myland;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.gago.david.myland.dummy.DummyContent;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, LandFragment.OnListFragmentInteractionListener,
            AddLandDetailsFragment.OnFragmentInteractionListener, SettingsFragment.OnListFragmentInteractionListener,
            SettingsFragment.OnTaskListFragmentInteractionListener{

    private boolean logout = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //deleteDatabase("myland.db");
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        LandFragment fragment = new LandFragment();
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit)
                .add(R.id.fragment_container, fragment, LandFragment.class.getName())
                .commit();
    }

    @Override
    public void onBackPressed(){
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if(getSupportFragmentManager().getBackStackEntryCount() == 0 && !logout){
                String reservation = "Press again to Leave";
                Toast.makeText(this, reservation, Toast.LENGTH_LONG).show();
                logout = true;
            }
            else if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                finish();
            }
            else{
                super.onBackPressed();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.lands) {
            LandFragment fragment = new LandFragment();
            replaceFragmentFromMenu(fragment);
        } else if (id == R.id.settings) {
            SettingsFragment fragment = new SettingsFragment();
            replaceFragmentFromMenu(fragment);
        } else if (id == R.id.exit) {
            finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void replaceFragmentFromMenu (Fragment fragment){
        String backStateName = fragment.getClass().getName();

        FragmentManager manager = getSupportFragmentManager();
        if(backStateName.equals(LandFragment.class.getName()))
            manager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        else {
            manager.popBackStack(backStateName, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            FragmentTransaction ft = manager.beginTransaction();
            ft.setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit);
            ft.replace(R.id.fragment_container, fragment);
            ft.addToBackStack(backStateName);
            ft.commit();
        }
        logout=false;
    }

    public void setActionBarTitle(String title){
        setTitle(title);
    }

    public void addLandDetails(String filename) {
        setActionBarTitle("Land Details");
        Fragment fragment = new AddLandDetailsFragment();
        Bundle args=new Bundle();
        args.putString("filename",filename);
        fragment.setArguments(args);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment).addToBackStack("main").commit();

    }

    public void removeLandDetails() {
        Fragment fragment = new AddLandDetailsFragment();
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction trans = manager.beginTransaction();
        trans.remove(fragment);
        trans.commit();
        manager.popBackStack();

    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void onListFragmentInteraction(LandObject item) {
        Intent intent = new Intent(this, ScrollingActivity.class);
        Bundle b = new Bundle();
        b.putString("name", item.name); //Your id
        intent.putExtras(b); //Put your id to your next Intent
        startActivity(intent);
    }

    @Override
    public void selectItem(PlantTypeObject item) {
        //TODO open Plant type fragment
    }

    @Override
    public boolean removeItem(PlantTypeObject item) {
        LandOpenHelper mDbHelper = new LandOpenHelper(this);

        // Gets the data repository in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        String whereClause = "Name = ?";
        String[] whereArgs = new String[]{item.name};

        int i = db.delete("PlantTypes", whereClause, whereArgs);
        Log.v("Remove item", i+" rows removed");
        db.close();
        return i > 0;
    }

    @Override
    public long addItem(PlantTypeObject item) {
        LandOpenHelper mDbHelper = new LandOpenHelper(this);

        // Gets the data repository in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("Name", item.name);
        values.put("Icon", item.icon);
        values.put("Color", item.color);

// Insert the new row, returning the primary key value of the new row
        long newRowId = db.insert("PlantTypes", null, values);
        if (newRowId == -1) {
            Toast.makeText(this,"Item Added", Toast.LENGTH_SHORT).show();
            Log.v("Add Item", "Failed to insert item: " + item.toString());
        }
        else {
            Toast.makeText(this,"Some error occurred while adding the Item", Toast.LENGTH_SHORT).show();
            Log.v("Add Item", "row inserted: " + newRowId);
        }

        db.close();
        return newRowId;
    }

    @Override
    public void selectItem(TaskTypeObject item) {
        //TODO open Plant type fragment
    }

    @Override
    public boolean removeItem(TaskTypeObject item) {
        LandOpenHelper mDbHelper = new LandOpenHelper(this);

        // Gets the data repository in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        String whereClause = "Name = ?";
        String[] whereArgs = new String[]{item.name};

        int i = db.delete("TaskTypes", whereClause, whereArgs);
        Log.v("Remove TaskType", i+" rows removed");
        db.close();
        return i > 0;
    }

    @Override
    public long addItem(TaskTypeObject item) {
        LandOpenHelper mDbHelper = new LandOpenHelper(this);

        // Gets the data repository in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("Name", item.name);
        values.put("Description", item.description);

// Insert the new row, returning the primary key value of the new row
        long newRowId = db.insert("TaskTypes", null, values);
        if (newRowId == -1) {
            Toast.makeText(this,"Task Type Added", Toast.LENGTH_SHORT).show();
            Log.v("Add TaskType", "Failed to insert task type: " + item.toString());
        }
        else {
            Toast.makeText(this,"Some error occurred while adding the Item", Toast.LENGTH_SHORT).show();
            Log.v("Add TaskType", "row inserted: " + newRowId);
        }

        db.close();
        return newRowId;
    }
}
