package com.cgm.checklist.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/** Comando do banco de dados sql
 */
public class DBHelper extends SQLiteOpenHelper {

    public static final String TABLE_NAME = "table_name";
    private static final String COL1 = "ID";
    public static final String NAME_ITEM = "name";
    public static final String TYPE_FOLDER = "type_folder";
    public static final String STATUS = "is_checked";

    // Construtor
    public DBHelper(Context context) {
        super(context, TABLE_NAME, null, 1);
    }

    // Cria a coluna do banco de dados
    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " ("
                + COL1 + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + NAME_ITEM + " TEXT UNIQUE,"
                + TYPE_FOLDER + " TEXT, "
                + STATUS + " INTEGER"
                + ")";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    // Adiciona o item no banco de dados
    public String AddData(String item, String type) {
        ContentValues valores = new ContentValues();
        long resultado;

        SQLiteDatabase db = this.getWritableDatabase();
        valores.put(NAME_ITEM, item);
        valores.put(TYPE_FOLDER, type);
        valores.put(STATUS, 0);

        resultado = db.insert(TABLE_NAME, null, valores);
        db.close();

        if (resultado == -1) {
            return "Nome já existe";
        } else {
            return "Item adicionado em: " + type;
        }
    }

    // Mostra todos os itens do banco de dados
    public Cursor getData(String type) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + TYPE_FOLDER + " = '" + type + "'";
        Cursor data = db.rawQuery(query, null);
        return data;
    }


//    public Cursor getItemChecked(String name) {
//        SQLiteDatabase db = this.getWritableDatabase();
//        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + NAME_ITEM + " = '" + name + "'";
//        Cursor data = db.rawQuery(query, null);
//        return data;
//    }

    // WHERE é o meu filtro, no caso usar TYPE_FOLDER se possível
//     + " WHERE " + TYPE_FOLDER + " like '" + "casa%'"
//     + " WHERE " + TYPE_FOLDER + " like '" + test +"%'"

    // Atualiza o status do item
    public void updateStatus(int status, String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "UPDATE " + TABLE_NAME + " SET " + STATUS + " = '" + status +
                "' WHERE " + NAME_ITEM + " = '" + name + "'";
        db.execSQL(query);
    }

    // Atualiza o nome do item dentro da pasta
//    public void updateItem(String newName, int id, String oldName) {
//        SQLiteDatabase db = this.getWritableDatabase();
//        String query = "UPDATE " + TABLE_NAME + " SET " + NAME_ITEM + " = '" + newName + "' WHERE " + COL1 + " = '" + id + "'" + " AND " + NAME_ITEM + " = '" + oldName + "'";
//        db.execSQL(query);
//    }

    // Deletar os itens JAH dentro da pasta
    public void deleteItems(String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "DELETE FROM " + TABLE_NAME + " WHERE " + NAME_ITEM + " = '" + name + "'";
        db.execSQL(query);
    }

    // Deleta a(s) pasta(s) seleciona(s) e os itens dentro dela(s)
    public void deleteAll(String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "DELETE FROM " + TABLE_NAME + " WHERE " + NAME_ITEM + " = '" + name + "'";
        db.execSQL(query);
        String query2 = "DELETE FROM " + TABLE_NAME + " WHERE " + TYPE_FOLDER + " = '" + name + "'";
        db.execSQL(query2);
    }
}
