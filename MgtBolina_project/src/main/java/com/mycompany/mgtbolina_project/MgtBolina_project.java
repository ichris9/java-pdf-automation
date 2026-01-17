package com.mycompany.mgtbolina_project;

import java.io.File;
import java.util.List;

public class MgtBolina_project {
    public static void main(String[] args) {
       File pdfFile = new File("C:\\Users\\chris\\Downloads\\danfe - 858111.PDF");
        
       //lê o pdf
       Pdf_Leitor leitor = new Pdf_Leitor();
       
       //pega todo o texto o pdf
       String textoBruto = leitor.ExtractText(pdfFile.getAbsolutePath());
       
       coletor_produtos_tabulaJava coletorTabula = new coletor_produtos_tabulaJava();
       List<produto> listaDeProdutos = coletorTabula.extrairTabelaPDF(pdfFile.getAbsolutePath());
        
       
       if(textoBruto != null){
         
           pdf_coletor_dados coletor = new pdf_coletor_dados();
           
           //texto bruto para extrair dados com regex
           String numNota = coletor.ExtractDanfeNumber(textoBruto);
           String numTotal = coletor.ExtractTotalNumber(textoBruto);
           String placaVeiculo = coletor.ExtracPlacaVeiculo(textoBruto);
           String razaoSocial = coletor.ExtractRazaoSocial(textoBruto);
           String data = coletor.ExtractDate(textoBruto);
           //String obra = coletor.ExtractObra(textoBruto);
           
          System.out.println("==========================");
           System.out.println(">>> DADOS DA NOTA <<<");
           System.out.println("==========================");
           System.out.println("Numero da Nota: " + numNota);
           System.out.println("Placa veiculo: " + placaVeiculo);
           System.out.println("Total da nota: "+ numTotal);
           System.out.println("Fornecedor: " + razaoSocial);
           System.out.println("Data: " + data);
           System.out.println("teste tabela:" + listaDeProdutos);
          
          System.out.print(textoBruto);
           
          
              
           //exportar para excel
           Exportador_Excel exporter = new Exportador_Excel();
           
           String excelFilePath = "C:\\Users\\chris\\OneDrive\\Documents\\teste1.xlsx";
           
           exporter.ExportDataTOExcel(excelFilePath, numNota, numTotal,data,placaVeiculo,razaoSocial,listaDeProdutos);
        
       }
    }
}
