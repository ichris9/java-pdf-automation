package com.mycompany.mgtbolina_project;

import java.io.File;

public class MgtBolina_project {
    public static void main(String[] args) {
       File pdfFile = new File("C:\\Users\\chris\\Downloads\\notafiscal-notebook.pdf");
        
       //lê o pdf
       Pdf_Leitor leitor = new Pdf_Leitor();
       
       //pega todo o texto o pdf
       String textoBruto = leitor.ExtractText(pdfFile.getPath());
        
       
       if(textoBruto != null){
         
           pdf_coletor_dados coletor = new pdf_coletor_dados();
           
           //texto brto para extrair dados com regex
           String numNota = coletor.ExtractDanfeNumber(textoBruto);
           String numTotal= coletor.ExtractTotalNumber(textoBruto);
           String placaVeiclo = coletor.ExtracPlacaVeiculo(textoBruto);
           
           System.out.println(">>>DADOS DA NOTA<<<");
           System.out.println("Numero da Nota: " + numNota);
          // System.out.println("Placa veiculo: " + placaVeiclo);
           System.out.println("Total da nota: "+ numTotal);
       }

    }
}
