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
        
        if (caminhoPDF == null || caminhoPDF.isEmpty()) {
            System.err.println("Erro: Caminho do PDF está vazio ou nulo!");
            return listaDeProdutos;
        }
        
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
        
        if (pagina == null) {
            System.err.println("Erro: Página está nula!");
            return produtos;
        }
        
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
        
        try {
            SpreadsheetExtractionAlgorithm algoritmo = new SpreadsheetExtractionAlgorithm();
            List<Table> tabelas = algoritmo.extract(pagina);
            
            for (Table tabela : tabelas) {
                produtos.addAll(processarTabela(tabela));
            }
        } catch (Exception e) {
            System.err.println("Erro no algoritmo Spreadsheet: " + e.getMessage());
        }
        
        return produtos;
    }
    
    private List<Produto> extrairComBasic(Page pagina) {
        List<Produto> produtos = new ArrayList<>();
        
        try {
            BasicExtractionAlgorithm algoritmo = new BasicExtractionAlgorithm();
            List<Table> tabelas = algoritmo.extract(pagina);
            
            for (Table tabela : tabelas) {
                produtos.addAll(processarTabela(tabela));
            }
        } catch (Exception e) {
            System.err.println("Erro no algoritmo Basic: " + e.getMessage());
        }
        
        return produtos;
    }
    
    private List<Produto> processarTabela(Table tabela) {
        List<Produto> produtos = new ArrayList<>();
        
        if (tabela == null || tabela.getRows().isEmpty()) {
            return produtos;
        }
        
        System.out.println("\n=== PROCESSANDO TABELA ===");
        System.out.println("Total de linhas na tabela: " + tabela.getRows().size());
        
        // Processa as linhas
        boolean headerEncontrado = false;
        int linhaNum = 0;
        
        for (List<RectangularTextContainer> linha : tabela.getRows()) {
            linhaNum++;
            
            try {
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
                    if (cell != null && cell.getText() != null) {
                        linhaCompleta.append(cell.getText().trim()).append(" ");
                    }
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
                    System.out.println("  -> ✓ PRODUTO ADICIONADO: " + p.descricao + " | " + p.unidade + " | R$ " + p.valorUnitario);
                }
                
            } catch (Exception e) {
                System.err.println("Erro ao processar linha " + linhaNum + ": " + e.getMessage());
            }
        }
        
        System.out.println("\n=== FIM DO PROCESSAMENTO ===");
        System.out.println("Total de produtos válidos: " + produtos.size() + "\n");
        
        return produtos;
    }
    
    // 🔥 ATUALIZADO: Agora captura UNIDADE
    private Produto parsearLinhaConcatenada(String linha) {
        if (linha == null || linha.isEmpty()) {
            return null;
        }
        
        try {
            linha = linha.replaceAll("\\s+", " ").trim();
            
            System.out.println("  [Tentando parsear]: " + linha);
            
            if (linha.length() < 10) {
                System.out.println("  [Rejeitado]: linha muito curta");
                return null;
            }
            
            String linhaUpper = linha.toUpperCase();
            if (linhaUpper.contains("CÓD") || linhaUpper.contains("DESCRIÇÃO") ||
                linhaUpper.contains("NCM") ||
                linhaUpper.contains("CST") || linhaUpper.contains("CFOP") ||
                linhaUpper.contains("ALIQ") || linhaUpper.contains("BASE") ||
                linhaUpper.contains("CÁLCULO")) {
                System.out.println("  [Rejeitado]: parece cabeçalho");
                return null;
            }
            
            // ─────────────────────────────────────────────────────────────────
            // ESTRATÉGIA: ancoragem no NCM (bloco de 8 dígitos isolado)
            // Isso resolve todos os casos problemáticos:
            //   • Ventilar: "SACO P/ ENTULHO 50KGS UNIDADE LOTE: 01 63053390 000 5102 UN ..."
            //   • Ferrari:  "9005 MARGI. BRILHO BR 16048 TOMADA 2P+T N.P.B 20A 250V MODULO 85366910 ..."
            // O NCM separa de forma precisa onde a descrição termina e os dados numéricos começam.
            // ─────────────────────────────────────────────────────────────────
            
            // Padrão 1 (PRINCIPAL): âncora no NCM de 8 dígitos
            Pattern pattern1 = Pattern.compile(
                "^(\\d+)\\s+" +                              // código
                "(.+?)\\s+" +                                // descrição — lazy, qualquer coisa
                "(\\d{8})\\s+" +                             // NCM (âncora — 8 dígitos exatos)
                "\\d{3}\\s+" +                               // CST
                "[\\d\\.]+\\s+" +                            // CFOP
                "([A-Z]{1,5}\\d{0,2})\\s+" +                 // UNID (PC, UN, TON, M3, etc)
                "[\\d,\\.]+\\s+" +                           // QUANT
                "(\\d{1,3}(?:\\.\\d{3})*,\\d{2,4})\\s+" +   // V.UNIT (aceita 2-4 casas: 2,2500)
                "(\\d{1,3}(?:\\.\\d{3})*,\\d{2})"           // V.TOTAL
            );
            
            // Padrão 2 (FLEXÍVEL): NCM com 6-8 dígitos para outros layouts
            Pattern pattern2 = Pattern.compile(
                "^(\\d+)\\s+" +
                "(.+?)\\s+" +
                "(\\d{6,8})\\s+\\d{3}\\s+[\\d\\.]+\\s+" +  // NCM+CST+CFOP
                "([A-Z]{1,5}\\d{0,2})\\s+" +
                "[\\d,\\.]+\\s+" +
                "(\\d{1,3}(?:\\.\\d{3})*,\\d{2,4})\\s+" +
                "(\\d{1,3}(?:\\.\\d{3})*,\\d{2})"
            );
            
            // Padrão 3 (FALLBACK): sem unidade
            Pattern pattern3 = Pattern.compile(
                "^(\\d+)\\s+" +
                "(.+?)\\s+" +
                ".*?" +
                "(\\d{1,3}(?:\\.\\d{3})*,\\d{2})\\s+" +
                "(\\d{1,3}(?:\\.\\d{3})*,\\d{2})"
            );
            
            Matcher m1 = pattern1.matcher(linha);
            Matcher m2 = pattern2.matcher(linha);
            Matcher m3 = pattern3.matcher(linha);
            
            // Tenta padrão 1 (com unidade)
            if (m1.find()) {
                String codigo = m1.group(1);
                String descricao = limparDescricao(m1.group(2).trim());
                String unidade = m1.group(4);
                String valorUnit = normalizarValorUnitario(m1.group(5));
                String valorTotal = m1.group(6);
                
                System.out.println("  [Match padrão 1 - COM UNIDADE!]");
                System.out.println("    Código: " + codigo);
                System.out.println("    Descrição: " + descricao);
                System.out.println("    Unidade: " + unidade);
                System.out.println("    V.Unit: " + valorUnit);
                System.out.println("    V.Total: " + valorTotal);
                
                if (isProdutoValido(codigo, descricao, valorUnit)) {
                    System.out.println("  [PRODUTO VÁLIDO!]");
                    return new Produto(descricao, valorUnit, unidade);
                }
            }
            // Tenta padrão 2 (flexível com unidade)
            else if (m2.find()) {
                String codigo = m2.group(1);
                String descricao = limparDescricao(m2.group(2).trim());
                String unidade = m2.group(4);
                String valorUnit = normalizarValorUnitario(m2.group(5));
                String valorTotal = m2.group(6);
                
                System.out.println("  [Match padrão 2 - COM UNIDADE!]");
                System.out.println("    Código: " + codigo);
                System.out.println("    Descrição: " + descricao);
                System.out.println("    Unidade: " + unidade);
                System.out.println("    V.Unit: " + valorUnit);
                System.out.println("    V.Total: " + valorTotal);
                
                if (isProdutoValido(codigo, descricao, valorUnit)) {
                    System.out.println("  [PRODUTO VÁLIDO!]");
                    return new Produto(descricao, valorUnit, unidade);
                }
            }
            // Tenta padrão 3 (fallback SEM unidade - usa "UN")
            else if (m3.find()) {
                String codigo = m3.group(1);
                String descricao = limparDescricao(m3.group(2).trim());
                String valorUnit = normalizarValorUnitario(m3.group(3));
                String valorTotal = m3.group(4);
                
                System.out.println("  [Match padrão 3 - SEM unidade (usa UN)]");
                System.out.println("    Código: " + codigo);
                System.out.println("    Descrição: " + descricao);
                System.out.println("    V.Unit: " + valorUnit);
                System.out.println("    V.Total: " + valorTotal);
                
                if (isProdutoValido(codigo, descricao, valorUnit)) {
                    System.out.println("  [PRODUTO VÁLIDO!]");
                    return new Produto(descricao, valorUnit);  // Usa construtor legado (UN padrão)
                }
            }
            else {
                System.out.println("  [Rejeitado]: nenhum padrão deu match");
            }
            
        } catch (Exception e) {
            System.err.println("  [Erro ao parsear]: " + e.getMessage());
        }
        
        return null;
    }
    
    private boolean temCabecalhoProdutos(List<RectangularTextContainer> linha) {
        if (linha == null || linha.isEmpty()) {
            return false;
        }
        
        try {
            StringBuilder linhaCompleta = new StringBuilder();
            for (RectangularTextContainer cell : linha) {
                if (cell != null && cell.getText() != null) {
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
            }
            
            String linhaStr = linhaCompleta.toString();
            if ((linhaStr.contains("CÓD") || linhaStr.contains("PRODUTO")) && 
                (linhaStr.contains("DESCRIÇÃO") || linhaStr.contains("SERVIÇO"))) {
                System.out.println(">>> Cabeçalho detectado pela linha completa");
                return true;
            }
            
        } catch (Exception e) {
            System.err.println("Erro ao verificar cabeçalho: " + e.getMessage());
        }
        
        return false;
    }
    
    private boolean isProdutoValido(String codigo, String descricao, String valorUnitario) {
        try {
            descricao = normalizarTexto(descricao);
            
            if (descricao == null || descricao.isEmpty()) {
                System.out.println("    ✗ Descrição vazia");
                return false;
            }
            
            if (descricao.matches("^\\d+$")) {
                System.out.println("    ✗ Descrição é só código: " + descricao);
                return false;
            }
            
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
            
            if (valorUnitario == null || valorUnitario.isEmpty()) {
                System.out.println("    ✗ Valor unitário vazio");
                return false;
            }
            
            String valorLimpo = limparValor(valorUnitario);
            if (!valorLimpo.matches("\\d+,\\d{2,4}") && !valorLimpo.matches("\\d+\\.\\d+,\\d{2,4}")) {
                System.out.println("    ✗ Valor unitário inválido: " + valorLimpo);
                return false;
            }
            
            if (descricao.length() < 3 || !descricao.matches(".*[A-Za-z]+.*")) {
                System.out.println("    ✗ Descrição muito curta ou sem letras");
                return false;
            }
            
            System.out.println("    ✓ Produto válido!");
            return true;
            
        } catch (Exception e) {
            System.err.println("    ✗ Erro na validação: " + e.getMessage());
            return false;
        }
    }
    
    private String limparValor(String valor) {
        if (valor == null) return "";
        return valor.replaceAll("[^0-9,.]", "").trim();
    }
    
    /**
     * Limpa textos extras que o Tabula injeta na descrição:
     *   - "LOTE: 01" (Ventilar e outros)
     *   - "IMPOSTO RECOLHIDO POR SUBSTITUICAO..." (Ventilar produto 2)
     *   - "SEPARADOS" isolado no final (Ferrari)
     */
    private String limparDescricao(String desc) {
        if (desc == null) return "";
        desc = desc
            .replaceAll("(?i)\\s+LOTE:\\s*\\d+.*$", "")
            .replaceAll("(?i)\\s+IMPOSTO\\s+RECOLHIDO.*$", "")
            .replaceAll("(?i)\\s+RICMS.*$", "")
            .replaceAll("(?i)\\s+TRIBUTARIA.*$", "")
            .replaceAll("(?i)\\bSEPARADOS\\b\\s*$", "")
            .trim();
        return desc;
    }
    
    /**
     * Normaliza valor unitário: converte 4 casas decimais para 2 (ex: 2,2500 → 2,25)
     * Também trata casos como 187,0602 → 187,06
     */
    private String normalizarValorUnitario(String valor) {
        if (valor == null) return "";
        valor = valor.trim();
        // Se tem mais de 2 casas decimais, trunca para 2
        if (valor.matches("\\d{1,3}(?:\\.\\d{3})*,\\d{3,}")) {
            int virgula = valor.lastIndexOf(',');
            return valor.substring(0, virgula + 3); // mantém 2 casas após vírgula
        }
        return valor;
    }
    
    private String normalizarTexto(String texto) {
        if (texto == null) return null;
        
        return texto
            .replace("ÃƒÂ§", "ç")
            .replace("ÃƒÂ£", "ã")
            .replace("ÃƒÂ©", "é")
            .replace("ÃƒÂ­", "í")
            .replace("ÃƒÂ³", "ó")
            .replace("ÃƒÂº", "ú")
            .replace("Ãƒ ", "à")
            .replace("Ãƒâ€¡", "Ç")
            .replace("ÃƒÆ'O", "ÃO")
            .replace("ÃƒÆ'", "Ã");
    }
}