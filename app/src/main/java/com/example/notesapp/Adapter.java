package com.example.notesapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.DialogInterface;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import org.bson.Document;
import org.bson.types.ObjectId;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;

public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

    List<String> titles,description, date, time;
    List<ObjectId> id;
    LayoutInflater inflater;
    Context mContext;
    AlertDialog.Builder builder;


    public Adapter(Context ctx, List<String> titles, List<String> description, List<String> date, List<String> time, List<ObjectId> id) {
        this.mContext = ctx;
        this.titles = titles;
        this.description = description;
        this.date = date;
        this.time = time;
        this.id = id;
        this.inflater = LayoutInflater.from(ctx);
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.custom_layout,parent,false);
        builder = new AlertDialog.Builder(mContext);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.title.setText(titles.get(position).substring(0, Math.min(titles.get(position).length(), 15)));
        holder.desc.setText(description.get(position).substring(0, Math.min(description.get(position).length(), 25)));
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd");
        String currentDate = simpleDateFormat.format(calendar.getTime());
        Log.d("DATECURRENT", currentDate + " " + date.get(position));
        if (currentDate.equals(date.get(position)))
            holder.tim.setText(time.get(position).split(":")[0] + ":" + time.get(position).split(":")[1]);
        else
            holder.tim.setVisibility(View.GONE);


    }

    @Override
    public int getItemCount() {
        return titles.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView title,desc,tim;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            desc = itemView.findViewById(R.id.desc);
            tim = itemView.findViewById(R.id.time);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, NewNoteActivity.class);
                    intent.putExtra("id",id.get(getAdapterPosition()).toString());
                    intent.putExtra("title",titles.get(getAdapterPosition()));
                    intent.putExtra("desc",description.get(getAdapterPosition()));
                    intent.putExtra("date",date.get(getAdapterPosition()));
                    intent.putExtra("time",time.get(getAdapterPosition()));
                    ((Activity) mContext).startActivityForResult(intent, 210);

//                    Toast.makeText(v.getContext(), "Clicked -> " + id.get(getAdapterPosition()) + " " + time.get(getAdapterPosition()), Toast.LENGTH_SHORT).show();
                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    builder.setMessage("Do you want to delete this note?")
                            .setCancelable(false)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int identify) {
                                    MongoClientURI uri = new MongoClientURI("mongodb://naren:qwerty123@test-shard-00-00.1halt.mongodb.net:27017,test-shard-00-01.1halt.mongodb.net:27017,test-shard-00-02.1halt.mongodb.net:27017/test?ssl=true&replicaSet=atlas-jyv6ux-shard-0&authSource=admin&retryWrites=true&w=majority");

                                    MongoClient mongoClient = new MongoClient(uri);
                                    MongoDatabase database = mongoClient.getDatabase("test");
                                    MongoCollection<Document> collection = database.getCollection("notes");
                                    collection.deleteOne(eq("_id", new ObjectId(id.get(getAdapterPosition()).toString())));
                                    removeAt(getAdapterPosition());
                                    dialog.cancel();
                                    Toast.makeText(mContext,"Note deleted successfully",
                                            Toast.LENGTH_SHORT).show();
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.setTitle("Delete Note");
                    alert.show();
                    return true;
                }
            });
        }
        public void removeAt(int position) {
            titles.remove(position);
            description.remove(position);
            id.remove(position);
            time.remove(position);
            date.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, titles.size());
        }
    }
}