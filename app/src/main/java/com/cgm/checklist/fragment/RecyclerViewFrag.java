package com.cgm.checklist.fragment;


import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.cgm.checklist.R;
import com.cgm.checklist.adapter.Item;
import com.cgm.checklist.adapter.ItemAdapter;
import com.cgm.checklist.database.DBHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class RecyclerViewFrag extends Fragment {

    DBHelper dbHelper;
    List<Item> items = new ArrayList<>();
    private ItemAdapter mAdapter;
    private String type_folder;
    private RecyclerView recyclerView;
    private EditText userInput;

    public static List<String> UserSelection = new ArrayList<>();
    boolean[] checkedItems;

    public RecyclerViewFrag() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        dbHelper = new DBHelper(getContext());

        View view = inflater.inflate(R.layout.recycler_view_frag, container, false);

        type_folder = getArguments().getString("NOME_DA_PASTA");

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(type_folder);

        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view_frag);
        mAdapter = new ItemAdapter(items, getContext(), dbHelper);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(mAdapter);

        populateItemsName();
        mAdapter.notifyDataSetChanged();

        userInput = (EditText) view.findViewById(R.id.editTextRecyclerFrag);

        userInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (event != null && KeyEvent.KEYCODE_ENTER == event.getKeyCode() || actionId == EditorInfo.IME_ACTION_DONE) {
                    String newEntry = userInput.getText().toString();
                    String resultado;
                    if (userInput.length() != 0) {
                        resultado = dbHelper.AddData(newEntry, type_folder);
                        Toast.makeText(getContext(), resultado, Toast.LENGTH_SHORT).show();
                        userInput.setText("");
                        // Impede de adicionar na lista nomes q jah existem
                        if (resultado.equals("Nome já existe")) {
                            return true;
                        } else {
                            items.add(new Item(newEntry));
                            mAdapter.notifyDataSetChanged();
                            return true;
                        }
                    } else {
                        toastMenssage("Digite um item");
                    }

                }
                return false;
            }
        });

        setHasOptionsMenu(true);
        // Inflate the layout for this fragment
        return view;
    }

//    @Override
//    public void onResume() {
//        super.onResume();
//        populateItemsName();
//    }

    //Ação do botão voltar DA ACTIONBAR
    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                break;
            }

            case R.id.action_delete_actb: {
                Cursor data = dbHelper.getData(type_folder);
                final ArrayList<String> listData = new ArrayList<>();
                while (data.moveToNext()) {
                    // Obtenha o valor do banco de dados na coluna -1
                    // Em seguida adiciona a lista
                    listData.add(data.getString(1));
                }

                String mList[] = new String[listData.size()];

                for (int i = 0; i < listData.size(); i++) {
                    mList[i] = listData.get(i);
                }

                // Carrega o boolean com tamanho da lista
                checkedItems = new boolean[mList.length];
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
                alertDialogBuilder.setTitle("Deletar itens: ");
                alertDialogBuilder.setMultiChoiceItems(mList, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int position, boolean isChecked) {

                        if (isChecked) {
                            // .contains serve para saber se a string CONTEM dentro do meu UserSelection
                            if (!UserSelection.contains(items.get(position).getNome())) {
                                UserSelection.add(items.get(position).getNome());
                            }
                        } else {
                            UserSelection.remove(items.get(position).getNome());
                        }
                        if (UserSelection.contains(null)) {
                            toastMenssage("Selecione um item");
                        }
                    }
                });

                alertDialogBuilder.setCancelable(false);
                alertDialogBuilder.setPositiveButton("Deletar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Verifica se foi selecionado alguma checkbox
                        if (UserSelection.size() == 0) {
                            toastMenssage("Selecione um item");
                        } else {
                            //percorre e deleta os itens do banco de dados dentro da pasta específica
                            for (int i = 0; i < UserSelection.size(); i++) {
                                dbHelper.deleteItems(UserSelection.get(i));
                            }
//                            toastMenssage("Itens deletados");
                            UserSelection.clear();
                            items.clear();
                            populateItemsName();
                            mAdapter.notifyDataSetChanged();
                            // ######## COLOCAR O NOTIFY AQUI ####
                        }
                    }
                }).setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        UserSelection.clear();
                        dialog.cancel();
                    }
                });

                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }

            default:break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.listview_activity, menu);
    }

    private void populateItemsName() {
        final Cursor data = dbHelper.getData(type_folder);
        while (data.moveToNext()) {
            items.add(new Item(data.getString(1)));
            mAdapter.notifyDataSetChanged();
        }

    }

    private void toastMenssage(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
}
