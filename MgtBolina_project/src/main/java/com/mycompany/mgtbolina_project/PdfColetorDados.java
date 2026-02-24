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
        if (text == null || text.isEmpty()) return null;
        try {
            text = normalizarTexto(text);
            Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
            Matcher matcher = pattern.matcher(text);
            if (matcher.find() && matcher.groupCount() >= 1) {
                String resultado = matcher.group(1);
                if (resultado != null) {
                    return normalizarTexto(resultado.trim());
                }
            }
        } catch (Exception e) {
            System.err.println("Erro ao processar regex: " + regex + " | " + e.getMessage());
        }
        return null;
    }
    
    private String extractWithMultiplePatterns(String text, String[] patterns) {
        if (text == null || text.isEmpty()) return "Não Encontrado (N/E)";
        for (String regex : patterns) {
            try {
                String result = FindFirstGroup(text, regex);
                if (result != null && !result.isEmpty()) return result;
            } catch (Exception e) {
                System.err.println("Erro ao tentar padrão: " + regex);
            }
        }
        return "Não Encontrado (N/E)";
    }

    // ─────────────────────────────────────────────────────────────────────────
    // NÚMERO DA NOTA
    // Casos conhecidos:
    //   Ventilar:  "N°. 175.384"  (número com ponto)
    //   Ferrari:   "Nº 43730"     (número sem ponto, label diferente)
    // ─────────────────────────────────────────────────────────────────────────
    public String ExtractDanfeNumber(String textoCompleto) {
        if (textoCompleto == null || textoCompleto.isEmpty()) return "Não Encontrado (N/E)";
        
        String[] patterns = {
            // ✅ Julio: "NF-e Nº 861.390 Série 4" — célula começa com NF-e
            "NF-e[\\s\\|\\n]*N[ºÂ°]?[\\s\\|\\n]*S[eé]rie[\\s\\|\\n]*(\\d{3}\\.\\d{3})",
            "NF-e[\\s\\|\\n]*N[ºÂ°]?[\\s\\|\\n]*S[eé]rie[\\s\\|\\n]*(\\d{6,})",
            "NF-e\\s+Nº\\s+(\\d{1,3}\\.\\d{3})\\b",
            "NF-e\\s+Nº\\s+(\\d{3,9})\\b",
            "N[ºo]\\s*\\.?\\s*(\\d{1,3}(?:\\.\\d{3})*|\\d+)",
            // ✅ Ventilar: "N°. 175.384" — número COM ponto como separador de milhar
            "N[°º][\\.]\\s*(\\d{1,3}\\.\\d{3})\\b",
            // Ventilar sem ponto no label: "N° 175.384"
            "N[°º]\\s*(\\d{1,3}\\.\\d{3})\\b",
            // Ferrari: "Nº 43730" — número simples
            "Nº\\s*(\\d{3,9})\\b",
            // Genérico: "N° 43730"
            "N[°º\\.][\\s\\.]*(\\d{4,9})\\b",
            // Com "SÉRIE" próximo (texto de tabela Ventilar)
            "(\\d{1,3}\\.\\d{3})[\\s\\n\\|]*S[ÉE]RIE",
            "(\\d{4,9})[\\s\\n\\|]*S[ÉE]RIE",
            // Fallbacks
            "NOTA\\s+FISCAL[^0-9]*(\\d{1,3}\\.\\d{3})",
            "NOTA\\s+FISCAL[^0-9]*(\\d{6,})",
            "DANFE[^0-9]*(\\d{1,3}\\.\\d{3})",
        };
        
        String resultado = extractWithMultiplePatterns(textoCompleto, patterns);
        if (resultado != null && !resultado.contains("N/E")) {
            resultado = resultado.replace(".", "");
        }
        return resultado;
    }
    
    // ─────────────────────────────────────────────────────────────────────────
    // VALOR TOTAL DA NOTA
    // Casos conhecidos:
    //   Ventilar:  "VALOR TOTAL DA NOTA\n0,00 0,00 0,00 0,00 0,00 468,31"
    //              → múltiplos valores na linha, o ÚLTIMO é o correto
    //   Ferrari:   "VALOR TOTAL DA NOTA\n,00 ,00 ,00 ,00 50,37"
    //              → valores sem dígito antes da vírgula (,00), o ÚLTIMO é o correto
    //   Tabela:    "ALOR TOTAL DA NOTA 468,31" (Tabula corta o "V")
    // ─────────────────────────────────────────────────────────────────────────
    public String ExtractTotalNumber(String textoCompleto) {
        if (textoCompleto == null || textoCompleto.isEmpty()) return "Não Encontrado (N/E)";
        
            try {
             String texto = normalizarTexto(textoCompleto);
            
            // 1. Nextline: texto bruto Ventilar/Ferrari — pega o ÚLTIMO valor da linha seguinte
            //    "VALOR TOTAL DA NOTA\n0,00 0,00 0,00 468,31"
            Pattern p = Pattern.compile(
                "VALOR\\s+TOTAL\\s+DA\\s+NOTA\\s*\\n([^\\n]+)",
                Pattern.CASE_INSENSITIVE
            );

            Matcher m = p.matcher(texto);
            if (m.find()) {
                String linha = m.group(1);

                Pattern pv = Pattern.compile("(\\d{1,3}(?:\\.\\d{3})*,\\d{2})");
                Matcher mv = pv.matcher(linha);

                String ultimo = null;
                while (mv.find()) {
                   ultimo = mv.group(1);
               }

                if (ultimo != null) {
                    return ultimo;
                }
            }
            
            // 2. Inline + pipe/EOL: tabela Julio/Ferrari — "VALOR TOTAL DA NOTA 1.934,50\n"
            Pattern p2 = Pattern.compile(
                "[AV]ALOR\\s+TOTAL\\s+DA\\s+NOTA\\s+(\\d{1,3}(?:\\.\\d{3})*,\\d{2})\\s*(?:\\||\\n|$)",
                Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
            Matcher m2 = p2.matcher(texto);
            if (m2.find()) {
                System.out.println("\u2713 Valor total (inline+pipe): " + m2.group(1));
                return m2.group(1);
            }
            
            // 3. Inline simples fallback
            Pattern p3 = Pattern.compile(
                "[AV]ALOR\\s+TOTAL\\s+DA\\s+NOTA\\s+(\\d{1,3}(?:\\.\\d{3})*,\\d{2})",
                Pattern.CASE_INSENSITIVE);
            Matcher m3 = p3.matcher(texto);
            if (m3.find()) return m3.group(1);
            // 3.1 Ventilar / layout tabela quebrada (valor espalhado em bloco)
Pattern pVentilar = Pattern.compile(
    "VALOR\\s+TOTAL\\s+DA\\s+NOTA(.{0,500})",
    Pattern.CASE_INSENSITIVE | Pattern.DOTALL
);

Matcher mVentilar = pVentilar.matcher(texto);
if (mVentilar.find()) {
    String bloco = mVentilar.group(1);

    Pattern pv = Pattern.compile("(\\d{1,3}(?:\\.\\d{3})*,\\d{2})");
    Matcher mv = pv.matcher(bloco);

    double maiorValor = 0.0;
    String valorFinal = null;

    while (mv.find()) {
        String valorStr = mv.group(1)
                .replace(".", "")
                .replace(",", ".");
        try {
            double valor = Double.parseDouble(valorStr);
            if (valor > maiorValor) {
                maiorValor = valor;
                valorFinal = mv.group(1);
            }
        } catch (Exception ignored) {}
    }

    if (valorFinal != null) {
        System.out.println("✓ Valor total (Ventilar bloco inteligente): " + valorFinal);
        return valorFinal;
    }
}
            // 4. "VALOR TOTAL DOS PRODUTOS" nextline
            Pattern p4 = Pattern.compile(
                "VALOR\\s+TOTAL\\s+DOS\\s+PRODUTOS[\\s\\|]*\\n([^\\n]+)",
                Pattern.CASE_INSENSITIVE);
            Matcher m4 = p4.matcher(texto);
            if (m4.find()) {
                String linha = m4.group(1);
                List<String> valores = new ArrayList<>();
                Pattern pv = Pattern.compile("\\d{1,3}(?:\\.\\d{3})*,\\d{2}");
                Matcher mv = pv.matcher(linha);
                while (mv.find()) valores.add(mv.group());
                if (!valores.isEmpty()) return valores.get(valores.size() - 1);
            }
            
            // 5. "VALOR TOTAL DOS PRODUTOS X,XX |" inline tabela
            Pattern p5 = Pattern.compile(
                "VALOR\\s+TOTAL\\s+DOS\\s+PRODUTOS\\s+(\\d{1,3}(?:\\.\\d{3})*,\\d{2})\\s*(?:\\||\\n|$)",
                Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
            Matcher m5 = p5.matcher(texto);
            if (m5.find()) return m5.group(1);
            
            // 6. NFS-e: "VALOR TOTAL DO SERVIÇO = R$ 5.800,00"
            Pattern p6 = Pattern.compile(
                "VALOR\\s+TOTAL\\s+DO\\s+SERVI[\\u00c7C]O\\s*=\\s*R\\$\\s*(\\d{1,3}(?:\\.\\d{3})*,\\d{2})",
                Pattern.CASE_INSENSITIVE);
            Matcher m6 = p6.matcher(texto);
            if (m6.find()) return m6.group(1);
            
            // 7. "#1º: 50,37" (Ferrari fatura, último fallback)
            Pattern p7 = Pattern.compile(
                "#1[\u00b0\u00ba]o?:\\s*(\\d{1,3}(?:\\.\\d{3})*,\\d{2})",
                Pattern.CASE_INSENSITIVE);
            Matcher m7 = p7.matcher(texto);
            if (m7.find()) return m7.group(1);
            
        } catch (Exception e) {
            System.err.println("Erro em ExtractTotalNumber: " + e.getMessage());
        }
        
        return "Não Encontrado (N/E)";
    }
    
    // ─────────────────────────────────────────────────────────────────────────
    // PLACA DO VEÍCULO
    // ─────────────────────────────────────────────────────────────────────────
    public String ExtracPlacaVeiculo(String textoCompleto) {
        if (textoCompleto == null || textoCompleto.isEmpty()) return "Não Encontrado (N/E)";
        String[] patterns = {
            "PLACA\\s+DO\\s+VE[ÍI]CULO[^A-Z0-9]*(\\w{7})",
            "PLACA\\s+DO\\s+VE[ÍI]CULO[^A-Z0-9]*([A-Z]{3}[-\\s]?\\d[A-Z0-9]\\d{2})",
            "PLACA\\s+DO\\s+VE[ÍI]CULO[^A-Z0-9]*([A-Z]{3}[-\\s]?\\d{4})",
            "PLACA[^A-Z0-9]*([A-Z]{3}\\d[A-Z0-9]\\d{2})",
            "PLACA[^A-Z0-9]*([A-Z]{3}\\d{4})"
        };
        return extractWithMultiplePatterns(textoCompleto, patterns);
    }
    
    // ─────────────────────────────────────────────────────────────────────────
    // FORNECEDOR (RAZÃO SOCIAL)
    // Casos conhecidos:
    //   Ventilar: "RECEBEMOS DE VENTILAR MAQUINAS E FERRAMENTAS LTDA OS PRODUTOS..."
    //   Ferrari:  "RECEBEMOS DE 2 FERRARI MATERIAIS ELETRICOS SOROCABA LTDA OS PRODUTOS..."
    //             (tem um número "2" antes do nome!)
    // ─────────────────────────────────────────────────────────────────────────
    public String ExtractRazaoSocial(String textoCompleto) {
        if (textoCompleto == null || textoCompleto.isEmpty()) return "Não Encontrado (N/E)";
        
        String[] patterns = {
            // PRINCIPAL: ancora em "OS PRODUTOS" no final — pega tudo entre "DE (opcional num)" e "OS PRODUTOS"
            "RECEBEMOS\\s+DE\\s+(?:\\d+\\s+)?([A-Z][A-Z0-9\\s&./\\-]+?(?:LTDA|ME|EPP|EIRELI|S\\.?A\\.?))\\s+OS\\s+PRODUTOS",
            // Fallback sem "OS PRODUTOS" (fim de linha)
            "RECEBEMOS\\s+DE\\s+(?:\\d+\\s+)?([A-Z][A-Z0-9\\s&./\\-]+?(?:LTDA|ME|EPP|EIRELI|S\\.?A\\.?))\\b",
            // Outros layouts
            "(?:NOME|RAZ[ÃA]O\\s+SOCIAL)[\\s\\|]*(.*?)(?:CNPJ|CPF|\\n)",
            "DESTINAT[ÁA]RIO.*?\\n\\s*([A-Z][A-Z\\s&.-]+(?:LTDA|ME|EPP|EIRELI|S\\.A\\.|SA)?)"
        };
        
        String resultado = extractWithMultiplePatterns(textoCompleto, patterns);
        if (resultado != null && !resultado.contains("N/E")) {
            resultado = resultado.replace("|", "").replaceAll("\\s+", " ").trim();
        }
        return resultado;
    }
    
    // ─────────────────────────────────────────────────────────────────────────
    // DATA DE EMISSÃO
    // Casos conhecidos:
    //   Ventilar (texto bruto): "DATA DA EMISSÃO\nMGT BOLINA ... 07/11/2025"
    //                           → data está na PRÓXIMA linha, após o label
    //   Ferrari (texto bruto):  "DATA /HORA/UTC DE EMISSÃO\nMGT BOLINA ... 03/12/2025 12:53:28"
    //                           → data+hora juntos, pegar só dd/mm/yyyy
    //   Tabela Ventilar:        "DATA DA EMISSÃO 07/11/2025 |" → inline
    // ─────────────────────────────────────────────────────────────────────────
    public String ExtractDate(String textoCompleto) {
        if (textoCompleto == null || textoCompleto.isEmpty()) return "Não Encontrado (N/E)";
        
        String[] patterns = {
            // Inline (tabela Ventilar): "DATA DA EMISSÃO 07/11/2025"
            "DATA\\s+DA\\s+EMISS[ÃA]O\\s+(\\d{2}/\\d{2}/\\d{4})",
            // Lookahead até 80 chars (texto bruto Ventilar: label na linha, data na próxima)
            "DATA\\s+DA\\s+EMISS[ÃA]O[\\s\\S]{1,80}?(\\d{2}/\\d{2}/\\d{4})",
            // Ferrari: "DATA /HORA/UTC DE EMISSÃO ... 03/12/2025 12:53:28"
            "EMISS[ÃA]O[\\s\\S]{1,80}?(\\d{2}/\\d{2}/\\d{4})",
            // Data antes de hora (dd/mm/yyyy seguido de hh:mm:ss)
            "(\\d{2}/\\d{2}/\\d{4})\\s+\\d{2}:\\d{2}:\\d{2}",
            // Data/hora UTC de saída
            "DATA\\s*/\\s*HORA\\s*/\\s*UTC\\s+DE\\s+SA[IÍ]DA[\\s\\S]{1,20}?(\\d{2}/\\d{2}/\\d{4})",
            // Genérico — ÚLTIMO para não pegar datas de vencimento de fatura
            "DATA\\s+DE\\s+EMISS[ÃA]O[\\s\\|]*(\\d{2}/\\d{2}/\\d{4})",
            // Formato com traço
            "DATA\\s+DE\\s+EMISS[ÃA]O[\\s\\|]*(\\d{2}-\\d{2}-\\d{4})",
            "(\\d{2}-\\d{2}-\\d{4})",
        };
        
        String resultado = extractWithMultiplePatterns(textoCompleto, patterns);
        if (resultado != null && !resultado.contains("N/E")) {
            resultado = resultado.replace("-", "/");
        }
        return resultado;
    }
    
    public String ExtractObra(String textoCompleto) {
        if (textoCompleto == null || textoCompleto.isEmpty()) return "Não Encontrado (N/E)";
        String[] patterns = {
            "OBRA[^\\n:]*[:\\-]?\\s*([A-Z0-9][A-Z0-9\\s.-]+)",
            "DESTINO[^\\n:]*[:\\-]?\\s*([A-Z0-9][A-Z0-9\\s.-]+)",
            "LOCAL\\s+(?:DE\\s+)?ENTREGA[^\\n:]*[:\\-]?\\s*([A-Z0-9][A-Z0-9\\s.-]+)"
        };
        return extractWithMultiplePatterns(textoCompleto, patterns);
    }
    
    // Método alternativo: extrai número procurando no texto bruto
    public String ExtractDanfeNumberSimples(String textoCompleto) {
        if (textoCompleto == null || textoCompleto.isEmpty()) return "Não Encontrado (N/E)";
        try {
            Pattern pattern = Pattern.compile("\\b(\\d{6,9})\\b");
            Matcher matcher = pattern.matcher(textoCompleto);
            List<String> numeros = new ArrayList<>();
            while (matcher.find() && numeros.size() < 10) {
                String num = matcher.group(1);
                if (num.length() >= 6 && num.length() <= 9) numeros.add(num);
            }
            for (String num : numeros) {
                if (num.length() >= 6 && num.length() <= 7) return num;
            }
            return numeros.isEmpty() ? "Não Encontrado (N/E)" : numeros.get(0);
        } catch (Exception e) {
            System.err.println("Erro em ExtractDanfeNumberSimples: " + e.getMessage());
            return "Não Encontrado (N/E)";
        }
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