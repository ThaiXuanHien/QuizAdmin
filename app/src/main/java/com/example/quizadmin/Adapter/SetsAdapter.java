package com.example.quizadmin.Adapter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quizadmin.Activity.QuestionActivity;
import com.example.quizadmin.Activity.SetsActivity;
import com.example.quizadmin.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.quizadmin.Activity.CategoryActivity.catList;
import static com.example.quizadmin.Activity.CategoryActivity.selected_cat_index;
import static com.example.quizadmin.Activity.SetsActivity.selected_set_index;

public class SetsAdapter extends RecyclerView.Adapter<SetsAdapter.ViewHolder> {

    private List<String> setList;

    public SetsAdapter(List<String> setList) {
        this.setList = setList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sets, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String itemSet = setList.get(position);
        holder.setData(position, itemSet, this);
    }

    @Override
    public int getItemCount() {
        return setList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView txtSetName;
        private ImageView btnDeleteSet;
        private Dialog loadingDialog;

        public ViewHolder(@NonNull View itemView) {

            super(itemView);
            txtSetName = itemView.findViewById(R.id.textViewSetName);
            btnDeleteSet = itemView.findViewById(R.id.imageViewBtnDeleteSet);
            loadingDialog = new Dialog(itemView.getContext());
            loadingDialog.setContentView(R.layout.loading_progressbar);
            loadingDialog.setCancelable(false);
            loadingDialog.getWindow().setBackgroundDrawableResource(R.drawable.progress_background);
            loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        public void setData(final int pos, final String itemSet, final SetsAdapter adapter) {
            txtSetName.setText("SET " + (pos + 1));

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    selected_set_index = pos;

                    Intent intent = new Intent(itemView.getContext(), QuestionActivity.class);
                    itemView.getContext().startActivity(intent);
                }
            });

            btnDeleteSet.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog dialog = new AlertDialog.Builder(itemView.getContext())
                            .setTitle("Delete Set").setMessage("Do you want to delete this Set ?")
                            .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    deleteSet(pos, itemSet, itemView.getContext(), adapter);
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

        private void deleteSet(final int pos, String itemSet, final Context context, final SetsAdapter adapter) {
            loadingDialog.show();

            final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

            firestore.collection("QUIZ").document(catList.get(selected_cat_index).getId())
                    .collection(itemSet).get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                            WriteBatch batch = firestore.batch();

                            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                                batch.delete(doc.getReference());
                            }

                            batch.commit().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    Map<String, Object> catDoc = new HashMap<>();
                                    int index = 1;
                                    for (int i = 0; i < setList.size(); i++) {
                                        if (i != pos) {
                                            catDoc.put("SET" + String.valueOf(index) + "_ID", setList.get(i));
                                            index++;
                                        }
                                    }

                                    catDoc.put("SETS", index - 1);

                                    firestore.collection("QUIZ").document(catList.get(selected_cat_index).getId())
                                            .update(catDoc)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Toast.makeText(context, "Set deleted Sucesfully", Toast.LENGTH_SHORT).show();

                                                    SetsActivity.setList.remove(pos);

                                                    catList.get(selected_cat_index).setNoOfSets(String.valueOf(SetsActivity.setList.size()));

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
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                            loadingDialog.dismiss();
                        }
                    });

        }

    }

}
