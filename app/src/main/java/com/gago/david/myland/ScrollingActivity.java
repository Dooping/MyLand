package com.gago.david.myland;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.TextView;


import com.lantouzi.wheelview.WheelView;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ScrollingActivity extends AppCompatActivity {

    private List<String> list;
    private WheelView wheel;
    private LandObject land;
    private String name;
    private boolean first = true;
    private Drawable[] layers;
    private CollapsingToolbarLayout toolbarLayout;
    private ArrayList<PlantTypeObject> plantTypeList;
    private int selected = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle b = getIntent().getExtras();
        name = b.getString("name");

        if (name == null) finish();



        land = readLand(name);

        setContentView(R.layout.activity_scrolling);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
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

        toolbarLayout = findViewById(R.id.toolbar_layout);
        layers = new Drawable[2];
        try {
            InputStream inputStream = getContentResolver().openInputStream(Uri.parse(land.imageUri));
            Drawable yourDrawable = Drawable.createFromStream(inputStream, land.imageUri );
            layers[0] = yourDrawable;
            toolbarLayout.setBackground(yourDrawable);
        } catch (FileNotFoundException e) {
            //yourDrawable = getResources().getDrawable(R.drawable.default_image);
        }

        toolbarLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (first) {
                    drawTrees();
                    first = false;
                }
            }
        });

        wheel = findViewById(R.id.wheelview);

        wheel.setOnWheelItemSelectedListener(new WheelView.OnWheelItemSelectedListener() {
            @Override
            public void onWheelItemChanged(WheelView wheelView, int position) {
                selected = position;
                drawTrees();
            }

            @Override
            public void onWheelItemSelected(WheelView wheelView, int position) {

            }
        });

        plantTypeList = readPlantTypes();
        setTitle(land.name);
        TextView description = findViewById(R.id.scrolling_description);
        description.setText(land.Description);
    }

    private void drawTrees(){
        int r = 15;

        Paint mPaint = new Paint();
        mPaint.setColor(Color.RED);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(2.5f);
        mPaint.setAntiAlias(true);



        float xRatio = toolbarLayout.getMeasuredWidth()/layers[0].getIntrinsicWidth();
        float yRatio = toolbarLayout.getMeasuredHeight()/layers[0].getIntrinsicHeight();

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
                    if (i == selected)
                        canvas.drawCircle(p.x * canvas.getWidth(), p.y * canvas.getHeight(), r, mPaint);
                    break;
                }
        }

        BitmapDrawable drawable = new BitmapDrawable(getResources(), bitmap);
        layers[1] = drawable;

        toolbarLayout.setBackground(new LayerDrawable(layers));
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
                "Land",
                "PlantType",
                "Description",
                "x",
                "y"
        };

        // Filter results WHERE "title" = 'My Title'
        String selection2 = "Land" + " = ?";

        // How you want the results sorted in the resulting Cursor
        String sortOrder2 = "rowid ASC";

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
            l.addPlant(new PlantObject(cursor2.getString(1), cursor2.getString(2), cursor2.getFloat(3), cursor2.getFloat(4)));
        return l;
    }

    @Override
    protected void onResume() {
        super.onResume();
        land = readLand(name);
        List<String> strings = new ArrayList<>(land.plants.size());
        for (PlantObject p : land.plants)
            strings.add(p.plantType);

        wheel.setItems(strings);
        wheel.setMinSelectableIndex(0);
        wheel.setMaxSelectableIndex(land.plants.size()-1);
        if (!first)
            drawTrees();
    }
}
