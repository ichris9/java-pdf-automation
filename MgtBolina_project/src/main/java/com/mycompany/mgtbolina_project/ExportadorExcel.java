package com.mycompany.mgtbolina_project;

import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExportadorExcel {
    
    // 👇 CONSTANTE: Linha onde ficam os cabeçalhos (linha 2 = índice 1)
    private static final int LINHA_HEADERS = 1;
    
    /**
     * Lista todas as abas (sheets) disponíveis no arquivo Excel
     * @param filePath Caminho do arquivo Excel
     * @return Lista com os nomes de todas as abas
     */
    public List<String> listarAbas(String filePath) {
        List<String> nomesAbas = new ArrayList<>();
        FileInputStream leituraBytes = null;
        
        try {
            leituraBytes = new FileInputStream(filePath);
            XSSFWorkbook workbook = new XSSFWorkbook(leituraBytes);
            
            int totalAbas = workbook.getNumberOfSheets();
            System.out.println("\n📊 Total de abas encontradas: " + totalAbas);
            
            for (int i = 0; i < totalAbas; i++) {
                String nomeAba = workbook.getSheetName(i);
                nomesAbas.add(nomeAba);
                System.out.println("  [" + (i + 1) + "] " + nomeAba);
            }
            
            workbook.close();
            
        } catch (IOException e) {
            System.err.println("❌ Erro ao listar abas: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (leituraBytes != null) {
                    leituraBytes.close();
                }
            } catch (IOException e) {
                System.err.println("Erro ao fechar arquivo: " + e.getMessage());
            }
        }
        
        return nomesAbas;
    }
    
    /**
     * Exporta dados para uma aba específica do Excel
     * @param filePath Caminho do arquivo Excel
     * @param nomeAba Nome da aba onde os dados serão inseridos
     * @param numNota Número da nota fiscal
     * @param valorTotal Valor total da nota
     * @param data Data da nota
     * @param placaVeic Placa do veículo
     * @param forn Fornecedor
     * @param unidade Unidade dos produtos (TON, KG, etc)
     * @param valorUnitario Valor unitário do produto
     * @param listaDeProdutos Lista de produtos a serem exportados
     */
    public void ExportDataTOExcel(String filePath, String nomeAba, String numNota, 
                                   String valorTotal, String data, String placaVeic, 
                                   String forn, String unidade, String valorUnitario,
                                   List<Produto> listaDeProdutos) {
        
        FileInputStream leituraBytes = null;
        FileOutputStream outputStream = null;
        
        try {
            leituraBytes = new FileInputStream(filePath);
            XSSFWorkbook workbook = new XSSFWorkbook(leituraBytes);
            
            // Busca a aba pelo nome
            XSSFSheet sheet = workbook.getSheet(nomeAba);
            
            if (sheet == null) {
                System.err.println("❌ ERRO: Aba '" + nomeAba + "' não encontrada!");
                System.err.println("💡 Abas disponíveis:");
                for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                    System.err.println("   - " + workbook.getSheetName(i));
                }
                workbook.close();
                return;
            }
            
            System.out.println("\n✅ Exportando para a aba: " + nomeAba);
            
            // Processa a exportação
            processarExportacao(sheet, numNota, valorTotal, data, placaVeic, forn, unidade, valorUnitario, listaDeProdutos);
            
            // Salvar o arquivo
            outputStream = new FileOutputStream(filePath);
            workbook.write(outputStream);
            
            System.out.println("✅ Dados exportados com sucesso para a aba '" + nomeAba + "'!");
            
            workbook.close();
            
        } catch (IOException e) {
            System.err.println("❌ Erro ao exportar para Excel: " + e.getMessage());
            e.printStackTrace();
        } finally {
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
    
    /**
     * Método legado - usa a primeira aba por padrão (mantido para compatibilidade)
     */
    public void ExportDataTOExcel(String filePath, String numNota, String valorTotal, 
                                   String data, String placaVeic, String forn, 
                                   List<Produto> listaDeProdutos) {
        
        FileInputStream leituraBytes = null;
        FileOutputStream outputStream = null;
        
        try {
            leituraBytes = new FileInputStream(filePath);
            XSSFWorkbook workbook = new XSSFWorkbook(leituraBytes);
            XSSFSheet sheet = workbook.getSheetAt(0);
            
            System.out.println("\n⚠️ Usando primeira aba por padrão: " + workbook.getSheetName(0));
            
            // Pega unidade e valor unitário do primeiro produto (para compatibilidade)
            String unidade = "";
            String valorUnitario = "";
            if (listaDeProdutos != null && !listaDeProdutos.isEmpty()) {
                Produto primeiro = listaDeProdutos.get(0);
                unidade = primeiro.unidade != null ? primeiro.unidade : "";
                valorUnitario = primeiro.valorUnitario != null ? primeiro.valorUnitario : "";
            }
            
            // Processa a exportação
            processarExportacao(sheet, numNota, valorTotal, data, placaVeic, forn, unidade, valorUnitario, listaDeProdutos);
            
            // Salvar o arquivo
            outputStream = new FileOutputStream(filePath);
            workbook.write(outputStream);
            
            System.out.println("✅ Dados exportados com sucesso!");
            
            workbook.close();
            
        } catch (IOException e) {
            System.err.println("Erro ao exportar para Excel: " + e.getMessage());
            e.printStackTrace();
        } finally {
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
    
    /**
     * Processa a exportação dos dados para a sheet
     * @param sheet A planilha onde os dados serão inseridos
     */
    private void processarExportacao(XSSFSheet sheet, String numNota, String valorTotal, 
                                     String data, String placaVeic, String forn, 
                                     String unidade, String valorUnitario,
                                     List<Produto> listaDeProdutos) {
        
        // 👇 MUDANÇA: Ler cabeçalhos na LINHA 2 (índice 1) em vez da linha 1 (índice 0)
        Row headers = sheet.getRow(LINHA_HEADERS);
        
        if (headers == null) {
            throw new RuntimeException("Planilha não possui cabeçalho na linha " + (LINHA_HEADERS + 1) + "!");
        }
        
        // Debug: mostra qual linha está sendo usada
        System.out.println("📋 Lendo cabeçalhos da linha " + (LINHA_HEADERS + 1) + " (índice " + LINHA_HEADERS + ")");
        
        int colNota = -1;
        int colTotal = -1;
        int colData = -1;
        int colPlacaVeiculo = -1;
        int colForn = -1;
        int colDescricao = -1;
        int colValorUnit = -1;
        int colUnidade = -1;
        
        // Identifica as colunas pelo nome do cabeçalho
        for (Cell cell : headers) {
            String nome = cell.getStringCellValue().trim().toUpperCase();
            
            if (nome.contains("NF")) {
                colNota = cell.getColumnIndex();
            }
            if (nome.contains("VLR TOTAL") && (nome.contains("TOTAL") || !nome.contains("UNIT"))) {
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
            if (nome.contains("DESCRIÇÃO") || nome.contains("PRODUTO")) {
                colDescricao = cell.getColumnIndex();
            }
            if (nome.contains("VLR UNITÁRIO")) {
                colValorUnit = cell.getColumnIndex();
            }
            if (nome.contains("UNID")) {
                colUnidade = cell.getColumnIndex();
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
        System.out.println("  Nota: " + (colNota >= 0 ? colNota : "NÃO ENCONTRADA"));
        System.out.println("  Total: " + (colTotal >= 0 ? colTotal : "NÃO ENCONTRADA"));
        System.out.println("  Fornecedor: " + (colForn >= 0 ? colForn : "NÃO ENCONTRADA"));
        System.out.println("  Data: " + (colData >= 0 ? colData : "NÃO ENCONTRADA"));
        System.out.println("  Placa: " + (colPlacaVeiculo >= 0 ? colPlacaVeiculo : "NÃO ENCONTRADA"));
        System.out.println("  Descrição: " + (colDescricao >= 0 ? colDescricao : "NÃO ENCONTRADA"));
        System.out.println("  Valor Unitário: " + (colValorUnit >= 0 ? colValorUnit : "NÃO ENCONTRADA"));
        System.out.println("  Unidade: " + (colUnidade >= 0 ? colUnidade : "NÃO ENCONTRADA"));
        
        // 👇 MUDANÇA: Adiciona dados DEPOIS da linha de headers
        // Procura a primeira linha vazia após os headers
        int proximaLinhaVazia = LINHA_HEADERS + 1; // Começa na linha após headers
        
        // Encontra a última linha com dados (se houver)
        int ultimaLinhaComDados = sheet.getLastRowNum();
        if (ultimaLinhaComDados > LINHA_HEADERS) {
            proximaLinhaVazia = ultimaLinhaComDados + 1;
        }
        
        System.out.println("📝 Adicionando dados a partir da linha " + (proximaLinhaVazia + 1));
        
        // Escreve os dados
        int linhasAdicionadas = 0;
        for (Produto p : listaDeProdutos) {
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
            
            // Usa o valor unitário do parâmetro (editável na interface)
            if (colValorUnit != -1) {
                row.createCell(colValorUnit).setCellValue(valorUnitario != null ? valorUnitario : "");
            } else {
                // Se não achou a coluna, usa índice fixo (coluna G = 6)
                row.createCell(6).setCellValue(valorUnitario != null ? valorUnitario : "");
            }
            
            // Usa a unidade do parâmetro (editável na interface)
            if (colUnidade != -1) {
                row.createCell(colUnidade).setCellValue(unidade != null ? unidade : "");
            }
            
            proximaLinhaVazia++;
            linhasAdicionadas++;
        }
        
        System.out.println("✓ " + linhasAdicionadas + " produto(s) adicionado(s)");
    }
}