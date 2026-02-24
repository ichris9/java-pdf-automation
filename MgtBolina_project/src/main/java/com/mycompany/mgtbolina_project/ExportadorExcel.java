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

    // Linha onde ficam os cabeçalhos (linha 2 = índice 1)
    private static final int LINHA_HEADERS = 1;

    /**
     * Lista todas as abas (sheets) disponíveis no arquivo Excel
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
                if (leituraBytes != null) leituraBytes.close();
            } catch (IOException e) {
                System.err.println("Erro ao fechar arquivo: " + e.getMessage());
            }
        }

        return nomesAbas;
    }

    /**
     * Exporta dados para uma aba específica do Excel.
     * Cada produto tem seus próprios valorUnitario, unidade e quantidade.
     */
    public void ExportDataTOExcel(String filePath, String nomeAba, String numNota,
                                   String data, String placaVeic,
                                   String forn, List<Produto> listaDeProdutos) {

        FileInputStream leituraBytes = null;
        FileOutputStream outputStream = null;

        try {
            leituraBytes = new FileInputStream(filePath);
            XSSFWorkbook workbook = new XSSFWorkbook(leituraBytes);

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

            processarExportacao(sheet, numNota, data, placaVeic, forn, listaDeProdutos);

            outputStream = new FileOutputStream(filePath);
            workbook.write(outputStream);

            System.out.println("✅ Dados exportados com sucesso para a aba '" + nomeAba + "'!");

            workbook.close();

        } catch (IOException e) {
            System.err.println("❌ Erro ao exportar para Excel: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (leituraBytes != null) leituraBytes.close();
                if (outputStream != null) outputStream.close();
            } catch (IOException e) {
                System.err.println("Erro ao fechar arquivos: " + e.getMessage());
            }
        }
    }

    /**
     * Processa a exportação dos dados para a sheet.
     * Cada produto escreve seu próprio valorUnitario, unidade e quantidade.
     */
    private void processarExportacao(XSSFSheet sheet, String numNota,
                                     String data, String placaVeic, String forn,
                                     List<Produto> listaDeProdutos) {

        Row headers = sheet.getRow(LINHA_HEADERS);

        if (headers == null) {
            throw new RuntimeException("Planilha não possui cabeçalho na linha " + (LINHA_HEADERS + 1) + "!");
        }

        System.out.println("📋 Lendo cabeçalhos da linha " + (LINHA_HEADERS + 1) + " (índice " + LINHA_HEADERS + ")");

        int colNota = -1;
        int colData = -1;
        int colPlacaVeiculo = -1;
        int colForn = -1;
        int colDescricao = -1;
        int colValorUnit = -1;
        int colUnidade = -1;
        int colQuantidade = -1;

        for (Cell cell : headers) {
            String nome = cell.getStringCellValue().trim().toUpperCase();

            if (nome.contains("NF")) {
                colNota = cell.getColumnIndex();
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
            if (nome.contains("VLR UNITÁRIO") || nome.contains("VALOR UNIT")) {
                colValorUnit = cell.getColumnIndex();
            }
            if (nome.contains("UNID")) {
                colUnidade = cell.getColumnIndex();
            }
            if (nome.contains("QUANT") || nome.contains("QUANTIDADE")) {
                colQuantidade = cell.getColumnIndex();
            }
        }

        if (colNota == -1) System.err.println("AVISO: Coluna 'NF/NOTA' não encontrada!");

        System.out.println("Colunas identificadas:");
        System.out.println("  Nota: " + (colNota >= 0 ? colNota : "NÃO ENCONTRADA"));
        System.out.println("  Fornecedor: " + (colForn >= 0 ? colForn : "NÃO ENCONTRADA"));
        System.out.println("  Data: " + (colData >= 0 ? colData : "NÃO ENCONTRADA"));
        System.out.println("  Placa: " + (colPlacaVeiculo >= 0 ? colPlacaVeiculo : "NÃO ENCONTRADA"));
        System.out.println("  Descrição: " + (colDescricao >= 0 ? colDescricao : "NÃO ENCONTRADA"));
        System.out.println("  Valor Unitário: " + (colValorUnit >= 0 ? colValorUnit : "NÃO ENCONTRADA"));
        System.out.println("  Unidade: " + (colUnidade >= 0 ? colUnidade : "NÃO ENCONTRADA"));
        System.out.println("  Quantidade: " + (colQuantidade >= 0 ? colQuantidade : "NÃO ENCONTRADA"));

        int proximaLinhaVazia = LINHA_HEADERS + 1;
        int ultimaLinhaComDados = sheet.getLastRowNum();
        if (ultimaLinhaComDados > LINHA_HEADERS) {
            proximaLinhaVazia = ultimaLinhaComDados + 1;
        }

        System.out.println("📝 Adicionando dados a partir da linha " + (proximaLinhaVazia + 1));

        int linhasAdicionadas = 0;
        for (Produto p : listaDeProdutos) {
            Row row = sheet.createRow(proximaLinhaVazia);

            if (colNota != -1)         row.createCell(colNota).setCellValue(numNota);
            if (colForn != -1)         row.createCell(colForn).setCellValue(forn);
            if (colData != -1)         row.createCell(colData).setCellValue(data);
            if (colPlacaVeiculo != -1) row.createCell(colPlacaVeiculo).setCellValue(placaVeic);

            // Descrição
            if (colDescricao != -1) {
                row.createCell(colDescricao).setCellValue(p.descricao != null ? p.descricao : "");
            } else {
                row.createCell(5).setCellValue(p.descricao != null ? p.descricao : "");
            }

            // Valor unitário PRÓPRIO do produto
            if (colValorUnit != -1) {
                row.createCell(colValorUnit).setCellValue(p.valorUnitario != null ? p.valorUnitario : "");
            } else {
                row.createCell(6).setCellValue(p.valorUnitario != null ? p.valorUnitario : "");
            }

            // Unidade PRÓPRIA do produto
            if (colUnidade != -1) {
                row.createCell(colUnidade).setCellValue(p.unidade != null ? p.unidade : "");
            }

            // Quantidade PRÓPRIA do produto
            if (colQuantidade != -1) {
                row.createCell(colQuantidade).setCellValue(p.quantidade != null ? p.quantidade : "");
            }

            proximaLinhaVazia++;
            linhasAdicionadas++;
        }

        System.out.println("✓ " + linhasAdicionadas + " produto(s) adicionado(s)");
    }
}