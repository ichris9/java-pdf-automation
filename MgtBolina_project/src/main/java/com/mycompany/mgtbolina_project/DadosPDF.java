package com.mycompany.mgtbolina_project;

import java.util.List;

/**
 * Classe para armazenar os dados extraídos de um único PDF
 */
public class DadosPDF {
    private String nomePDF;
    private String numNota;
    private String valorTotal;
    private String data;
    private String fornecedor;
    private String unidade;
    private String valorUnitario;  // 👈 NOVO CAMPO
    private String placaVeiculo;
    private List<Produto> listaDeProdutos;
    
    public DadosPDF(String nomePDF) {
        this.nomePDF = nomePDF;
    }
    
    // Getters e Setters
    public String getNomePDF() {
        return nomePDF;
    }
    
    public void setNomePDF(String nomePDF) {
        this.nomePDF = nomePDF;
    }
    
    public String getNumNota() {
        return numNota;
    }
    
    public void setNumNota(String numNota) {
        this.numNota = numNota;
    }
    
    public String getValorTotal() {
        return valorTotal;
    }
    
    public void setValorTotal(String valorTotal) {
        this.valorTotal = valorTotal;
    }
    
    public String getData() {
        return data;
    }
    
    public void setData(String data) {
        this.data = data;
    }
    
    public String getPlacaVeiculo() {
        return placaVeiculo;
    }
    
    public void setPlacaVeiculo(String placaVeiculo) {
        this.placaVeiculo = placaVeiculo;
    }
    
    public String getFornecedor() {
        return fornecedor;
    }
    
    public void setFornecedor(String fornecedor) {
        this.fornecedor = fornecedor;
    }
    
    public String getUnidade() {
        return unidade;
    }
    
    public void setUnidade(String unidade) {
        this.unidade = unidade;
    }
    
    // 👇 NOVO GETTER E SETTER PARA VALOR UNITÁRIO
    public String getValorUnitario() {
        return valorUnitario;
    }
    
    public void setValorUnitario(String valorUnitario) {
        this.valorUnitario = valorUnitario;
    }
    
    
    
    public List<Produto> getListaDeProdutos() {
        return listaDeProdutos;
    }
    
    public void setListaDeProdutos(List<Produto> listaDeProdutos) {
        this.listaDeProdutos = listaDeProdutos;
    }
    
    @Override
    public String toString() {
        return "PDF: " + nomePDF + 
               " | Nota: " + (numNota != null ? numNota : "N/E") +
               " | Produtos: " + (listaDeProdutos != null ? listaDeProdutos.size() : 0);
    }
}