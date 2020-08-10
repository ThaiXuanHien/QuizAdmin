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

import com.example.quizadmin.Activity.QuestionDetailsActivity;
import com.example.quizadmin.R;
import com.example.quizadmin.model.Question;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.quizadmin.Activity.CategoryActivity.catList;
import static com.example.quizadmin.Activity.CategoryActivity.selected_cat_index;
import static com.example.quizadmin.Activity.QuestionActivity.questionList;
import static com.example.quizadmin.Activity.SetsActivity.selected_set_index;
import static com.example.quizadmin.Activity.SetsActivity.setList;

public class QuestionAdapter extends RecyclerView.Adapter<QuestionAdapter.ViewHolder> {

    private List<Question> ques_list;

    public QuestionAdapter(List<Question> ques_list) {
        this.ques_list = ques_list;
    }

    @NonNull
    @Override
    public QuestionAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_category, viewGroup, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuestionAdapter.ViewHolder viewHolder, int pos) {
        viewHolder.setData(pos, this);
    }

    @Override
    public int getItemCount() {
        return ques_list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView title;
        private ImageView btnDeleteQt;
        private Dialog loadingDialog;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.textViewCatName);
            btnDeleteQt = itemView.findViewById(R.id.imageViewBtnDeleteCat);

            loadingDialog = new Dialog(itemView.getContext());
            loadingDialog.setContentView(R.layout.loading_progressbar);
            loadingDialog.setCancelable(false);
            loadingDialog.getWindow().setBackgroundDrawableResource(R.drawable.progress_background);
            loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        private void setData(final int pos, final QuestionAdapter adapter) {
            title.setText("QUESTION " + String.valueOf(pos + 1));

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(itemView.getContext(), QuestionDetailsActivity.class);
                    intent.putExtra("ACTION", "EDIT");
                    intent.putExtra("Q_ID", pos);
                    itemView.getContext().startActivity(intent);
                }
            });

            btnDeleteQt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    AlertDialog dialog = new AlertDialog.Builder(itemView.getContext())
                            .setTitle("Delete Question")
                            .setMessage("Do you want to delete this Question ?")
                            .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    deleteQuestion(pos, itemView.getContext(), adapter);
                                }
                            })
                            .setNegativeButton("Cancel", null)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();

                    dialog.getButton(dialog.BUTTON_POSITIVE).setBackgroundColor(Color.RED);
                    dialog.getButton(dialog.BUTTON_NEGATIVE).setBackgroundColor(Color.GREEN);

                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    params.setMargins(0, 0, 50, 0);
                    dialog.getButton(dialog.BUTTON_NEGATIVE).setLayoutParams(params);


                }
            });
        }

        private void deleteQuestion(final int pos, final Context context, final QuestionAdapter adapter) {
            loadingDialog.show();

            final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

            firestore.collection("QUIZ").document(catList.get(selected_cat_index).getId())
                    .collection(setList.get(selected_set_index)).document(questionList.get(pos).getQuesID())
                    .delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            Map<String, Object> quesDoc = new HashMap<>();
                            int index = 1;
                            for (int i = 0; i < questionList.size(); i++) {
                                if (i != pos) {
                                    quesDoc.put("Q" + String.valueOf(index) + "_ID", questionList.get(i).getQuesID());
                                    index++;
                                }
                            }

                            quesDoc.put("COUNT", String.valueOf(index - 1));

                            firestore.collection("QUIZ").document(catList.get(selected_cat_index).getId())
                                    .collection(setList.get(selected_set_index)).document("QUESTIONS_LIST")
                                    .set(quesDoc)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(context, "Question Deleted Successfully", Toast.LENGTH_SHORT).show();

                                            questionList.remove(pos);
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


    }
}