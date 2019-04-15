package com.cgm.checklist.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
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
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 */
public class MenuFrag extends Fragment {


    DBHelper dbHelper;
    private String type_folder;
    ListView listView;
    ArrayAdapter adapter;
    EditText editText;

    public static List<String> UserSelection = new ArrayList<>();
    boolean[] checkedItems;

    public MenuFrag() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        dbHelper = new DBHelper(getContext());

        View view = inflater.inflate(R.layout.menu_frag, container, false);

        listView = (ListView) view.findViewById(R.id.list_view_frag);
        editText = (EditText) view.findViewById(R.id.editTextMenuFrag);

        LoadDataMenu();

        setHasOptionsMenu(true);
        // Inflate the layout for this fragment
        return view;
    }

    public void LoadDataMenu() {
        Cursor data = dbHelper.getData("menu");
        final ArrayList<String> listData = new ArrayList<>();
        while (data.moveToNext()) {
            // Obtenha o valor do banco de dados na coluna -1
            // Em seguida adiciona a lista
            listData.add(data.getString(1));
        }

        adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, listData);

        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                type_folder = listView.getItemAtPosition(position).toString();

//                ListItemsFrag listItemsFrag = new ListItemsFrag();
//                Bundle bundle = new Bundle();
//                bundle.putString("NOME_DA_PASTA", type_folder);
//                listItemsFrag.setArguments(bundle);
//                FragmentTransaction manager = getFragmentManager().beginTransaction();
//                manager.replace(R.id.frameContainer, listItemsFrag).addToBackStack(null).commit();



                RecyclerViewFrag recyclerViewFrag = new RecyclerViewFrag();
                Bundle bundle = new Bundle();
                bundle.putString("NOME_DA_PASTA", type_folder);
                recyclerViewFrag.setArguments(bundle);
                FragmentTransaction manager = getFragmentManager().beginTransaction();
                manager.replace(R.id.frameContainer, recyclerViewFrag).addToBackStack(null).commit();

            }
        });

        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (event != null && KeyEvent.KEYCODE_ENTER == event.getKeyCode() || actionId == EditorInfo.IME_ACTION_DONE) {
                    String newEntry = editText.getText().toString();
                    String resultado;
                    if (editText.length() != 0) {
                        resultado = dbHelper.AddData(newEntry, "menu");
                        Toast.makeText(getContext(), resultado, Toast.LENGTH_SHORT).show();
                        editText.setText("");
                        if (resultado.equals("Nome já existe")) {
                            return true;
                        } else {
                            listData.add(newEntry);
                            adapter.notifyDataSetChanged();
                            return true;
                        }
                    } else {
                        toastMenssage("Digite um nome");
                        return true;
                    }
                }
                return false;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_delete_folder) {
            String padrao = "menu";
            // Obtem os dados e anexar a uma lista
            Cursor data = dbHelper.getData(padrao);
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
                        if (!UserSelection.contains(listView.getItemAtPosition(position))) {
                            UserSelection.add((String) listView.getItemAtPosition(position));
                        }
                    } else {
                        UserSelection.remove(listView.getItemAtPosition(position));
                    }
                }
            });

            alertDialogBuilder.setCancelable(false);
            alertDialogBuilder.setPositiveButton("Deletar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Verifica se foi selecionado alguma checkbox
                    if (UserSelection.size() == 0) {
                        toastMenssage("Selecione uma pasta");
                    } else {
                        //percorre e deleta as pastas e os itens do banco de dados
                        for (int i = 0; i < UserSelection.size(); i++) {
                            dbHelper.deleteAll(UserSelection.get(i));
                            adapter.remove(UserSelection.get(i));
                            adapter.notifyDataSetChanged();
                        }
                        toastMenssage("Itens deletados");
                        UserSelection.clear();
                        adapter.notifyDataSetChanged();
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

        return super.onOptionsItemSelected(item);
    }

    // Aparece uma menssagem Toast
    public void toastMenssage(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
}
