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
import java.util.ArrayList;
import java.util.List;
import javafx.scene.input.TransferMode;

/**
 * Interface gráfica com suporte a múltiplos PDFs.
 * Cada produto tem seu próprio valorUnitario, unidade e quantidade.
 */
public class InterfaceGrafica extends Application {

    private static final String COLOR_BG = "#0F172A";
    private static final String COLOR_CARD = "#1E293B";
    private static final String COLOR_ACCENT = "#38BDF8";
    private static final String COLOR_SUCCESS = "#10B981";
    private static final String COLOR_WARNING = "#F59E0B";
    private static final String COLOR_TEXT_MAIN = "#F8FAFC";
    private static final String COLOR_TEXT_DIM = "#94A3B8";

    private TextArea logArea;
    private Label statusLabel;
    private Label pdfFilesLabel;
    private Label excelFileLabel;
    private Button processButton;
    private Button exportButton;
    private VBox dropZone;
    private Stage primaryStage;

    private List<File> pdfFiles = new ArrayList<>();
    private File excelFile;
    private List<DadosPDF> dadosProcessados = new ArrayList<>();

    // Seletor de PDF
    private ComboBox<String> seletorPDF;
    private int pdfAtualIndex = -1;

    // Campos gerais do PDF
    private TextField campoNumNota;
    private TextField campoData;
    private TextField campoPlaca;
    private TextField campoFornecedor;

    // Área de listagem de produtos (somente leitura visual)
    private TextArea campoProdutos;

    // Campos de edição por produto
    private ComboBox<String> seletorProduto;
    private TextField campoValorUnitarioProduto;
    private TextField campoUnidadeProduto;
    private TextField campoQuantidadeProduto;
    private Button salvarProdutoButton;

    private VBox editPanel;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        primaryStage.setTitle("MgtBolina | NF-e Extractor (Múltiplos PDFs)");

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, " + COLOR_BG + ", #1e293b);");

        VBox centerContent = createCenterContent();
        ScrollPane scrollPane = new ScrollPane(centerContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scrollPane.setPannable(true);

        root.setCenter(scrollPane);

        VBox bottomContent = createBottomContent();
        root.setBottom(bottomContent);

        Scene scene = new Scene(root, 1100, 900);
        primaryStage.setScene(scene);
        primaryStage.show();

        logMessage("✅ Sistema iniciado! Selecione um ou mais PDFs e uma planilha Excel.");
    }

    private VBox createCenterContent() {
        VBox container = new VBox(20);
        container.setAlignment(Pos.TOP_CENTER);
        container.setPadding(new Insets(30));

        // Header
        VBox header = new VBox(8);
        header.setAlignment(Pos.CENTER);
        Label titleLabel = new Label("Extração Inteligente");
        titleLabel.setFont(Font.font("Inter", FontWeight.BOLD, 36));
        titleLabel.setTextFill(Color.valueOf(COLOR_TEXT_MAIN));
        Label subtitleLabel = new Label("Processe múltiplos PDFs de uma vez");
        subtitleLabel.setFont(Font.font("Inter", FontWeight.NORMAL, 16));
        subtitleLabel.setTextFill(Color.valueOf(COLOR_TEXT_DIM));
        header.getChildren().addAll(titleLabel, subtitleLabel);

        dropZone = createDropZone();
        VBox filesInfo = createFilesInfoSection();

        HBox actionBox = new HBox(15);
        actionBox.setAlignment(Pos.CENTER);

        Button selectPdfButton = createStyledButton("📄 Selecionar PDF(s)", COLOR_CARD, COLOR_TEXT_MAIN);
        selectPdfButton.setOnAction(e -> selectPDFFiles());

        Button selectExcelButton = createStyledButton("📊 Selecionar Excel", COLOR_CARD, COLOR_TEXT_MAIN);
        selectExcelButton.setOnAction(e -> selectExcelFile());

        processButton = createStyledButton("🔍 Processar PDFs", COLOR_ACCENT, "#0F172A");
        processButton.setDisable(true);
        processButton.setOnAction(e -> processarTodosPDFs());

        exportButton = createStyledButton("✅ Exportar para Excel", COLOR_SUCCESS, COLOR_TEXT_MAIN);
        exportButton.setDisable(true);
        exportButton.setOnAction(e -> exportarParaExcel());

        Button clearQueueButton = createStyledButton("🗑️ Limpar Fila", "#EF4444", COLOR_TEXT_MAIN);
        clearQueueButton.setOnAction(e -> limparFila());

        actionBox.getChildren().addAll(selectPdfButton, selectExcelButton, processButton, exportButton, clearQueueButton);

        statusLabel = new Label("Pronto para iniciar");
        statusLabel.setTextFill(Color.valueOf(COLOR_TEXT_DIM));
        statusLabel.setFont(Font.font("Inter", FontWeight.MEDIUM, 14));

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

        pdfFilesLabel = new Label("PDFs: Nenhum arquivo selecionado");
        pdfFilesLabel.setFont(Font.font("Inter", FontWeight.NORMAL, 12));
        pdfFilesLabel.setTextFill(Color.valueOf(COLOR_TEXT_DIM));

        excelFileLabel = new Label("Excel: Nenhum arquivo selecionado");
        excelFileLabel.setFont(Font.font("Inter", FontWeight.NORMAL, 12));
        excelFileLabel.setTextFill(Color.valueOf(COLOR_TEXT_DIM));

        section.getChildren().addAll(titleLabel, pdfFilesLabel, excelFileLabel);
        return section;
    }

    private VBox createEditPanel() {
        VBox panel = new VBox(12);
        panel.setMaxWidth(950);
        panel.setPadding(new Insets(20));
        panel.setStyle("-fx-background-color: " + COLOR_CARD + "; -fx-background-radius: 15;");
        panel.setVisible(false);
        panel.setManaged(false);

        Label titleLabel = new Label("📝 Dados Extraídos - Revise e Edite se Necessário");
        titleLabel.setFont(Font.font("Inter", FontWeight.BOLD, 16));
        titleLabel.setTextFill(Color.valueOf(COLOR_TEXT_MAIN));

        // Seletor de PDF
        HBox selectorBox = new HBox(10);
        selectorBox.setAlignment(Pos.CENTER_LEFT);
        Label selectorLabel = new Label("Selecionar PDF:");
        selectorLabel.setTextFill(Color.valueOf(COLOR_TEXT_MAIN));
        selectorLabel.setFont(Font.font("Inter", FontWeight.MEDIUM, 12));

        seletorPDF = new ComboBox<>();
        seletorPDF.setStyle("-fx-background-color: " + COLOR_BG + "; -fx-text-fill: " + COLOR_TEXT_MAIN + ";");
        seletorPDF.setPrefWidth(450);
        seletorPDF.setOnAction(e -> carregarDadosPDFSelecionado());

        selectorBox.getChildren().addAll(selectorLabel, seletorPDF);

        // Grid de campos gerais
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(12);
        grid.setPadding(new Insets(15, 0, 0, 0));

        Label labelNota = createFieldLabel("Número da Nota:");
        campoNumNota = createTextField();

        Label labelData = createFieldLabel("Data:");
        campoData = createTextField();

        Label labelPlaca = createFieldLabel("Placa do Veículo:");
        campoPlaca = createTextField();

        Label labelFornecedor = createFieldLabel("Fornecedor:");
        campoFornecedor = createTextField();

        grid.add(labelNota,       0, 0); grid.add(campoNumNota,   1, 0);
        grid.add(labelData,       0, 1); grid.add(campoData,       1, 1);
        grid.add(labelPlaca,      0, 2); grid.add(campoPlaca,      1, 2);
        grid.add(labelFornecedor, 0, 3); grid.add(campoFornecedor, 1, 3);

        // Botão salvar campos gerais
        Button salvarGeralButton = createStyledButton("💾 Salvar Dados Gerais", COLOR_ACCENT, "#0F172A");
        salvarGeralButton.setOnAction(e -> salvarDadosGerais());

        // ── Seção de edição de produtos individualmente ──
        Label labelProdutosTitle = new Label("📦 Produtos Extraídos");
        labelProdutosTitle.setFont(Font.font("Inter", FontWeight.BOLD, 14));
        labelProdutosTitle.setTextFill(Color.valueOf(COLOR_TEXT_MAIN));

        // Lista visual dos produtos
        campoProdutos = new TextArea();
        campoProdutos.setEditable(false);
        campoProdutos.setPrefRowCount(6);
        campoProdutos.setStyle(
            "-fx-control-inner-background: " + COLOR_BG + "; " +
            "-fx-text-fill: " + COLOR_TEXT_MAIN + "; " +
            "-fx-font-family: 'Courier New'; " +
            "-fx-font-size: 11px;"
        );

        // Seletor de produto para edição
        HBox produtoSelectorBox = new HBox(10);
        produtoSelectorBox.setAlignment(Pos.CENTER_LEFT);
        Label labelSeletorProduto = new Label("Editar produto nº:");
        labelSeletorProduto.setTextFill(Color.valueOf(COLOR_TEXT_MAIN));
        labelSeletorProduto.setFont(Font.font("Inter", FontWeight.MEDIUM, 12));

        seletorProduto = new ComboBox<>();
        seletorProduto.setStyle("-fx-background-color: " + COLOR_BG + "; -fx-text-fill: " + COLOR_TEXT_MAIN + ";");
        seletorProduto.setPrefWidth(250);
        seletorProduto.setOnAction(e -> carregarCamposProduto());

        produtoSelectorBox.getChildren().addAll(labelSeletorProduto, seletorProduto);

        // Grid de edição de produto individual
        GridPane gridProduto = new GridPane();
        gridProduto.setHgap(15);
        gridProduto.setVgap(10);
        gridProduto.setPadding(new Insets(10, 0, 0, 0));

        Label labelValorUnitario = createFieldLabel("Valor Unitário:");
        campoValorUnitarioProduto = createTextField();
        campoValorUnitarioProduto.setPromptText("Ex: 2,25");

        Label labelUnidade = createFieldLabel("Unidade:");
        campoUnidadeProduto = createTextField();
        campoUnidadeProduto.setPromptText("Ex: UN, TON, KG");

        Label labelQuantidade = createFieldLabel("Quantidade:");
        campoQuantidadeProduto = createTextField();
        campoQuantidadeProduto.setPromptText("Ex: 125");

        gridProduto.add(labelValorUnitario,        0, 0); gridProduto.add(campoValorUnitarioProduto, 1, 0);
        gridProduto.add(labelUnidade,              0, 1); gridProduto.add(campoUnidadeProduto,       1, 1);
        gridProduto.add(labelQuantidade,           0, 2); gridProduto.add(campoQuantidadeProduto,    1, 2);

        salvarProdutoButton = createStyledButton("💾 Salvar Produto", COLOR_SUCCESS, COLOR_TEXT_MAIN);
        salvarProdutoButton.setDisable(true);
        salvarProdutoButton.setOnAction(e -> salvarEdicaoProduto());

        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: #334155;");

        panel.getChildren().addAll(
            titleLabel,
            selectorBox,
            grid,
            salvarGeralButton,
            sep,
            labelProdutosTitle,
            campoProdutos,
            produtoSelectorBox,
            gridProduto,
            salvarProdutoButton
        );
        return panel;
    }

    private VBox createDropZone() {
        VBox dropBox = new VBox(10);
        dropBox.setAlignment(Pos.CENTER);
        dropBox.setPrefHeight(120);
        dropBox.setMaxWidth(600);
        dropBox.setStyle(
            "-fx-background-color: " + COLOR_CARD + "; " +
            "-fx-border-color: " + COLOR_ACCENT + "; " +
            "-fx-border-width: 2; " +
            "-fx-border-style: dashed; " +
            "-fx-border-radius: 15; " +
            "-fx-background-radius: 15;"
        );

        Label dropLabel = new Label("📂 Arraste e solte PDFs aqui");
        dropLabel.setFont(Font.font("Inter", FontWeight.BOLD, 14));
        dropLabel.setTextFill(Color.valueOf(COLOR_TEXT_DIM));

        Label hintLabel = new Label("ou use os botões abaixo");
        hintLabel.setFont(Font.font("Inter", FontWeight.NORMAL, 11));
        hintLabel.setTextFill(Color.valueOf(COLOR_TEXT_DIM));

        dropBox.getChildren().addAll(dropLabel, hintLabel);

        dropBox.setOnDragOver(event -> {
            if (event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });

        dropBox.setOnDragDropped(event -> {
            var dragboard = event.getDragboard();
            boolean success = false;
            if (dragboard.hasFiles()) {
                for (File arquivo : dragboard.getFiles()) {
                    if (arquivo.getName().toLowerCase().endsWith(".pdf")) {
                        if (!pdfFiles.contains(arquivo)) {
                            pdfFiles.add(arquivo);
                        }
                    }
                }
                atualizarLabelPDFs();
                verificarBotoes();
                success = true;
            }
            event.setDropCompleted(success);
            event.consume();
        });

        return dropBox;
    }

    private VBox createBottomContent() {
        VBox bottomBox = new VBox(10);
        bottomBox.setPadding(new Insets(15));
        bottomBox.setStyle("-fx-background-color: " + COLOR_CARD + ";");

        Label logLabel = new Label("📋 Log de Processamento");
        logLabel.setFont(Font.font("Inter", FontWeight.BOLD, 13));
        logLabel.setTextFill(Color.valueOf(COLOR_TEXT_MAIN));

        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setPrefRowCount(6);
        logArea.setStyle(
            "-fx-control-inner-background: " + COLOR_BG + "; " +
            "-fx-text-fill: " + COLOR_TEXT_MAIN + "; " +
            "-fx-font-family: 'Courier New'; " +
            "-fx-font-size: 11px;"
        );

        bottomBox.getChildren().addAll(logLabel, logArea);
        return bottomBox;
    }

    private Button createStyledButton(String text, String bgColor, String textColor) {
        Button button = new Button(text);
        button.setFont(Font.font("Inter", FontWeight.BOLD, 13));
        button.setStyle(
            "-fx-background-color: " + bgColor + "; " +
            "-fx-text-fill: " + textColor + "; " +
            "-fx-background-radius: 8; " +
            "-fx-padding: 12 24 12 24; " +
            "-fx-cursor: hand;"
        );
        button.setOnMouseEntered(e ->
            button.setStyle(
                "-fx-background-color: derive(" + bgColor + ", 10%); " +
                "-fx-text-fill: " + textColor + "; " +
                "-fx-background-radius: 8; " +
                "-fx-padding: 12 24 12 24; " +
                "-fx-cursor: hand;"
            )
        );
        button.setOnMouseExited(e ->
            button.setStyle(
                "-fx-background-color: " + bgColor + "; " +
                "-fx-text-fill: " + textColor + "; " +
                "-fx-background-radius: 8; " +
                "-fx-padding: 12 24 12 24; " +
                "-fx-cursor: hand;"
            )
        );
        return button;
    }

    private Label createFieldLabel(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Inter", FontWeight.MEDIUM, 12));
        label.setTextFill(Color.valueOf(COLOR_TEXT_MAIN));
        return label;
    }

    private TextField createTextField() {
        TextField textField = new TextField();
        textField.setStyle(
            "-fx-background-color: " + COLOR_BG + "; " +
            "-fx-text-fill: " + COLOR_TEXT_MAIN + "; " +
            "-fx-prompt-text-fill: " + COLOR_TEXT_DIM + "; " +
            "-fx-font-family: 'Inter';"
        );
        textField.setPrefWidth(350);
        return textField;
    }

    private void selectPDFFiles() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecione um ou mais PDFs");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
        );

        List<File> selectedFiles = fileChooser.showOpenMultipleDialog(primaryStage);

        if (selectedFiles != null && !selectedFiles.isEmpty()) {
            for (File file : selectedFiles) {
                if (!pdfFiles.contains(file)) {
                    pdfFiles.add(file);
                }
            }
            atualizarLabelPDFs();
            verificarBotoes();
            logMessage("📄 " + selectedFiles.size() + " PDF(s) adicionado(s) à fila");
        }
    }

    private void selectExcelFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecione o arquivo Excel");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Excel Files", "*.xlsx", "*.xls")
        );

        File selectedFile = fileChooser.showOpenDialog(primaryStage);

        if (selectedFile != null) {
            excelFile = selectedFile;
            excelFileLabel.setText("Excel: " + excelFile.getName());
            verificarBotoes();
            logMessage("📊 Excel selecionado: " + excelFile.getName());
        }
    }

    private void atualizarLabelPDFs() {
        if (pdfFiles.isEmpty()) {
            pdfFilesLabel.setText("PDFs: Nenhum arquivo selecionado");
        } else {
            pdfFilesLabel.setText("PDFs: " + pdfFiles.size() + " arquivo(s) na fila");
        }
    }

    private void verificarBotoes() {
        boolean temPDFs = !pdfFiles.isEmpty();
        boolean temExcel = excelFile != null;

        processButton.setDisable(!temPDFs);
        exportButton.setDisable(dadosProcessados.isEmpty() || !temExcel);
    }

    private void processarTodosPDFs() {
        if (pdfFiles.isEmpty()) {
            logMessage("⚠️ Nenhum PDF selecionado!");
            return;
        }

        processButton.setDisable(true);
        statusLabel.setText("⏳ Processando PDFs...");
        statusLabel.setTextFill(Color.valueOf(COLOR_WARNING));

        dadosProcessados.clear();
        seletorPDF.getItems().clear();

        new Thread(() -> {
            try {
                int total = pdfFiles.size();
                int processados = 0;

                for (File pdfFile : pdfFiles) {
                    processados++;
                    final int numAtual = processados;

                    Platform.runLater(() -> {
                        logMessage("\n═══════════════════════════════════════");
                        logMessage("🔍 Processando PDF " + numAtual + "/" + total);
                        logMessage("📄 " + pdfFile.getName());
                        logMessage("═══════════════════════════════════════");
                    });

                    DadosPDF dadosPDF = processarUmPDF(pdfFile);
                    dadosProcessados.add(dadosPDF);

                    Platform.runLater(() ->
                        seletorPDF.getItems().add("PDF " + numAtual + ": " + pdfFile.getName())
                    );
                }

                Platform.runLater(() -> {
                    logMessage("\n✅ Processamento completo! " + dadosProcessados.size() + " PDF(s) processado(s)");
                    statusLabel.setText("✅ Processamento concluído!");
                    statusLabel.setTextFill(Color.valueOf(COLOR_SUCCESS));

                    editPanel.setVisible(true);
                    editPanel.setManaged(true);

                    if (!seletorPDF.getItems().isEmpty()) {
                        seletorPDF.getSelectionModel().select(0);
                        carregarDadosPDFSelecionado();
                    }

                    exportButton.setDisable(false);
                    processButton.setDisable(false);
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    logMessage("\n❌ ERRO: " + e.getMessage());
                    e.printStackTrace();
                    statusLabel.setText("❌ Erro no processamento");
                    statusLabel.setTextFill(Color.valueOf("#EF4444"));
                    processButton.setDisable(false);

                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Erro");
                    alert.setHeaderText("Erro ao processar PDFs");
                    alert.setContentText("Detalhes: " + e.getMessage());
                    alert.showAndWait();
                });
            }
        }).start();
    }

    private DadosPDF processarUmPDF(File pdfFile) {
        DadosPDF dadosPDF = new DadosPDF(pdfFile.getName());

        try {
            PdfLeitor leitor = new PdfLeitor();
            String textoParaRegex = leitor.ExtractText(pdfFile.getAbsolutePath());

            if (textoParaRegex != null && !textoParaRegex.isEmpty()) {
                PdfColetorDados coletor = new PdfColetorDados();

                dadosPDF.setNumNota(coletor.ExtractDanfeNumber(textoParaRegex));
                Platform.runLater(() -> logMessage("  📋 Nota: " + dadosPDF.getNumNota()));

                dadosPDF.setPlacaVeiculo(coletor.ExtracPlacaVeiculo(textoParaRegex));
                Platform.runLater(() -> logMessage("  🚗 Placa: " + dadosPDF.getPlacaVeiculo()));

                dadosPDF.setFornecedor(coletor.ExtractRazaoSocial(textoParaRegex));
                Platform.runLater(() -> logMessage("  🏢 Fornecedor: " + dadosPDF.getFornecedor()));

                dadosPDF.setData(coletor.ExtractDate(textoParaRegex));
                Platform.runLater(() -> logMessage("  📅 Data: " + dadosPDF.getData()));

                ColetorProdutos coletorProdutos = new ColetorProdutos();
                List<Produto> produtos = coletorProdutos.extrairTabelaPDF(pdfFile.getAbsolutePath());

                if (produtos == null || produtos.isEmpty()) {
                    produtos = new ArrayList<>();
                    Platform.runLater(() -> logMessage("  ⚠️ Nenhum produto encontrado"));
                } else {
                    final int totalProdutos = produtos.size();
                    Platform.runLater(() -> logMessage("  ✅ " + totalProdutos + " produto(s) encontrado(s)"));
                }

                dadosPDF.setListaDeProdutos(produtos);
            }

        } catch (Exception e) {
            Platform.runLater(() -> {
                logMessage("  ❌ Erro ao processar: " + e.getMessage());
                e.printStackTrace();
            });
        }

        return dadosPDF;
    }

    /**
     * Carrega os campos gerais e a lista de produtos do PDF selecionado.
     */
    private void carregarDadosPDFSelecionado() {
        int index = seletorPDF.getSelectionModel().getSelectedIndex();
        if (index >= 0 && index < dadosProcessados.size()) {
            pdfAtualIndex = index;
            DadosPDF dados = dadosProcessados.get(index);

            campoNumNota.setText(dados.getNumNota() != null ? dados.getNumNota() : "");
            campoData.setText(dados.getData() != null ? dados.getData() : "");
            campoPlaca.setText(dados.getPlacaVeiculo() != null ? dados.getPlacaVeiculo() : "");
            campoFornecedor.setText(dados.getFornecedor() != null ? dados.getFornecedor() : "");

            // Atualiza lista visual de produtos
            atualizarListaProdutos(dados);

            // Atualiza seletor de produto
            seletorProduto.getItems().clear();
            seletorProduto.setValue(null);
            campoValorUnitarioProduto.clear();
            campoUnidadeProduto.clear();
            campoQuantidadeProduto.clear();
            salvarProdutoButton.setDisable(true);

            if (dados.getListaDeProdutos() != null && !dados.getListaDeProdutos().isEmpty()) {
                for (int i = 0; i < dados.getListaDeProdutos().size(); i++) {
                    Produto p = dados.getListaDeProdutos().get(i);
                    String label = "[" + (i + 1) + "] " + truncar(p.descricao, 40);
                    seletorProduto.getItems().add(label);
                }
                seletorProduto.getSelectionModel().select(0);
                carregarCamposProduto();
            }

            logMessage("\n📝 Carregado para edição: " + dados.getNomePDF());
        }
    }

    /**
     * Atualiza a TextArea com a lista de produtos do PDF atual.
     */
    private void atualizarListaProdutos(DadosPDF dados) {
        if (dados.getListaDeProdutos() != null && !dados.getListaDeProdutos().isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < dados.getListaDeProdutos().size(); i++) {
                Produto p = dados.getListaDeProdutos().get(i);
                sb.append(String.format("[%d] %s\n    Unid: %s | Qtd: %s | Vlr Unit: R$ %s\n\n",
                    i + 1,
                    p.descricao != null ? p.descricao : "—",
                    p.unidade != null ? p.unidade : "—",
                    p.quantidade != null && !p.quantidade.isEmpty() ? p.quantidade : "—",
                    p.valorUnitario != null ? p.valorUnitario : "—"
                ));
            }
            campoProdutos.setText(sb.toString());
        } else {
            campoProdutos.setText("Nenhum produto encontrado");
        }
    }

    /**
     * Carrega os campos de edição com os dados do produto selecionado no seletorProduto.
     */
    private void carregarCamposProduto() {
        if (pdfAtualIndex < 0 || pdfAtualIndex >= dadosProcessados.size()) return;

        int prodIdx = seletorProduto.getSelectionModel().getSelectedIndex();
        if (prodIdx < 0) return;

        DadosPDF dados = dadosProcessados.get(pdfAtualIndex);
        if (dados.getListaDeProdutos() == null || prodIdx >= dados.getListaDeProdutos().size()) return;

        Produto p = dados.getListaDeProdutos().get(prodIdx);

        campoValorUnitarioProduto.setText(p.valorUnitario != null ? p.valorUnitario : "");
        campoUnidadeProduto.setText(p.unidade != null ? p.unidade : "");
        campoQuantidadeProduto.setText(p.quantidade != null ? p.quantidade : "");

        salvarProdutoButton.setDisable(false);
    }

    /**
     * Salva as edições nos dados gerais do PDF atual (nota, data, placa, fornecedor).
     */
    private void salvarDadosGerais() {
        if (pdfAtualIndex >= 0 && pdfAtualIndex < dadosProcessados.size()) {
            DadosPDF dados = dadosProcessados.get(pdfAtualIndex);

            dados.setNumNota(campoNumNota.getText());
            dados.setData(campoData.getText());
            dados.setPlacaVeiculo(campoPlaca.getText());
            dados.setFornecedor(campoFornecedor.getText());

            logMessage("✅ Dados gerais salvos para: " + dados.getNomePDF());

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Salvo");
            alert.setHeaderText("Dados gerais salvos!");
            alert.setContentText("Alterações salvas para: " + dados.getNomePDF());
            alert.showAndWait();
        }
    }

    /**
     * Salva as edições de um produto individual (valorUnitario, unidade, quantidade).
     */
    private void salvarEdicaoProduto() {
        if (pdfAtualIndex < 0 || pdfAtualIndex >= dadosProcessados.size()) return;

        int prodIdx = seletorProduto.getSelectionModel().getSelectedIndex();
        if (prodIdx < 0) return;

        DadosPDF dados = dadosProcessados.get(pdfAtualIndex);
        if (dados.getListaDeProdutos() == null || prodIdx >= dados.getListaDeProdutos().size()) return;

        Produto p = dados.getListaDeProdutos().get(prodIdx);
        p.valorUnitario = campoValorUnitarioProduto.getText().trim();
        p.unidade = campoUnidadeProduto.getText().trim();
        p.quantidade = campoQuantidadeProduto.getText().trim();

        // Atualiza lista visual
        atualizarListaProdutos(dados);

        logMessage("✅ Produto [" + (prodIdx + 1) + "] atualizado: "
            + truncar(p.descricao, 30)
            + " | " + p.unidade + " | qtd: " + p.quantidade + " | R$ " + p.valorUnitario);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Salvo");
        alert.setHeaderText("Produto atualizado!");
        alert.setContentText("Produto [" + (prodIdx + 1) + "] " + truncar(p.descricao, 40) + " salvo com sucesso.");
        alert.showAndWait();
    }

    private void exportarParaExcel() {
        if (dadosProcessados.isEmpty()) {
            logMessage("⚠️ Nenhum dado para exportar!");
            return;
        }

        if (excelFile == null) {
            logMessage("⚠️ Nenhum arquivo Excel selecionado!");
            return;
        }

        exportButton.setDisable(true);
        statusLabel.setText("⏳ Exportando para Excel...");
        statusLabel.setTextFill(Color.valueOf(COLOR_WARNING));

        ExportadorExcel exporter = new ExportadorExcel();

        SeletorDeAbas.selecionarAbaAsync(
            exporter,
            excelFile.getAbsolutePath(),
            new SeletorDeAbas.SelecionarAbaCallback() {
                @Override
                public void onAbaSelecionada(String nomeAba) {
                    new Thread(() -> {
                        try {
                            logMessage("\n📤 Iniciando exportação para Excel...");
                            logMessage("📁 Caminho: " + excelFile.getAbsolutePath());
                            logMessage("📋 Aba destino: " + nomeAba);

                            int totalProdutosExportados = 0;

                            for (DadosPDF dados : dadosProcessados) {
                                logMessage("\n📄 Exportando: " + dados.getNomePDF());
                                logMessage("   Nota: " + dados.getNumNota());
                                logMessage("   Produtos: " + (dados.getListaDeProdutos() != null ? dados.getListaDeProdutos().size() : 0));

                                if (dados.getListaDeProdutos() != null && !dados.getListaDeProdutos().isEmpty()) {
                                    exporter.ExportDataTOExcel(
                                        excelFile.getAbsolutePath(),
                                        nomeAba,
                                        dados.getNumNota(),
                                        dados.getData(),
                                        dados.getPlacaVeiculo(),
                                        dados.getFornecedor(),
                                        dados.getListaDeProdutos()
                                    );
                                    totalProdutosExportados += dados.getListaDeProdutos().size();
                                }
                            }

                            final int totalFinal = totalProdutosExportados;

                            Platform.runLater(() -> {
                                logMessage("\n✅ Exportação completa!");
                                logMessage("📊 Total de produtos exportados: " + totalFinal);
                                logMessage("📋 Aba utilizada: " + nomeAba);
                                logMessage("💾 Arquivo: " + excelFile.getAbsolutePath());
                                statusLabel.setText("✅ Exportação concluída!");
                                statusLabel.setTextFill(Color.valueOf(COLOR_SUCCESS));

                                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                alert.setTitle("Sucesso");
                                alert.setHeaderText("✅ Exportação concluída!");
                                alert.setContentText(
                                    dadosProcessados.size() + " PDF(s) exportado(s)\n" +
                                    totalFinal + " produto(s) total\n" +
                                    "Aba: " + nomeAba + "\n\n" +
                                    "Arquivo: " + excelFile.getName()
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
                                alert.setContentText("Erro: " + e.getMessage());
                                alert.showAndWait();

                                exportButton.setDisable(false);
                            });
                        }
                    }).start();
                }

                @Override
                public void onErro(String mensagem) {
                    Platform.runLater(() -> {
                        logMessage("❌ Erro ao listar abas: " + mensagem);
                        statusLabel.setText("❌ Erro ao acessar Excel");
                        statusLabel.setTextFill(Color.valueOf("#EF4444"));

                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Erro");
                        alert.setHeaderText("❌ Erro ao acessar Excel");
                        alert.setContentText(mensagem);
                        alert.showAndWait();

                        exportButton.setDisable(false);
                    });
                }

                @Override
                public void onCancelado() {
                    Platform.runLater(() -> {
                        logMessage("⚠️ Exportação cancelada pelo usuário");
                        statusLabel.setText("⚠️ Exportação cancelada");
                        statusLabel.setTextFill(Color.valueOf(COLOR_WARNING));
                        exportButton.setDisable(false);
                    });
                }
            }
        );
    }

    private void logMessage(String message) {
        Platform.runLater(() -> logArea.appendText(message + "\n"));
    }

    private void limparFila() {
        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacao.setTitle("Confirmar Limpeza");
        confirmacao.setHeaderText("Limpar Fila de PDFs?");
        confirmacao.setContentText(
            "Isso irá remover todos os PDFs selecionados e dados processados.\n\n" +
            "PDFs: " + pdfFiles.size() + "\n" +
            "Dados processados: " + dadosProcessados.size() + "\n\n" +
            "Deseja continuar?"
        );

        ButtonType botaoSim = new ButtonType("Sim, Limpar", ButtonBar.ButtonData.OK_DONE);
        ButtonType botaoNao = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirmacao.getButtonTypes().setAll(botaoSim, botaoNao);

        confirmacao.showAndWait().ifPresent(resposta -> {
            if (resposta == botaoSim) {
                pdfFiles.clear();
                dadosProcessados.clear();
                pdfAtualIndex = -1;

                if (seletorPDF != null) {
                    seletorPDF.getItems().clear();
                    seletorPDF.setValue(null);
                }
                if (seletorProduto != null) {
                    seletorProduto.getItems().clear();
                    seletorProduto.setValue(null);
                }

                if (campoNumNota != null) campoNumNota.clear();
                if (campoData != null) campoData.clear();
                if (campoPlaca != null) campoPlaca.clear();
                if (campoFornecedor != null) campoFornecedor.clear();
                if (campoValorUnitarioProduto != null) campoValorUnitarioProduto.clear();
                if (campoUnidadeProduto != null) campoUnidadeProduto.clear();
                if (campoQuantidadeProduto != null) campoQuantidadeProduto.clear();
                if (campoProdutos != null) campoProdutos.clear();
                if (salvarProdutoButton != null) salvarProdutoButton.setDisable(true);

                if (editPanel != null) {
                    editPanel.setVisible(false);
                    editPanel.setManaged(false);
                }

                pdfFilesLabel.setText("PDFs: Nenhum arquivo selecionado");
                processButton.setDisable(true);
                exportButton.setDisable(true);

                statusLabel.setText("Fila limpa - Pronto para começar novamente");
                statusLabel.setTextFill(Color.valueOf(COLOR_TEXT_DIM));

                logArea.clear();
                logMessage("🗑️ Fila limpa com sucesso!");
                logMessage("✅ Sistema pronto para processar novos PDFs.");
            }
        });
    }

    /** Trunca string para exibição no ComboBox/Label */
    private String truncar(String s, int maxLen) {
        if (s == null) return "";
        return s.length() > maxLen ? s.substring(0, maxLen) + "…" : s;
    }

    public static void main(String[] args) {
        launch(args);
    }
}