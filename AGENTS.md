# AGENTS.md — Esports Hub Android

## Escopo e restrições

Cliente Android de partidas de **CS2 e VALORANT**: agenda, placares/status, ligas/tiers, equipes, escalações/mapas, filtros, transmissões externas e aviso de atualização.

Este mapa reflete o código analisado, inclusive alterações locais não commitadas. Confirmar os arquivos pertinentes antes de agir; código/configuração prevalecem sobre README e roadmap. Atualizar este documento por mudanças relevantes, sem diário de tarefas. `../esports-pro-api` é outro repositório Git: usar sempre a raiz correta para Git/Gradle.

**Restrições globais (todos os papéis):**

- **Escopo:** alterar somente o autorizado. Análise não autoriza correções; achados adicionais devem ser relatados, sem auditoria/campanha paralela. Preservar funcionalidades; remoção exige autorização.
- **Backend/banco:** somente leitura, salvo autorização explícita para alteração. Nunca modificar automaticamente código servidor, dados, schemas, migrations, RLS/políticas, funções ou configurações. Correções padrão limitam-se ao Android; relatar evidência, impacto e ajuste necessário no servidor para autorização separada.
- Não executar operações destrutivas, testes ofensivos ou cargas contra sistemas reais. Não iniciar API, containers, jobs, provisionamento, reset, migrations ou testes/scripts backend como validação Android: podem alterar dados externos; o lifespan da API agenda sincronizações e limpeza. Não executar SQL nem contatar produção fora de necessidade e autorização de escopo.
- **Git:** sem autorização explícita, não criar commits/tags, push, merge, rebase ou alterações de histórico. Preservar modificações e arquivos não rastreados do usuário, sem atribuí-los à tarefa; não usar reset, checkout/restore ou clean para limpar o ambiente.
- **Arquivos:** `README.md` e `README.en.md` exigem autorização explícita. Não alterar arquivos fora do escopo nem artefatos de release para simular validação.
- **Ambiente:** usuário instalou Android Studio e testa no próprio celular. Não exigir/instalar SDKs, JDKs, ferramentas ou componentes ausentes, inclusive por provisionamento automático. Não executar emuladores ou testes dependentes de Android Studio, emulador ou dispositivo, incluindo instrumentação/`connectedAndroidTest`. Usar apenas ferramentas disponíveis; respeitar pedidos para não compilar nem executar Gradle.
- **Confidencialidade:** nunca expor credenciais, tokens, chaves privadas ou dados sensíveis em comandos, logs, relatórios, fixtures ou commits. Não imprimir `.env`, `local.properties`, assinatura ou strings de conexão; investigar nomes/referências sem seus valores. Não armazenar dados confidenciais inseguramente, improvisar criptografia/segurança ou confiar só no cliente. Classificar chaves pelo contexto/privilégios: chave pública de API não é automaticamente segredo.
- **Veracidade:** distinguir fato, risco e hipótese; nunca declarar testes, builds, validação visual/dispositivo/release ou revisão por outro agente sem execução real.

## Arquitetura e mapa de consulta

Android nativo, Kotlin/Gradle Kotlin DSL, módulo único `:app`, pacote/applicationId `com.example.esportsnews`. **MVVM enxuta:** `AndroidViewModel` → singleton Retrofit, estado via `StateFlow`, coroutines/`viewModelScope`; sem Repository/UseCase, framework DI, Room, DataStore ou persistência de partidas implementados. Partidas ficam em memória, Coil gerencia imagens e AppCompat armazena idioma; não prometer offline completo.

**Stack:** Compose/Material 3, Activity Compose/AppCompat, Navigation Compose, Retrofit **3**/Gson/OkHttp, Coil Compose 2 e JUnit 4. Versões efetivas estão em `gradle/libs.versions.toml` **e** `app/build.gradle.kts` (há versões explícitas além do BOM); consultá-los antes de mudanças, sem atualização em massa. Não afirmar obsolescência/vulnerabilidade sem evidência.

**Build:** minSdk **26**, compile/targetSdk **35**; source/target Java **11**, daemon Gradle exige JDK **21** (nível de linguagem ≠ runtime Gradle). Wrapper Gradle 9.4.1, AGP 9.2.1 e plugin Kotlin/Compose 2.2.10 no retrato analisado. Release usa R8/minificação e shrinkResources; modelos de domínio têm `@Keep`. `versionName` consulta tags e `versionCode` conta commits; não alterar Git para ajustar build.

`P = app/src/main/java/com/example/esportsnews`; caminhos relativos à raiz Android:

| Arquivo/área | Consultar para |
| --- | --- |
| `P/MainActivity.kt` | AppCompatActivity, ViewModel compartilhado, NavHost, lifecycle da rota/polling |
| `P/MatchesViewModel.kt` | Estado lista/detalhe, filtros, páginas, jobs/gerações, polling e versão |
| `P/Network.kt` / `P/AppUpdates.kt` | ApiService, singleton Retrofit/OkHttp, DTOs paginação/versão e comparação/validação de atualização |
| `P/MatchesScreen.kt` / `P/MatchDetailScreen.kt` | Lista/filtros/calendário/refresh/update; detalhe/equipes/jogadores/mapas/streams |
| `P/MatchComponents.kt` / `P/ShimmerEffect.kt` | Cards, status, datas, skeleton/animações |
| `P/MatchDates.kt` / `P/MatchRequests.kt` / `P/RequestErrors.kt` | Datas/fuso, calendário, sessão de detalhe, paginação, condições de polling e mensagens de falha |
| `P/MatchStreams.kt` / `P/MatchWinners.kt` | Ordenação/domínios/canais de streams, bandeiras e associação de vencedor por ID PandaScore |
| `P/model/` / `P/MatchDetail.kt` | DTOs Gson; MatchDetail está fora da pasta model, mas declara pacote `.model` |
| `P/ExternalLinks.kt` | Validação de URL e Intent de navegador |
| `P/LanguageSelector.kt` / `P/ui/theme/` | Idioma AppCompat; cores, tipografia e tema |
| `app/src/main/res/` | Strings pt padrão/en/es, locales e regras de backup |
| `app/src/{main,debug}/AndroidManifest.xml` | Permissões, componentes e cleartext por variante |
| `app/src/{test,androidTest}/` | Testes JVM e instrumentados, respectivamente |

## Contratos e invariantes

- **Rotas:** `matches_list` → `match_detail/{matchId}` (`Int`), mesmo ViewModel da Activity. Detalhe carrega por ID, limpa ao sair usando token proprietário da sessão, independente dos tokens de requisição/refresh; renderiza somente resposta com ID correspondente. Servidor aceita ID interno ou PandaScore; navegação envia `match.id`.
- **Lista:** `GET api/v1/matches/`, página inicial 1, limite padrão 15; envelope `total/page/size/has_more/items`. Acumular sem duplicar `id`, consolidar avanço só após sucesso, respeitar fim/retry e preservar dados em erro de refresh/próxima página.
- **Filtros remotos:** jogo/status/tier. `CS2` na UI → `CSGO` na API; chaves internas `Todos/Ao Vivo/Em breve/Finalizados` mapeiam status para null/`running/not_started/finished`. Traduzir rótulos sem alterar identificadores.
- **Filtros locais:** nome de equipe e data somente sobre páginas carregadas. API aceita `data_calendario`, mas ViewModel não o envia. Com filtro local, carregar mais por botão; sem filtro, pelo rodapé. Vazio local não significa ausência no servidor inteiro.
- **Datas:** `OffsetDateTime` aceita offsets/frações e converte ao fuso do dispositivo; card, detalhe e filtro usam essa conversão. Oito dias começando ontem em `StateFlow`; calendário/fuso revistos ao ativar a tela e a cada minuto na lista, ou 30 s no detalhe. Data selecionada que sai do calendário é limpa. Ao Vivo limpa data; selecionar data nesse modo volta a Todos e recarrega. Filtro local por dia e filtro UTC do servidor não são semanticamente equivalentes.
- **Polling:** 30 s, habilitado pelo lifecycle `STARTED` da rota de lista, status Todos/Ao Vivo; somente página 1, sem requisição ativa/erro pendente. Não estender implicitamente a background/detalhe.
- **Detalhe:** `GET api/v1/matches/{match_id}/details`. Refresh/retry preservam dados válidos; loading inicial e refresh separados. Polling próprio de 30 s somente com rota `STARTED`, partida `running`, sem requisição ativa ou erro; pausa/cancela polling ao sair de `STARTED`, invalida respostas antigas e cessa novas consultas após status finalizado. Refresh manual pode substituir polling em curso. Campos obrigatórios inválidos geram erro localizado; equipes/listas/imagens opcionais podem ser nulas. Defaults Kotlin não garantem contrato Gson.
- **Dados do detalhe:** campeonato, data/hora, formato, tier e premiação exibidos quando informados; cliente não assume BO3. Vencedor de mapa compara `winner_id` ao `TeamDetail.pandascoreId`, nunca ao ID interno. Elencos completos rotulados como jogadores da equipe, sem afirmar titulares. Streams ordenadas por idioma, `main` e `official`, com plataforma por domínio; URLs inválidas omitidas. Placeholders locais e bandeiras somente com código de país de duas letras.
- **Versão:** `GET api/v1/system/app-version` → `version/download_url/release_notes`; compara numericamente versões estáveis `major.minor.patch`, com prefixo `v` opcional, sem oferecer downgrade. Valida versão/link HTTP(S); notas ausentes/nulas são opcionais. Consulta inicial e manual com estados verificando/sem atualização/disponível/falha, sem consultas simultâneas. Abre página externa da versão, sem instalação automática. DTO `AppVersion` protegido por `@Keep`; falhas esperadas dessa consulta opcional não bloqueiam partidas. O servidor retorna `1.0.0` como fallback em falha do GitHub; o cliente não consegue distinguir esse caso de sucesso real.
- **Acesso:** sem login/conta/sessão de usuário. Interceptor envia `X-API-Key` ← `BuildConfig.API_KEY` ← `API_KEY` em `local.properties`; servidor usa `API_ACCESS_KEY`, com chave administrativa distinta. Chave da aplicação não é identidade/permissão de usuário.

## Implementação, estado e UI

- Escolher a menor solução tecnicamente correta, respeitando MVVM/contratos. Não criar código, classes, interfaces, camadas, abstrações ou dependências sem necessidade demonstrada (dependências exigem justificativa e avaliação de compatibilidade/licença). Não reescrever código funcional nem fazer refatoração extensa sem benefício comprovado.
- Reutilizar cliente HTTP, componentes, links, datas, status e controles de loading. Kotlin oficial: classes/Composables PascalCase, funções/propriedades camelCase; preservar snake_case dos DTOs ou mapear com `@SerializedName`, sem renomear payload por estética. Preservar avisos de licença. Comentários apenas para decisões, restrições ou comportamentos não evidentes.
- ViewModel controla operações/estado; Composables renderizam e enviam eventos. Usar efeitos com chaves/descarte coerentes, nunca requests no corpo da composição. Não reter Activity, NavController ou Context de tela em ViewModel/singleton.
- Concorrência estruturada/`viewModelScope`, sem `GlobalScope`; propagar `CancellationException`. Preservar cancelamento de jobs **e contadores de geração**, inclusive em `finally`, para respostas antigas não sobrescreverem filtros, detalhe ou flags.
- Distinguir falhas de rede/HTTP/parsing de erros de programação. Não usar capturas indiscriminadas, código defensivo excessivo ou fallbacks que inventem sucesso/lista vazia/dados de domínio. Placeholder não é dado confirmado; tolerância da consulta de versão não se aplica às partidas.
- Preservar APIs desde 26; revisar guards, lifecycle, recriação da Activity, idioma e restauração de navegação. Não mudar min/targetSdk incidentalmente; avaliar mudanças de comportamento em Android posterior.
- **Tema:** esquema escuro por padrão alinhado a `DarkBackground/CardSurface/CardHeader/AccentBlue/LiveRed`, inclusive MaterialTheme do detalhe; cor dinâmica desativada por padrão. Cabeçalho/filtros rolam com a lista para caber em telas pequenas/fontes grandes. Shimmer e pisca respeitam `ValueAnimator.areAnimatorsEnabled`, observado uma vez na raiz e propagado por CompositionLocal; indicador ao vivo também tem texto.
- Textos em `res/values/`, `values-en/`, `values-es/`, mantendo formatos/placeholders e locales/seleção AppCompat. Mensagens úteis e localizadas, sem exceções brutas, URLs internas, IPs ou headers; sanitizar diagnóstico.
- Considerar tela pequena, orientação, fonte ampliada, nomes longos, recortes, barras e safe insets/edge-to-edge (target 35). Não cortar conteúdo com alturas rígidas nem depender só de cor/animação. Semântica, foco, TalkBack, alvos de toque e descrições localizadas; descrição nula só para decoração/conteúdo já descrito. Considerar movimento reduzido em shimmer/pisca.
- Distinguir loading inicial/refresh/próxima página, erro com/sem dados e vazio filtrado; manter retry/navegação. Dados ausentes de equipe, transmissão, imagem ou mapa não devem quebrar layout/ações.

## Segurança específica

- HTTPS em release; manifest main bloqueia cleartext, debug permite para servidor LAN. Preservar separação; não levar HTTP/IP local a produção, desabilitar TLS ou usar trust manager permissivo.
- Endereço via `BuildConfig.API_BASE_URL`: release fixa HTTPS público; debug lê `DEBUG_API_BASE_URL` de `local.properties` ignorado pelo Git, com fallback de loopback. Nunca versionar IP pessoal. A URL local exige HTTP(S), host, ausência de credenciais/query/fragmento e barra final; loopback no celular requer encaminhamento USB configurado separadamente.
- Chave em APK/BuildConfig é extraível: ofuscação não substitui autorização servidor. Nunca embutir chave administrativa/privada, token PandaScore ou credenciais de banco.
- Streams/download via `openExternalWebLink`: URL absoluta HTTP(S), host, sem userinfo; trata ausência de navegador/bloqueio por política. Não contornar com intents arbitrários, esquemas executáveis, WebView ou instalação automática. URL da API é entrada não confiável; substring usada para rótulo de plataforma não valida domínio.
- Imagens vêm da API, `flagcdn.com` e placeholders externos: avaliar privacidade/disponibilidade/consumo, sem encaminhar headers de autenticação do backend.
- Manifest: `INTERNET`, launcher exportada. Não há serviço explícito de armazenamento automático de locales no manifest atual. Justificar novas permissões/componentes e minimizar exportação. Backup habilitado, XMLs sem exclusões específicas: revisar backup/extração/transferência **antes** de introduzir dados sensíveis persistidos.
- Gson usa reflexão: revisar proteção R8 dos DTOs afetados, inclusive os de Network.kt; não desligar minificação globalmente. Debug aprovado não valida serialização release.
- API própria: FastAPI/Pydantic, SQLAlchemy async/asyncpg, PostgreSQL/Alembic, jobs PandaScore e releases GitHub; Android não acessa banco/PandaScore diretamente. Suporte de conexão Supabase e scripts de permissões no backend **não comprovam** políticas aplicadas/produção. Riscos servidor/banco devem ser avaliados por contratos/código disponíveis, respeitando leitura exclusiva.

## Agentes e colaboração

Manter o coordenador e estas seis especialidades, usando apenas as pertinentes. Não exigem subagentes: sem execução real, são responsabilidades de análise, sem simular consultas/revisões. Todos seguem as restrições globais; desempenho pertence a qualidade/estabilidade, sem agente adicional.

| Agente | Responsabilidade/área e padrão | Limites e colaboração necessária |
| --- | --- | --- |
| **Coordenador técnico** | Escopo, estado inicial, impacto/arquivos, divisão de trabalho útil e revisão integrada | Não ampliar permissões; um dono por arquivo, sem cerimônia trivial; acionar especialistas por impacto e consolidar validação |
| **Arquitetura Android e estado** | MainActivity, ViewModel, navegação, modelos e Gradle; MVVM, lifecycle/compatibilidade | Sem impor Clean Architecture/DI/modularização; UI para fluxo/eventos, qualidade para concorrência, segurança para configuração/DTOs |
| **UI/UX Compose e localização** | Telas, componentes, tema/strings, idioma, acessibilidade e estados visuais; reutilizar recursos | Preservar negócio do ViewModel; arquitetura para eventos/lifecycle, segurança para links/mensagens |
| **Segurança Android** | Manifests, permissões/backup, ExternalLinks, interceptor/chave e release/R8 | Sem autenticação fictícia ou confiança em segredo embutido; contratos para origem/privilégio, UI para interação segura |
| **Contratos e segurança de integrações** | Network/DTOs; leitura de API `app/api/v1`, `schemas`, `core/security.py` e persistência necessária; autorização/nulabilidade/paginação/exposição | Backend/banco somente leitura; relatar ajustes servidor, colaborar com arquitetura/segurança em correções do cliente |
| **Qualidade, estabilidade e desempenho** | Testes JVM, paginação/jobs/erros, recomposição/imagens/animações; evidência e validação proporcional | Sem instrumentação proibida ou otimização especulativa; consultar donos de UI/estado antes de mudar comportamento |
| **Git, GitHub e versionamento (`git_versioning`)** | Estado/diffs, commits coesos, branches/conflitos, tags, versões Android, changelogs, PRs e releases conforme convenções existentes | Consultivo por padrão; efeitos colaterais exigem autorização específica conforme regras abaixo. O coordenador o utiliza em tarefas de versionamento quando disponível, sem acionamento obrigatório nas demais tarefas; não subdelegar |

**Pedidos de versionamento:**

- “Mostra o status” autoriza somente leitura; “prepara uma release” autoriza análise e proposta de versão/changelog/release, sem publicação ou outras escritas implícitas.
- Nunca criar commits sem solicitação explícita. “Commita” ou “faz um commit” autoriza um único commit local das alterações pertinentes ao contexto, incluindo staging seletivo, sem pedir nova autorização. Confirmar a raiz e apresentar o escopo antes de executar; se houver ambiguidade sobre arquivos ou mudanças, esclarecer antes de commitar.
- Antes do commit, conferir `git status`, `git diff`, `git diff --cached` e arquivos untracked pertinentes; distinguir alterações preexistentes. Incluir apenas arquivos/trechos relacionados, sem segredos, temporários ou gerados; não usar staging indiscriminado. Preservar staging alheio; se impedir isolar o commit, esclarecer sem desfazê-lo.
- Gerar automaticamente a mensagem em Conventional Commits, respeitando as convenções existentes. Após concluir, informar hash, mensagem e arquivos incluídos.
- A autorização de commit não inclui push, tags, criação/exclusão/troca de branches, merge, rebase, alteração de histórico, criação de PR ou publicação de release; cada operação exige autorização específica.
- Antes de propor versões/releases, confirmar no Gradle como tags e histórico determinam `versionName`/`versionCode`. Não criar commits artificiais ou tags desnecessárias para alterar números, nem modificar automaticamente Gradle/configurações de release em tarefas comuns de commit.

**Fluxo operacional:**

1. Ler instruções e contexto do fluxo, verificar estado local; buscar símbolos/contratos com `rg` antes de abrir arquivos.
2. Consultar só arquivos necessários usando o mapa; ignorar .git, caches, builds, binários/APKs, dependências e gerados salvo necessidade específica. Reaproveitar contexto já apurado.
3. Mapear impacto em UI/estado/rede/segurança/compatibilidade; envolver outro papel só por dependência real. Compartilhar contrato, decisão e arquivos, sem repetir investigação; em paralelo autorizado, coordenar arquivos compartilhados antes de editar.
4. Aplicar solução pequena, revisar regressões/diff e validar conforme ambiente. Evitar delegação, documentos, temporários, planos extensos e relatórios sem benefício. Economia de tokens não justifica omitir verificações críticas.

## Investigação e prevenção de regressões

Investigar o fluxo tocado; priorizar impacto, gravidade, evidência, alcance e risco de regressão. Classificar: **comprovado** (arquivo/linha + contrato/reprodução/teste), **risco fundamentado** (condição ainda não reproduzida), **hipótese** (verificação pendente). Achados fora do escopo: condição, consequência e proposta breves, respeitando confidencialidade e limites globais.

Selecionar cenários pertinentes, sem repetir verificações já suficientes:

- **Fluxos/dados:** filtros combinados, cliques/navegação rápidos, reentrada no detalhe, duplicatas/fim/retry de páginas, respostas vazias/incompletas/inválidas; datas com offsets/frações, virada de dia, fuso/idioma.
- **Concorrência:** respostas obsoletas após filtro/ID, cancelamento durante rede/parsing, flags em finally, disputa refresh/polling/paginação e jobs multiplicados por recomposição/retorno.
- **Rede:** offline, timeout, parsing, 403/404/429/5xx; preservar dados válidos e comunicar falha. Sem retry infinito/agressivo/sem cancelamento; considerar timeouts atuais de 60 s.
- **Segurança:** vazamentos em logs/telas/backups, armazenamento, autenticação/autorização, entradas externas, transporte, permissões/componentes; contratos API/banco, sem presumir produção.
- **Recursos:** main thread sem I/O bloqueante, leaks/retenção de Context, chaves estáveis em listas lazy, imagens dimensionadas e animações necessárias; demonstrar/medir custo antes de otimizar recomposição, alocações, filtro ou memória da lista acumulada.
- **Compatibilidade/manutenção/UX:** Android futuro/background/insets, dependências/BOM/R8, complexidade/duplicação, acessibilidade e estados de loading/erro/offline, conforme padrões anteriores.

## Validação e conclusão

- Antes de Gradle, conferir JDK, SDK, distribuição e dependências instalados, sem revelar configuração sensível. Wrapper/Foojay podem baixar componentes; `--offline` **não garante** que distribuição/toolchain já estejam disponíveis. Se faltar componente, relatar impedimento; não provisionar.
- Com ambiente completo, usar wrapper/PowerShell na raiz Android e apenas tarefas relevantes: `./gradlew.bat --offline :app:testDebugUnitTest`, `:app:lintDebug` ou `:app:assembleDebug`. Não executar todas por rotina.
- Testes JVM: `ExternalLinksTest`, `MatchContractTest`, `AppUpdatesTest`, `MatchDatesTest`, `MatchRequestsTest`, `MatchDataTest` e `RequestErrorsTest`; `ExampleUnitTest` é exemplo, sem cobertura funcional. Regressões escritas para offsets/fuso/calendário, propriedade/invalidação de sessões, paginação, regras de polling, streams, vencedores, nulabilidade e classificação de erros. Testes de regras não comprovam integração real de jobs/lifecycle ou UI. Adicionar regressões proporcionais a mudanças de comportamento, sem espelhar apenas a implementação.
- Usar dados sintéticos sem segredos e isolamento de rede. Quando execução não for viável, revisar código/contratos/recursos estaticamente e indicar lacunas. Inspeção estática/JVM não comprovam aparência em tela, compatibilidade Android ou release.
- Alteração documental requer revisão de conteúdo/diff, sem build/testes do app desnecessários.
- Concluir só com escopo atendido, contratos e regressões pertinentes revisados, status/diff (inclusive novos arquivos) conferidos no repositório correto e sem mudanças incidentais. Relatar concisamente o que mudou/por quê, verificações realmente executadas/resultados, limitações e riscos materiais pendentes; respeitar as restrições de veracidade e Git.
