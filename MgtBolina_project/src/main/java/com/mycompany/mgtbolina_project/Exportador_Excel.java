package com.mycompany.mgtbolina_project;

import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class Exportador_Excel {
    
    public void ExportDataTOExcel(String filePath, String numNota, String valorTotal, 
                                   String data, String placaVeic, String forn, 
                                   List<produto> listaDeProdutos) {
        
        FileInputStream leituraBytes = null;
        FileOutputStream outputStream = null;
        
        try {
            leituraBytes = new FileInputStream(filePath);
            XSSFWorkbook workbook = new XSSFWorkbook(leituraBytes);
            XSSFSheet sheet = workbook.getSheetAt(0);
            
            // Ler cabeçalhos
            Row headers = sheet.getRow(0);
            
            if (headers == null) {
                throw new RuntimeException("Planilha não possui cabeçalho na primeira linha!");
            }
            
            int colNota = -1;
            int colTotal = -1;
            int colData = -1;
            int colPlacaVeiculo = -1;
            int colForn = -1;
            int colDescricao = -1;
            int colValorUnit = -1;
            
            // Identifica as colunas pelo nome do cabeçalho
            for (Cell cell : headers) {
                String nome = cell.getStringCellValue().trim().toUpperCase();
                
                if (nome.contains("NOTA")) {
                    colNota = cell.getColumnIndex();
                }
                if (nome.contains("VALOR") && (nome.contains("TOTAL") || !nome.contains("UNIT"))) {
                    colTotal = cell.getColumnIndex();
                }
                if (nome.contains("FORNECEDOR") || nome.contains("RAZAO")) {
                    colForn = cell.getColumnIndex();
                }
                if (nome.contains("PLACA")) {
                    colPlacaVeiculo = cell.getColumnIndex();
                }
                if (nome.contains("DATA")) {
                    colData = cell.getColumnIndex();
                }
                if (nome.contains("DESCRI") || nome.contains("PRODUTO")) {
                    colDescricao = cell.getColumnIndex();
                }
                if (nome.contains("UNIT")) {
                    colValorUnit = cell.getColumnIndex();
                }
            }
            
            // Valida se encontrou as colunas essenciais
            if (colNota == -1) {
                System.err.println("AVISO: Coluna 'NOTA' não encontrada!");
            }
            if (colTotal == -1) {
                System.err.println("AVISO: Coluna 'VALOR' ou 'TOTAL' não encontrada!");
            }
            
            System.out.println("Colunas identificadas:");
            System.out.println("  Nota: " + colNota);
            System.out.println("  Total: " + colTotal);
            System.out.println("  Fornecedor: " + colForn);
            System.out.println("  Data: " + colData);
            System.out.println("  Placa: " + colPlacaVeiculo);
            System.out.println("  Descrição: " + colDescricao);
            System.out.println("  Valor Unitário: " + colValorUnit);
            
            // Escreve os dados
            int linhasAdicionadas = 0;
            for (produto p : listaDeProdutos) {
                int proximaLinhaVazia = sheet.getLastRowNum() + 1;
                Row row = sheet.createRow(proximaLinhaVazia);
                
                // Preenche os dados comuns (repetindo para cada produto)
                if (colNota != -1) {
                    row.createCell(colNota).setCellValue(numNota);
                }
                if (colForn != -1) {
                    row.createCell(colForn).setCellValue(forn);
                }
                if (colData != -1) {
                    row.createCell(colData).setCellValue(data);
                }
                if (colPlacaVeiculo != -1) {
                    row.createCell(colPlacaVeiculo).setCellValue(placaVeic);
                }
                if (colTotal != -1) {
                    row.createCell(colTotal).setCellValue(valorTotal);
                }
                
                // Preenche descrição e valor do produto
                if (colDescricao != -1) {
                    row.createCell(colDescricao).setCellValue(p.descricao);
                } else {
                    // Se não achou a coluna, usa índice fixo (coluna F = 5)
                    row.createCell(5).setCellValue(p.descricao);
                }
                
                if (colValorUnit != -1) {
                    row.createCell(colValorUnit).setCellValue(p.valorUnitario);
                } else {
                    // Se não achou a coluna, usa índice fixo (coluna G = 6)
                    row.createCell(6).setCellValue(p.valorUnitario);
                }
                
                linhasAdicionadas++;
            }
            
            // IMPORTANTE: Salvar o arquivo!
            outputStream = new FileOutputStream(filePath);
            workbook.write(outputStream);
            
            System.out.println("✓ Sucesso! " + linhasAdicionadas + " produto(s) exportado(s) para: " + filePath);
            
            // Fechar recursos
            workbook.close();
            
        } catch (IOException e) {
            System.err.println("Erro ao exportar para Excel: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Garante que os streams sejam fechados
            try {
                if (leituraBytes != null) {
                    leituraBytes.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
                System.err.println("Erro ao fechar arquivos: " + e.getMessage());
            }
        }
    }
}