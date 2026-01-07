package com.mycompany.mgtbolina_project;

import java.io.FileOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

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
    
    public void ExportDataTOExcel(String filePath, String numNota, String valorTotal, String data, String placaVeic, String forn, List<produto> listaDeProdutos){
        
        
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
                
               for (produto p : listaDeProdutos) {
        int proximaLinhaVazia = sheet.getLastRowNum() + 1;
        Row row = sheet.createRow(proximaLinhaVazia);

        // Preenche os dados comuns (repetindo para cada produto)
        row.createCell(colNota).setCellValue(numNota);
        row.createCell(colForn).setCellValue(forn);
        row.createCell(colData).setCellValue(data);
        row.createCell(colPlacaVeiculo).setCellValue(placaVeic);

        // Preenche os dados que vieram do Tabula
        // Você precisará mapear onde quer colocar o Nome do Produto e Preço Unitário
        row.createCell(5).setCellValue(p.descricao); // Coluna F
        row.createCell(6).setCellValue(p.valorUnitario); // Coluna G
    }
            
        }catch(IOException e){
            System.out.println("IOException");
        }
    }
}
