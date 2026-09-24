# 🤝 Guia de Contribuição — Kpybara Engine

Que bom ver você por aqui! O **Kpybara Engine** nasceu de uma dor real na sala de aula e de um desejo simples: criar uma alternativa de tecnologia educacional leve, ética, aberta e que rode liso até nos celulares mais modestos.

Este projeto é uma **base/prova de conceito em construção**. Ele só vai se transformar em uma ferramenta de grande impacto com a força da comunidade. Toda contribuição — seja uma linha de código C++, uma otimização no Flutter, uma ilustração do Kpy ou uma nova lição de curso — é infinitamente bem-vinda.

---

## 🧭 Como Você Pode Ajudar?

Não importa o seu nível de experiência, sempre há espaço para colaborar:

1. **Engenharia de Software (C++20 & Core Nativo):**
   - Otimizar o *Arena Allocator* e garimpar cada byte de memória.
   - Refinar a segurança do *Sandbox* e a thread do *Watchdog* (tempo limite de 300 ms).
   - Expandir bindings Dart FFI com *Zero-Copy structs*.
   - Portar ou adaptar o motor para novas arquiteturas ou linguagens.

2. **Frontend & Interface (Flutter / Dart):**
   - Melhorar os renderizadores das 4 Primitivas Universais (*Selection*, *Sequence*, *Hotspot Image*, *Canvas Grid*).
   - Garantir acessibilidade (WCAG AA) e performance fluida a 60/90 FPS no hardware de entrada.
   - Desenvolver a interface das abas de **Cursos Nativos** e **Cursos da Comunidade**.

3. **Criação de Conteúdo & Pedagogia:**
   - Montar e validar cursos base (ex: *Inglês do Zero*, *Lógica de Programação*, *Física Clássica*).
   - Desenhar modelos de arquivos `.kpy` para professores e escolas.

4. **Design, Arte & UX:**
   - Criar artes em pixel-art/doodle do mascote **Kpy**.
   - Melhorar a experiência de uso do aplicativo para estudantes e professores.

---

## ⏱️ Nota sobre o Tempo de Resposta e Revisão de PRs

> **Aviso Importante:** O idealizador e mantenedor deste projeto é estudante e concilia o Kpybara Engine com sua rotina de estudos. 
>
> Por conta disso, a revisão manual de *Issues* e *Pull Requests* pode levar algum tempo.
>
> 🤖 **Uso de Bot de IA para Triagem:** Para não deixar contribuições travadas na fila, estamos abertos à implementação de **Bots de IA automatizados no GitHub Actions** para realizar a primeira análise de sintaxe, checagem de regras de memória e testes de regressão dos PRs. Se você tiver sugestões sobre essa automação, deixe seu comentário nas *Discussions* do repositório!

---

## 🛠️ Regras e Padrões Técnicos

Para manter o motor estável e leve no hardware alvo (ex: Moto G22 / 2 GB RAM):

- **Teto Rígido de Memória:** O Core C++ **não pode** ultrapassar o limite estipulado pelo *Arena Allocator* (10 MB RAM). Evite alocações dinâmicas de *heap* fora do alocador dedicado.
- **Zero-Copy FFI:** Transmissão de dados via C FFI deve ser feita preferencialmente por ponteiros e structs compactas (`#pragma pack(push, 1)`), sem serialização JSON pesada no caminho crítico.
- **Segurança Primária:** Código rodando no motor deve respeitar o Sandbox (sem chamadas arbitrárias ao sistema operacional ou IO não autorizado).
- **Estilo de Código:**
  - **C++:** Diretrizes baseadas no *C++ Core Guidelines*, com foco em `const-correctness` e uso de `noexcept` em rotinas críticas.
  - **Dart/Flutter:** Seguir as convenções padrão do `dart analyze`.

---

## 🚀 Passo a Passo para Enviar sua Contribuição

1. **Escolha ou Crie uma Issue:**
   Antes de começar a codificar uma grande mudança, verifique as [Issues](https://github.com/ApoloTeamAdm/Kpybara-Engine/issues) existentes ou abra uma nova para discutir a sua ideia.

2. **Faça o Fork do Repositório:**
   Crie uma cópia do repositório na sua conta do GitHub.

3. **Crie uma Branch Temática:**
   ```bash
   git checkout -b feature/minha-nova-funcionalidade
   # ou
   git checkout -b fix/correcao-de-bug
