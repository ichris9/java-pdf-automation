package com.mycompany.mgtbolina_project;

import technology.tabula.ObjectExtractor;
import technology.tabula.Page;
import technology.tabula.PageIterator;
import technology.tabula.Table;
import technology.tabula.extractors.SpreadsheetExtractionAlgorithm; 

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.Loader;
import java.io.File;
import java.util.List;
import technology.tabula.RectangularTextContainer;

public class PdfLeitor {
    
    public String ExtractText(String filePath){
        StringBuilder textoCompleto = new StringBuilder();
        
         //PDD.load serve para abrir o arquivo
         try(PDDocument document = Loader.loadPDF(new File(filePath))){
             ObjectExtractor extractor = new ObjectExtractor(document);
             
             //percorre todas as páginas
             PageIterator pageInterator = extractor.extract();
             
             //algoritmo de extração
             SpreadsheetExtractionAlgorithm algoritmnh = new SpreadsheetExtractionAlgorithm();
             
             while(pageInterator.hasNext()){
                 Page page = pageInterator.next();
                 
                 List<Table> tables = algoritmnh.extract(page);
                 
                 for(Table table: tables){
                     //transfroma a tabela em string
                     for (List<RectangularTextContainer> row : table.getRows()) {
                         for (RectangularTextContainer cell : row) {
                            // Pega o texto da célula e adiciona um separador (tabulação)
                                textoCompleto.append(cell.getText()).append(" | ");
                        }
                        textoCompleto.append("\n"); // Pula linha ao fim de cada linha da tabela
                    }
                 }
             }
             return textoCompleto.toString();
             
             
         } catch(Exception e){
             System.err.println("Erro ao usar tabula" + e.getMessage());
             return null;
         }
         
         
    }
    
    
}
