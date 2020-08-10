package com.example.quizadmin.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.quizadmin.Adapter.QuestionAdapter;
import com.example.quizadmin.R;
import com.example.quizadmin.model.Question;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.quizadmin.Activity.CategoryActivity.catList;
import static com.example.quizadmin.Activity.CategoryActivity.selected_cat_index;
import static com.example.quizadmin.Activity.SetsActivity.selected_set_index;
import static com.example.quizadmin.Activity.SetsActivity.setList;

public class QuestionActivity extends AppCompatActivity {
    private RecyclerView rcvQt;
    private Button btnAddQt;
    public static List<Question> questionList = new ArrayList<>();
    private QuestionAdapter adapter;
    private FirebaseFirestore firestore;
    private Dialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);


        Toolbar toolbar = findViewById(R.id.toolbarQt);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Questions");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        rcvQt = findViewById(R.id.recyclerviewQt);
        btnAddQt = findViewById(R.id.buttonAddQt);

        loadingDialog = new Dialog(QuestionActivity.this);
        loadingDialog.setContentView(R.layout.loading_progressbar);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setBackgroundDrawableResource(R.drawable.progress_background);
        loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        btnAddQt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(QuestionActivity.this, QuestionDetailsActivity.class);
                intent.putExtra("ACTION","ADD");
                startActivity(intent);
            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rcvQt.setLayoutManager(layoutManager);

        firestore = FirebaseFirestore.getInstance();

        loadQuestions();
    }

    private void loadQuestions() {
        questionList.clear();

        loadingDialog.show();

        firestore.collection("QUIZ").document(catList.get(selected_cat_index).getId())
                .collection(setList.get(selected_set_index)).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        Map<String, QueryDocumentSnapshot> docList = new HashMap<>();

                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            docList.put(doc.getId(), doc);
                        }

                        QueryDocumentSnapshot questionListDoc = docList.get("QUESTIONS_LIST");

                        String count = questionListDoc.getString("COUNT");

                        for (int i = 0; i < Integer.valueOf(count); i++) {
                            String quesID = questionListDoc.getString("Q" + String.valueOf(i + 1) + "_ID");

                            QueryDocumentSnapshot quesDoc = docList.get(quesID);

                            questionList.add(new Question(
                                    quesID,
                                    quesDoc.getString("QUESTION"),
                                    quesDoc.getString("A"),
                                    quesDoc.getString("B"),
                                    quesDoc.getString("C"),
                                    quesDoc.getString("D"),
                                    Integer.valueOf(quesDoc.getString("ANSWER"))
                            ));

                        }

                        adapter = new QuestionAdapter(questionList);
                        rcvQt.setAdapter(adapter);

                        loadingDialog.dismiss();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(QuestionActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        loadingDialog.dismiss();
                    }
                });
    }
    @Override
    protected void onResume() {
        super.onResume();

        if(adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == android.R.id.home)
        {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}
