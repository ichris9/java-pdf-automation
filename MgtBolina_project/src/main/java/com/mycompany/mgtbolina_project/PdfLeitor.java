package com.mycompany.mgtbolina_project;

import technology.tabula.ObjectExtractor;
import technology.tabula.Page;
import technology.tabula.PageIterator;
import technology.tabula.Table;
import technology.tabula.extractors.SpreadsheetExtractionAlgorithm; 

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.Loader;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import technology.tabula.RectangularTextContainer;

/**
 * Leitor de PDF com detecção automática de PDFs escaneados
 * OCR configurado para funcionar SEM instalação externa do Tesseract
 * Usa tessdata embutido no JAR
 */
public class PdfLeitor {
    
    private static final int LIMITE_CARACTERES_MINIMO = 100;
    private static Path tessdataPath = null;
    private static boolean tessdataExtraido = false;
    
    public PdfLeitor() {
        // Construtor padrão
    }
    
    /**
     * Método principal que detecta automaticamente se precisa usar OCR
     */
    public String ExtractText(String filePath) {
        System.out.println("\n=== INICIANDO EXTRAÇÃO DE PDF ===");
        System.out.println("Arquivo: " + filePath);
        
        try {
            // PASSO 1: Tenta extração normal com Tabula
            String textoTabula = extrairComTabula(filePath);
            
            // PASSO 2: Verifica se o texto extraído é suficiente
            if (textoTabula != null && textoTabula.length() >= LIMITE_CARACTERES_MINIMO) {
                System.out.println("✓ PDF contém texto extraível (" + textoTabula.length() + " caracteres)");
                System.out.println("✓ Usando extração normal (Tabula)");
                return textoTabula;
            }
            
            // PASSO 3: Texto insuficiente - é um PDF escaneado!
            System.out.println("⚠ PDF parece ser escaneado (apenas " + 
                (textoTabula != null ? textoTabula.length() : 0) + " caracteres extraídos)");
            System.out.println("🔍 Ativando OCR com Tesseract embutido...");
            
            String textoOCR = extrairComOCR(filePath);
            
            if (textoOCR != null && !textoOCR.trim().isEmpty()) {
                System.out.println("✓ OCR concluído com sucesso (" + textoOCR.length() + " caracteres)");
                return textoOCR;
            } else {
                System.err.println("❌ Falha no OCR - retornando texto parcial");
                return textoTabula != null ? textoTabula : "";
            }
            
        } catch (Exception e) {
            System.err.println("❌ Erro geral na extração: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Extração normal usando Tabula (para PDFs com texto)
     */
    private String extrairComTabula(String filePath) {
        StringBuilder textoCompleto = new StringBuilder();
        
        try (PDDocument document = Loader.loadPDF(new File(filePath))) {
            ObjectExtractor extractor = new ObjectExtractor(document);
            PageIterator pageIterator = extractor.extract();
            SpreadsheetExtractionAlgorithm algoritmo = new SpreadsheetExtractionAlgorithm();
            
            while (pageIterator.hasNext()) {
                Page page = pageIterator.next();
                List<Table> tables = algoritmo.extract(page);
                
                for (Table table : tables) {
                    for (List<RectangularTextContainer> row : table.getRows()) {
                        for (RectangularTextContainer cell : row) {
                            textoCompleto.append(cell.getText()).append(" | ");
                        }
                        textoCompleto.append("\n");
                    }
                }
            }
            
            return textoCompleto.toString();
            
        } catch (Exception e) {
            System.err.println("Erro ao usar Tabula: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Prepara tessdata embutido (extrai do JAR ou baixa)
     */
    private synchronized void prepararTessdata() throws IOException {
        if (tessdataExtraido) {
            return; // Já foi extraído
        }
        
        try {
            // Cria diretório temporário para tessdata
            Path tempDir = Files.createTempDirectory("tessdata");
            tessdataPath = tempDir;
            
            System.out.println("📂 Preparando tessdata em: " + tessdataPath);
            
            // Tenta extrair por.traineddata do JAR (se estiver embutido)
            String resourcePath = "/tessdata/por.traineddata";
            InputStream is = getClass().getResourceAsStream(resourcePath);
            
            if (is != null) {
                // Extrai do JAR
                Path porFile = tessdataPath.resolve("por.traineddata");
                Files.copy(is, porFile, StandardCopyOption.REPLACE_EXISTING);
                is.close();
                System.out.println("✓ Tessdata extraído do JAR");
            } else {
                // Não está embutido - tenta baixar
                System.out.println("⚠ Tessdata não encontrado no JAR");
                System.out.println("💡 Baixando por.traineddata da internet...");
                baixarTessdata();
            }
            
            tessdataExtraido = true;
            
        } catch (Exception e) {
            System.err.println("❌ Erro ao preparar tessdata: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Baixa tessdata da internet (fallback)
     */
    private void baixarTessdata() throws IOException {
        try {
            // URL do arquivo por.traineddata do GitHub oficial do Tesseract
            String url = "https://github.com/tesseract-ocr/tessdata/raw/main/por.traineddata";
            
            System.out.println("📥 Baixando de: " + url);
            
            Path porFile = tessdataPath.resolve("por.traineddata");
            
            // Baixa usando Java 11+ HttpClient
            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                .uri(java.net.URI.create(url))
                .build();
            
            java.net.http.HttpResponse<InputStream> response = client.send(
                request, 
                java.net.http.HttpResponse.BodyHandlers.ofInputStream()
            );
            
            if (response.statusCode() == 200) {
                Files.copy(response.body(), porFile, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("✓ Tessdata baixado com sucesso!");
            } else {
                throw new IOException("Falha no download: HTTP " + response.statusCode());
            }
            
        } catch (Exception e) {
            System.err.println("❌ Erro ao baixar tessdata: " + e.getMessage());
            throw new IOException("Não foi possível obter tessdata", e);
        }
    }
    
    /**
     * Extração usando OCR (Tesseract) para PDFs escaneados
     */
    private String extrairComOCR(String filePath) {
        StringBuilder textoOCR = new StringBuilder();
        
        try (PDDocument document = Loader.loadPDF(new File(filePath))) {
            
            // Prepara tessdata
            prepararTessdata();
            
            // Configura o Tesseract
            Tesseract tesseract = new Tesseract();
            
            // Define o caminho do tessdata extraído
            tesseract.setDatapath(tessdataPath.toString());
            System.out.println("📂 Usando tessdata em: " + tessdataPath);
            
            // Configura para português
            tesseract.setLanguage("por");
            
            // Otimizações para melhorar precisão
            tesseract.setPageSegMode(1); // Automatic page segmentation with OSD
            tesseract.setOcrEngineMode(1); // Neural nets LSTM engine only
            
            // Renderiza o PDF em imagens
            PDFRenderer renderer = new PDFRenderer(document);
            int totalPaginas = document.getNumberOfPages();
            
            System.out.println("📄 Processando " + totalPaginas + " página(s) com OCR...");
            
            for (int pagina = 0; pagina < totalPaginas; pagina++) {
                System.out.println("  Processando página " + (pagina + 1) + "/" + totalPaginas + "...");
                
                // Renderiza a página em alta resolução (300 DPI para melhor OCR)
                BufferedImage imagem = renderer.renderImageWithDPI(pagina, 300, ImageType.RGB);
                
                // Aplica OCR na imagem
                String textoPagina = tesseract.doOCR(imagem);
                
                if (textoPagina != null && !textoPagina.trim().isEmpty()) {
                    textoOCR.append(textoPagina).append("\n\n");
                    System.out.println("    ✓ " + textoPagina.length() + " caracteres extraídos");
                } else {
                    System.out.println("    ⚠ Nenhum texto encontrado nesta página");
                }
            }
            
            System.out.println("✓ OCR finalizado!");
            return textoOCR.toString();
            
        } catch (IOException e) {
            System.err.println("❌ Erro ao abrir PDF para OCR: " + e.getMessage());
            e.printStackTrace();
            return null;
        } catch (TesseractException e) {
            System.err.println("❌ Erro no Tesseract OCR: " + e.getMessage());
            System.err.println("💡 Verifique se o arquivo por.traineddata foi baixado corretamente");
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            System.err.println("❌ Erro desconhecido no OCR: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Método auxiliar para detectar se um PDF é escaneado
     */
    public boolean isPDFEscaneado(String filePath) {
        try {
            String texto = extrairComTabula(filePath);
            return texto == null || texto.length() < LIMITE_CARACTERES_MINIMO;
        } catch (Exception e) {
            return true;
        }
    }
}