package com.cgm.checklist.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.CheckedTextView;

import com.cgm.checklist.R;
import com.cgm.checklist.database.DBHelper;

import java.util.ArrayList;
import java.util.List;


public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.CustomViewHolder> {

    private List<Item> items;
    Context context;
    DBHelper dbHelper;

    SparseBooleanArray itemStateArray = new SparseBooleanArray();

    public class CustomViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public CheckedTextView checkedTextView;

        public CustomViewHolder(View itemView) {
            super(itemView);
            checkedTextView = (CheckedTextView) itemView.findViewById(R.id.checkTextView);
            itemView.setOnClickListener(this);
        }

        void bind(int position) {
            if (!itemStateArray.get(position, false)) {
                checkedTextView.setChecked(false);
            } else {
                checkedTextView.setChecked(true);
            }
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            /**Quando a preferencia de seleção simples dos itens estiver ativa, após sair do app os
             * itens selecionados serão descelecionados altomaticamente. Caso esteja desativada, faz
             * os itens ficarem marcados mesmo que saia do app
             */
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            boolean simpleSelection = sharedPreferences.getBoolean("simple_selection", true);
            boolean multiSelection  = sharedPreferences.getBoolean("persistence_selection", true);
            boolean deleteSelection = sharedPreferences.getBoolean("delete_selection", true);

            if (simpleSelection) {
                if (!itemStateArray.get(adapterPosition, false)) {
                    checkedTextView.setChecked(true);
                    itemStateArray.put(adapterPosition, true);
                } else  {
                    checkedTextView.setChecked(false);
                    itemStateArray.put(adapterPosition, false);
                }
            }

            if (multiSelection) {
                if (!itemStateArray.get(adapterPosition, false)) {
                    checkedTextView.setChecked(true);
                    itemStateArray.put(adapterPosition, true);
                    String name = checkedTextView.getText().toString();
                    dbHelper.updateStatus(1, name);
                } else  {
                    checkedTextView.setChecked(false);
                    itemStateArray.put(adapterPosition, false);
                    String name = checkedTextView.getText().toString();
                    dbHelper.updateStatus(0, name);
                }
            }

            // Deleta item ao ser clicado
            if (deleteSelection) {
                if (!itemStateArray.get(adapterPosition, false)) {
                    checkedTextView.setChecked(true);
                    itemStateArray.put(adapterPosition, true);
                    // Pega nome do item clicado
                    String deletaItem = checkedTextView.getText().toString();
                    // Deleta do banco de dados
                    dbHelper.deleteItems(deletaItem);
                    // Remove da lista
                    items.remove(adapterPosition);
                    // Notifica o adapter
                    notifyItemRemoved(adapterPosition);
                    notifyItemRangeChanged(adapterPosition, items.size());
                } else  {
                    checkedTextView.setChecked(false);
                    itemStateArray.put(adapterPosition, false);
                }
            }
        }
    }

    public ItemAdapter(List<Item> items, Context context, DBHelper dbHelper) {
        this.items = items;
        this.context = context;
        this.dbHelper = dbHelper; // Posso tirar isso daqui
    }

    @NonNull
    @Override
    public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int position) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_adapter, parent, false);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean multiSelection  = sharedPreferences.getBoolean("persistence_selection", true);
        if (multiSelection) {
            final Cursor data = dbHelper.getData("puc");
            while (data.moveToNext()) {
                if (data.getString(3).equals("1")) {
                    Item item = items.get(data.getPosition());
                    item.setPosition(data.getPosition());
                    item.setChecked(true);
                    itemStateArray.put(item.getPosition(), true);
                }
            }
        }

        return new CustomViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomViewHolder holder, final int position) {
        Item item = items.get(position);
        holder.bind(position);
        holder.checkedTextView.setText(item.getNome());
        //setAnimation(holder.itemView, position); // TESTE ANIMAÇÂO FADE IN MUDAR ANIMAÇÂO
        holder.checkedTextView.setChecked(item.getChecked());

    }

    private int lastPosition = -1; // TESTE ANIMAÇÂO FADE IN MUDAR ANIMAÇÂO

    private void setAnimation(View viewToAnimate, int position) { // TESTE ANIMAÇÂO FADE IN MUDAR ANIMAÇÂO
        if (position > lastPosition) {
            Animation animation = new AlphaAnimation(0.0f, 1.0f);
            animation.setDuration(2000);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
