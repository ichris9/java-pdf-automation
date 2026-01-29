package com.mycompany.mgtbolina_project;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.util.List;
import javafx.scene.input.TransferMode;

public class InterfaceGrafica extends Application {
    
    // Cores Estilo Startup
    private static final String COLOR_BG = "#0F172A";
    private static final String COLOR_CARD = "#1E293B";
    private static final String COLOR_ACCENT = "#38BDF8";
    private static final String COLOR_SUCCESS = "#10B981";
    private static final String COLOR_WARNING = "#F59E0B";
    private static final String COLOR_TEXT_MAIN = "#F8FAFC";
    private static final String COLOR_TEXT_DIM = "#94A3B8";

    private TextArea logArea;
    private Label statusLabel;
    private Label pdfFileLabel;
    private Label excelFileLabel;
    private Button processButton;
    private Button exportButton;
    private VBox dropZone;
    private Stage primaryStage;
    
    // Arquivos selecionados
    private File pdfFile;
    private File excelFile;
    
    // Campos para edição manual dos dados extraídos
    private TextField campoNumNota;
    private TextField campoValorTotal;
    private TextField campoData;
    private TextField campoPlaca;
    private TextField campoFornecedor;
    private TextArea campoProdutos;
    private VBox editPanel;
    
    // Dados extraídos
    private String numNota;
    private String valorTotal;
    private String data;
    private String placaVeiculo;
    private String fornecedor;
    private List<Produto> listaDeProdutos;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        primaryStage.setTitle("MgtBolina | NF-e Extractor");

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, " + COLOR_BG + ", #1e293b);");

        // MUDANÇA PRINCIPAL: Envolver o conteúdo central em um ScrollPane
        VBox centerContent = createCenterContent();
        ScrollPane scrollPane = new ScrollPane(centerContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scrollPane.setPannable(true); // Permite arrastar para rolar
        
        root.setCenter(scrollPane);

        VBox bottomContent = createBottomContent();
        root.setBottom(bottomContent);

        Scene scene = new Scene(root, 1100, 850);
        primaryStage.setScene(scene);
        primaryStage.show();
        
        logMessage("✅ Sistema iniciado! Selecione um PDF e uma planilha Excel.");
        System.out.println("DEBUG: Interface iniciada com sucesso");
    }

    private VBox createCenterContent() {
        VBox container = new VBox(20);
        container.setAlignment(Pos.TOP_CENTER); // Mudei de CENTER para TOP_CENTER
        container.setPadding(new Insets(30));

        // Header
        VBox header = new VBox(8);
        header.setAlignment(Pos.CENTER);
        Label titleLabel = new Label("Extração Inteligente");
        titleLabel.setFont(Font.font("Inter", FontWeight.BOLD, 36));
        titleLabel.setTextFill(Color.valueOf(COLOR_TEXT_MAIN));

        Label subtitleLabel = new Label("Converta seus arquivos PDF para Excel em segundos");
        subtitleLabel.setFont(Font.font("Inter", FontWeight.NORMAL, 16));
        subtitleLabel.setTextFill(Color.valueOf(COLOR_TEXT_DIM));
        header.getChildren().addAll(titleLabel, subtitleLabel);

        // Drop Zone - REDUZIDO
        dropZone = createDropZone();

        // Files info section
        VBox filesInfo = createFilesInfoSection();

        // Action Buttons Row
        HBox actionBox = new HBox(15);
        actionBox.setAlignment(Pos.CENTER);

        Button selectPdfButton = createStyledButton("📄 Selecionar PDF", COLOR_CARD, COLOR_TEXT_MAIN);
        selectPdfButton.setOnAction(e -> {
            System.out.println("DEBUG: Botão PDF clicado");
            selectPDFFile();
        });

        Button selectExcelButton = createStyledButton("📊 Selecionar Excel", COLOR_CARD, COLOR_TEXT_MAIN);
        selectExcelButton.setOnAction(e -> {
            System.out.println("DEBUG: Botão Excel clicado");
            selectExcelFile();
        });

        processButton = createStyledButton("🔍 Processar PDF", COLOR_ACCENT, "#0F172A");
        processButton.setDisable(true);
        processButton.setOnAction(e -> processarPDF());

        exportButton = createStyledButton("✅ Exportar para Excel", COLOR_SUCCESS, COLOR_TEXT_MAIN);
        exportButton.setDisable(true);
        exportButton.setOnAction(e -> exportarParaExcel());

        actionBox.getChildren().addAll(selectPdfButton, selectExcelButton, processButton, exportButton);

        // Status
        statusLabel = new Label("Pronto para iniciar");
        statusLabel.setTextFill(Color.valueOf(COLOR_TEXT_DIM));
        statusLabel.setFont(Font.font("Inter", FontWeight.MEDIUM, 14));

        // Painel de edição dos dados
        editPanel = createEditPanel();

        container.getChildren().addAll(header, dropZone, filesInfo, actionBox, statusLabel, editPanel);
        return container;
    }

    private VBox createFilesInfoSection() {
        VBox section = new VBox(10);
        section.setAlignment(Pos.CENTER);
        section.setPadding(new Insets(10));
        section.setMaxWidth(600);
        section.setStyle("-fx-background-color: " + COLOR_CARD + "; -fx-background-radius: 15;");

        Label titleLabel = new Label("📁 Arquivos Selecionados");
        titleLabel.setFont(Font.font("Inter", FontWeight.BOLD, 14));
        titleLabel.setTextFill(Color.valueOf(COLOR_TEXT_MAIN));

        pdfFileLabel = new Label("PDF: Nenhum arquivo selecionado");
        pdfFileLabel.setFont(Font.font("Inter", FontWeight.NORMAL, 12));
        pdfFileLabel.setTextFill(Color.valueOf(COLOR_TEXT_DIM));

        excelFileLabel = new Label("Excel: Nenhum arquivo selecionado");
        excelFileLabel.setFont(Font.font("Inter", FontWeight.NORMAL, 12));
        excelFileLabel.setTextFill(Color.valueOf(COLOR_TEXT_DIM));

        section.getChildren().addAll(titleLabel, pdfFileLabel, excelFileLabel);
        return section;
    }

    private VBox createEditPanel() {
        VBox panel = new VBox(12);
        panel.setMaxWidth(900);
        panel.setPadding(new Insets(20));
        panel.setStyle("-fx-background-color: " + COLOR_CARD + "; -fx-background-radius: 15;");
        panel.setVisible(false);
        panel.setManaged(false);

        Label titleLabel = new Label("📝 Dados Extraídos - Revise e Edite se Necessário");
        titleLabel.setFont(Font.font("Inter", FontWeight.BOLD, 16));
        titleLabel.setTextFill(Color.valueOf(COLOR_TEXT_MAIN));

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(12);
        grid.setPadding(new Insets(15, 0, 0, 0));

        // Criar campos de edição
        campoNumNota = createTextField("Ex: 043730");
        campoValorTotal = createTextField("Ex: 1.921,00");
        campoData = createTextField("Ex: 17/10/2025");
        campoPlaca = createTextField("Ex: ABC1234");
        campoFornecedor = createTextField("Ex: EMPRESA LTDA");
        
        campoProdutos = new TextArea();
        campoProdutos.setPromptText("Produtos extraídos...");
        campoProdutos.setPrefHeight(150); // Aumentei de 120 para 150
        campoProdutos.setEditable(false);
        campoProdutos.setStyle(
            "-fx-control-inner-background: #0F172A;" +
            "-fx-text-fill: " + COLOR_TEXT_MAIN + ";" +
            "-fx-border-color: transparent;" +
            "-fx-background-radius: 8;" +
            "-fx-font-family: 'Courier New';" +
            "-fx-font-size: 12;"
        );

        // Labels e campos no grid
        int row = 0;
        grid.add(createLabel("Número da Nota:"), 0, row);
        grid.add(campoNumNota, 1, row++);
        
        grid.add(createLabel("Valor Total:"), 0, row);
        grid.add(campoValorTotal, 1, row++);
        
        grid.add(createLabel("Data:"), 0, row);
        grid.add(campoData, 1, row++);
        
        grid.add(createLabel("Placa Veículo:"), 0, row);
        grid.add(campoPlaca, 1, row++);
        
        grid.add(createLabel("Fornecedor:"), 0, row);
        grid.add(campoFornecedor, 1, row++);
        
        grid.add(createLabel("Produtos:"), 0, row);
        grid.add(campoProdutos, 1, row++);

        panel.getChildren().addAll(titleLabel, grid);
        return panel;
    }

    private Label createLabel(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Inter", FontWeight.MEDIUM, 13));
        label.setTextFill(Color.valueOf(COLOR_TEXT_MAIN));
        return label;
    }

    private TextField createTextField(String prompt) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.setPrefWidth(400);
        field.setStyle(
            "-fx-background-color: #0F172A;" +
            "-fx-text-fill: " + COLOR_TEXT_MAIN + ";" +
            "-fx-border-color: transparent;" +
            "-fx-background-radius: 8;" +
            "-fx-padding: 10;"
        );
        return field;
    }

    private VBox createDropZone() {
        VBox zone = new VBox(15);
        zone.setAlignment(Pos.CENTER);
        zone.setPadding(new Insets(40)); // Reduzido de 50 para 40
        zone.setMaxWidth(600);
        zone.setStyle(
            "-fx-background-color: " + COLOR_CARD + ";" +
            "-fx-border-color: " + COLOR_ACCENT + ";" +
            "-fx-border-width: 2;" +
            "-fx-border-style: dashed;" +
            "-fx-border-radius: 15;" +
            "-fx-background-radius: 15;"
        );

        Label iconLabel = new Label("📂");
        iconLabel.setFont(Font.font(40)); // Reduzido de 48 para 40

        Label mainText = new Label("Arraste seus arquivos aqui");
        mainText.setFont(Font.font("Inter", FontWeight.BOLD, 16)); // Reduzido de 18
        mainText.setTextFill(Color.valueOf(COLOR_TEXT_MAIN));

        Label subText = new Label("ou clique nos botões abaixo para selecionar");
        subText.setFont(Font.font("Inter", FontWeight.NORMAL, 13)); // Reduzido de 14
        subText.setTextFill(Color.valueOf(COLOR_TEXT_DIM));

        zone.getChildren().addAll(iconLabel, mainText, subText);

        // Drag and Drop
        zone.setOnDragOver(event -> {
            if (event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });

        zone.setOnDragDropped(event -> {
            boolean success = false;
            if (event.getDragboard().hasFiles()) {
                for (File file : event.getDragboard().getFiles()) {
                    String fileName = file.getName().toLowerCase();
                    System.out.println("DEBUG: Arquivo arrastado: " + fileName);
                    if (fileName.endsWith(".pdf")) {
                        pdfFile = file;
                        pdfFileLabel.setText("PDF: " + file.getName());
                        pdfFileLabel.setTextFill(Color.valueOf(COLOR_SUCCESS));
                        logMessage("✅ PDF selecionado: " + file.getName());
                        success = true;
                    } else if (fileName.endsWith(".xlsx") || fileName.endsWith(".xls")) {
                        excelFile = file;
                        excelFileLabel.setText("Excel: " + file.getName());
                        excelFileLabel.setTextFill(Color.valueOf(COLOR_SUCCESS));
                        logMessage("✅ Excel selecionado: " + file.getName());
                        success = true;
                    }
                }
                updateButtonStates();
            }
            event.setDropCompleted(success);
            event.consume();
        });

        return zone;
    }

    private VBox createBottomContent() {
        VBox bottom = new VBox(10);
        bottom.setPadding(new Insets(15)); // Reduzido de 20 para 15
        bottom.setStyle("-fx-background-color: " + COLOR_CARD + ";");

        Label logLabel = new Label("📋 Log de Atividades");
        logLabel.setFont(Font.font("Inter", FontWeight.BOLD, 13)); // Reduzido de 14
        logLabel.setTextFill(Color.valueOf(COLOR_TEXT_MAIN));

        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setPrefHeight(120); // Reduzido de 150 para 120
        logArea.setWrapText(true);
        logArea.setStyle(
            "-fx-control-inner-background: #0F172A;" +
            "-fx-text-fill: " + COLOR_TEXT_MAIN + ";" +
            "-fx-border-color: transparent;" +
            "-fx-font-family: 'Courier New';" +
            "-fx-font-size: 11;"
        );

        bottom.getChildren().addAll(logLabel, logArea);
        return bottom;
    }

    private Button createStyledButton(String text, String bgColor, String textColor) {
        Button button = new Button(text);
        button.setFont(Font.font("Inter", FontWeight.BOLD, 13)); // Reduzido de 14
        button.setStyle(
            "-fx-background-color: " + bgColor + ";" +
            "-fx-text-fill: " + textColor + ";" +
            "-fx-background-radius: 10;" +
            "-fx-padding: 10 20;" + // Reduzido de 12 24
            "-fx-cursor: hand;"
        );
        
        button.setOnMouseEntered(e -> button.setStyle(
            "-fx-background-color: derive(" + bgColor + ", -10%);" +
            "-fx-text-fill: " + textColor + ";" +
            "-fx-background-radius: 10;" +
            "-fx-padding: 10 20;" +
            "-fx-cursor: hand;"
        ));
        
        button.setOnMouseExited(e -> button.setStyle(
            "-fx-background-color: " + bgColor + ";" +
            "-fx-text-fill: " + textColor + ";" +
            "-fx-background-radius: 10;" +
            "-fx-padding: 10 20;" +
            "-fx-cursor: hand;"
        ));

        return button;
    }

    private void selectPDFFile() {
        System.out.println("DEBUG: Entrando em selectPDFFile()");
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Selecionar arquivo PDF");
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Arquivos PDF", "*.pdf", "*.PDF")
            );
            
            try {
                File userHome = new File(System.getProperty("user.home"));
                File downloads = new File(userHome, "Downloads");
                if (downloads.exists()) {
                    fileChooser.setInitialDirectory(downloads);
                }
            } catch (Exception e) {
                System.err.println("DEBUG: Erro ao definir diretório inicial: " + e.getMessage());
            }
            
            File selected = fileChooser.showOpenDialog(primaryStage);
            
            if (selected != null) {
                pdfFile = selected;
                System.out.println("DEBUG: PDF selecionado - " + selected.getAbsolutePath());
                pdfFileLabel.setText("PDF: " + selected.getName());
                pdfFileLabel.setTextFill(Color.valueOf(COLOR_SUCCESS));
                logMessage("✅ PDF selecionado: " + selected.getName());
                updateButtonStates();
            }
        } catch (Exception e) {
            System.err.println("DEBUG: ERRO em selectPDFFile: " + e.getMessage());
            e.printStackTrace();
            logMessage("❌ Erro ao selecionar PDF: " + e.getMessage());
        }
    }

    private void selectExcelFile() {
        System.out.println("DEBUG: Entrando em selectExcelFile()");
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Selecionar planilha Excel");
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Arquivos Excel", "*.xlsx", "*.xls", "*.XLSX", "*.XLS")
            );
            
            try {
                File userHome = new File(System.getProperty("user.home"));
                File documents = new File(userHome, "Documents");
                if (!documents.exists()) {
                    documents = new File(userHome, "OneDrive\\Documents");
                }
                if (documents.exists()) {
                    fileChooser.setInitialDirectory(documents);
                }
            } catch (Exception e) {
                System.err.println("DEBUG: Erro ao definir diretório inicial: " + e.getMessage());
            }
            
            File selected = fileChooser.showOpenDialog(primaryStage);
            
            if (selected != null) {
                excelFile = selected;
                System.out.println("DEBUG: Excel selecionado - " + selected.getAbsolutePath());
                excelFileLabel.setText("Excel: " + selected.getName());
                excelFileLabel.setTextFill(Color.valueOf(COLOR_SUCCESS));
                logMessage("✅ Excel selecionado: " + selected.getName());
                updateButtonStates();
            }
        } catch (Exception e) {
            System.err.println("DEBUG: ERRO em selectExcelFile: " + e.getMessage());
            e.printStackTrace();
            logMessage("❌ Erro ao selecionar Excel: " + e.getMessage());
        }
    }

    private void updateButtonStates() {
        System.out.println("DEBUG: updateButtonStates - PDF: " + (pdfFile != null) + ", Excel: " + (excelFile != null));
        if (pdfFile != null && excelFile != null) {
            processButton.setDisable(false);
            statusLabel.setText("✅ Pronto para processar");
            statusLabel.setTextFill(Color.valueOf(COLOR_SUCCESS));
        } else if (pdfFile != null) {
            statusLabel.setText("⚠️ Selecione também a planilha Excel");
            statusLabel.setTextFill(Color.valueOf(COLOR_WARNING));
        } else if (excelFile != null) {
            statusLabel.setText("⚠️ Selecione também o arquivo PDF");
            statusLabel.setTextFill(Color.valueOf(COLOR_WARNING));
        }
    }

    private void logMessage(String message) {
        Platform.runLater(() -> {
            if (logArea != null) {
                logArea.appendText(message + "\n");
                logArea.setScrollTop(Double.MAX_VALUE);
            }
        });
    }

    private void processarPDF() {
        if (pdfFile == null || excelFile == null) {
            logMessage("❌ Erro: Selecione o PDF e o Excel antes de processar!");
            return;
        }

        processButton.setDisable(true);
        statusLabel.setText("⏳ Processando PDF...");
        statusLabel.setTextFill(Color.valueOf(COLOR_WARNING));
        logArea.clear();
        logMessage("🔄 Iniciando processamento do PDF: " + pdfFile.getName());
        logMessage("📊 Excel de destino: " + excelFile.getName());

        new Thread(() -> {
            try {
                logMessage("\n📖 Lendo conteúdo do PDF...");
                
                PdfLeitor leitor = new PdfLeitor();
                String textoBruto = leitor.ExtractText(pdfFile.getAbsolutePath());
                
                if (textoBruto == null || textoBruto.isEmpty()) {
                    Platform.runLater(() -> {
                        logMessage("❌ Erro: Não foi possível extrair texto do PDF!");
                        statusLabel.setText("❌ Falha na leitura");
                        statusLabel.setTextFill(Color.valueOf("#EF4444"));
                        processButton.setDisable(false);
                    });
                    return;
                }

                String textoParaRegex = textoBruto.replace("|", " ");
                
                logMessage("✅ Texto extraído com sucesso (" + textoBruto.length() + " caracteres)");
                logMessage("\n🔍 Extraindo dados da nota fiscal...");

                PdfColetorDados coletor = new PdfColetorDados();
                
                try {
                    this.numNota = coletor.ExtractDanfeNumber(textoParaRegex);
                    logMessage("  📄 Número da Nota: " + (this.numNota != null ? this.numNota : "N/E"));
                } catch (Exception e) {
                    logMessage("  ⚠️ Erro ao extrair número da nota: " + e.getMessage());
                    this.numNota = "N/E";
                }
                
                try {
                    this.valorTotal = coletor.ExtractTotalNumber(textoParaRegex);
                    logMessage("  💰 Valor Total: " + (this.valorTotal != null ? this.valorTotal : "N/E"));
                } catch (Exception e) {
                    logMessage("  ⚠️ Erro ao extrair valor total: " + e.getMessage());
                    this.valorTotal = "N/E";
                }
                
                try {
                    this.placaVeiculo = coletor.ExtracPlacaVeiculo(textoParaRegex);
                    logMessage("  🚗 Placa: " + (this.placaVeiculo != null ? this.placaVeiculo : "N/E"));
                } catch (Exception e) {
                    logMessage("  ⚠️ Erro ao extrair placa: " + e.getMessage());
                    this.placaVeiculo = "N/E";
                }
                
                try {
                    this.fornecedor = coletor.ExtractRazaoSocial(textoParaRegex);
                    logMessage("  🏢 Fornecedor: " + (this.fornecedor != null ? this.fornecedor : "N/E"));
                } catch (Exception e) {
                    logMessage("  ⚠️ Erro ao extrair fornecedor: " + e.getMessage());
                    this.fornecedor = "N/E";
                }
                
                try {
                    this.data = coletor.ExtractDate(textoParaRegex);
                    logMessage("  📅 Data: " + (this.data != null ? this.data : "N/E"));
                } catch (Exception e) {
                    logMessage("  ⚠️ Erro ao extrair data: " + e.getMessage());
                    this.data = "N/E";
                }

                logMessage("\n🛒 Extraindo produtos...");
                try {
                    ColetorProdutos coletorProdutos = new ColetorProdutos();
                    this.listaDeProdutos = coletorProdutos.extrairTabelaPDF(pdfFile.getAbsolutePath());
                    
                    if (this.listaDeProdutos == null || this.listaDeProdutos.isEmpty()) {
                        logMessage("  ⚠️ Nenhum produto foi encontrado!");
                        this.listaDeProdutos = new java.util.ArrayList<>();
                    } else {
                        logMessage("  ✅ " + this.listaDeProdutos.size() + " produto(s) extraído(s)");
                    }
                } catch (Exception e) {
                    logMessage("  ❌ Erro ao extrair produtos: " + e.getMessage());
                    e.printStackTrace();
                    this.listaDeProdutos = new java.util.ArrayList<>();
                }

                Platform.runLater(() -> {
                    try {
                        campoNumNota.setText(numNota != null ? numNota : "");
                        campoValorTotal.setText(valorTotal != null ? valorTotal : "");
                        campoData.setText(data != null ? data : "");
                        campoPlaca.setText(placaVeiculo != null ? placaVeiculo : "");
                        campoFornecedor.setText(fornecedor != null ? fornecedor : "");
                        
                        if (listaDeProdutos != null && !listaDeProdutos.isEmpty()) {
                            StringBuilder sb = new StringBuilder();
                            for (int i = 0; i < listaDeProdutos.size(); i++) {
                                Produto p = listaDeProdutos.get(i);
                                sb.append(String.format("[%d] %s - R$ %s\n", 
                                    i + 1, p.descricao, p.valorUnitario));
                            }
                            campoProdutos.setText(sb.toString());
                        } else {
                            campoProdutos.setText("Nenhum produto encontrado");
                        }

                        editPanel.setVisible(true);
                        editPanel.setManaged(true);
                        exportButton.setDisable(false);
                        
                        logMessage("\n✅ Processamento concluído! Revise os dados e clique em 'Exportar'");
                        statusLabel.setText("✅ Processado! Revise e exporte.");
                        statusLabel.setTextFill(Color.valueOf(COLOR_SUCCESS));
                        processButton.setDisable(false);
                        
                    } catch (Exception e) {
                        logMessage("❌ Erro ao atualizar interface: " + e.getMessage());
                        e.printStackTrace();
                    }
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    logMessage("\n❌ ERRO CRÍTICO: " + e.getMessage());
                    e.printStackTrace();
                    statusLabel.setText("❌ Erro no processamento");
                    statusLabel.setTextFill(Color.valueOf("#EF4444"));
                    processButton.setDisable(false);
                    
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Erro");
                    alert.setHeaderText("Erro ao processar PDF");
                    alert.setContentText("Detalhes: " + e.getMessage());
                    alert.showAndWait();
                });
            }
        }).start();
    }

    private void exportarParaExcel() {
        if (excelFile == null || listaDeProdutos == null) {
            logMessage("❌ Erro: Não há dados para exportar!");
            return;
        }

        exportButton.setDisable(true);
        statusLabel.setText("⏳ Exportando para Excel...");
        statusLabel.setTextFill(Color.valueOf(COLOR_WARNING));

        new Thread(() -> {
            try {
                String notaFinal = campoNumNota.getText();
                String totalFinal = campoValorTotal.getText();
                String dataFinal = campoData.getText();
                String placaFinal = campoPlaca.getText();
                String fornecedorFinal = campoFornecedor.getText();

                logMessage("\n📤 Iniciando exportação para Excel...");
                logMessage("📁 Caminho: " + excelFile.getAbsolutePath());
                logMessage("\n📋 Dados a exportar:");
                logMessage("   Nota: " + notaFinal);
                logMessage("   Total: " + totalFinal);
                logMessage("   Data: " + dataFinal);
                logMessage("   Placa: " + placaFinal);
                logMessage("   Fornecedor: " + fornecedorFinal);
                logMessage("   Produtos: " + listaDeProdutos.size());
                
                ExportadorExcel exporter = new ExportadorExcel();
                exporter.ExportDataTOExcel(
                    excelFile.getAbsolutePath(),
                    notaFinal,
                    totalFinal,
                    dataFinal,
                    placaFinal,
                    fornecedorFinal,
                    listaDeProdutos
                );

                Platform.runLater(() -> {
                    logMessage("\n✅ Dados exportados com sucesso!");
                    logMessage("📊 " + listaDeProdutos.size() + " produto(s) adicionado(s)");
                    logMessage("💾 Arquivo: " + excelFile.getAbsolutePath());
                    statusLabel.setText("✅ Exportação concluída!");
                    statusLabel.setTextFill(Color.valueOf(COLOR_SUCCESS));
                    
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Sucesso");
                    alert.setHeaderText("✅ Exportação concluída!");
                    alert.setContentText(
                        "Dados exportados para:\n" +
                        excelFile.getName() + "\n\n" +
                        "Total de produtos: " + listaDeProdutos.size()
                    );
                    alert.showAndWait();
                    
                    exportButton.setDisable(false);
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    logMessage("\n❌ ERRO na exportação: " + e.getMessage());
                    e.printStackTrace();
                    statusLabel.setText("❌ Falha na exportação");
                    statusLabel.setTextFill(Color.valueOf("#EF4444"));
                    
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Erro");
                    alert.setHeaderText("❌ Falha ao exportar");
                    alert.setContentText(
                        "Erro: " + e.getMessage() + "\n\n" +
                        "Verifique:\n" +
                        "1. Excel não está aberto\n" +
                        "2. Permissão para editar\n" +
                        "3. Estrutura do arquivo"
                    );
                    alert.showAndWait();
                    
                    exportButton.setDisable(false);
                });
            }
        }).start();
    }

    public static void main(String[] args) { 
        launch(args); 
    }
}