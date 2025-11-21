package com.mycompany.mgtbolina_project;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class pdf_coletor_dados {
    
    //método auxiliar de busca para usar o princípio Dont-Repeat-Yourself
    private String FindFistGroup(String text, String regex){
        // Flags: CASE_INSENSITIVE (ignora maiúsculas/minúsculas) e DOTALL (inclui quebras de linha no '.')
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher matcher = pattern.matcher(text);
        
        if(matcher.find()){
            //retorna o primeiro grupo de captura
            return matcher.group(1).trim();
        }
        return "Não Aplicavel (N/A)";//campo não encontrado
    }
    
    
    
    public String ExtractDanfeNumber(String texto){
       
    }
}
