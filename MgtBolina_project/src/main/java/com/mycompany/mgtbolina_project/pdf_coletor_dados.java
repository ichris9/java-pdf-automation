package com.mycompany.mgtbolina_project;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList; 
import java.util.List;

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
       String regex = "[N \\n\\s+]([0-9\\.0-9]{7,8})";
       
       return FindFistGroup(textoCompleto, regex);
    }
    
    public String ExtractTotalNumber(String textoCompleto){
        String regex = "(?si)VALOR TOTAL DOS PRODUTOS.*?((?:\\d{1,3}(?:\\.\\d{3})*|\\d+),\\d{2})";
        
        return FindFistGroup(textoCompleto, regex);
    }
    
    public String ExtracPlacaVeiculo(String textoCompleto){
        String regex = "(?si)PLACA DO VEÍCULO.*?((?:[A-Z]{3}\\d{4})|(?:[A-Z]{3}\\d[A-Z]\\d{2}))";
        
        return FindFistGroup(textoCompleto, regex);
    }
    
    public String ExtractRazaoSocial(String textoCompleto){
        String regex ="(?si)(?:DESTINAT[ÁA]RIO\\/REMETENTE)\n.*?(?:NOME RAZ[ÃA]O SOCIAL)\\n.*?([ A-Z ]+?(?:LTDA)+?)";

        return FindFistGroup(textoCompleto, regex);
    }
    
    public String ExtractDate(String TextCompleto){
        String regex ="(?si)(?:DATA DE EMISSÃO|DATA DE SA[ÍI]DA\\/ENTRADA).*?([\\d]{2}\\/[\\d]{2}\\/[\\d]{4})";
        
        return FindFistGroup(TextCompleto, regex);
    }
    
    public String ExtractObra(String textoCompleto){
        String regex = "";
        
        return FindFistGroup(textoCompleto, regex);
    }
    
    
    
    

    
}
