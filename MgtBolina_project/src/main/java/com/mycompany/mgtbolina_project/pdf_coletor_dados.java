package com.mycompany.mgtbolina_project;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class pdf_coletor_dados {
    
    //método auxiliar de busca para usar o princípio Dont-Repeat-Yourself
    private String FindFistGroup(String text, String regex){
        // Flags: CASE_INSENSITIVE (ignora maiúsculas/minúsculas) e DOTALL (inclui quebras de linha no '.')
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE );
        Matcher matcher = pattern.matcher(text);
        
        if(matcher.find()){
            //retorna o primeiro grupo de captura
            return matcher.group(1).trim();
        }
        return "Não Encontrada (N/E) ou Vazio";//campo não encontrado
    }
    
    
    
    public String ExtractDanfeNumber(String textoCompleto){
       String regex = "[1\\s\\-\\s SAÍDA\\n]([\\d]{6,6})";
       
       return FindFistGroup(textoCompleto, regex);
    }
    
    public String ExtractTotalNumber(String textoCompleto){
        String regex = "VALOR TOTAL DOS PRODUTOS[\\s+\\n+]+([\\d\\.,]+)";
        
        return FindFistGroup(textoCompleto, regex);
    }
    
    public String ExtracPlacaVeiculo(String textoCompleto){
        String regex = "PLACA DO VEÍCULO[\\s+\\n]+([A-Z0-9]{7,7})";
        
        return FindFistGroup(textoCompleto, regex);
    }
}
