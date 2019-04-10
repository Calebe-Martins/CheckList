package com.cgm.checklist.fragment;


import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cgm.checklist.R;
import com.cgm.checklist.database.DBHelper;

import java.util.ArrayList;
import java.util.List;

public class ListItemsFrag extends Fragment {

    DBHelper dbHelper;
    private String type_folder;
    ArrayAdapter adapter;

    private ListView listItems;
    private EditText userInput;

    // Ação de deletar os itens
    public static List<String> UserSelection = new ArrayList<>();
    public static final List<String> IsChecked = new ArrayList<>();
    boolean[] checkedItems;

    public ListItemsFrag() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        dbHelper = new DBHelper(getContext());

        View view = inflater.inflate(R.layout.list_items_frag, container, false);

        type_folder = getArguments().getString("NOME_DA_PASTA");

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(type_folder);

        listItems = (ListView) view.findViewById(R.id.list_itemsFrag);
        userInput = (EditText) view.findViewById(R.id.editTextItemFrag);

        setHasOptionsMenu(true);
        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onResume() {
        LoadDataItems();
        super.onResume();
    }

    // Carrega os itens da pasta selecionada
    public void LoadDataItems() {
        final Cursor data = dbHelper.getData(type_folder);
        final ArrayList<String> listData = new ArrayList<>();
        while (data.moveToNext()) {
            // Obtenha o valor do banco de dados na coluna -1
            // Em seguida adiciona a lista
            listData.add(data.getString(1));
            if (data.getString(3).equals("1")) { // Salva os itens q tem 1 nos STATUS
                if (!IsChecked.contains(data.getPosition())) { // Adiciona a IsChecked para setar como checado
                    IsChecked.add(String.valueOf(data.getPosition()));
                }
            }
        }

        // Seta a lista para poder selecionar multiplos itens
        listItems.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        // Adapter para click nas checkbox
        adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_multiple_choice, listData);
        // Criador da lista adaptada e seta a lista adaptada
        listItems.setAdapter(adapter);

        /**Quando a preferencia de seleção simples dos itens estiver ativa, após sair do app os
         * itens selecionados serão descelecionados altomaticamente. Caso esteja desativada, faz
         * os itens ficarem marcados mesmo que saia do app
         */
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean simpleSelection = sharedPreferences.getBoolean("simple_selection", true);
        boolean multiSelection  = sharedPreferences.getBoolean("persistence_selection", true);
        boolean deleteSelection = sharedPreferences.getBoolean("delete_selection", true);

        if (simpleSelection) { IsChecked.clear(); }

        // Quando a multiple estiver selecionada, salva os itens marcados
        if (multiSelection) {
            // Verifica se tem 1 nos STATUS e manda ele checado
            for (int i = 0; i < IsChecked.size(); i++) {
                int aux = Integer.parseInt(IsChecked.get(i));
                listItems.setItemChecked(aux, true);
            }
            IsChecked.clear();

            listItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String name = (String) listItems.getItemAtPosition(position);

                    if (listItems.isItemChecked(position)) {
                        dbHelper.updateStatus(1, name);
                    } else {
                        dbHelper.updateStatus(0, name);
                    }
                }
            });
        }

        if (deleteSelection) {
            listItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                    String deletaItem = (String) listItems.getItemAtPosition(position);
                    // Deleta o item do banco de dados
                    dbHelper.deleteItems(deletaItem);
                    // Handler pelo visto é um thread
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // Deleta o item da listview
                            listData.remove(position);
                            adapter.notifyDataSetChanged();
                        }
                    },500);
                    startAnimation(listItems);
                    // Seta o proximo item como não checkado
                    listItems.setItemChecked(position, false);
                }
            });
        }

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
                        if(resultado.equals("Nome já existe")) {
                            return true;
                        } else {
                            listData.add(newEntry);
                            adapter.notifyDataSetChanged();
                            return true;
                        }
                    } else {
                        toastMenssage("Digite um item");
                        return true;
                    }
                }
                return false;
            }
        });
    }

    private void startAnimation(View view) {
        Animation animation;

        if (view != null) {
            animation = AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_out);
            animation.setFillAfter(false);
            view.setAnimation(animation);
        }
    }

    //Ação do botão voltar DA ACTIONBAR
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) { //Botão adicional na ToolBar
        switch (item.getItemId()) {
            case android.R.id.home: {// ID do seu botão (gerado automaticamente pelo android, usando como está, deve funcionar
                break;
            }
            // Botão delete da action bar para excluir os itens do banco de dados
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
                            if (!UserSelection.contains(listItems.getItemAtPosition(position))) {
                                UserSelection.add((String) listItems.getItemAtPosition(position));
                            }
                        } else {
                            UserSelection.remove(listItems.getItemAtPosition(position));
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
                            toastMenssage("Itens deletados");
                            UserSelection.clear();
                            LoadDataItems();
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
        return true;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.listview_activity, menu);
    }

    // Aparece uma menssagem Toast
    public void toastMenssage(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
}
