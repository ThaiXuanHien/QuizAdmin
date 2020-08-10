package com.example.quizadmin.Adapter;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quizadmin.Activity.CategoryActivity;
import com.example.quizadmin.Activity.SetsActivity;
import com.example.quizadmin.R;
import com.example.quizadmin.model.Category;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

    private List<Category> catList;

    public CategoryAdapter(List<Category> catList) {
        this.catList = catList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String catName = catList.get(position).getName();
        holder.setData(catName, position, this);
    }

    @Override
    public int getItemCount() {
        return catList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView imgBtnDelete, imgBtnEdit;
        private TextView txtCatName;
        private Dialog loadingDialog, editDialog;
        private EditText edtEditCatName;
        private Button btnUpdate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgBtnDelete = itemView.findViewById(R.id.imageViewBtnDeleteCat);
            imgBtnEdit = itemView.findViewById(R.id.imageViewBtnEditCat);
            txtCatName = itemView.findViewById(R.id.textViewCatName);

            loadingDialog = new Dialog(itemView.getContext());
            loadingDialog.setContentView(R.layout.loading_progressbar);
            loadingDialog.setCancelable(false);
            loadingDialog.getWindow().setBackgroundDrawableResource(R.drawable.progress_background);
            loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            editDialog = new Dialog(itemView.getContext());
            editDialog.setContentView(R.layout.edit_category);
            editDialog.setCancelable(true);
            editDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            edtEditCatName = editDialog.findViewById(R.id.editTextEditCatName);
            btnUpdate = editDialog.findViewById(R.id.buttonUpdate);
        }

        public void setData(String catName, final int pos, final CategoryAdapter adapter) {
            txtCatName.setText(catName);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CategoryActivity.selected_cat_index = pos;
                    Intent intent = new Intent(itemView.getContext(), SetsActivity.class);
                    itemView.getContext().startActivity(intent);
                }
            });

            imgBtnEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    edtEditCatName.setText(catList.get(pos).getName());
                    editDialog.show();
                }
            });
            btnUpdate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (edtEditCatName.getText().toString().trim().isEmpty()) {
                        edtEditCatName.setError("Không được để trống");
                        return;
                    }
                    updateCategory(edtEditCatName.getText().toString(), pos, itemView.getContext(), adapter);
                }
            });
            imgBtnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    AlertDialog dialog = new AlertDialog.Builder(itemView.getContext())
                            .setTitle("Delete Category").setMessage("Do you want to delete this Category ?")
                            .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    deleteCategory(pos, itemView.getContext(), adapter);
                                }
                            })
                            .setNegativeButton("Cancel", null)
                            .setIcon(android.R.drawable.ic_dialog_alert).show();
                    dialog.getButton(Dialog.BUTTON_POSITIVE).setBackgroundColor(Color.RED);
                    dialog.getButton(Dialog.BUTTON_NEGATIVE).setBackgroundColor(Color.GREEN);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    params.setMargins(0, 0, 50, 0);
                    dialog.getButton(dialog.BUTTON_NEGATIVE).setLayoutParams(params);
                }
            });
        }

        private void updateCategory(final String editCatName, final int pos, final Context context, final CategoryAdapter adapter) {
            editDialog.dismiss();
            loadingDialog.show();
            Map<String, Object> catData = new HashMap<>();
            catData.put("NAME", editCatName);

            final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

            firestore.collection("QUIZ").document(catList.get(pos).getId())
                    .update(catData)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            Map<String, Object> catDoc = new HashMap<>();
                            catDoc.put("CAT" + String.valueOf(pos + 1) + "_NAME", editCatName);

                            firestore.collection("QUIZ").document("Categories")
                                    .update(catDoc)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {

                                            Toast.makeText(context, "Category Name Changed Successfully", Toast.LENGTH_SHORT).show();
                                            CategoryActivity.catList.get(pos).setName(editCatName);
                                            adapter.notifyDataSetChanged();

                                            loadingDialog.dismiss();

                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                                            loadingDialog.dismiss();
                                        }
                                    });

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                            loadingDialog.dismiss();
                        }
                    });
        }

        private void deleteCategory(final int id, final Context context, final CategoryAdapter adapter) {
            loadingDialog.show();
            FirebaseFirestore firestore = FirebaseFirestore.getInstance();
            Map<String, Object> catDoc = new HashMap<>();
            int index = 1;
            for (int i = 0; i < catList.size(); i++) {
                if (i != id) {
                    catDoc.put("CAT" + index + "_ID", catList.get(i).getId());
                    catDoc.put("CAT" + index + "_NAME", catList.get(i).getName());
                    index++;
                }
            }
            catDoc.put("COUNT", index - 1);
            firestore.collection("QUIZ").document("Categories")
                    .set(catDoc).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(context, "Category deleted successfully", Toast.LENGTH_SHORT).show();
                    CategoryActivity.catList.remove(id);
                    adapter.notifyDataSetChanged();
                    loadingDialog.dismiss();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                    loadingDialog.dismiss();
                }
            });
        }
    }


}
