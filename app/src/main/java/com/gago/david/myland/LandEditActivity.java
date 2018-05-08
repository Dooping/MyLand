package com.gago.david.myland;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.github.chrisbanes.photoview.PhotoView;

import java.util.ArrayList;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class LandEditActivity extends AppCompatActivity implements PopupMenuAdapter.OnMenuItemInteractionListener {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = false;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();

    private float lastX = 0.0f;
    private float lastY = 0.0f;
    private float tempX;
    private float tempY;
    PopupWindow popupWindow;
    LandObject land;
    Drawable[] layers;
    PhotoView photo;
    private boolean first = true;
    private ArrayList<PlantTypeObject> plantTypeList;

    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_land_edit);

        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.photo_view);
        View layout = findViewById(R.id.frame_layout);
        layout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getActionMasked() == MotionEvent.ACTION_DOWN) {
                    lastX = motionEvent.getX();
                    lastY = motionEvent.getY();
                }
                return false;
            }
        });
        photo = (PhotoView) mContentView;
        Bundle b = getIntent().getExtras();
        String name = b.getString("name");
        land = readLand(name);

        plantTypeList = readPlantTypes();

        //photo.setImageURI(Uri.parse(land.imageUri));
        layers = new Drawable[2];
        layers[0] = Drawable.createFromPath(Uri.parse(land.imageUri).getPath());
        layers[1] = new ColorDrawable(Color.TRANSPARENT);
        photo.setImageDrawable(new LayerDrawable(layers));
        photo.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (first) {
                    drawTrees();
                    first = false;
                }
            }
        });


        // Set up the user interaction to manually show or hide the system UI.
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //toggle();
            }
        });

        mContentView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {


                popupWindowsort().showAtLocation(view, Gravity.NO_GRAVITY, Math.round(lastX), Math.round(lastY));
                Log.v("EDIT", "should be showing at x:"+Math.round(lastX)+" y:"+Math.round(lastY));
                float viewX = lastX - photo.getLeft();
                float viewY = lastY - photo.getTop();
                Log.v("photo", viewX+":"+viewY);
                RectF r = photo.getDisplayRect();
                tempX = (viewX-r.left)/r.width();
                tempY = (viewY-r.top)/r.height();

                Log.v("rect", r.toString());
                Log.v("coords", tempX+":"+tempY);

                //Toast.makeText(getApplicationContext(), lastX+":"+lastY, Toast.LENGTH_SHORT).show();
                return false;
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);
    }

    private void drawTrees(){
        int r = 5;

        Paint mPaint = new Paint();
        mPaint.setColor(Color.RED);



        float xRatio = photo.getMeasuredWidth()/layers[0].getIntrinsicWidth();
        float yRatio = photo.getMeasuredHeight()/layers[0].getIntrinsicHeight();

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

        for ( PlantObject p : land.plants )
            for(PlantTypeObject type : plantTypeList)
                if(type.name.equals(p.plantType)){
                    Drawable d = ContextCompat.getDrawable(this, type.icon);
                    d.setBounds(Math.round(p.x*canvas.getWidth())-d.getIntrinsicWidth()/4,Math.round(p.y*canvas.getHeight()-d.getIntrinsicHeight()/4),
                            Math.round(p.x*canvas.getWidth())+d.getIntrinsicWidth()/4, Math.round(p.y*canvas.getHeight())+d.getIntrinsicHeight()/4);
                    d.setColorFilter(new PorterDuffColorFilter(Color.parseColor(type.color), PorterDuff.Mode.SRC_IN));
                    //And draw it...
                    d.draw(canvas);
                    break;
                }

        BitmapDrawable drawable = new BitmapDrawable(getResources(), bitmap);
        layers[1] = drawable;
        photo.setImageDrawable(new LayerDrawable(layers));
    }

    private LandObject readLand(String landName){
        LandOpenHelper mDbHelper = new LandOpenHelper(getApplicationContext());

        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                "Name",
                "ImageUri",
                "Description"
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
        LandObject l =  new LandObject(landName, cursor.getString(1), cursor.getString(2));
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
            l.addPlant(new PlantObject(cursor2.getInt(1), cursor2.getString(2), cursor2.getString(3), cursor2.getFloat(4), cursor2.getFloat(5)));
        return l;
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

        return plants;
    }

    private PopupWindow popupWindowsort() {

        // initialize a pop up window type
        popupWindow = new PopupWindow(getApplicationContext());
        ArrayList<PlantTypeObject> sortList = readPlantTypes();

        ArrayAdapter<PlantTypeObject> adapter = new PopupMenuAdapter(this, sortList);
        // the drop down list is a list view
        ListView listViewSort = new ListView(this);

        // set our adapter and pass our pop up window contents
        listViewSort.setAdapter(adapter);

        // set on item selected
        //listViewSort.setOnItemClickListener(onItemClickListener());



        // set the listview as popup content
        popupWindow.setContentView(listViewSort);

        // some other visual settings for popup window
        popupWindow.setFocusable(true);
        listViewSort.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        popupWindow.setWidth(listViewSort.getMeasuredWidth()+50);
       // popupWindow.setBackgroundDrawable(getResources().getDrawable(R.drawable.background));
        popupWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);

        return popupWindow;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in delay milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    public void onMenuItemInteraction(final PlantTypeObject item){
        //Toast.makeText(getApplicationContext(), "item: "+item.name, Toast.LENGTH_SHORT).show();
        popupWindow.dismiss();
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle(item.name);
        alertDialog.setMessage("Description");

        final EditText input = new EditText(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        alertDialog.setView(input);
        ColorFilter filter = new PorterDuffColorFilter(Color.parseColor(item.color), PorterDuff.Mode.SRC_IN);
        Drawable icon = getResources().getDrawable(item.icon);
        icon.setColorFilter(filter);
        alertDialog.setIcon(icon);

        alertDialog.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        PlantObject p = new PlantObject(item.name, input.getText().toString(), tempX, tempY);
                        land.addPlant(p);
                        addPlantQuery(p);
                        drawTrees();
                        Log.v("plant", land.toString());
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

    private void addPlantQuery(PlantObject p){
        LandOpenHelper mDbHelper = new LandOpenHelper(this);

        // Gets the data repository in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

// Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put("Land", land.name);
        values.put("PlantType", p.plantType);
        values.put("Description", p.description);
        values.put("x", p.x);
        values.put("y", p.y);

// Insert the new row, returning the primary key value of the new row
        long newRowId = db.insert("Plants", null, values);
        p.id = (int)newRowId;
        Log.v("ADDDETAIL", "row inserted: "+newRowId);
        if (newRowId == -1)
            Toast.makeText(this,"Some error happened while adding the item", Toast.LENGTH_SHORT).show();
        else {
            Toast.makeText(this,"Item Added", Toast.LENGTH_SHORT).show();
            //meter na layer
        }
    }

}
