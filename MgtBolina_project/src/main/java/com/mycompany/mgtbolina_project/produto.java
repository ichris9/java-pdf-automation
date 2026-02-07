 package com.mycompany.mgtbolina_project;

public class Produto {
    public String descricao;      // Descrição do produto
    public String valorUnitario;  // Valor unitário
    public String unidade;        // Unidade (TON, KG, UN, M3, etc)

    // Construtor com 3 parâmetros (NOVO)
    public Produto(String descricao, String valorUnitario, String unidade) {
        this.descricao = descricao;
        this.valorUnitario = valorUnitario;
        this.unidade = unidade != null && !unidade.isEmpty() ? unidade : "UN";
    }
    
    // Construtor legado com 2 parâmetros (mantido para compatibilidade)
    public Produto(String descricao, String valorUnitario) {
        this.descricao = descricao;
        this.valorUnitario = valorUnitario;
        this.unidade = "UN"; // Padrão: UN (unidade)
    }

    @Override
    public String toString() {
        return "\ndescricao: " + descricao + 
               "\nvalorUnitario: " + valorUnitario +
               "\nunidade: " + unidade;
    }
}

