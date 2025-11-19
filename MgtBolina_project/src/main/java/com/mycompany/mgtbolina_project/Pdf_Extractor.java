package com.mycompany.mgtbolina_project;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.Loader;
import java.io.File;
import java.io.IOException;

public class Pdf_Extractor {
    
    public String ExtractText(String filePath){
         //PDD.load serve para abrir o arquivo
         try(PDDocument document = Loader.loadPDF(new File(filePath))){
             
             //pdf Striper é a classe que faz a estração do texto
             PDFTextStripper stripper = new PDFTextStripper();
             
             return stripper.getText(document);
             
         }catch(IOException e){
             System.err.println("erro ao extrair texto do pdf: "+e.getMessage());
             return null;
         }
    }
}
