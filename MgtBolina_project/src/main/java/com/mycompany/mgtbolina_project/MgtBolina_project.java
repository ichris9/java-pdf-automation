package com.mycompany.mgtbolina_project;

import java.io.File;
import java.util.List;

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
           
           List<String[]> itemsExtraidos = coletor.extractProductItens(textoBruto);
           System.out.println("==========================");
           System.out.println(">>> DADOS DA NOTA <<<");
            System.out.println("==========================");
           System.out.println("Numero da Nota: " + numNota);
          // System.out.println("Placa veiculo: " + placaVeiclo);
           System.out.println("Total da nota: "+ numTotal);
           
           //impressão tabela de itens
           
           if(!itemsExtraidos.isEmpty()){
               System.out.println("----------------------------------------------------------------------------------");
               System.out.printf("%-40s | %-12s | %s\n", "DESCRIÇÃO", "V. UNITÁRIO", "V. TOTAL");
               System.out.println("----------------------------------------------------------------------------------");
               
               for(String[] item : itemsExtraidos){
                   System.out.printf("%-40s | %-12s | %s\n", item[0], item[1], item[2]);
               }
                System.out.println("----------------------------------------------------------------------------------");
                
           }
           else{
               System.out.println("erro no regex");
           }
              
           //exportar para excel
           Exportador_Excel exporter = new Exportador_Excel();
           
           String excelFiePath = "C:\\Users\\chris\\OneDrive\\Documents\\Teste1-pdf.xlsx";
           
           exporter.ExportDataTOExcel(excelFiePath, numNota, numTotal);
        
       }
    }
}
