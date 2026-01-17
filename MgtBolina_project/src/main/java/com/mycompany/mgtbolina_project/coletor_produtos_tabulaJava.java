package com.mycompany.mgtbolina_project;

import org.apache.pdfbox.pdmodel.PDDocument;
import technology.tabula.ObjectExtractor;
import technology.tabula.Page;
import technology.tabula.RectangularTextContainer;
import technology.tabula.Table;
import technology.tabula.extractors.BasicExtractionAlgorithm;
import technology.tabula.extractors.SpreadsheetExtractionAlgorithm;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import org.apache.pdfbox.Loader;

public class coletor_produtos_tabulaJava {
    
    public List<produto> extrairTabelaPDF(String caminhoPDF) {
        List<produto> listaDeProdutos = new ArrayList<>();
        
        try (PDDocument doc = Loader.loadPDF(new File(caminhoPDF))) {
            ObjectExtractor extractor = new ObjectExtractor(doc);
            
            // Tentar extrair de todas as páginas
            for (int pageNum = 1; pageNum <= doc.getNumberOfPages(); pageNum++) {
                Page pagina = extractor.extract(pageNum);
                
                // Tentar ambos os algoritmos
                List<produto> produtosDaPagina = tentarExtracao(pagina);
                listaDeProdutos.addAll(produtosDaPagina);
            }
            
            extractor.close();
            
        } catch (Exception e) {
            System.err.println("Erro ao ler tabela do PDF: " + e.getMessage());
            e.printStackTrace();
        }
        
        return listaDeProdutos;
    }
    
    private List<produto> tentarExtracao(Page pagina) {
        List<produto> produtos = new ArrayList<>();
        
        // Primeiro tenta com SpreadsheetExtractionAlgorithm
        produtos = extrairComSpreadsheet(pagina);
        
        // Se não encontrou produtos válidos, tenta com BasicExtractionAlgorithm
        if (produtos.isEmpty()) {
            produtos = extrairComBasic(pagina);
        }
        
        return produtos;
    }
    
    private List<produto> extrairComSpreadsheet(Page pagina) {
        List<produto> produtos = new ArrayList<>();
        SpreadsheetExtractionAlgorithm algoritmo = new SpreadsheetExtractionAlgorithm();
        List<Table> tabelas = algoritmo.extract(pagina);
        
        for (Table tabela : tabelas) {
            produtos.addAll(processarTabela(tabela));
        }
        
        return produtos;
    }
    
    private List<produto> extrairComBasic(Page pagina) {
        List<produto> produtos = new ArrayList<>();
        BasicExtractionAlgorithm algoritmo = new BasicExtractionAlgorithm();
        List<Table> tabelas = algoritmo.extract(pagina);
        
        for (Table tabela : tabelas) {
            produtos.addAll(processarTabela(tabela));
        }
        
        return produtos;
    }
    
    private List<produto> processarTabela(Table tabela) {
        List<produto> produtos = new ArrayList<>();
        
        if (tabela.getRows().isEmpty()) {
            return produtos;
        }
        
        System.out.println("\n=== PROCESSANDO TABELA ===");
        System.out.println("Total de linhas na tabela: " + tabela.getRows().size());
        
        // Processa as linhas
        boolean headerEncontrado = false;
        int linhaNum = 0;
        
        for (List<RectangularTextContainer> linha : tabela.getRows()) {
            linhaNum++;
            
            // Verifica se é o cabeçalho da tabela de produtos
            if (!headerEncontrado && temCabecalhoProdutos(linha)) {
                System.out.println(">>> CABEÇALHO DE PRODUTOS ENCONTRADO na linha " + linhaNum + " <<<");
                headerEncontrado = true;
                continue; // Pula o cabeçalho
            }
            
            // Só processa linhas após encontrar o cabeçalho
            if (!headerEncontrado) {
                continue;
            }
            
            // CASO ESPECIAL: Se toda a linha está em uma única célula, precisa fazer parse manual
            if (linha.size() == 1 || linha.size() <= 5) {
                String linhaCompleta = linha.get(0).getText().trim();
                
                // Tenta fazer parse da linha concatenada
                produto p = parsearLinhaConcatenada(linhaCompleta);
                if (p != null) {
                    produtos.add(p);
                    System.out.println("✓ PRODUTO ADICIONADO (parse manual): " + p.descricao + " | R$ " + p.valorUnitario);
                }
                continue;
            }
            
            // Processa normalmente se tem colunas separadas
            int[] indices = identificarColunas(tabela);
            int colCodigo = indices[0];
            int colDescricao = indices[1];
            int colValorUnit = indices[2];
            
            // Verifica se a linha tem colunas suficientes
            if (linha.size() <= Math.max(colDescricao, colValorUnit)) {
                continue;
            }
            
            // Pega os dados da linha
            String codigo = colCodigo != -1 && colCodigo < linha.size() ? linha.get(colCodigo).getText().trim() : "";
            String descricao = colDescricao < linha.size() ? linha.get(colDescricao).getText().trim() : "";
            String valorUnitario = colValorUnit != -1 && colValorUnit < linha.size() ? linha.get(colValorUnit).getText().trim() : "";
            
            // FILTROS IMPORTANTES: só adiciona se for realmente um produto
            if (isProdutoValido(codigo, descricao, valorUnitario)) {
                produtos.add(new produto(descricao, limparValor(valorUnitario)));
                System.out.println("✓ PRODUTO ADICIONADO: " + descricao + " | R$ " + valorUnitario);
            }
        }
        
        System.out.println("\n=== FIM DO PROCESSAMENTO ===");
        System.out.println("Total de produtos válidos: " + produtos.size() + "\n");
        
        return produtos;
    }
    
    // Novo método para parsear linha concatenada
    private produto parsearLinhaConcatenada(String linha) {
        // Exemplo: "4776 BICA FINA DE GRANITO 25171000 000  5.101 TON 38,420 50,00  1.921,00"
        
        // Remove espaços múltiplos
        linha = linha.replaceAll("\\s+", " ").trim();
        
        System.out.println("  [Tentando parsear]: " + linha);
        
        // Ignora linhas vazias ou muito curtas
        if (linha.isEmpty() || linha.length() < 10) {
            System.out.println("  [Rejeitado]: linha muito curta");
            return null;
        }
        
        // Ignora cabeçalhos e outras linhas indesejadas
        String linhaUpper = linha.toUpperCase();
        if (linhaUpper.contains("CÓD") || linhaUpper.contains("DESCRIÇÃO") ||
            linhaUpper.contains("DADOS") || linhaUpper.contains("CÁLCULO") ||
            linhaUpper.contains("INFORMAÇÕES") || linhaUpper.contains("ALIQUOTA") ||
            linhaUpper.contains("ISSQN") || linhaUpper.contains("RESERVADO")) {
            System.out.println("  [Rejeitado]: é cabeçalho ou informação adicional");
            return null;
        }
        
        // Tenta extrair usando regex mais flexível:
        // Padrão: começa com números (código), seguido de texto (descrição), depois valores monetários
        // Procura especificamente por dois valores monetários: V.UNIT e V.TOTAL
        Pattern pattern = Pattern.compile("^(\\d+)\\s+([A-ZÀ-Ú][A-ZÀ-Úa-zà-ú\\s]+?)\\s+\\d+.*?(\\d{1,3}(?:\\.\\d{3})*,\\d{2})\\s+(\\d{1,3}(?:\\.\\d{3})*,\\d{2})");
        Matcher matcher = pattern.matcher(linha);
        
        if (matcher.find()) {
            String codigo = matcher.group(1);
            String descricao = matcher.group(2).trim();
            String valorUnit = matcher.group(3);
            String valorTotal = matcher.group(4);
            
            System.out.println("  [Match encontrado!]");
            System.out.println("    Código: " + codigo);
            System.out.println("    Descrição: " + descricao);
            System.out.println("    V.Unit: " + valorUnit);
            System.out.println("    V.Total: " + valorTotal);
            
            // Valida se parece um produto real
            if (descricao.length() > 3 && !descricao.matches("^\\d+$") && descricao.matches(".*[A-Za-z]+.*")) {
                System.out.println("  [PRODUTO VÁLIDO!]");
                return new produto(descricao, valorUnit);
            } else {
                System.out.println("  [Rejeitado]: descrição inválida");
            }
        } else {
            System.out.println("  [Rejeitado]: não deu match na regex");
        }
        
        return null;
    }
    
    private int[] identificarColunas(Table tabela) {
        int colCodigo = -1;
        int colDescricao = -1;
        int colValorUnit = -1;
        int colValorTotal = -1;
        
        // Procura pelo cabeçalho "DADOS DO PRODUTO / SERVIÇOS"
        for (List<RectangularTextContainer> linha : tabela.getRows()) {
            for (int i = 0; i < linha.size(); i++) {
                String texto = linha.get(i).getText().trim().toUpperCase();
                
                // Procura coluna de código
                if ((texto.contains("CÓD") || texto.equals("COD") || 
                     texto.contains("CODIGO")) && colCodigo == -1) {
                    colCodigo = i;
                }
                
                // Procura coluna de descrição
                if ((texto.contains("DESCRI") || texto.contains("PRODUTO")) && colDescricao == -1) {
                    colDescricao = i;
                }
                
                // Procura coluna de valor unitário
                if ((texto.contains("V. UNIT") || texto.contains("VL. UNIT") || 
                     texto.contains("VALOR UNIT")) && colValorUnit == -1) {
                    colValorUnit = i;
                }
                
                // Procura coluna de valor total
                if ((texto.contains("V. TOTAL") || texto.contains("VL. TOTAL") || 
                     texto.contains("VALOR TOTAL")) && colValorTotal == -1) {
                    colValorTotal = i;
                }
            }
            
            // Se encontrou as colunas principais, para de procurar
            if (colDescricao != -1 && colValorUnit != -1) {
                break;
            }
        }
        
        // Valores padrão baseados na estrutura comum das DANFEs
        // Estrutura: CÓD | DESCRIÇÃO | NCM | CST | CFOP | UNID | QUANT | V.UNIT | V.TOTAL ...
        if (colCodigo == -1) colCodigo = 0;
        if (colDescricao == -1) colDescricao = 1;
        if (colValorUnit == -1) colValorUnit = 7;  // Geralmente V.UNIT está na coluna 7
        if (colValorTotal == -1) colValorTotal = 8;  // V.TOTAL na coluna 8
        
        return new int[]{colCodigo, colDescricao, colValorUnit, colValorTotal};
    }
    
    private boolean temCabecalhoProdutos(List<RectangularTextContainer> linha) {
        // Verifica se a linha contém palavras típicas de cabeçalho de produtos
        StringBuilder linhaCompleta = new StringBuilder();
        for (RectangularTextContainer cell : linha) {
            String texto = cell.getText().trim().toUpperCase();
            linhaCompleta.append(texto).append(" ");
            
            if (texto.contains("DADOS DO PRODUTO") || 
                texto.contains("DESCRIÇÃO DO PRODUTO") ||
                texto.contains("DADOS DOS PRODUTOS") ||
                (texto.contains("CÓD") && texto.contains("PRODUTO"))) {
                System.out.println(">>> Cabeçalho detectado pela célula: " + texto);
                return true;
            }
        }
        
        // Também procura na linha completa
        String linhaStr = linhaCompleta.toString();
        if (linhaStr.contains("CÓD") && linhaStr.contains("DESCRIÇÃO") && linhaStr.contains("PRODUTO")) {
            System.out.println(">>> Cabeçalho detectado pela linha completa");
            return true;
        }
        
        return false;
    }
    
    private boolean isProdutoValido(String codigo, String descricao, String valorUnitario) {
        // Remove encoding problems
        descricao = normalizarTexto(descricao);
        
        // 1. Descrição não pode estar vazia
        if (descricao == null || descricao.isEmpty()) {
            return false;
        }
        
        // 2. Descrição NÃO pode ser apenas números (isso seria o código)
        if (descricao.matches("^\\d+$")) {
            System.out.println("❌ Rejeitado (só código): " + descricao);
            return false;
        }
        
        // 3. Não pode ser cabeçalho
        String descUpper = descricao.toUpperCase();
        if (descUpper.contains("DESCRIÇÃO") || descUpper.contains("DESCRICAO") ||
            descUpper.contains("PRODUTO") || descUpper.contains("CÓD") ||
            descUpper.contains("NCM") || descUpper.contains("CFOP") ||
            descUpper.contains("DADOS DO") || descUpper.contains("SERVIÇOS") ||
            descUpper.contains("IDENTIFICAÇÃO") || descUpper.contains("ASSINATURA") ||
            descUpper.contains("RECEPTOR") || descUpper.contains("RECOLHIMENTO") ||
            descUpper.contains("ESTADUAL") || descUpper.contains("TRIBUTÁRIO") ||
            descUpper.contains("MUNICÍPIO") || descUpper.contains("ENDEREÇO") ||
            descUpper.contains("ESPÉCIE") || descUpper.contains("MARCA") ||
            descUpper.contains("ALIQ") || descUpper.contains("CST")) {
            return false;
        }
        
        // 4. Valor unitário precisa ser um número válido
        if (valorUnitario == null || valorUnitario.isEmpty()) {
            return false;
        }
        
        String valorLimpo = limparValor(valorUnitario);
        if (!valorLimpo.matches("\\d+,\\d{2}") && !valorLimpo.matches("\\d+\\.\\d+,\\d{2}")) {
            return false;
        }
        
        // 5. Descrição precisa ter mais de 3 caracteres E conter letras
        if (descricao.length() < 4 || !descricao.matches(".*[A-Za-z]+.*")) {
            return false;
        }
        
        return true;
    }
    
    private String limparValor(String valor) {
        if (valor == null) return "";
        // Remove espaços e mantém apenas números, vírgula e ponto
        return valor.replaceAll("[^0-9,.]", "").trim();
    }
    
    private String normalizarTexto(String texto) {
        if (texto == null) return null;
        
        return texto
            .replace("Ã§", "ç")
            .replace("Ã£", "ã")
            .replace("Ã©", "é")
            .replace("Ã­", "í")
            .replace("Ã³", "ó")
            .replace("Ãº", "ú")
            .replace("Ã ", "à")
            .replace("Ã‡", "Ç")
            .replace("ÃƒO", "ÃO")
            .replace("Ãƒ", "Ã");
    }
}