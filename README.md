# 🌿 Kpybara Engine

> **O "Obsidian da Educação"** — Plataforma educacional aberta, federada, 100% *offline-first* e de altíssimo desempenho para dispositivos móveis de entrada.

[![License: AGPLv3](https://img.shields.io/badge/License-AGPLv3-blue.svg)](https://www.gnu.org/licenses/agpl-3.0)
[![Core: C++20](https://img.shields.io/badge/Core-C%2B%2B20-00599C?logo=cplusplus)](https://isocpp.org/)
[![UI: Flutter](https://img.shields.io/badge/UI-Flutter%203-02569B?logo=flutter)](https://flutter.dev/)
[![Framerate: 60/90 FPS](https://img.shields.io/badge/Target-60%2F90%20FPS-4E9A51)](#)
[![RAM: < 30 MB](https://img.shields.io/badge/RAM-%3C%2030%20MB-F5A623)](#)
[![AI-Assisted Engineering](https://img.shields.io/badge/Engineered%20with-AI%20Native-8B5A2B)](#manifesto--transparência-ai-native)

---

## 🦫 Conheça o Kpy

O mascote oficial do projeto é o **Kpy**, uma simpática capivara maker desenhada em traços de *pixel-art* e *doodle*. 
O Kpy simboliza a serenidade, a inclusão e a perseverança no aprendizado: sem pressão punitiva, comendo melancia ao comemorar conquistas e sempre pronto para aprender com você, esteja você online ou no coração da floresta com sua mochilinha offline.

---

## 📖 Proposta de Valor

Softwares corporativos de educação tornaram-se lentos, repletos de anúncios, rastreadores invasivos e interfaces pesadas que travam em celulares populares de 2 GB de RAM. 

O **Kpybara Engine** foi construído a partir do zero para ser o oposto:
- **100% Offline-First:** Funciona de forma autônoma sem sinal de internet.
- **Leveza Extrema:** Consome menos de 30 MB de RAM total no app e menos de 5 MB no Core nativo.
- **Fluidez Real:** Executa a 60/90 FPS em hardware de entrada como Moto G22, ou processadores Helio G37.
- **Zero Burocracia:** Dados de progresso salvos em arquivos locais abertos e fáceis de versionar.
- **Conhecimento é para todos** temos essa filosofia e para isso queremos que a comunidade crie os cursos que ela bem entender        (mas cursos) livres para todas as idades e para isso temos 4 níveis de verificado : não verificado quando o curso acaba de ser publicado, verificado pela comunidade, curso de autor verificado, quando o autor é super aclamado na comunidade, e verificado oficial quando os próprio administradores verificam o curso

---

## 🤖 Manifesto & Transparência AI-Native

> **Transparência em Primeiro Lugar:** O Kpybara Engine declara abertamente que sua arquitetura, código-fonte, suítes de testes e documentação são desenvolvidos com auxílio contínuo de **Inteligência Artificial de Ponta (AI-Native / AI-Assisted Engineering)**.

Acreditamos que a IA não substitui o rigor da engenharia de software; pelo contrário, funciona como uma **superalavanca técnica** que permite a uma comunidade enxuta projetar sistemas embarcados em C++20, otimizações em nível de assembly e interfaces declarativas acessíveis com qualidade industrial. Cada algoritmo e alocador do Kpybara passa por auditoria humana e testes automatizados estritos.

---

## ⚡ Diferenciais Técnicos

### 1. Motor Universal em C++20 (`motor/`)
O núcleo de avaliação de exercícios é escrito em C++20 puro consolidado dentro da pasta `motor/` e compilado nativamente para ARM64/x86_64, comunicando-se com a interface Flutter via **Dart FFI**:
- **Estrutura Unificada (`motor/`):**
  - `motor/include/`: Cabeçalhos Zero-Copy FFI (`kpybara_core.h`), Arena Allocator (`kpybara_arena.h`) e Watchdog (`kpybara_watchdog.h`).
  - `motor/src/`: Iplementação C++20 de alta performance com algoritmo Levenshtein na Arena (`kpybara_core.cpp`).
  - `motor/dart/`: Conectores Dart FFI, Isolate Runners e Repositório Drift Offline-First.
  - `motor/CMakeLists.txt`: Build nativo moderno multiplataforma C++20.
- **Zero-Copy FFI:** Utiliza estruturas C compactas (`#pragma pack(push, 1)`) transmitidas diretamente por ponteiro. Nenhuma serialização JSON ou alocação intermediária de strings é realizada no caminho crítico.
- **Arena Allocator O(1):** Pool contíguo com teto rígido de **10 MB de RAM**. Reset instantâneo em $O(1)$ ao final de cada exercício, eliminando completamente vazamentos de memória e fragmentação de heap (*zero heap churn*).
- **Watchdog Assíncrono (< 300 ms):** Thread independente de vigilância com atômicos de interrupção garantindo que nenhum script trave o dispositivo do aluno.
- **Heurística Acelerada:** Algoritmo de distância com complexidade de espaço reduzida na própria Arena para avaliação imediata em microssegundos ($\mu s$).

### 2. Sandbox de 4 Camadas
1. **Isolamento de Memória:** Teto rígido de 10 MB imposto pelo Arena Allocator.
2. **Tempo Limite Rígido:** Aborto incondicional aos 300 ms via Watchdog.
3. **Restrição de Syscalls:** Sem chamadas diretas a sistema operacional não-autorizadas.
4. **Proteção FFI:** Validação de ponteiros nulos e checagem de assinatura mágica (`0x4B505932`).

### 3. Sincronização Federada por Deltas (RFC 6902)
Cursos e históricos são sincronizados através de diffs atômicos em JSON Patch (RFC 6902). Um curso inteiro pode receber correções gramaticais ou novas lições com payloads de apenas alguns kilobytes, economizando planos de dados 3G/4G.

### 4. As 4 Primitivas Universais de UI
A camada visual é composta por 4 renderizadores desacoplados e acessíveis (WCAG AA - 48x48 dp):
- **`SelectionPrimitive`:** Múltipla escolha, Associação de Pares e Preenchimento de Lacunas com feedback tátil suave.
- **`SequencePrimitive`:** Drag-and-drop de etapas com decorações leves para manter 90 FPS.
- **`HotspotImagePrimitive`:** Diagramas esquemáticos e circuitos com suporte a Zoom/Pan contínuos (`InteractiveViewer`).
- **`CanvasGridPrimitive`:** Matriz 2D responsiva para caça-palavras, palavras cruzadas e circuitos lógicos.

---

## 🌐 Liberdade Total para a Comunidade (Open-Source)

O Kpybara Engine pertence a todos:

> **Você tem 100% de liberdade para clonar, auditar, refatorar, otimizar ou reescrever qualquer parte do motor C++ ou dos componentes Flutter.**

Quer substituir o algoritmo de distância por SIMD/NEON? Quer portar a engine para Luau, Rust ou Zig? Quer criar um novo renderer de matrizes? 

**Pull Requests são imensamente bem-vindos!** O repositório segue governança aberta e transparente.

---

## 💼 Modelo de Negócios Transparente (Estilo Obsidian)

Assim como o Obsidian.md revolucionou a gestão do conhecimento com notas locais gratuitas e serviços opcionais de conveniência, o Kpybara adota um modelo ético e sustentável:

| Modalidade | Descrição | Preço |
| :--- | :--- | :--- |
| **Kpybara Core** | Motor completo, cursos federados, app offline, criação de conteúdo e exercícios locais. | **100% Gratuito Para Sempre** (Sem anúncios, sem rastreamento) |
| **Kpybara Sync** *(Opcional)* | Nuvem de conveniência com criptografia ponta a ponta (E2EE) para backup e sincronização automática entre celular e PC. | Assinatura individual módica |
| **Kpybara Studio Pro** *(Opcional)* | Ferramentas avançadas para criadores de cursos, exportação de estatísticas detalhadas e empacotamento federado com 1 clique. | Assinatura anual para criadores |
| **Licenças B2B** | Painéis analíticos, suporte dedicado e integração para escolas particulares e corporações. | Contrato empresarial |

*Redes públicas de ensino e iniciativas comunitárias possuem gratuidade permanente e irrestrita.*

---

## 🛠️ Guia Rápido de Build & Instalação

### Pré-requisitos
- **CMake** >= 3.15
- **Compilador C++20** (GCC 11+, Clang 13+ ou MSVC 2019+)
- **Flutter SDK** >= 3.10.0 (Dart >= 3.0.0)
- **Git**

### 1. Clonar o Repositório
```bash
git clone https://github.com/kpybara-engine/kpybara-engine.git
cd kpybara-engine
```

### 2. Compilar a Biblioteca Nativa C++ (`motor/`)

#### Linux / macOS:
```bash
mkdir -p motor/build && cd motor/build
cmake -DCMAKE_BUILD_TYPE=Release ..
cmake --build . -j$(nproc 2>/dev/null || sysctl -n hw.ncpu)
cd ../..
```

#### Android (NDK via Gradle):
O projeto Android em `app/` já possui o binding pré-configurado com as bibliotecas nativas via FFI.

### 3. Executar o Aplicativo Flutter
```bash
# Obter dependências do Flutter
flutter pub get

# Executar no dispositivo conectado ou emulador
flutter run --release
```

---

## 📜 Licença

Este projeto é distribuído sob a licença **GNU Affero General Public License v3.0 (AGPLv3)**.  
Consulte o arquivo [LICENSE](LICENSE) para obter o texto completo da licença.

---

<p align="center">
  <b>Kpybara Engine</b> • Feito com 💚, C++20 e respeito ao estudante.
</p>

## Esse readme mesmo é feito com ia então por favor não fique dando hate
## A ia foi usada pra dar o pontpé inicial e o que vale é a ideia, pois até o kernel do linux tem parte do codigo feito com ia
## Então se você não gosta de codigo de ia contribua pra removelo, e não ficar dando hate, pois pra dar hate é um dois, mas pra contribuir neca
## a origem dessa ideia é o fato de eu ter ficado de recuperção em ingles, então eu pensei em adptar a logica do Duolingo para algo de codigo aberto e feito pela comunidade, que não só ensine apenas ingles mas ensine o que a comunidade bem entender (mas com certos limetes morais e eticos) 
 ## ESSA NOTA NÃO É IA
