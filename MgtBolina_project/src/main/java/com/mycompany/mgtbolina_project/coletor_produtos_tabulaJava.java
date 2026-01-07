package com.mycompany.mgtbolina_project;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import technology.tabula.ObjectExtractor;
import technology.tabula.Page;
import technology.tabula.RectangularTextContainer;
import technology.tabula.Table;
import technology.tabula.extractors.BasicExtractionAlgorithm;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.pdfbox.Loader;
import technology.tabula.extractors.SpreadsheetExtractionAlgorithm;

public class coletor_produtos_tabulaJava {
    
    public List<produto> extrairTabelaPDF(String caminhoPDF){
        List<produto> listaDeProdutos = new ArrayList<>();
        
        try(PDDocument doc = Loader.loadPDF(new File(caminhoPDF))){
            ObjectExtractor extractor = new ObjectExtractor(doc);
            Page pagina = extractor.extract(1);//foca na pagina 1 do pdf
            
            
            SpreadsheetExtractionAlgorithm algoritimo = new SpreadsheetExtractionAlgorithm();
            List<Table> tabelas = algoritimo.extract(pagina);
            
            for(Table tabela: tabelas){
                for(List<RectangularTextContainer> linha : tabela.getRows()){
                  
                    if (linha.size() > 7) {
                        String textoDescricao = linha.get(1).getText().trim();
                        String textoPreco = linha.get(7).getText().trim();

                        // Filtro: Se não for o título da coluna e não estiver vazio, adiciona
                        if (!textoDescricao.isEmpty() && !textoDescricao.contains("DESCRIÇÃO")) {
                            listaDeProdutos.add(new produto(textoDescricao, textoPreco));
                    }
                }
             
            }
                    
                    
        }
    } catch (Exception e) {
            System.out.println("Erro ao ler tabela: " + e.getMessage());
        }
        return listaDeProdutos;
    }
    
}
