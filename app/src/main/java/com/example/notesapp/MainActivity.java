package com.example.notesapp;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.StrictMode;
import android.util.Log;
import android.view.MenuInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;
import com.mongodb.*;
import com.mongodb.client.model.Sorts;


public class MainActivity extends AppCompatActivity {
    RecyclerView dataList;
    TextView noData;

    List<String> titles, description, date, time;
    List<ObjectId> id;
    Adapter adapter;
    MongoCollection<Document> collection;
    Block<Document> getData = new Block<Document>() {
        @Override
        public void apply(final Document document) {
            String title = document.get("title").toString();
            String desc = document.get("desc").toString();
            titles.add(title);
            description.add(desc);
            date.add(document.get("date").toString());
            time.add(document.get("time").toString());
            id.add(new ObjectId(document.get("_id").toString()));
//            Log.d("objectid", (new ObjectId(document.get("_id").toString())).toString());
        }
    };

    void fetchData() {
        MongoClientURI uri = new MongoClientURI("mongodb://naren:qwerty123@test-shard-00-00.1halt.mongodb.net:27017,test-shard-00-01.1halt.mongodb.net:27017,test-shard-00-02.1halt.mongodb.net:27017/test?ssl=true&replicaSet=atlas-jyv6ux-shard-0&authSource=admin&retryWrites=true&w=majority");

        MongoClient mongoClient = new MongoClient(uri);
        MongoDatabase database = mongoClient.getDatabase("test");
        collection = database.getCollection("notes");
        titles = new ArrayList<>();
        description = new ArrayList<>();
        date = new ArrayList<>();
        time = new ArrayList<>();
        id = new ArrayList<>();

        if (collection.count() != 0) {
            noData.setVisibility(View.GONE);
            dataList.setVisibility(View.VISIBLE);
            collection.find().sort(Sorts.descending("date","time")).forEach(getData);

//            collection.find().sort(new Document("date", -1)).sort(new Document("time", -1)).forEach(getData);
        } else {
            noData.setVisibility(View.VISIBLE);
//            dataList.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        dataList = findViewById(R.id.dataList);
        noData = findViewById(R.id.nodata);

        fetchData();

        adapter = new Adapter(this, titles, this.description, this.date, this.time, this.id);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3, GridLayoutManager.VERTICAL, false);
        dataList.setLayoutManager(gridLayoutManager);
        dataList.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, NewNoteActivity.class);
                startActivityForResult(intent, 190);

            }
        });
    }

    private void navigateToAdd() {
        Toast.makeText(this, "Clicked menu add", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(MainActivity.this, NewNoteActivity.class);
        startActivityForResult(intent, 190);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        Toast.makeText(getApplicationContext(),"Requestcode: " + String.valueOf(requestCode),Toast.LENGTH_LONG).show();
        if (requestCode == 190 || requestCode == 210) {
            fetchData();
            adapter = new Adapter(this, titles, this.description, this.date, this.time, this.id);
            dataList.setAdapter(adapter);
        }
    }

    private MenuItem mMenuItem;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.top_app_bar, menu);
        mMenuItem = menu.findItem(R.id.addNewNote);
        Log.d("MENUITEM", mMenuItem.toString());
        return true;
    }

    private void setMenuItemEnabled(boolean enabled) {
        mMenuItem.setEnabled(enabled);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch (item.getItemId()) {
            case R.id.addNewNote:
                navigateToAdd();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }
}
