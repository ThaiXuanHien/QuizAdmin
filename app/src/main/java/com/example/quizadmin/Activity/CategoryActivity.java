package com.example.quizadmin.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.quizadmin.Adapter.CategoryAdapter;
import com.example.quizadmin.R;
import com.example.quizadmin.model.Category;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoryActivity extends AppCompatActivity {

    private RecyclerView rcvCategory;
    private Button btnAddNewCat, btnConfirm;
    public static List<Category> catList = new ArrayList<>();
    private FirebaseFirestore firestore;
    private Dialog loadingDialog, addCatDialog;
    private EditText edtCatName;
    private CategoryAdapter adapter;
    public static int selected_cat_index = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);
        Toolbar toolbar = findViewById(R.id.toolbarCategory);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Categories");


        btnAddNewCat = (Button) findViewById(R.id.buttonAddCat);
        rcvCategory = (RecyclerView) findViewById(R.id.recyclerviewCategory);


        loadingDialog = new Dialog(CategoryActivity.this);
        loadingDialog.setContentView(R.layout.loading_progressbar);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setBackgroundDrawableResource(R.drawable.progress_background);
        loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        addCatDialog = new Dialog(CategoryActivity.this);
        addCatDialog.setContentView(R.layout.add_category);
        addCatDialog.setCancelable(true);
        addCatDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        edtCatName = (EditText) addCatDialog.findViewById(R.id.editTextCatName);
        btnConfirm = (Button) addCatDialog.findViewById(R.id.buttonConfirm);


        firestore = FirebaseFirestore.getInstance();


        btnAddNewCat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edtCatName.getText().clear();
                addCatDialog.show();
            }
        });

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edtCatName.getText().toString().trim().isEmpty()) {
                    edtCatName.setError("Không được để trống");
                    return;
                }
                addNewCategory(edtCatName.getText().toString());
            }
        });
        LinearLayoutManager layout = new LinearLayoutManager(this);
        layout.setOrientation(LinearLayoutManager.VERTICAL);
        rcvCategory.setLayoutManager(layout);
        loadData();

    }

    private void addNewCategory(final String inputCatName) {
        addCatDialog.dismiss();
        loadingDialog.show();

        Map<String, Object> catData = new HashMap<>();
        catData.put("NAME", inputCatName);
        catData.put("SETS", 0);
        catData.put("COUNTER", "1");

        final String docId = firestore.collection("QUIZ").document().getId();
        firestore.collection("QUIZ").document(docId).set(catData).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Map<String, Object> catDoc = new HashMap<>();
                catDoc.put("CAT" + (catList.size() + 1) + "_NAME", inputCatName);
                catDoc.put("CAT" + (catList.size() + 1) + "_ID", docId);
                catDoc.put("COUNT", catList.size() + 1);
                firestore.collection("QUIZ").document("Categories").update(catDoc).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(CategoryActivity.this, "Category Added Successfully", Toast.LENGTH_SHORT).show();
                        catList.add(new Category(docId, inputCatName, "0", "1"));
                        adapter.notifyItemInserted(catList.size());
                        loadingDialog.dismiss();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(CategoryActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        loadingDialog.dismiss();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(CategoryActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                loadingDialog.dismiss();
            }
        });

    }

    private void loadData() {
        loadingDialog.show();
        catList.clear();
        firestore.collection("QUIZ").document("Categories").get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists()) {
                        long count = (long) doc.get("COUNT");
                        for (int i = 1; i <= count; i++) {
                            String catName = doc.getString("CAT" + i + "_NAME");
                            String catId = doc.getString("CAT" + i + "_ID");
                            catList.add(new Category(catId, catName, "0", "1"));
                        }
                        adapter = new CategoryAdapter(catList);
                        rcvCategory.setAdapter(adapter);
                    } else {
                        Toast.makeText(CategoryActivity.this, "No Categoty Document Exists!", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else {
                    Toast.makeText(CategoryActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
                loadingDialog.dismiss();
            }
        });
    }
}
