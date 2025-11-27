package com.mycompany.mgtbolina_project;

import java.io.FileOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

//apachePOI
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class Exportador_Excel {
    
    public void ExportDataTOExcel(String filePath, String numNota, String valorTotal, String data, String placaVeic, String forn){
        
        
        try{
            FileInputStream LeituraByts = new FileInputStream(filePath);//pegas os bytes do arquivo
            XSSFWorkbook workbook = new XSSFWorkbook(LeituraByts);//faz a leitura dos bytes e faz um .xlsx manipulavel
            XSSFSheet sheet = workbook.getSheetAt(0);
            
            //ler cabeçalhos
            Row headers = sheet.getRow(0);
            
            int colNota = -1;
            int colTotal = -1;
            int colData = -1;
            int colPlacaVeiculo = -1;
            int colForn = -1;
            
            for(Cell cell: headers){
                String nome = cell.getStringCellValue().trim();
                
                if(nome.equalsIgnoreCase("NOTA")){
                    colNota = cell.getColumnIndex();
                }
                
                if(nome.equalsIgnoreCase("VALOR")){
                    colTotal = cell.getColumnIndex();
                }
                
                if(nome.equalsIgnoreCase("FORNECEDOR")){
                    colForn = cell.getColumnIndex();
                }
                
                if(nome.equalsIgnoreCase("PLACA")){
                    colPlacaVeiculo = cell.getColumnIndex();
                }
                
                if(nome.equalsIgnoreCase("DATA")){
                    colData = cell.getColumnIndex();
                }
            }
            if (colNota == -1 || colTotal == -1) {
            throw new RuntimeException("Não foi encontrado a coluna 'NOTA' ou 'VALOR' no cabeçalho");
            }
                //#escrita
                
                int lastRow = sheet.getLastRowNum()+1;
                Row newRow = sheet.createRow(lastRow);
                
                newRow.createCell(colNota).setCellValue(numNota);
                newRow.createCell(colTotal).setCellValue(valorTotal);
                newRow.createCell(colData).setCellValue(data);
                newRow.createCell(colPlacaVeiculo).setCellValue(placaVeic);
                newRow.createCell(colForn).setCellValue(forn);
                
                //escrita
                LeituraByts.close();
                
                FileOutputStream saidaBytes = new FileOutputStream(filePath);
                
                workbook.write(saidaBytes);
                
                saidaBytes.close();
                
                workbook.close();
        
            
        }catch(IOException e){
            System.out.println("IOException");
        }
    }
}
