package com.mycompany.mgtbolina_project;

import javafx.scene.control.ChoiceDialog;
import javafx.application.Platform;
import java.util.List;
import java.util.Optional;

/**
 * Classe utilitária para permitir seleção de abas do Excel pelo usuário
 * Usando JavaFX em vez de Scanner para compatibilidade com a InterfaceGrafica
 */
public class SeletorDeAbas {
    
    /**
     * Permite o usuário selecionar uma aba do Excel usando um dialog gráfico
     * Se houver apenas 1 aba, retorna ela automaticamente
     * Se houver múltiplas abas, mostra um ChoiceDialog para o usuário escolher
     * 
     * @param exporter Instância do ExportadorExcel
     * @param excelFilePath Caminho do arquivo Excel
     * @return Nome da aba selecionada, ou null se houver erro ou cancelamento
     */
    public static String selecionarAba(ExportadorExcel exporter, String excelFilePath) {
        
        // Lista todas as abas disponíveis
        List<String> abas = exporter.listarAbas(excelFilePath);
        
        if (abas == null || abas.isEmpty()) {
            System.err.println("❌ Erro: Nenhuma aba encontrada no arquivo Excel!");
            return null;
        }
        
        // Se houver apenas 1 aba, usa ela automaticamente
        if (abas.size() == 1) {
            String abaUnica = abas.get(0);
            System.out.println("\n✅ Apenas 1 aba encontrada: '" + abaUnica + "'");
            System.out.println("   Usando automaticamente...\n");
            return abaUnica;
        }
        
        // Se houver múltiplas abas, mostra dialog para escolha
        final String[] abaSelecionada = {null};
        
        // Precisa rodar na thread do JavaFX
        Platform.runLater(() -> {
            ChoiceDialog<String> dialog = new ChoiceDialog<>(abas.get(0), abas);
            
            dialog.setTitle("Seleção de Aba (Obra)");
            dialog.setHeaderText("📊 Múltiplas abas encontradas no Excel");
            dialog.setContentText(
                "Foram encontradas " + abas.size() + " abas/planilhas.\n" +
                "Cada aba representa uma obra diferente.\n\n" +
                "Selecione a aba onde os dados devem ser inseridos:"
            );
            
            // Estilização
            dialog.getDialogPane().setStyle(
                "-fx-background-color: #1E293B; " +
                "-fx-font-family: 'Inter';"
            );
            
            Optional<String> resultado = dialog.showAndWait();
            
            resultado.ifPresent(aba -> {
                abaSelecionada[0] = aba;
                System.out.println("\n✅ Aba selecionada: '" + aba + "'\n");
            });
        });
        
        // Aguarda a seleção (blocking)
        try {
            Thread.sleep(100);
            while (abaSelecionada[0] == null) {
                Thread.sleep(100);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        return abaSelecionada[0];
    }
    
    /**
     * Versão assíncrona para uso dentro da thread do JavaFX
     * Retorna imediatamente com callback
     */
    public static void selecionarAbaAsync(ExportadorExcel exporter, String excelFilePath, 
                                          SelecionarAbaCallback callback) {
        
        List<String> abas = exporter.listarAbas(excelFilePath);
        
        if (abas == null || abas.isEmpty()) {
            callback.onErro("Nenhuma aba encontrada no arquivo Excel!");
            return;
        }
        
        // Se houver apenas 1 aba, usa automaticamente
        if (abas.size() == 1) {
            String abaUnica = abas.get(0);
            System.out.println("✅ Apenas 1 aba: '" + abaUnica + "' - usando automaticamente");
            callback.onAbaSelecionada(abaUnica);
            return;
        }
        
        // Múltiplas abas - mostra dialog
        Platform.runLater(() -> {
            ChoiceDialog<String> dialog = new ChoiceDialog<>(abas.get(0), abas);
            
            dialog.setTitle("Seleção de Aba (Obra)");
            dialog.setHeaderText("📊 Múltiplas abas encontradas no Excel");
            dialog.setContentText(
                "Foram encontradas " + abas.size() + " abas/planilhas.\n" +
                "Cada aba representa uma obra diferente.\n\n" +
                "Selecione a aba onde os dados devem ser inseridos:"
            );
            
            // Estilização para combinar com a interface
            dialog.getDialogPane().setStyle(
                "-fx-background-color: #1E293B; " +
                "-fx-font-family: 'Inter';"
            );
            
            Optional<String> resultado = dialog.showAndWait();
            
            resultado.ifPresentOrElse(
                aba -> {
                    System.out.println("✅ Aba selecionada: '" + aba + "'");
                    callback.onAbaSelecionada(aba);
                },
                () -> {
                    System.out.println("⚠️ Seleção cancelada pelo usuário");
                    callback.onCancelado();
                }
            );
        });
    }
    
    /**
     * Interface de callback para seleção assíncrona
     */
    public interface SelecionarAbaCallback {
        void onAbaSelecionada(String nomeAba);
        default void onErro(String mensagem) {
            System.err.println("❌ Erro: " + mensagem);
        }
        default void onCancelado() {
            System.out.println("⚠️ Seleção de aba cancelada");
        }
    }
}