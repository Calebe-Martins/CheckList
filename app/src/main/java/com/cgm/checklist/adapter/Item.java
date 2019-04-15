package com.cgm.checklist.adapter;

public class Item {

    private String nome;
    private int position;
    private boolean checked;

    public boolean getChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public Item(String nome) {
        this.nome = nome;
    }

    public String getNome() {
        return nome;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
