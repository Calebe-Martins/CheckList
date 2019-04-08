package com.cgm.checklist;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;

import com.cgm.checklist.database.DBHelper;

public class Testes extends AppCompatActivity {

    public RecyclerView recyclerView;

    DBHelper dbHelper;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.testes);

        dbHelper = new DBHelper(context);
        recyclerView = (RecyclerView) findViewById(R.id.Recyclerview);

        String[] names = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10"};
    }
}
