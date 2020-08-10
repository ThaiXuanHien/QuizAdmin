package com.example.quizadmin.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Dialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.quizadmin.R;
import com.example.quizadmin.model.Question;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import static com.example.quizadmin.Activity.CategoryActivity.catList;
import static com.example.quizadmin.Activity.CategoryActivity.selected_cat_index;
import static com.example.quizadmin.Activity.QuestionActivity.questionList;
import static com.example.quizadmin.Activity.SetsActivity.selected_set_index;
import static com.example.quizadmin.Activity.SetsActivity.setList;

public class QuestionDetailsActivity extends AppCompatActivity {
    private EditText edtQt, edtOptionA, edtOptionB, edtOptionC, edtOptionD, edtAnswer;
    private Button btnAddQtDetails;
    private String qtStr, aStr, bStr, cStr, dStr, ansStr;
    private Dialog loadingDialog;
    private FirebaseFirestore firestore;
    private String action;
    private int qID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_details);

        Toolbar toolbar = findViewById(R.id.toolbarQtDetails);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        edtQt = findViewById(R.id.edittextQuestion);
        edtOptionA = findViewById(R.id.edittextOptionA);
        edtOptionB = findViewById(R.id.edittextOptionB);
        edtOptionC = findViewById(R.id.edittextOptionC);
        edtOptionD = findViewById(R.id.edittextOptionD);
        edtAnswer = findViewById(R.id.edittextAnswer);
        btnAddQtDetails = findViewById(R.id.buttonAddQtDetails);

        loadingDialog = new Dialog(QuestionDetailsActivity.this);
        loadingDialog.setContentView(R.layout.loading_progressbar);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setBackgroundDrawableResource(R.drawable.progress_background);
        loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);

        firestore = FirebaseFirestore.getInstance();

        action = getIntent().getStringExtra("ACTION");

        if(action.compareTo("EDIT") == 0)
        {
            qID = getIntent().getIntExtra("Q_ID",0);
            loadData(qID);
            getSupportActionBar().setTitle("Question " + String.valueOf(qID + 1));
            btnAddQtDetails.setText("UPDATE");
        }
        else
        {
            getSupportActionBar().setTitle("Question " + String.valueOf(questionList.size() + 1));
            btnAddQtDetails.setText("ADD");
        }

        btnAddQtDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                qtStr = edtQt.getText().toString();
                aStr = edtOptionA.getText().toString();
                bStr = edtOptionB.getText().toString();
                cStr = edtOptionC.getText().toString();
                dStr = edtOptionD.getText().toString();
                ansStr = edtAnswer.getText().toString();

                if(qtStr.isEmpty()) {
                    edtQt.setError("Enter Question");
                    return;
                }

                if(aStr.isEmpty()) {
                    edtOptionA.setError("Enter option A");
                    return;
                }

                if(bStr.isEmpty()) {
                    edtOptionB.setError("Enter option B ");
                    return;
                }
                if(cStr.isEmpty()) {
                    edtOptionC.setError("Enter option C");
                    return;
                }
                if(dStr.isEmpty()) {
                    edtOptionD.setError("Enter option D");
                    return;
                }
                if(ansStr.isEmpty()) {
                    edtAnswer.setError("Enter correct answer");
                    return;
                }

                if(action.compareTo("EDIT") == 0)
                {
                    editQuestion();
                }
                else {
                    addNewQuestion();

                }

            }
        });
    }

    private void addNewQuestion() {
        loadingDialog.show();

        Map<String,Object> quesData = new HashMap<>();

        quesData.put("QUESTION",qtStr);
        quesData.put("A",aStr);
        quesData.put("B",bStr);
        quesData.put("C",cStr);
        quesData.put("D",dStr);
        quesData.put("ANSWER",ansStr);


        final String doc_id = firestore.collection("QUIZ").document(catList.get(selected_cat_index).getId())
                .collection(setList.get(selected_set_index)).document().getId();

        firestore.collection("QUIZ").document(catList.get(selected_cat_index).getId())
                .collection(setList.get(selected_set_index)).document(doc_id)
                .set(quesData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        Map<String,Object> quesDoc = new HashMap<>();
                        quesDoc.put("Q" + String.valueOf(questionList.size() + 1) + "_ID", doc_id);
                        quesDoc.put("COUNT",String.valueOf(questionList.size() + 1));

                        firestore.collection("QUIZ").document(catList.get(selected_cat_index).getId())
                                .collection(setList.get(selected_set_index)).document("QUESTIONS_LIST")
                                .update(quesDoc)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(QuestionDetailsActivity.this, " Question Added Successfully", Toast.LENGTH_SHORT).show();

                                        questionList.add(new Question(
                                                doc_id,
                                                qtStr,aStr,bStr,cStr,dStr, Integer.valueOf(ansStr)
                                        ));

                                        loadingDialog.dismiss();
                                        QuestionDetailsActivity.this.finish();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(QuestionDetailsActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                                        loadingDialog.dismiss();
                                    }
                                });


                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(QuestionDetailsActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                        loadingDialog.dismiss();
                    }
                });
    }


    private void loadData(int id)
    {
        edtQt.setText(questionList.get(id).getQuestion());
        edtOptionA.setText(questionList.get(id).getOptionA());
        edtOptionB.setText(questionList.get(id).getOptionB());
        edtOptionC.setText(questionList.get(id).getOptionC());
        edtOptionD.setText(questionList.get(id).getOptionD());
        edtAnswer.setText(String.valueOf(questionList.get(id).getCorrectAns()));
    }


    private void editQuestion()
    {
        loadingDialog.show();

        Map<String,Object> quesData = new HashMap<>();
        quesData.put("QUESTION", qtStr);
        quesData.put("A",aStr);
        quesData.put("B",bStr);
        quesData.put("C",cStr);
        quesData.put("D",dStr);
        quesData.put("ANSWER",ansStr);


        firestore.collection("QUIZ").document(catList.get(selected_cat_index).getId())
                .collection(setList.get(selected_set_index)).document(questionList.get(qID).getQuesID())
                .set(quesData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        Toast.makeText(QuestionDetailsActivity.this,"Question updated successfully",Toast.LENGTH_SHORT).show();

                        questionList.get(qID).setQuestion(qtStr);
                        questionList.get(qID).setOptionA(aStr);
                        questionList.get(qID).setOptionB(bStr);
                        questionList.get(qID).setOptionC(cStr);
                        questionList.get(qID).setOptionD(dStr);
                        questionList.get(qID).setCorrectAns(Integer.valueOf(ansStr));

                        loadingDialog.dismiss();
                        QuestionDetailsActivity.this.finish();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(QuestionDetailsActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                        loadingDialog.dismiss();
                    }
                });

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
