package com.mycompany.mgtbolina_project;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.List;

public class PdfColetorDados {
    
    // Normaliza caracteres com problemas de encoding
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
    
    private String FindFirstGroup(String text, String regex) {
        if (text == null || text.isEmpty()) {
            return null;
        }
        
        try {
            // Normaliza o texto antes de processar
            text = normalizarTexto(text);
            
            Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
            Matcher matcher = pattern.matcher(text);
            
            if (matcher.find()) {
                // PROTEÇÃO: Verifica se o grupo 1 existe antes de acessá-lo
                if (matcher.groupCount() >= 1) {
                    String resultado = matcher.group(1);
                    if (resultado != null) {
                        resultado = resultado.trim();
                        return normalizarTexto(resultado);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Erro ao processar regex: " + regex);
            System.err.println("Mensagem: " + e.getMessage());
        }
        
        return null;
    }
    
    private String extractWithMultiplePatterns(String text, String[] patterns) {
        if (text == null || text.isEmpty()) {
            return "Não Encontrado (N/E)";
        }
        
        for (String regex : patterns) {
            try {
                String result = FindFirstGroup(text, regex);
                if (result != null && !result.isEmpty()) {
                    return result;
                }
            } catch (Exception e) {
                System.err.println("Erro ao tentar padrão: " + regex);
                // Continua para o próximo padrão
            }
        }
        return "Não Encontrado (N/E)";
    }
    
    public String ExtractDanfeNumber(String textoCompleto) {
        if (textoCompleto == null || textoCompleto.isEmpty()) {
            return "Não Encontrado (N/E)";
        }
        
        String[] patterns = {
            // Padrão com pipe e quebras de linha (como vem do Tabula)
            "NF-e[\\s\\|\\n]*N[ºÂ°]?[\\s\\|\\n]*S[eé]rie[\\s\\|\\n]*(\\d{3}\\.\\d{3})",
            "NF-e[\\s\\|\\n]*N[ºÂ°]?[\\s\\|\\n]*S[eé]rie[\\s\\|\\n]*(\\d{6,})",
            // Procura por "Série" seguido do número
            "\\b(\\d{5,6})\\b",
            "S[eé]rie[\\s\\|\\n]+(\\d{6,})",
            // Padrão direto: pega número com 6-9 dígitos após "Nº" ou "NF-e"
            "N[ºÂ°][\\s\\|\\n]+(\\d{6,5})",
            "N[ºÂ°][\\s\\|\\n]+(\\d{5,4})",
            "NF-e[\\s\\|\\n]+N[ºÂ°]?[\\s\\|\\n]+(\\d{3}\\.\\d{3})",
            "NF-e[\\s\\|\\n]+N[ºÂ°]?[\\s\\|\\n]+(\\d{6,})",
            // Outros padrões comuns
            "NOTA\\s+FISCAL[^0-9]*(\\d{6,})",
            "DANFE[^0-9]*(\\d{6,})"
        };
        
        String resultado = extractWithMultiplePatterns(textoCompleto, patterns);
        
        // Remove pontos se houver
        if (resultado != null && !resultado.contains("N/E")) {
            resultado = resultado.replace(".", "");
        }
        
        return resultado;
    }
    
    public String ExtractTotalNumber(String textoCompleto) {
        if (textoCompleto == null || textoCompleto.isEmpty()) {
            return "Não Encontrado (N/E)";
        }
        
        String[] patterns = {
            //novos patterns
            "VALOR\\s+TOTAL\\s+DA\\s+NOTA[\\s\\|\\n]+.*?(\\d{1,3}(?:\\.\\d{3})*,\\d{1,2})",
            "(?:#1[ºÂ°o]:)\\s*(\\d{1,3}(?:\\.\\d{3})*,\\d{2})",
            "VALOR\\s+TOTAL\\s+DOS\\s+PRODUTOS[\\s\\|\\n]+.*?(\\d{1,3}(?:\\.\\d{3})*,\\d{1,2})",
            "VALOR\\s+TOTAL\\s+DA\\s+NOTA[\\s\\|\\n]+[,\\d\\s]+(\\d{1,3}(?:\\.\\d{3})*,\\d{2})",
            "(?:#1º:)\\s*(\\d{1,3}(?:\\.\\d{3})*,\\d{2})",
            "VALOR\\s+TOTAL\\s+DOS\\s+PRODUTOS[\\s\\|\\n]+[,\\d\\s]+(\\d{1,3}(?:\\.\\d{3})*,\\d{2})",
            
            //antigos
            "VALOR\\s+TOTAL\\s+DA\\s+NOTA[\\s\\|\\n]+(\\d{1,3}(?:\\.\\d{3})*,\\d{2})",
            "VLR\\.\\s+TOTAL\\.[\\s\\|\\n]+(\\d{1,3}(?:\\.\\d{3})*,\\d{2})",
            "VALOR\\s+TOTAL\\s+DA\\s+NOTA[^0-9]*(\\d{1,3}(?:\\.\\d{3})*,\\d{2})",
            "VALOR\\s+TOTAL\\s+DOS\\s+PRODUTOS[^0-9]*(\\d{1,3}(?:\\.\\d{3})*,\\d{2})",
            "TOTAL\\s+(?:DA\\s+)?NOTA[^0-9]*(\\d{1,3}(?:\\.\\d{3})*,\\d{2})",
            "VALOR\\s+TOTAL[^0-9]*(\\d{1,3}(?:\\.\\d{3})*,\\d{2})",
            "TOTAL\\s[^0-9]*(\\d{1,3}(?:\\.\\d{3})*,\\d{2})",
            "VALOR\\s+TOTAL\\s+DOS\\s+PRODUTOS\\n\\s[^0-9]*(\\d{1,3}(?:\\.\\d{3})*,\\d{2})",
        };
        
        return extractWithMultiplePatterns(textoCompleto, patterns);
    }
    
    public String ExtracPlacaVeiculo(String textoCompleto) {
        if (textoCompleto == null || textoCompleto.isEmpty()) {
            return "Não Encontrado (N/E)";
        }
        
        String[] patterns = {
            "PLACA\\s+DO\\s+VE[ÍI]CULO[^A-Z0-9]*(\\w{7})",  // Pega GIL7F05
            "PLACA\\s+DO\\s+VE[ÍI]CULO[^A-Z0-9]*([A-Z]{3}[-\\s]?\\d[A-Z0-9]\\d{2})",
            "PLACA\\s+DO\\s+VE[ÍI]CULO[^A-Z0-9]*([A-Z]{3}[-\\s]?\\d{4})",
            "PLACA[^A-Z0-9]*([A-Z]{3}\\d[A-Z0-9]\\d{2})",
            "PLACA[^A-Z0-9]*([A-Z]{3}\\d{4})"
        };
        
        return extractWithMultiplePatterns(textoCompleto, patterns);
    }
    
    public String ExtractRazaoSocial(String textoCompleto) {
        if (textoCompleto == null || textoCompleto.isEmpty()) {
            return "Não Encontrado (N/E)";
        }
        
        String[] patterns = {
            //novos
            "RECEBEMOS\\s+DE\\s+\\d*\\s*([A-Z0-9][A-Z0-9\\s&.-]+?(?:LTDA|ME|EPP|EIRELI|S\\.?A\\.?))",
            "RECEBEMOS\\s+DE\\s+([A-Z0-9][A-Z\\s&.-]+?(?:LTDA|ME|EPP|EIRELI|S\\.?A\\.?))",
            // Padrão para pegar "JULIO JULIO MINERACAO LTDA" que aparece logo após o número da nota
            "\\d{3}\\.\\d{3}[\\s\\n]+\\d+[\\s\\n]+([A-Z][A-Z\\s&.-]+(?:LTDA|ME|EPP|EIRELI|S\\.?A\\.?|LTDA\\.?))",
            // Outros padrões
            "RECEBEMOS\\s+DE\\s+([A-Z][A-Z\\s&.-]+(?:LTDA|ME|EPP|EIRELI|S\\.?A\\.?))",
            "(?:NOME|RAZ[ÃA]O\\s+SOCIAL)[\\s\\|]*(.*?)(?:CNPJ|CPF|\\n)",
            "DESTINAT[ÁA]RIO.*?\\n\\s*([A-Z][A-Z\\s&.-]+(?:LTDA|ME|EPP|EIRELI|S\\.A\\.|SA)?)"
        };
        
        String resultado = extractWithMultiplePatterns(textoCompleto, patterns);
        
        // Limpar números e pipes
        if (resultado != null && !resultado.contains("N/E")) {
            resultado = resultado
                .replaceAll("\\d+", "")
                .replace("|", "")
                .replaceAll("\\s+", " ")
                .trim();
        }
        
        return resultado;
    }
    
    public String ExtractDate(String textoCompleto) {
        if (textoCompleto == null || textoCompleto.isEmpty()) {
            return "Não Encontrado (N/E)";
        }
        
        String[] patterns = {
            //novos
            "(\\d{2}/\\d{2}/\\d{4})\\s+\\d{2}:\\d{2}:\\d{2}\\s+-\\d{2}:\\d{2}\\s+[\\s\\S]*?DATA\\s*/?HORA\\s*/?UTC\\s+DE\\s+SA[IÍ]DA",
            "\\d{2}/\\d{2}/\\d{4}(?=\\s+\\d{2}:\\d{2}:\\d{2})",
            "[D]?ATA\\s*/\\s*HORA\\s*/\\s*UTC\\s+DE\\s+SA[IÍA]DA[\\s\\|\\n]+(\\d{2}/\\d{2}/\\d{4})",
            "EMISS[ÃA]O[\\s\\|\\n]+(\\d{2}/\\d{2}/\\d{4})",
            "ATA/HORA/UTC\\s+DE\\s+SA[IÍ]DA\\s+(\\d{2}/\\d{2}/\\d{4})",
            "DATA/HORA/UTC\\s+DE\\s+SA[IÍ]DA\\s+(\\d{2}/\\d{2}/\\d{4})",
            "EMISS[ÃA]O[\\s\\|\\n]+(\\d{2}/\\d{2}/\\d{4})",
            // Padrão específico que aparece no seu PDF: "17/10/2025" próximo a outras coisas
            "(\\d{2}/\\d{2}/\\d{4})",
            // Padrão para data de emissão
            "DATA\\s+DE\\s+EMISS[ÃA]O[\\s\\|]*(\\d{2}/\\d{2}/\\d{4})",
            "EMISS[ÃA]O[\\s\\|\\n]+(\\d{2}/\\d{2}/\\d{4})",
            "DATA\\s+DE\\s+SA[ÍI]DA[\\s\\|]*(\\d{2}/\\d{2}/\\d{4})",
            "DATA\\s+DE\\s+ENTRADA[\\s\\|]*(\\d{2}/\\d{2}/\\d{4})",
            // Padrão genérico
            "DATA[\\s\\|]*(\\d{2}/\\d{2}/\\d{4})",
            // Formato com traço
            "DATA\\s+DE\\s+EMISS[ÃA]O[\\s\\|]*(\\d{2}-\\d{2}-\\d{4})",
            "(\\d{2}-\\d{2}-\\d{4})",
            //formato com data e hora
            "DATA\\s*/\\s*HORA\\s*/\\s*UTC\\s+DE\\s+EMISS[ÃA]O[\\s\\|\\n]+(\\d{2}/\\d{2}/\\d{4})",
        };
        
        String resultado = extractWithMultiplePatterns(textoCompleto, patterns);
        
        // Normalizar formato (trocar - por /)
        if (resultado != null && !resultado.contains("N/E")) {
            resultado = resultado.replace("-", "/");
        }
        
        return resultado;
    }
    
    public String ExtractObra(String textoCompleto) {
        if (textoCompleto == null || textoCompleto.isEmpty()) {
            return "Não Encontrado (N/E)";
        }
        
        String[] patterns = {
            "OBRA[^\\n:]*[:\\-]?\\s*([A-Z0-9][A-Z0-9\\s.-]+)",
            "DESTINO[^\\n:]*[:\\-]?\\s*([A-Z0-9][A-Z0-9\\s.-]+)",
            "LOCAL\\s+(?:DE\\s+)?ENTREGA[^\\n:]*[:\\-]?\\s*([A-Z0-9][A-Z0-9\\s.-]+)"
        };
        
        return extractWithMultiplePatterns(textoCompleto, patterns);
    }
    
    // Método alternativo: extrai número procurando no texto bruto
    public String ExtractDanfeNumberSimples(String textoCompleto) {
        if (textoCompleto == null || textoCompleto.isEmpty()) {
            return "Não Encontrado (N/E)";
        }
        
        try {
            // Procura por sequência de 6-9 dígitos que pode ser a nota
            // Geralmente aparece após "Série" ou perto de "NF-e"
            Pattern pattern = Pattern.compile("\\b(\\d{6,9})\\b");
            Matcher matcher = pattern.matcher(textoCompleto);
            
            // Pega os primeiros números encontrados
            List<String> numeros = new ArrayList<>();
            while (matcher.find() && numeros.size() < 10) {
                String num = matcher.group(1);
                // Ignora CNPJ (muito longo) e números muito curtos
                if (num.length() >= 6 && num.length() <= 9) {
                    numeros.add(num);
                }
            }
            
            // Geralmente o número da nota é um dos primeiros números de 6-7 dígitos
            for (String num : numeros) {
                if (num.length() >= 6 && num.length() <= 7) {
                    return num;
                }
            }
            
            return numeros.isEmpty() ? "Não Encontrado (N/E)" : numeros.get(0);
            
        } catch (Exception e) {
            System.err.println("Erro em ExtractDanfeNumberSimples: " + e.getMessage());
            return "Não Encontrado (N/E)";
        }
    }
    public String ExtractUnidade(String textoCompleto){
        if(textoCompleto == null || textoCompleto.isEmpty()){
            return "Não Encontrado (N/E)";
        }
        
        String[] patterns =
        { "^(\\d+)\\s+" +
                "([A-ZÀ-Ú][A-ZÀ-Úa-zà-ú0-9\\s/\\-\\.]+?)\\s+" +
                "\\d{8}\\s+" +
                "\\d{3}\\s+" +
                "[\\d\\.]+\\s+" +
                "([A-Z0-9]{1,6})\\s+" +         // 👈 UNID (TON, KG, UN, M3, etc)
                "[\\d,]+\\s+" +
                "(\\d{1,3}(?:\\.\\d{3})*,\\d{2})\\s+" +
                "(\\d{1,3}(?:\\.\\d{3})*,\\d{2})" +
                "^(\\d+)\\s+" +
                "([A-ZÀ-Ú][A-ZÀ-Úa-zà-ú0-9\\s/\\-\\.]+?)\\s+" +
                ".*?\\s([A-Z0-9]{1,6})\\s+" +   // 👈 UNID
                ".*?" +
                "(\\d{1,3}(?:\\.\\d{3})*,\\d{2})\\s+" +
                "(\\d{1,3}(?:\\.\\d{3})*,\\d{2})" +
                "^(\\d+)\\s+" +
                "([A-ZÀ-Ú][A-ZÀ-Úa-zà-ú0-9\\s/\\-\\.]+?)\\s+" +
                ".*?" +
                "(\\d{1,3}(?:\\.\\d{3})*,\\d{2})\\s+" +
                "(\\d{1,3}(?:\\.\\d{3})*,\\d{2})"
        
        };
         return extractWithMultiplePatterns(textoCompleto, patterns);
    } 
     
    
    // Método útil para debug
    public void debugExtraction(String textoCompleto) {
        if (textoCompleto == null || textoCompleto.isEmpty()) {
            System.out.println("\n=== DEBUG DE EXTRAÇÃO ===");
            System.out.println("ERRO: Texto completo está vazio ou nulo!");
            System.out.println("========================\n");
            return;
        }
        
        System.out.println("\n=== DEBUG DE EXTRAÇÃO ===");
        System.out.println("Primeiros 500 caracteres do texto:");
        System.out.println(textoCompleto.substring(0, Math.min(500, textoCompleto.length())));
        System.out.println("\n--- Dados Extraídos ---");
        System.out.println("Nota (regex): " + ExtractDanfeNumber(textoCompleto));
        System.out.println("Nota (simples): " + ExtractDanfeNumberSimples(textoCompleto));
        System.out.println("Total: " + ExtractTotalNumber(textoCompleto));
        System.out.println("Placa: " + ExtracPlacaVeiculo(textoCompleto));
        System.out.println("Razão Social: " + ExtractRazaoSocial(textoCompleto));
        System.out.println("Data: " + ExtractDate(textoCompleto));
        System.out.println("Obra: " + ExtractObra(textoCompleto));
        System.out.println("========================\n");
    }
}