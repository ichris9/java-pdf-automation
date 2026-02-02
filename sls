[33mcommit 52843c15a584571701e8f2d1198edce4552b115a[m[33m ([m[1;36mHEAD[m[33m -> [m[1;32mmain[m[33m, [m[1;31morigin/main[m[33m, [m[1;31morigin/HEAD[m[33m)[m
Author: Christian H. <christian.h2k6@gmail.com>
Date:   Thu Jan 29 11:49:20 2026 -0300

    update README
    
    Atualiza a descrição do projeto e adiciona resultados e futuras funcionalidades.

[1mdiff --git a/README.md b/README.md[m
[1mindex f75f871..0b28e10 100644[m
[1m--- a/README.md[m
[1m+++ b/README.md[m
[36m@@ -1,9 +1,30 @@[m
[31m-# DO PDF PARA O EXCEL[m
[32m+[m[32m# 📄 PDF para Excel[m
 [m
[31m-## OBJETIVO[m
[31m-Esse sistema, eu estou faznedo para a empresa em que trabalho. É uma automação de um processo manual e demorado.[m
[31m-O sistema lê os campos do pdf que a empresa precisa preencher e passa para planilha do excel da empresa.[m
[32m+[m[32m## Objetivo[m
[32m+[m[32mEste projeto automatiza um processo manual e repetitivo utilizado na empresa onde trabalho, que era demorado e sujeito a erros.[m
 [m
[31m-No meio deste processo estou aprendendo a como manuesear o git e o gitHub.[m
[32m+[m[32mA aplicação realiza a **leitura automática de campos relevantes em arquivos PDF** e transfere essas informações diretamente para a **planilha padrão em Excel** utilizada pela empresa.[m
 [m
[32m+[m[32m---[m
 [m
[32m+[m[32m## Resultados[m
[32m+[m[32mAntes da automação, o preenchimento manual levava, em média:[m
[32m+[m
[32m+[m[32m- **Cenário desfavorável:** até **1 minuto e 10 segundos (70 s)** por folha[m[41m  [m
[32m+[m[32m- **Cenário otimista:** cerca de **45 segundos** por folha[m[41m  [m
[32m+[m
[32m+[m[32mCom a automação:[m
[32m+[m
[32m+[m[32m- **Cenário desfavorável:** aproximadamente **23 segundos por folha**[m[41m  [m
[32m+[m[32m  _(46 segundos para processar duas notas simultaneamente)_[m
[32m+[m[32m- **Cenário otimista:** cerca de **15 segundos por folha**[m
[32m+[m
[32m+[m[32mCom base nos testes realizados, o sistema reduziu o **tempo médio de processamento por folha em aproximadamente 55% a 65%**, dependendo do cenário analisado.[m
[32m+[m
[32m+[m[32m---[m
[32m+[m
[32m+[m[32m## Futuras Features[m
[32m+[m[32mAs próximas funcionalidades serão focadas principalmente na **redução do tempo de processamento** e na **melhoria da experiência do usuário**, incluindo:[m
[32m+[m
[32m+[m[32m- Suporte ao envio de **múltiplos arquivos PDF simultaneamente**, reduzindo o tempo total do processo.[m
[32m+[m[32m- Implementação de **OCR**, possibilitando a leitura de PDFs provenientes de **scanners de imagem**.[m
