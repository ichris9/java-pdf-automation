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

public class ColetorProdutos {
    
    public List<Produto> extrairTabelaPDF(String caminhoPDF) {
        List<Produto> listaDeProdutos = new ArrayList<>();
        
        try (PDDocument doc = Loader.loadPDF(new File(caminhoPDF))) {
            ObjectExtractor extractor = new ObjectExtractor(doc);
            
            // Tentar extrair de todas as páginas
            for (int pageNum = 1; pageNum <= doc.getNumberOfPages(); pageNum++) {
                Page pagina = extractor.extract(pageNum);
                
                // Tentar ambos os algoritmos
                List<Produto> produtosDaPagina = tentarExtracao(pagina);
                listaDeProdutos.addAll(produtosDaPagina);
            }
            
            extractor.close();
            
        } catch (Exception e) {
            System.err.println("Erro ao ler tabela do PDF: " + e.getMessage());
            e.printStackTrace();
        }
        
        return listaDeProdutos;
    }
    
    private List<Produto> tentarExtracao(Page pagina) {
        List<Produto> produtos = new ArrayList<>();
        
        // Primeiro tenta com SpreadsheetExtractionAlgorithm
        produtos = extrairComSpreadsheet(pagina);
        
        // Se não encontrou produtos válidos, tenta com BasicExtractionAlgorithm
        if (produtos.isEmpty()) {
            produtos = extrairComBasic(pagina);
        }
        
        return produtos;
    }
    
    private List<Produto> extrairComSpreadsheet(Page pagina) {
        List<Produto> produtos = new ArrayList<>();
        SpreadsheetExtractionAlgorithm algoritmo = new SpreadsheetExtractionAlgorithm();
        List<Table> tabelas = algoritmo.extract(pagina);
        
        for (Table tabela : tabelas) {
            produtos.addAll(processarTabela(tabela));
        }
        
        return produtos;
    }
    
    private List<Produto> extrairComBasic(Page pagina) {
        List<Produto> produtos = new ArrayList<>();
        BasicExtractionAlgorithm algoritmo = new BasicExtractionAlgorithm();
        List<Table> tabelas = algoritmo.extract(pagina);
        
        for (Table tabela : tabelas) {
            produtos.addAll(processarTabela(tabela));
        }
        
        return produtos;
    }
    
    private List<Produto> processarTabela(Table tabela) {
        List<Produto> produtos = new ArrayList<>();
        
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
            
            // Concatena toda a linha para análise
            StringBuilder linhaCompleta = new StringBuilder();
            for (RectangularTextContainer cell : linha) {
                linhaCompleta.append(cell.getText().trim()).append(" ");
            }
            String textoLinha = linhaCompleta.toString().trim();
            
            System.out.println("\n[Linha " + linhaNum + "] Analisando: " + textoLinha);
            
            // Ignora linhas vazias
            if (textoLinha.isEmpty() || textoLinha.length() < 10) {
                System.out.println("  -> Rejeitado: linha muito curta");
                continue;
            }
            
            // Tenta fazer parse da linha
            Produto p = parsearLinhaConcatenada(textoLinha);
            if (p != null) {
                produtos.add(p);
                System.out.println("  -> ✓ PRODUTO ADICIONADO: " + p.descricao + " | R$ " + p.valorUnitario);
            }
        }
        
        System.out.println("\n=== FIM DO PROCESSAMENTO ===");
        System.out.println("Total de produtos válidos: " + produtos.size() + "\n");
        
        return produtos;
    }
    
    // Método aprimorado para parsear linha concatenada
    private Produto parsearLinhaConcatenada(String linha) {
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
            linhaUpper.contains("ISSQN") || linhaUpper.contains("RESERVADO") ||
            linhaUpper.contains("BASE DE") || linhaUpper.contains("VALOR DO") ||
            linhaUpper.contains("COMPLEMENTARES")) {
            System.out.println("  [Rejeitado]: é cabeçalho ou informação adicional");
            return null;
        }
        
        // REGEX MELHORADO: Captura código, descrição e valores monetários
        // Padrão: CÓDIGO DESCRIÇÃO NCM CST CFOP UNID QUANT V.UNIT V.TOTAL ...
        // Exemplo: "4776 BICA FINA DE GRANITO 25171000 000 5.101 TON 38,420 50,00 1.921,00"
        
        // Padrão 1: Mais específico para DANFEs (com NCM, CST, CFOP)
        Pattern pattern1 = Pattern.compile(
            "^(\\d+)\\s+" +                                    // Código do produto
            "([A-ZÀ-Ú][A-ZÀ-Úa-zà-ú0-9\\s/\\-\\.]+?)\\s+"+     // Descrição
            "\\d{8}\\s+" +                                      // NCM
            "\\d{3}\\s+" +                                      // CST
            "[\\d\\.]+\\s+" +                                   // CFOP
            "[A-Z]+\\s+" +                                      // UNID
            "[\\d,]+\\s+" +                                     // QUANT
            "(\\d{1,3}(?:\\.\\d{3})*,\\d{2})\\s+" +           // V.UNIT
            "(\\d{1,3}(?:\\.\\d{3})*,\\d{2})"                 // V.TOTAL
        );
        
        // Padrão 2: Mais flexível (caso o padrão 1 não funcione)
        Pattern pattern2 = Pattern.compile(
            "^(\\d+)\\s+" +                                    // Código
            "([A-ZÀ-Ú][A-ZÀ-Úa-zà-ú0-9\\s/\\-\\.]+?)\\s+" +     // Descrição
            ".*?" +                                             // Tudo no meio
            "(\\d{1,3}(?:\\.\\d{3})*,\\d{2})\\s+" +           // Penúltimo valor (V.UNIT)
            "(\\d{1,3}(?:\\.\\d{3})*,\\d{2})"                 // Último valor (V.TOTAL)
        );
        
        Matcher matcher1 = pattern1.matcher(linha);
        Matcher matcher2 = pattern2.matcher(linha);
        
        Matcher matcherFinal = null;
        if (matcher1.find()) {
            matcherFinal = matcher1;
            System.out.println("  [Match com padrão específico DANFE!]");
        } else if (matcher2.find()) {
            matcherFinal = matcher2;
            System.out.println("  [Match com padrão flexível!]");
        }
        
        if (matcherFinal != null) {
            String codigo = matcherFinal.group(1);
            String descricao = matcherFinal.group(2).trim();
            String valorUnit = matcherFinal.group(3);
            String valorTotal = matcherFinal.group(4);
            
            System.out.println("  [Dados capturados:]");
            System.out.println("    Código: " + codigo);
            System.out.println("    Descrição: " + descricao);
            System.out.println("    V.Unit: " + valorUnit);
            System.out.println("    V.Total: " + valorTotal);
            
            // Valida se parece um produto real
            if (isProdutoValido(codigo, descricao, valorUnit)) {
                System.out.println("  [PRODUTO VÁLIDO!]");
                return new Produto(descricao, valorUnit);
            } else {
                System.out.println("  [Rejeitado]: validação falhou");
            }
        } else {
            System.out.println("  [Rejeitado]: não deu match em nenhum padrão regex");
        }
        
        return null;
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
                texto.contains("DADOS DO SERVIÇO") ||
                (texto.contains("CÓD") && texto.contains("PRODUTO"))) {
                System.out.println(">>> Cabeçalho detectado pela célula: " + texto);
                return true;
            }
        }
        
        // Também procura na linha completa
        String linhaStr = linhaCompleta.toString();
        if ((linhaStr.contains("CÓD") || linhaStr.contains("PRODUTO")) && 
            (linhaStr.contains("DESCRIÇÃO") || linhaStr.contains("SERVIÇO"))) {
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
            System.out.println("    ✗ Descrição vazia");
            return false;
        }
        
        // 2. Descrição NÃO pode ser apenas números (isso seria o código)
        if (descricao.matches("^\\d+$")) {
            System.out.println("    ✗ Descrição é só código: " + descricao);
            return false;
        }
        
        // 3. Não pode ser cabeçalho ou informação administrativa
        String descUpper = descricao.toUpperCase();
        if (descUpper.contains("DESCRIÇÃO") || descUpper.contains("DESCRICAO") ||
            descUpper.contains("CÓD") || descUpper.contains("NCM") || 
            descUpper.contains("CFOP") || descUpper.contains("DADOS DO") || 
            descUpper.contains("SERVIÇOS") || descUpper.contains("IDENTIFICAÇÃO") || 
            descUpper.contains("ASSINATURA") || descUpper.contains("RECEPTOR") || 
            descUpper.contains("RECOLHIMENTO") || descUpper.contains("ESTADUAL") || 
            descUpper.contains("TRIBUTÁRIO") || descUpper.contains("MUNICÍPIO") || 
            descUpper.contains("ENDEREÇO") || descUpper.contains("ESPÉCIE") || 
            descUpper.contains("MARCA") || descUpper.contains("ALIQ") || 
            descUpper.contains("CST") || descUpper.contains("BASE DE") ||
            descUpper.contains("VALOR DO") || descUpper.contains("COMPLEMENTARES")) {
            System.out.println("    ✗ Contém palavra de cabeçalho");
            return false;
        }
        
        // 4. Valor unitário precisa ser um número válido
        if (valorUnitario == null || valorUnitario.isEmpty()) {
            System.out.println("    ✗ Valor unitário vazio");
            return false;
        }
        
        String valorLimpo = limparValor(valorUnitario);
        if (!valorLimpo.matches("\\d+,\\d{2}") && !valorLimpo.matches("\\d+\\.\\d+,\\d{2}")) {
            System.out.println("    ✗ Valor unitário inválido: " + valorLimpo);
            return false;
        }
        
        // 5. Descrição precisa ter mais de 2 caracteres E conter letras
        if (descricao.length() < 3 || !descricao.matches(".*[A-Za-z]+.*")) {
            System.out.println("    ✗ Descrição muito curta ou sem letras");
            return false;
        }
        
        System.out.println("    ✓ Produto válido!");
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
            .replace("ÃÂ§", "ç")
            .replace("ÃÂ£", "ã")
            .replace("ÃÂ©", "é")
            .replace("ÃÂ­", "í")
            .replace("ÃÂ³", "ó")
            .replace("ÃÂº", "ú")
            .replace("Ã ", "à")
            .replace("Ã‡", "Ç")
            .replace("ÃƒO", "ÃO")
            .replace("Ãƒ", "Ã");
    }
}