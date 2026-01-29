# 📄 PDF para Excel

## Objetivo
Este projeto automatiza um processo manual e repetitivo utilizado na empresa onde trabalho, que era demorado e sujeito a erros.

A aplicação realiza a **leitura automática de campos relevantes em arquivos PDF** e transfere essas informações diretamente para a **planilha padrão em Excel** utilizada pela empresa.

---

## Resultados
Antes da automação, o preenchimento manual levava, em média:

- **Cenário desfavorável:** até **1 minuto e 10 segundos (70 s)** por folha  
- **Cenário otimista:** cerca de **45 segundos** por folha  

Com a automação:

- **Cenário desfavorável:** aproximadamente **23 segundos por folha**  
  _(46 segundos para processar duas notas simultaneamente)_
- **Cenário otimista:** cerca de **15 segundos por folha**

Com base nos testes realizados, o sistema reduziu o **tempo médio de processamento por folha em aproximadamente 55% a 65%**, dependendo do cenário analisado.

---

## Futuras Features
As próximas funcionalidades serão focadas principalmente na **redução do tempo de processamento** e na **melhoria da experiência do usuário**, incluindo:

- Suporte ao envio de **múltiplos arquivos PDF simultaneamente**, reduzindo o tempo total do processo.
- Implementação de **OCR**, possibilitando a leitura de PDFs provenientes de **scanners de imagem**.
