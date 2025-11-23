package com.mycompany.mgtbolina_project;

import java.io.FileOutputStream;
import java.io.File;

//apachePOI
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

public class Exportador_Excel {
    
    public void ExportDataTOExcel(String filePath, String numNota, String valorTotal){
        
        //cria aba
        try(Workbook workbook = WorkbookFactory.create(new File (filePath))){
            
            Sheet sheet = workbook.getSheetAt(0);//pega a primeira aba
            
            Row rowNota = sheet.getRow(1);
            if(rowNota == null) {
                System.err.println("Erro: A linha de destino (Linha 2) não existe no template. A formatação será perdida.");
                rowNota = sheet.createRow(1);
            }
            
            // --- 2. RECUPERA CÉLULAS E ESTILOS (A2 e B2) ---
            
            // Célula A2 (Índice 0)
            Cell cell0 = rowNota.getCell(0);
            // Se a célula A2 não existe, a formatação já está em risco.
            if(cell0 == null) {
                cell0 = rowNota.createCell(0);
            }
            // AQUI OCORRE O PREENCHIMENTO: Apenas altera o VALOR
            cell0.setCellValue(numNota);
            
            // Célula B2 (Índice 1)
            Cell cell1 = rowNota.getCell(1);
            if(cell1 == null) {
                cell1 = rowNota.createCell(1);
            }
            cell1.setCellValue(valorTotal);
            
            // --- 3. SALVA O ARQUIVO ---
            try(FileOutputStream out = new FileOutputStream(filePath)){
                workbook.write(out);
                System.out.println("✅ Sucesso: Dados exportados, checar formatação em: " + filePath);
            }
            
        }catch(Exception e){
                System.err.println("❌ Erro ao manipular o Excel: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
