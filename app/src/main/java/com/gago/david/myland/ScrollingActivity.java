package com.gago.david.myland;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AlphaAnimation;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.gago.david.myland.Adapters.TaskListAdapter;
import com.gago.david.myland.Models.LandContract;
import com.gago.david.myland.Models.LandObject;
import com.gago.david.myland.Models.PlantObject;
import com.gago.david.myland.Models.PlantTypeObject;
import com.gago.david.myland.Models.PriorityObject;
import com.gago.david.myland.Models.TaskObject;
import com.lantouzi.wheelview.WheelView;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class ScrollingActivity extends AppCompatActivity implements AddTaskFragment.OnFragmentInteractionListener,
        NavigationView.OnNavigationItemSelectedListener, TaskEditFragment.OnFragmentInteractionListener {

    private WheelView wheel;
    private LandObject land;
    private String name;
    private boolean first = true;
    private Drawable[] layers;
    private CollapsingToolbarLayout toolbarLayout;
    private ArrayList<PlantTypeObject> plantTypeList;
    private int selected = 0;
    private FloatingActionButton editButton;
    private FloatingActionButton addTaskButton;
    private FloatingActionButton removeButton;
    private FloatingActionButton doneButton;
    private FloatingActionButton deleteButton;
    private TaskListAdapter mAdapter;
    private ArrayList<TaskObject> tasks = new ArrayList<>();
    private TextView description, state;
    private LinearLayout descriptionLayout;
    private Fragment fragment;
    private List<PriorityObject> priorities;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        Bundle b = getIntent().getExtras();
        name = b.getString("name");

        if (name == null) finish();



        land = readLand(name);
        priorities = LandOpenHelper.readPriorities(this);

        setContentView(R.layout.activity_scrolling);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        SharedPreferences prefs = getSharedPreferences(SettingsFragment.MY_PREFS_NAME, MODE_PRIVATE);
        String user = prefs.getString("user", "");

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View headerView = navigationView.getHeaderView(0);
        TextView userView = headerView.findViewById(R.id.user);
        userView.setText(user);
        LinearLayout navLayout = headerView.findViewById(R.id.nav_layout);
        navLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ScrollingActivity.this, Login.class);
                startActivity(intent);
            }
        });

        editButton = findViewById(R.id.fab);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                //        .setAction("Action", null).show();
                Intent intent = new Intent(getApplicationContext(), LandEditActivity.class);
                Bundle b = new Bundle();
                b.putString("name", name); //Your id
                intent.putExtras(b); //Put your id to your next Intent
                startActivity(intent);
            }
        });

        removeButton = findViewById(R.id.remove);
        doneButton = findViewById(R.id.done);
        deleteButton = findViewById(R.id.delete);


        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(view.getRootView().getContext());
                alertDialog.setTitle(R.string.close);
                alertDialog.setMessage(R.string.close_task_ask);

                alertDialog.setPositiveButton(R.string.yes,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                if (fragment instanceof TaskEditFragment) {
                                    TaskObject task = ((TaskEditFragment) fragment).closeTask();
                                    getSupportFragmentManager().popBackStack();
                                    tasks.remove(task);
                                    filter();

                                    doneButton.setVisibility(View.GONE);
                                    deleteButton.setVisibility(View.GONE);
                                }
                            }
                        });

                alertDialog.setNegativeButton(R.string.no,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                alertDialog.show();
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(view.getRootView().getContext());
                alertDialog.setTitle(R.string.delete);
                alertDialog.setMessage(R.string.delete_task);

                alertDialog.setPositiveButton(R.string.yes,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                if(fragment instanceof TaskEditFragment){
                                    TaskObject task = ((TaskEditFragment) fragment).deleteTask();
                                    getSupportFragmentManager().popBackStack();
                                    tasks.remove(task);
                                    filter();

                                    doneButton.setVisibility(View.GONE);
                                    deleteButton.setVisibility(View.GONE);
                                }
                            }
                        });

                alertDialog.setNegativeButton(R.string.no,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                alertDialog.show();
            }
        });

        addTaskButton = findViewById(R.id.add_task_button);
        addTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addTaskPressed();
            }
        });

        toolbarLayout = findViewById(R.id.toolbar_layout);
        layers = new Drawable[2];
        layers[0] = new BitmapDrawable(getResources(), LandOpenHelper.getImage(land.imageUri));
        toolbarLayout.setBackground(layers[0]);
        /*try {
            InputStream inputStream = getContentResolver().openInputStream(Uri.parse(land.imageUri));
            Drawable yourDrawable = Drawable.createFromStream(inputStream, land.imageUri );
            layers[0] = yourDrawable;
            toolbarLayout.setBackground(yourDrawable);
        } catch (FileNotFoundException e) {
            //yourDrawable = getResources().getDrawable(R.drawable.default_image);
        }*/

        toolbarLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (first) {
                    drawTrees();
                    first = false;
                }
            }
        });

        RecyclerView recyclerView = findViewById(R.id.task_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        tasks = LandOpenHelper.readTasks(this, land.name);
        mAdapter = new TaskListAdapter(tasks, this, priorities);
        recyclerView.setAdapter(mAdapter);

        wheel = findViewById(R.id.wheelview);

        wheel.setOnWheelItemSelectedListener(new WheelView.OnWheelItemSelectedListener() {
            @Override
            public void onWheelItemChanged(WheelView wheelView, int position) {
                selected = position;
                drawTrees();
                filter();

            }

            @Override
            public void onWheelItemSelected(WheelView wheelView, int position) {

            }
        });

        plantTypeList = readPlantTypes();
        int unit = prefs.getInt("unit", 0); //0 is the default value.
        String area = (unit==0) ? " ("+Math.round(land.area)+"m\u00B2)" : " ("+Math.round(land.area/10000)+"ha)";
        setTitle(land.name + ((land.area==0) ? "" : area));
        description = findViewById(R.id.scrolling_description);
        description.setText(land.Description);
        descriptionLayout = findViewById(R.id.description_layout);
        descriptionLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(ScrollingActivity.this);

                final EditText input = new EditText(ScrollingActivity.this);
                if (selected < plantTypeList.size()+2) {
                    alertDialog.setTitle(R.string.edit_land);
                    input.setText(land.Description);
                }
                else {
                    alertDialog.setTitle(R.string.edit_item);
                    input.setText(land.plants.get(selected-2-plantTypeList.size()).description);
                }
                alertDialog.setMessage(R.string.state);

                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                input.setLayoutParams(lp);
                alertDialog.setView(input);
                ColorFilter filter = new PorterDuffColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_IN);
                Drawable icon = getResources().getDrawable(R.drawable.ic_action_edit);
                icon.setColorFilter(filter);
                alertDialog.setIcon(icon);

                alertDialog.setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                description.setText(input.getText());
                                if (selected < plantTypeList.size()+2) {
                                    land.Description = input.getText().toString();
                                    updateLand();
                                }
                                else {
                                    land.plants.get(selected-2-plantTypeList.size()).description = input.getText().toString();
                                    updateItem(land.plants.get(selected-2-plantTypeList.size()));
                                }
                            }
                        });

                alertDialog.setNegativeButton(getResources().getString(R.string.cancel),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });

                alertDialog.show();
            }
        });
        state = findViewById(R.id.state);
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);

        //scrollView.fullScroll(NestedScrollView.FOCUS_UP);
    }

    private void filter(){
        if(selected == 0)
            mAdapter.getFilter().filter("all");
        else if(selected == 1)
            mAdapter.getFilter().filter("land");
        else if (selected < plantTypeList.size()+2){
            String filter = "group";
            for (PlantObject p : land.plants)
                if(plantTypeList.get(selected-2).name.equals(p.plantType))
                    filter = filter.concat(" "+p.id);
            mAdapter.getFilter().filter(filter);
        }
        else
            mAdapter.getFilter().filter("item "+land.plants.get(selected-2-plantTypeList.size()).id);

        if (selected < plantTypeList.size()+2)
            removeButton.setVisibility(View.GONE);
        else
            removeButton.setVisibility(View.VISIBLE);

        if (selected < 1)
            addTaskButton.setVisibility(View.GONE);
        else
            addTaskButton.setVisibility(View.VISIBLE);

        if (selected < plantTypeList.size()+2 && !description.getText().toString().equals(land.Description))
            setDescription(land.Description, R.string.land_state);
        else if(selected >= plantTypeList.size()+2 && !description.getText().toString().equals(land.plants.get(selected-2-plantTypeList.size()).description))
            setDescription(land.plants.get(selected-2-plantTypeList.size()).description, R.string.item_state);
    }

    private void addTaskPressed() {
        AddTaskFragment fragment = new AddTaskFragment();
        Bundle args=new Bundle();
        args.putString("land" ,land.name);
        String type = "all";
        ArrayList<Integer> list = new ArrayList<>();
        if(wheel.getSelectedPosition()==0)
            for (PlantObject p : land.plants)
                list.add(p.id);
        else if (wheel.getSelectedPosition()==1)
            type = "land";
        else if (wheel.getSelectedPosition()-2 < plantTypeList.size()){
            for (PlantObject p : land.plants)
                if(plantTypeList.get(wheel.getSelectedPosition()-2).name.equals(p.plantType))
                    list.add(p.id);
            type = "group";
        }
        else {
            list.add(land.plants.get(wheel.getSelectedPosition() - 2 - plantTypeList.size()).id);
            type = "item";
        }

        Log.v("ScrollingActivity", "type:"+type+" list:"+list);

        args.putString("type" ,type);
        args.putIntegerArrayList("plandIndex", list);
        fragment.setArguments(args);
        FragmentManager manager = getSupportFragmentManager();
        manager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.enter_vertical, R.anim.exit_vertical, R.anim.pop_enter_vertical, R.anim.pop_exit_vertical)
                .add(R.id.add_fragment_container, fragment, LandFragment.class.getName())
                .addToBackStack(fragment.getClass().getName())
                .commit();

        editButton.setVisibility(View.GONE);
        addTaskButton.setVisibility(View.GONE);
        removeButton.setVisibility(View.GONE);
    }

    private void drawTrees(){
        int r = 15;

        Paint mPaint = new Paint();
        mPaint.setColor(Color.RED);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(2.5f);
        mPaint.setAntiAlias(true);

        float xRatio = (float)toolbarLayout.getMeasuredWidth()/(float)layers[0].getIntrinsicWidth();
        float yRatio = (float)toolbarLayout.getMeasuredHeight()/(float)layers[0].getIntrinsicHeight();

        int xSize, ySize;

        if(xRatio<yRatio){
            xSize = Math.round(xRatio*layers[0].getIntrinsicWidth());
            ySize = Math.round(xRatio*layers[0].getIntrinsicHeight());
        }
        else{
            xSize = Math.round(yRatio*layers[0].getIntrinsicWidth());
            ySize = Math.round(yRatio*layers[0].getIntrinsicHeight());
        }

        Bitmap bitmap = Bitmap.createBitmap(xSize, ySize, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);

        for ( int i = 0; i < land.plants.size(); i++ ) {
            PlantObject p = land.plants.get(i);
            for (PlantTypeObject type : plantTypeList)
                if (type.name.equals(p.plantType)) {
                    Drawable d = ContextCompat.getDrawable(this, type.icon);
                    d.setBounds(Math.round(p.x * canvas.getWidth()) - d.getIntrinsicWidth() / 8, Math.round(p.y * canvas.getHeight() - d.getIntrinsicHeight() / 8),
                            Math.round(p.x * canvas.getWidth()) + d.getIntrinsicWidth() / 8, Math.round(p.y * canvas.getHeight()) + d.getIntrinsicHeight() / 8);
                    d.setColorFilter(new PorterDuffColorFilter(Color.parseColor(type.color), PorterDuff.Mode.SRC_IN));
                    //And draw it...
                    d.draw(canvas);

                    if ((i + 2 + plantTypeList.size() == selected) || selected == 0 || ((selected-2 < plantTypeList.size()) && selected > 1 && p.plantType.equals(plantTypeList.get(selected-2).name)))
                        canvas.drawCircle(p.x * canvas.getWidth(), p.y * canvas.getHeight(), r, mPaint);
                    break;
                }
        }

        BitmapDrawable drawable = new BitmapDrawable(getResources(), bitmap);
        layers[1] = drawable;

        toolbarLayout.setBackground(new LayerDrawable(layers));
    }

    public void removeTree(View view) {

        PlantObject p = land.plants.get(selected-2-plantTypeList.size());
        PlantTypeObject plantTypeObject = null;
        for (PlantTypeObject pl : plantTypeList)
            if(pl.name.equals(p.plantType)){
                plantTypeObject= pl;
                break;
            }

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(view.getRootView().getContext());
        alertDialog.setTitle(p.plantType);
        alertDialog.setMessage(R.string.remove_tree);

        ColorFilter filter = new PorterDuffColorFilter(Color.parseColor(plantTypeObject.color), PorterDuff.Mode.SRC_IN);
        Drawable icon = view.getRootView().getContext().getResources().getDrawable(plantTypeObject.icon);
        icon.setColorFilter(filter);
        alertDialog.setIcon(icon);

        alertDialog.setPositiveButton(R.string.yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        LandOpenHelper mDbHelper = new LandOpenHelper(ScrollingActivity.this);

                        // Gets the data repository in write mode
                        SQLiteDatabase db = mDbHelper.getWritableDatabase();

                        int id = land.plants.get(selected-2-plantTypeList.size()).id;
                        String whereClause = "Id = ?";
                        String[] whereArgs = new String[]{""+id};

// Create a new map of values, where column names are the keys
                        int i = db.delete("Plants", whereClause, whereArgs);
                        Log.v("Remove Plant", i+" rows removed");
                        db.close();
                        selected--;
                        for (Iterator<TaskObject> iterator = tasks.iterator(); iterator.hasNext();) {
                            TaskObject t = iterator.next();
                            if(t.plantIndex != null && t.plantIndex == id)
                                iterator.remove();
                        }
                        filter();
                        initiateStuff();
                    }
                });

        alertDialog.setNegativeButton(R.string.no,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        alertDialog.show();


    }

    public void deleteLand(View view){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(view.getRootView().getContext());
        alertDialog.setTitle(land.name);
        alertDialog.setMessage(R.string.remove_land);

        alertDialog.setPositiveButton(R.string.yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        LandOpenHelper.deleteLand(land, ScrollingActivity.this);
                        finish();
                    }
                });

        alertDialog.setNegativeButton(R.string.no,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        alertDialog.show();
    }

    private ArrayList<PlantTypeObject> readPlantTypes(){
        LandOpenHelper mDbHelper = new LandOpenHelper(getApplicationContext());

        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                "Name",
                "Icon",
                "Color"
        };

        // How you want the results sorted in the resulting Cursor
        String sortOrder = null;

        Cursor cursor = db.query(
                "PlantTypes",   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                null,              // The columns for the WHERE clause
                null,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                sortOrder               // The sort order
        );

        ArrayList<PlantTypeObject> plants = new ArrayList<>();

        while(cursor.moveToNext()) {
            PlantTypeObject o = new PlantTypeObject(cursor.getString(0), cursor.getInt(1), cursor.getString(2));
            plants.add(o);
        }

        cursor.close();
        db.close();
        return plants;
    }

    private LandObject readLand(String landName){
        LandOpenHelper mDbHelper = new LandOpenHelper(getApplicationContext());

        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                "Name",
                "ImageUri",
                "Description",
                "Area"
        };

        // Filter results WHERE "title" = 'My Title'
        String selection = "Name" + " = ?";
        String[] selectionArgs = { landName };

        // How you want the results sorted in the resulting Cursor
        String sortOrder = null;

        Cursor cursor = db.query(
                "Lands",   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                selection,              // The columns for the WHERE clause
                selectionArgs,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                sortOrder               // The sort order
        );
        cursor.moveToFirst();
        LandObject l =  new LandObject(landName, cursor.getString(1), cursor.getString(2), cursor.getDouble(cursor.getColumnIndex("Area")));
        cursor.close();
        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection2 = {
                "Id",
                "Land",
                "PlantType",
                "Description",
                "x",
                "y"
        };

        // Filter results WHERE "title" = 'My Title'
        String selection2 = "Land" + " = ?";

        // How you want the results sorted in the resulting Cursor
        String sortOrder2 = "Id ASC";

        Cursor cursor2 = db.query(
                "Plants",   // The table to query
                projection2,             // The array of columns to return (pass null to get all)
                selection2,              // The columns for the WHERE clause
                selectionArgs,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                sortOrder2               // The sort order
        );
        while (cursor2.moveToNext())
            l.addPlant(new PlantObject(cursor2.getInt(cursor2.getColumnIndex("Id"))
                    , cursor2.getString(2), cursor2.getString(3), cursor2.getFloat(4), cursor2.getFloat(5)));

        Log.v("ScrollingActivity", ""+cursor2.getColumnIndex("Id"));
        cursor.close();
        cursor2.close();
        db.close();
        return l;
    }

    @Override
    protected void onResume() {
        super.onResume();
        initiateStuff();
    }

    private void initiateStuff(){
        land = readLand(name);
        List<String> strings = new ArrayList<>(land.plants.size());
        strings.add(getResources().getString(R.string.all_tasks));
        strings.add(getResources().getString(R.string.land));
        for (PlantTypeObject p : plantTypeList)
            strings.add(p.name + " (" + getResources().getString(R.string.all) + ")");
        for (PlantObject p : land.plants)
            strings.add(p.plantType);

        wheel.setItems(strings);
        wheel.setMinSelectableIndex(0);
        wheel.setMaxSelectableIndex(strings.size()-1);
        /*if (land.plants.size()==0)
            removeButton.setVisibility(View.GONE);*/
        if (!first)
            drawTrees();
    }

    public void showButtons(){
        editButton.setVisibility(View.VISIBLE);
        addTaskButton.setVisibility(View.VISIBLE);
        if (selected > plantTypeList.size()+1)
            removeButton.setVisibility(View.VISIBLE);
    }

    @Override
    public void onFragmentInteraction(ArrayList<TaskObject> tasks) {
        Fragment fragment = new AddTaskFragment();
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction trans = manager.beginTransaction();
        trans.remove(fragment);
        trans.commit();
        manager.popBackStack();
        Log.v("tasks", tasks.toString());
        addTaskQuery(tasks);
        filter();
    }

    private void addTaskQuery(ArrayList<TaskObject> tasks){
        LandOpenHelper mDbHelper = new LandOpenHelper(this);
        boolean success = true;

        // Gets the data repository in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        SharedPreferences prefs = getSharedPreferences(SettingsFragment.MY_PREFS_NAME, MODE_PRIVATE);
        String user = prefs.getString("user", "");

        for(TaskObject t : tasks) {
            ContentValues values = new ContentValues();
            values.put("Land", t.land);
            values.put(LandContract.TaskEntry.COLUMN_USER, user);
            values.put("PlantIndex", t.plantIndex);
            values.put("TaskType", t.taskType);
            values.put("Priority", t.priority);
            values.put("CreationDate", t.creationDate.getTime());
            if (t.targetDate != null)
                values.put("ExpirationDate", t.targetDate.getTime());
            values.put("Completed", t.completed);
            values.put("Observations", t.observations);

// Insert the new row, returning the primary key value of the new row
            long newRowId = db.insert("Tasks", null, values);
            if (newRowId == -1)
                success = false;
            else {
                Log.v("AddTask", "row inserted: " + newRowId);
                this.tasks.add(t);
                t.rowid = newRowId;
            }

        }
        if (!success)
            Toast.makeText(this,R.string.task_add_error, Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(this,R.string.task_added, Toast.LENGTH_SHORT).show();

        db.close();
    }



    private void updateLand(){

        LandOpenHelper mDbHelper = new LandOpenHelper(this);

        // Gets the data repository in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

// Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put("Name", land.name);
        values.put("ImageUri", land.imageUri);
        values.put("Description", land.Description);

// Insert the new row, returning the primary key value of the new row
        String whereClause = "Name = ?";
        String[] whereArgs = new String[]{land.name};
        long newRowId = db.update("Lands", values, whereClause, whereArgs);
        Log.v("Update Detail", "row updated: "+newRowId);
        if (newRowId == -1)
            Toast.makeText(this,R.string.land_update_error, Toast.LENGTH_SHORT).show();
        else {
            Toast.makeText(this,R.string.land_update_success, Toast.LENGTH_SHORT).show();
            //meter na layer
        }
        db.close();
    }

    private void updateItem(PlantObject p) {
        LandOpenHelper mDbHelper = new LandOpenHelper(this);

        // Gets the data repository in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

// Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put("Id", p.id);
        values.put("Land", land.name);
        values.put("description", p.description);
        values.put("PlantType", p.plantType);
        values.put("x", p.x);
        values.put("y", p.y);

// Insert the new row, returning the primary key value of the new row
        String whereClause = "Id = ?";
        String[] whereArgs = new String[]{""+p.id};
        long newRowId = db.update("Plants", values, whereClause, whereArgs);
        Log.v("Update item", "row updated: "+newRowId);
        if (newRowId == -1)
            Toast.makeText(this,R.string.item_update_error, Toast.LENGTH_SHORT).show();
        else {
            Toast.makeText(this,R.string.item_update_success, Toast.LENGTH_SHORT).show();
            //meter na layer
        }
        db.close();
    }

    private void setDescription(final String s, final int title){
        Log.v("height", s);
        int measuredTextHeight = AddTaskFragment.getHeight(this, s, 14, description.getWidth(), Typeface.DEFAULT);
        ValueAnimator anim = ValueAnimator.ofInt(description.getMeasuredHeight(), measuredTextHeight);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int val = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = description.getLayoutParams();
                layoutParams.height = val;
                description.setLayoutParams(layoutParams);
            }
        });
        anim.addListener(new AnimatorListenerAdapter()
        {
            @Override
            public void onAnimationEnd(Animator animation)
            {
                description.setText(s);
                state.setText(title);
                AlphaAnimation fadeIn = new AlphaAnimation(0.0f , 1.0f ) ;
                descriptionLayout.startAnimation(fadeIn);
                fadeIn.setDuration(300);
                fadeIn.setFillAfter(true);
                fadeIn.setStartOffset(300);
            }

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                AlphaAnimation fadeOut = new AlphaAnimation( 1.0f , 0.0f ) ;
                fadeOut.setDuration(300);
                fadeOut.setFillAfter(true);
                descriptionLayout.startAnimation(fadeOut);
            }
        });
        anim.setDuration(300);
        anim.start();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.lands) {
            finishAfterTransition();
        } else if (id == R.id.settings) {
            Intent data = new Intent();
            data.putExtra("menu", "settings");
            setResult(RESULT_OK, data);
            finish();
        } else if (id == R.id.exit) {
            Intent homeIntent = new Intent(Intent.ACTION_MAIN);
            homeIntent.addCategory( Intent.CATEGORY_HOME );
            homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(homeIntent);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void selectTask(TaskObject task) {
        fragment = TaskEditFragment.newInstance(task);
        FragmentManager manager = getSupportFragmentManager();
        manager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.enter_vertical, R.anim.exit_vertical, R.anim.pop_enter_vertical, R.anim.pop_exit_vertical)
                .add(R.id.add_fragment_container, fragment, LandFragment.class.getName())
                .addToBackStack(fragment.getClass().getName())
                .commit();
        addTaskButton.setVisibility(View.GONE);
        doneButton.setVisibility(View.VISIBLE);
        deleteButton.setVisibility(View.VISIBLE);
        editButton.setVisibility(View.GONE);
        removeButton.setVisibility(View.GONE);
    }

    @Override
    public void updateTask(final TaskObject newTask, final TaskObject oldTask) {
        if(newTask.completed){
            boolean success = LandOpenHelper.updateTask(newTask, ScrollingActivity.this);
            if (success) {
                Toast.makeText(ScrollingActivity.this, R.string.task_close_success, Toast.LENGTH_SHORT).show();
            } else
                Toast.makeText(ScrollingActivity.this, R.string.task_close_error, Toast.LENGTH_SHORT).show();
            filter();
        }
        else {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setTitle(R.string.update);
            alertDialog.setMessage(R.string.save_changes);

            alertDialog.setPositiveButton(R.string.yes,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            boolean success = LandOpenHelper.updateTask(newTask, ScrollingActivity.this);
                            if (success) {
                                Toast.makeText(ScrollingActivity.this, R.string.update_task_success, Toast.LENGTH_SHORT).show();
                                tasks.set(tasks.indexOf(oldTask), newTask);
                            } else
                                Toast.makeText(ScrollingActivity.this, R.string.update_task_error, Toast.LENGTH_SHORT).show();
                            filter();
                        }
                    });

            alertDialog.setNegativeButton(R.string.no,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            filter();
                        }
                    });
            alertDialog.show();
        }
    }

    @Override
    public void notUpdateTask() {
        doneButton.setVisibility(View.GONE);
        deleteButton.setVisibility(View.GONE);
        editButton.setVisibility(View.VISIBLE);
        mAdapter.notifyDataSetChanged();
    }
}
