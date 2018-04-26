package com.gago.david.myland;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class ScrollingActivity extends AppCompatActivity {

    String imageUri;
    String description;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle b = getIntent().getExtras();
        final String name = b.getString("name");

        if (name == null) finish();

        Cursor cursor = readLand(name);

        cursor.moveToFirst();
        imageUri = cursor.getString(1);
        description = cursor.getString(2);
        cursor.close();

        setContentView(R.layout.activity_scrolling);
        Toolbar toolbar = findViewById(R.id.toolbar);
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

        CollapsingToolbarLayout toolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        try {
            InputStream inputStream = getContentResolver().openInputStream(Uri.parse(imageUri));
            Drawable yourDrawable = Drawable.createFromStream(inputStream, imageUri );
            toolbarLayout.setBackground(yourDrawable);
        } catch (FileNotFoundException e) {
            //yourDrawable = getResources().getDrawable(R.drawable.default_image);
        }


    }

    private Cursor readLand(String landName){
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
        return cursor;
    }

}
