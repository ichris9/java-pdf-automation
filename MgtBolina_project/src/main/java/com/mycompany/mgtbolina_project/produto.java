package com.mycompany.mgtbolina_project;
public class produto {
    public String descricao; // Deixando public para facilitar seu acesso direto
    public String valorUnitario;

    public produto(String descricao, String valorUnitario) {
        this.descricao = descricao;
        this.valorUnitario = valorUnitario;
    }

    @Override
    public String toString() {
        return "\ndescricao:" + descricao + "\nvalorUnitario: " + valorUnitario;
    }
    
}

