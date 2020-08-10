package com.example.quizadmin.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;


import com.example.quizadmin.Adapter.SetsAdapter;
import com.example.quizadmin.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.quizadmin.Activity.CategoryActivity.catList;
import static com.example.quizadmin.Activity.CategoryActivity.selected_cat_index;

public class SetsActivity extends AppCompatActivity {

    private Button btnAddSet;
    private RecyclerView rcvSets;
    public static List<String> setList = new ArrayList<>();
    private SetsAdapter adapter;
    private FirebaseFirestore firestore;
    private Dialog loadingDialog;
    public static int selected_set_index = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sets);

        btnAddSet = (Button) findViewById(R.id.buttonAddSet);
        rcvSets = (RecyclerView) findViewById(R.id.recyclerviewSets);

        Toolbar toolbar = findViewById(R.id.toolbarSets);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Sets");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        loadingDialog = new Dialog(SetsActivity.this);
        loadingDialog.setContentView(R.layout.loading_progressbar);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setBackgroundDrawableResource(R.drawable.progress_background);
        loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        btnAddSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewSet();
            }
        });

        firestore = FirebaseFirestore.getInstance();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rcvSets.setLayoutManager(layoutManager);
        loadSets();

    }


    private void loadSets() {
        setList.clear();
        loadingDialog.show();

        firestore.collection("QUIZ").document(catList.get(selected_cat_index).getId())
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                long noOfSets = (long) documentSnapshot.get("SETS");

                for (int i = 1; i <= noOfSets; i++) {
                    setList.add(documentSnapshot.getString("SET" + String.valueOf(i) + "_ID"));
                }

                catList.get(selected_cat_index).setSetCounter(documentSnapshot.getString("COUNTER"));
                catList.get(selected_cat_index).setNoOfSets(String.valueOf(noOfSets));

                adapter = new SetsAdapter(setList);
                rcvSets.setAdapter(adapter);

                loadingDialog.dismiss();

            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(SetsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        loadingDialog.dismiss();
                    }
                });

    }

    private void addNewSet() {
        loadingDialog.show();

        final String curr_cat_id = catList.get(selected_cat_index).getId();
        final String curr_counter = catList.get(selected_cat_index).getSetCounter();

        Map<String, Object> qData = new HashMap<>();
        qData.put("COUNT", "0");

        firestore.collection("QUIZ").document(curr_cat_id)
                .collection(curr_counter).document("QUESTIONS_LIST")
                .set(qData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        Map<String, Object> catDoc = new HashMap<>();
                        catDoc.put("COUNTER", String.valueOf(Integer.valueOf(curr_counter) + 1));
                        catDoc.put("SET" + String.valueOf(setList.size() + 1) + "_ID", curr_counter);
                        catDoc.put("SETS", setList.size() + 1);

                        firestore.collection("QUIZ").document(curr_cat_id)
                                .update(catDoc)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        Toast.makeText(SetsActivity.this, " Set Added Successfully", Toast.LENGTH_SHORT).show();

                                        setList.add(curr_counter);
                                        catList.get(selected_cat_index).setNoOfSets(String.valueOf(setList.size()));
                                        catList.get(selected_cat_index).setSetCounter(String.valueOf(Integer.valueOf(curr_counter) + 1));

                                        adapter.notifyItemInserted(setList.size());
                                        loadingDialog.dismiss();

                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(SetsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                        loadingDialog.dismiss();
                                    }
                                });

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(SetsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        loadingDialog.dismiss();
                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}
