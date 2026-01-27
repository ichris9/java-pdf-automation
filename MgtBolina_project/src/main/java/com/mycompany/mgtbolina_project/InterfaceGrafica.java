package com.mycompany.mgtbolina_project;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;

public class InterfaceGrafica extends Application {
    
    private TextArea logArea;
    private Label statusLabel;
    private Label fileNameLabel;
    private Button processButton;
    private Button selectExcelButton;
    private VBox dropZone;
    
    private File pdfFile;
    private File excelFile;
    
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Extrator de Notas Fiscais - MgtBolina");
        
        // Container principal com gradiente
        BorderPane root = new BorderPane();
        root.setStyle(
            "-fx-background-color: linear-gradient(to bottom right, #667eea 0%, #764ba2 50%, #f093fb 100%);"
        );
        
        // Área central
        VBox centerContent = createCenterContent();
        root.setCenter(centerContent);
        
        // Área inferior (log)
        VBox bottomContent = createBottomContent();
        root.setBottom(bottomContent);
        
        Scene scene = new Scene(root, 900, 700);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    private VBox createCenterContent() {
        VBox container = new VBox(30);
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(40));
        
        // Título
        Label titleLabel = new Label("📄 Extrator de Dados de NF-e");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 32));
        titleLabel.setTextFill(Color.WHITE);
        titleLabel.setEffect(createDropShadow());
        
        // Subtítulo
        Label subtitleLabel = new Label("Arraste o PDF ou clique para selecionar");
        subtitleLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
        subtitleLabel.setTextFill(Color.rgb(255, 255, 255, 0.8));
        
        // Drop Zone (área de arrastar e soltar)
        dropZone = createDropZone();
        
        // Nome do arquivo selecionado
        fileNameLabel = new Label("Nenhum arquivo selecionado");
        fileNameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        fileNameLabel.setTextFill(Color.WHITE);
        
        // Botão para selecionar Excel
        selectExcelButton = createStyledButton("📊 Selecionar Planilha Excel", "#10b981");
        selectExcelButton.setOnAction(e -> selectExcelFile());
        
        // Botão de processar
        processButton = createStyledButton("🚀 Processar PDF", "#3b82f6");
        processButton.setDisable(true);
        processButton.setOnAction(e -> processarPDF());
        
        // Status
        statusLabel = new Label("Aguardando arquivo PDF...");
        statusLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        statusLabel.setTextFill(Color.rgb(255, 255, 255, 0.9));
        
        container.getChildren().addAll(
            titleLabel,
            subtitleLabel,
            dropZone,
            fileNameLabel,
            selectExcelButton,
            processButton,
            statusLabel
        );
        
        return container;
    }
    
    private VBox createDropZone() {
        VBox zone = new VBox(20);
        zone.setAlignment(Pos.CENTER);
        zone.setPrefHeight(200);
        zone.setMaxWidth(500);
        zone.setStyle(
            "-fx-background-color: rgba(255, 255, 255, 0.1);" +
            "-fx-border-color: rgba(255, 255, 255, 0.4);" +
            "-fx-border-width: 3;" +
            "-fx-border-style: dashed;" +
            "-fx-border-radius: 15;" +
            "-fx-background-radius: 15;" +
            "-fx-cursor: hand;"
        );
        
        Label iconLabel = new Label("📁");
        iconLabel.setFont(Font.font(60));
        
        Label textLabel = new Label("Arraste o PDF aqui\nou clique para selecionar");
        textLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        textLabel.setTextFill(Color.WHITE);
        textLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        
        zone.getChildren().addAll(iconLabel, textLabel);
        
        // Configurar drag and drop
        setupDragAndDrop(zone);
        
        // Click para selecionar arquivo
        zone.setOnMouseClicked(e -> selectPDFFile());
        
        // Efeitos de hover
        zone.setOnMouseEntered(e -> {
            zone.setStyle(
                "-fx-background-color: rgba(255, 255, 255, 0.2);" +
                "-fx-border-color: rgba(255, 255, 255, 0.6);" +
                "-fx-border-width: 3;" +
                "-fx-border-style: dashed;" +
                "-fx-border-radius: 15;" +
                "-fx-background-radius: 15;" +
                "-fx-cursor: hand;" +
                "-fx-scale-x: 1.02;" +
                "-fx-scale-y: 1.02;"
            );
        });
        
        zone.setOnMouseExited(e -> {
            zone.setStyle(
                "-fx-background-color: rgba(255, 255, 255, 0.1);" +
                "-fx-border-color: rgba(255, 255, 255, 0.4);" +
                "-fx-border-width: 3;" +
                "-fx-border-style: dashed;" +
                "-fx-border-radius: 15;" +
                "-fx-background-radius: 15;" +
                "-fx-cursor: hand;"
            );
        });
        
        return zone;
    }
    
    private void setupDragAndDrop(VBox zone) {
        zone.setOnDragOver((DragEvent event) -> {
            if (event.getGestureSource() != zone && event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });
        
        zone.setOnDragEntered((DragEvent event) -> {
            if (event.getGestureSource() != zone && event.getDragboard().hasFiles()) {
                zone.setStyle(
                    "-fx-background-color: rgba(59, 130, 246, 0.3);" +
                    "-fx-border-color: #3b82f6;" +
                    "-fx-border-width: 3;" +
                    "-fx-border-style: solid;" +
                    "-fx-border-radius: 15;" +
                    "-fx-background-radius: 15;"
                );
            }
            event.consume();
        });
        
        zone.setOnDragExited((DragEvent event) -> {
            zone.setStyle(
                "-fx-background-color: rgba(255, 255, 255, 0.1);" +
                "-fx-border-color: rgba(255, 255, 255, 0.4);" +
                "-fx-border-width: 3;" +
                "-fx-border-style: dashed;" +
                "-fx-border-radius: 15;" +
                "-fx-background-radius: 15;"
            );
            event.consume();
        });
        
        zone.setOnDragDropped((DragEvent event) -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            
            if (db.hasFiles()) {
                List<File> files = db.getFiles();
                if (!files.isEmpty()) {
                    File file = files.get(0);
                    if (file.getName().toLowerCase().endsWith(".pdf")) {
                        pdfFile = file;
                        fileNameLabel.setText("📄 " + file.getName());
                        statusLabel.setText("PDF carregado! Agora selecione a planilha Excel.");
                        statusLabel.setTextFill(Color.rgb(16, 185, 129));
                        checkIfCanProcess();
                        success = true;
                    } else {
                        showError("Por favor, selecione um arquivo PDF!");
                    }
                }
            }
            
            event.setDropCompleted(success);
            event.consume();
        });
    }
    
    private void selectPDFFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecionar PDF da Nota Fiscal");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Arquivos PDF", "*.pdf")
        );
        
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            pdfFile = file;
            fileNameLabel.setText("📄 " + file.getName());
            statusLabel.setText("PDF carregado! Agora selecione a planilha Excel.");
            statusLabel.setTextFill(Color.rgb(16, 185, 129));
            checkIfCanProcess();
        }
    }
    
    private void selectExcelFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecionar Planilha Excel");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Arquivos Excel", "*.xlsx", "*.xls")
        );
        
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            excelFile = file;
            logMessage("✓ Planilha Excel selecionada: " + file.getName());
            statusLabel.setText("Arquivos prontos! Clique em Processar.");
            statusLabel.setTextFill(Color.rgb(16, 185, 129));
            checkIfCanProcess();
        }
    }
    
    private void checkIfCanProcess() {
        processButton.setDisable(pdfFile == null || excelFile == null);
    }
    
    private void processarPDF() {
        if (pdfFile == null || excelFile == null) {
            showError("Por favor, selecione o PDF e a planilha Excel!");
            return;
        }
        
        // Desabilitar botão durante processamento
        processButton.setDisable(true);
        statusLabel.setText("⏳ Processando...");
        statusLabel.setTextFill(Color.rgb(251, 191, 36));
        logArea.clear();
        
        // Processar em thread separada para não travar a UI
        new Thread(() -> {
            try {
                logMessage("===========================================");
                logMessage("📄 INICIANDO PROCESSAMENTO DO PDF");
                logMessage("===========================================");
                logMessage("Arquivo: " + pdfFile.getName());
                logMessage("Destino: " + excelFile.getName());
                logMessage("");
                
                // 1. Extrair texto bruto do PDF
                logMessage("🔍 Etapa 1: Extraindo texto do PDF...");
                PdfLeitor leitor = new PdfLeitor();
                String textoBruto = leitor.ExtractText(pdfFile.getAbsolutePath());
                
                if (textoBruto == null || textoBruto.isEmpty()) {
                    Platform.runLater(() -> {
                        showError("Erro: Não foi possível extrair texto do PDF!");
                        statusLabel.setText("❌ Erro ao processar PDF");
                        statusLabel.setTextFill(Color.rgb(239, 68, 68));
                        processButton.setDisable(false);
                    });
                    return;
                }
                
                logMessage("✓ Texto extraído com sucesso!");
                logMessage("");
                
                // 2. Extrair tabela de produtos
                logMessage("📊 Etapa 2: Extraindo produtos da tabela...");
                ColetorProdutos coletorTabula = new ColetorProdutos();
                List<Produto> listaDeProdutos = coletorTabula.extrairTabelaPDF(pdfFile.getAbsolutePath());
                
                logMessage("✓ Encontrados " + listaDeProdutos.size() + " produtos");
                logMessage("");
                
                // 3. Extrair dados da nota
                logMessage("🔎 Etapa 3: Extraindo dados da nota fiscal...");
                PdfColetorDados coletor = new PdfColetorDados();
                
                String numNota = coletor.ExtractDanfeNumber(textoBruto);
                String numTotal = coletor.ExtractTotalNumber(textoBruto);
                String placaVeiculo = coletor.ExtracPlacaVeiculo(textoBruto);
                String razaoSocial = coletor.ExtractRazaoSocial(textoBruto);
                String data = coletor.ExtractDate(textoBruto);
                
                logMessage("  • Número da Nota: " + numNota);
                logMessage("  • Valor Total: R$ " + numTotal);
                logMessage("  • Placa Veículo: " + placaVeiculo);
                logMessage("  • Fornecedor: " + razaoSocial);
                logMessage("  • Data: " + data);
                logMessage("");
                
                // 4. Mostrar produtos encontrados
                logMessage("📦 Produtos extraídos:");
                for (int i = 0; i < listaDeProdutos.size(); i++) {
                    Produto p = listaDeProdutos.get(i);
                    logMessage("  " + (i+1) + ". " + p.descricao + " - R$ " + p.valorUnitario);
                }
                logMessage("");
                
                // 5. Exportar para Excel
                if (!listaDeProdutos.isEmpty()) {
                    logMessage("💾 Etapa 4: Exportando para Excel...");
                    ExportadorExcel exporter = new ExportadorExcel();
                    exporter.ExportDataTOExcel(
                        excelFile.getAbsolutePath(), 
                        numNota, 
                        numTotal, 
                        data, 
                        placaVeiculo, 
                        razaoSocial, 
                        listaDeProdutos
                    );
                    
                    logMessage("✓ Dados exportados com sucesso!");
                    logMessage("");
                    logMessage("===========================================");
                    logMessage("✅ PROCESSAMENTO CONCLUÍDO COM SUCESSO!");
                    logMessage("===========================================");
                    
                    Platform.runLater(() -> {
                        statusLabel.setText("✅ Processamento concluído!");
                        statusLabel.setTextFill(Color.rgb(16, 185, 129));
                        showSuccess("Dados extraídos e exportados com sucesso!\n\n" +
                                  "Produtos encontrados: " + listaDeProdutos.size() + "\n" +
                                  "Planilha: " + excelFile.getName());
                        processButton.setDisable(false);
                    });
                } else {
                    logMessage("⚠️ AVISO: Nenhum produto foi encontrado!");
                    logMessage("A planilha não será atualizada.");
                    
                    Platform.runLater(() -> {
                        statusLabel.setText("⚠️ Nenhum produto encontrado");
                        statusLabel.setTextFill(Color.rgb(251, 191, 36));
                        showWarning("Nenhum produto foi encontrado no PDF.\n" +
                                  "Verifique se o arquivo está no formato correto.");
                        processButton.setDisable(false);
                    });
                }
                
            } catch (Exception e) {
                e.printStackTrace();
                logMessage("❌ ERRO: " + e.getMessage());
                
                Platform.runLater(() -> {
                    statusLabel.setText("❌ Erro no processamento");
                    statusLabel.setTextFill(Color.rgb(239, 68, 68));
                    showError("Erro ao processar PDF:\n" + e.getMessage());
                    processButton.setDisable(false);
                });
            }
        }).start();
    }
    
    private VBox createBottomContent() {
        VBox container = new VBox(10);
        container.setPadding(new Insets(20));
        container.setStyle("-fx-background-color: rgba(0, 0, 0, 0.3);");
        
        Label logLabel = new Label("📋 Log de Processamento:");
        logLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        logLabel.setTextFill(Color.WHITE);
        
        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setPrefHeight(150);
        logArea.setStyle(
            "-fx-control-inner-background: rgba(0, 0, 0, 0.5);" +
            "-fx-text-fill: white;" +
            "-fx-font-family: 'Consolas', 'Courier New', monospace;" +
            "-fx-font-size: 12px;"
        );
        
        container.getChildren().addAll(logLabel, logArea);
        return container;
    }
    
    private Button createStyledButton(String text, String color) {
        Button button = new Button(text);
        button.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        button.setPrefWidth(300);
        button.setPrefHeight(45);
        button.setStyle(
            "-fx-background-color: " + color + ";" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 10;" +
            "-fx-cursor: hand;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 3);"
        );
        
        // Efeitos de hover
        button.setOnMouseEntered(e -> {
            button.setStyle(
                "-fx-background-color: derive(" + color + ", 20%);" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 10;" +
                "-fx-cursor: hand;" +
                "-fx-scale-x: 1.05;" +
                "-fx-scale-y: 1.05;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 15, 0, 0, 5);"
            );
        });
        
        button.setOnMouseExited(e -> {
            button.setStyle(
                "-fx-background-color: " + color + ";" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 10;" +
                "-fx-cursor: hand;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 3);"
            );
        });
        
        return button;
    }
    
    private DropShadow createDropShadow() {
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.5));
        shadow.setRadius(10);
        shadow.setOffsetY(3);
        return shadow;
    }
    
    private void logMessage(String message) {
        Platform.runLater(() -> {
            logArea.appendText(message + "\n");
        });
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erro");
        alert.setHeaderText("Ocorreu um erro");
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Aviso");
        alert.setHeaderText("Atenção");
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Sucesso");
        alert.setHeaderText("Operação concluída!");
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}