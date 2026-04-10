# Documentação do Sistema de Download de Mídia

Este documento descreve as funcionalidades e a lógica por trás do sistema de download de música e vídeo implementado no projeto Boilerplate.

## Visão Geral

O sistema permite que o usuário pesquise por conteúdos (músicas ou artistas), visualize os detalhes do que foi encontrado (título, duração, thumbnail e tamanho aproximado) e, após a confirmação, realize o download no formato escolhido (MP3 ou MP4).

---

## Componentes Principais

### 1. `MusicDownloadViewModel`
É o cérebro da interface. Gerencia o estado da UI, as buscas e coordena os downloads com o banco de dados e o motor de download.

#### Funções:
*   **`searchMetadata(query: String)`**:
    *   **O que faz**: Inicia a busca inicial.
    *   **Como funciona**: Chama o `downloader.fetchMetadata` para obter informações do vídeo sem baixar o arquivo completo. Atualiza o estado para mostrar a prévia ao usuário.
*   **`startDownload(query: String, format: String, selectedQuality: String)`**:
    *   **O que faz**: Realiza o download real do arquivo.
    *   **Como funciona**: Executa o processo de download (via `yt-dlp`). Ao finalizar com sucesso, insere o registro no banco de dados local (Room) para que apareça no histórico.
*   **`resetState()`**:
    *   **O que faz**: Limpa a busca atual.
    *   **Como funciona**: Redefine o `DownloadUiState` para o valor inicial, permitindo uma nova pesquisa do zero.
*   **`deleteItem(item: DownloadedItem)`**:
    *   **O que faz**: Remove um item do histórico e do disco.
    *   **Como funciona**: Tenta deletar o arquivo físico da pasta de músicas/vídeos e remove a entrada correspondente no banco de dados.
*   **`openDownloads()`**: Abre a pasta onde os arquivos são salvos no sistema operacional.
*   **`openFile(path: String)`**: Abre um arquivo específico usando o player padrão do sistema.

### 2. `MediaDownloader` (Interface)
Define o contrato para as operações de download. Possui implementações específicas para **Desktop** (usando `yt-dlp` e `ffmpeg`) e **Android**.

*   **`fetchMetadata`**: Retorna um objeto `Metadata` com detalhes do conteúdo.
*   **`download`**: Executa o download e fornece callbacks de progresso.

### 3. `DownloadUiState` (Modelo de Dados)
Representa o estado atual da tela de download.
*   `isSearching`: Indica se está buscando metadados.
*   `isDownloading`: Indica se o download está em progresso.
*   `metadataFetched`: Define se as informações já foram carregadas e estão prontas para exibição.
*   `progress`: Valor de 0.0 a 1.0 do progresso atual.

---

## Fluxo de Uso

1.  **Busca**: O usuário digita um termo e clica em "Search".
2.  **Prévia**: O app exibe o card com a imagem e detalhes do conteúdo.
3.  **Configuração**: O usuário escolhe entre MP3 ou MP4 (e a qualidade, se for vídeo).
4.  **Download**: O usuário clica em "Confirm Download".
5.  **Progresso**: Uma barra de progresso mostra o avanço em tempo real.
6.  **Histórico**: Após concluído, o item aparece nas abas "Music" ou "Videos" para ser aberto ou excluído.

---

## Requisitos Técnicos (Desktop)
*   **yt-dlp**: Utilizado para extração de links e download.
*   **FFmpeg**: Utilizado para conversão de áudio e junção de trilhas de vídeo de alta qualidade.
*   **Localização dos arquivos**: Os downloads são salvos por padrão em `~/Music/MyDownloaderApp`.
