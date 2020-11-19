package com.example.notesapp;

import android.content.Intent;
import android.os.Bundle;

import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import org.bson.Document;
import org.bson.types.ObjectId;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.in;
import static com.mongodb.client.model.Updates.combine;
import static com.mongodb.client.model.Updates.currentDate;
import static com.mongodb.client.model.Updates.set;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class NewNoteActivity extends AppCompatActivity {


    EditText noteTitle, noteDesc;
    TextView status,timeView;
    MongoCollection<Document> collection;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_add);
        MongoClientURI uri = new MongoClientURI("mongodb://naren:qwerty123@test-shard-00-00.1halt.mongodb.net:27017,test-shard-00-01.1halt.mongodb.net:27017,test-shard-00-02.1halt.mongodb.net:27017/test?ssl=true&replicaSet=atlas-jyv6ux-shard-0&authSource=admin&retryWrites=true&w=majority");

        MongoClient mongoClient = new MongoClient(uri);
        MongoDatabase database = mongoClient.getDatabase("test");
        collection = database.getCollection("notes");

        noteTitle = findViewById(R.id.note_title);
        noteDesc = findViewById(R.id.note_desc);
        status = findViewById(R.id.status);
        timeView = findViewById(R.id.time);


        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);
        setTitle("Add Note");


        Intent intent = getIntent();
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd");
        String currentDate = simpleDateFormat.format(calendar.getTime());
        String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
        if (intent.hasExtra("title")) {
            setTitle("Edit Note");
            noteTitle.setText(intent.getStringExtra("title"));
            noteDesc.setText(intent.getStringExtra("desc"));

            LocalDate dateBefore = LocalDate.parse(intent.getStringExtra("date").replaceAll("/","-"));
            LocalDate dateAfter = LocalDate.parse(currentDate.replaceAll("/","-"));

            long days = ChronoUnit.DAYS.between(dateBefore, dateAfter);

            if (days==1)
                status.setText(String.valueOf(days) + " day ago");
            else if (days>1)
                status.setText(String.valueOf(days) + " days ago");
            else
                status.setText("Edited today");

            timeView.setText(intent.getStringExtra("date").replaceAll("/","-") + "  " + getIntent().getStringExtra("time").split(":")[0] + ":" + getIntent().getStringExtra("time").split(":")[1]);

        } else {
            setTitle("Add Note");
            status.setText("Editing");
            timeView.setText(currentDate.replaceAll("/","-") + "  " + currentTime.split(":")[0] + ":" + currentTime.split(":")[1]);
        }
    }


    private void saveNote() {
        String title = noteTitle.getText().toString();
        String description = noteDesc.getText().toString();

        if (title.trim().isEmpty() || description.trim().isEmpty()) {
            Toast.makeText(this, "Please provide both title and description", Toast.LENGTH_SHORT).show();
            return;
        }
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd");
        String currentDate = simpleDateFormat.format(calendar.getTime());
        String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
        Document document = new Document("title", title)
                .append("desc", description)
                .append("date", currentDate)
                .append("time", currentTime);

        if(getIntent().hasExtra("id")) {
            collection.updateOne(
                    eq("_id", new ObjectId(getIntent().getStringExtra("id"))),
                    combine(set("title", title), set("desc", description), set("time", currentTime), set("date", currentDate)));
            Toast.makeText(getApplicationContext(), "Note updated successfully",Toast.LENGTH_LONG).show();

        }
        else {
            collection.insertOne(document);
            Toast.makeText(getApplicationContext(), "Note saved successfully",Toast.LENGTH_LONG).show();

        }
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.add_note_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save_note:
                saveNote();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}

