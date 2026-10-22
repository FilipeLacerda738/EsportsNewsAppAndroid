# Plano de refatoração e organização do Android

Refatorar o Android gradualmente, começando pela organização dos arquivos e depois separando responsabilidades. Na leitura que originou este plano, `MatchesViewModel.kt` tinha 583 linhas concentrando listagem, detalhes, cache, paginação, filtros, polling e atualização do aplicativo. Confirmar o código atual antes de executar cada etapa.

## Escopo

- **Incluído:** estrutura de pacotes, estado das telas, acesso à API, componentes Compose, dependências e documentação.
- **Fora:** alterações no backend, novas funcionalidades, mudanças visuais e divisão em módulos Gradle nesta etapa.

O código já possui regras e testes úteis para cache, datas, contratos e descarte de respostas antigas. A refatoração deve preservar essas proteções.

## Estrutura proposta

Dentro de `com.example.esportsnews`:

```text
app/
  MainActivity.kt
  EsportsApp.kt
  navigation/
    AppNavHost.kt
    AppRoutes.kt
    HubDrawer.kt

data/
  remote/
    ApiService.kt
    RetrofitClient.kt
    dto/
  repository/
    MatchesRepository.kt
    AppUpdatesRepository.kt
  cache/
    MatchMemoryCache.kt

feature/
  matches/
    MatchesRoute.kt
    MatchesScreen.kt
    MatchesViewModel.kt
    MatchesUiState.kt
    MatchFilters.kt
    components/
  matchdetail/
    MatchDetailRoute.kt
    MatchDetailScreen.kt
    MatchDetailViewModel.kt
    MatchDetailUiState.kt
    components/
  settings/
    SettingsScreen.kt
    LanguageSelector.kt
  updates/
    AppUpdatesViewModel.kt
    AppUpdateDialog.kt
    AppUpdates.kt

core/
  network/
  links/
  time/
  ui/
    components/
    theme/
```

As funções específicas de partidas, como vencedores, streams e premiação, ficam próximas da funcionalidade que as utiliza. `core` recebe apenas código compartilhado.

## Etapas

- [x] **1. Registrar a base de validação.** Executar os testes existentes e documentar os fluxos que precisam continuar funcionando: filtros, busca, paginação, detalhes, idiomas e atualização. Preservar as alterações locais já presentes.

- [x] **2. Reorganizar arquivos sem alterar comportamento.** Mover classes e testes para os pacotes correspondentes. Corrigir `MatchDetail.kt`, que está fisicamente na raiz, embora declare o pacote `model`. Compilar antes de avançar.

- [x] **3. Separar o acesso aos dados.** Dividir `Network.kt`, que reúne DTOs, interface Retrofit e configuração HTTP. Introduzir repositórios recebidos por construtor, permitindo substituir a API nos testes. Manter inicialmente os modelos serializados existentes para limitar a migração.

- [x] **4. Dividir o ViewModel por responsabilidade.** Manter listagem, busca e paginação em `MatchesViewModel`; transferir detalhes para `MatchDetailViewModel` e atualização para `AppUpdatesViewModel`. Definir explicitamente quem possui os caches e os jobs para evitar requisições duplicadas e perda de dados ao navegar.

- [x] **5. Consolidar o estado de cada tela.** Criar `MatchesUiState` e `MatchDetailUiState`, atualizados de forma consistente. Substituir filtros baseados em textos como `"Todos"` e `"Ao Vivo"` por tipos próprios, separando valor da API e tradução exibida.

- [x] **6. Separar integração e apresentação no Compose.** Usar componentes `Route` para observar estado e controlar lifecycle; deixar `Screen` receber estado e callbacks. Extrair de `MatchDetailScreen.kt` seus blocos de apresentação e separar os filtros de `MatchesScreen.kt`. Adotar coleta de estado vinculada ao lifecycle.

- [x] **7. Simplificar navegação e componentes compartilhados.** Extrair `NavHost` e drawer de `MainActivity`. Retirar `HubDrawer` e `AppUpdateDialog` de `SettingsScreen.kt`. Dividir `MatchComponents.kt` por componentes coerentes, mantendo pequenos elementos relacionados juntos.

- [x] **8. Padronizar build e documentação.** Centralizar as dependências em `libs.versions.toml`, revisando a declaração explícita do Material 3 junto ao BOM. Corrigir nos READMEs o header real `X-API-Key`, configuração das propriedades, requisitos Java e descrição do cache em memória. Documentar o debug apontando para o Render.

- [x] **9. Validar cada extração e concluir a regressão.** Executar `:app:testDebugUnitTest`, `:app:assembleDebug` e lint. Acrescentar testes relevantes dos ViewModels com API e relógio controláveis: troca rápida de filtros, resposta antiga, cancelamento, falha na próxima página, cache vencido e polling ao sair da tela. Validar navegação e idiomas no emulador; evitar testes automatizados repetitivos contra o Render.

## Riscos e critérios de conclusão

O trecho mais sensível é a coordenação de **polling, cache e respostas concorrentes**. As verificações atuais de geração e sessão devem acompanhar a extração, com testes antes de serem simplificadas.

A refatoração estará concluída quando cada tela tiver responsabilidade definida, os ViewModels puderem ser testados sem rede real e os fluxos atuais mantiverem o comportamento.

## Observações da leitura inicial

- O `AGENTS.md` estava ausente na raiz durante a elaboração do plano. Verificar novamente antes de implementar e seguir as instruções aplicáveis.
- Nenhum arquivo de código foi alterado nem testes foram executados para elaborar o plano.
- Este documento registra o plano; sua criação não inicia a implementação.


## Registro da execução — 2026-10-22

- Base inicial: 48 testes unitários aprovados antes da alteração do código. O shell não tinha Java configurado; os comandos foram executados com `JAVA_HOME` apontando para o JDK do Android Studio. O daemon Gradle usou JDK 21.
- Alterações locais preexistentes em README, build e arquivos da IDE foram preservadas. Nenhum commit, troca de branch ou alteração no backend foi realizado. `local.properties` não foi alterado.
- Código e testes reorganizados em `app`, `data`, `feature` e `core`; os modelos Gson foram mantidos com os mesmos campos e anotações em `data/remote/dto`.
- Listagem, detalhes e atualização possuem ViewModels distintos, com repositórios recebidos pelo construtor. Relógios de tempo decorrido e de consulta são substituíveis nos ViewModels de partidas.
- A Activity possui os três ViewModels: o cache de 40 páginas pertence à listagem; o cache de 20 detalhes pertence ao ViewModel de detalhes. Navegar preserva ambos; encerrar a Activity definitivamente libera os ViewModels e cancela seus jobs. Cada Route controla a atividade do seu destino; sessões e gerações continuam descartando respostas antigas.
- Estados imutáveis por tela, filtros tipados, coleta vinculada ao lifecycle, componentes de apresentação extraídos e navegação/drawer separados da Activity. Configurações e diálogo de atualização também recebem estado e callbacks por suas rotas.
- Dependências centralizadas no catálogo. Material 3 segue o BOM. Espresso e AndroidX Test JUnit foram atualizados para executar a regressão na imagem Android 16 disponível; a tentativa inicial com Espresso antigo na imagem Android 17 falhou na infraestrutura de injeção de eventos.

### Fluxos e evidências

| Fluxo preservado | Validação realizada |
| --- | --- |
| Filtros | Troca rápida descarta resposta antiga; CS2 envia CSGO; ao vivo limpa a data; escolher data desativa ao vivo. |
| Busca | Debounce de 350 ms e cancelamento da busca anterior, com API simulada. |
| Paginação | Falha na próxima página preserva itens e retry solicita a mesma página. |
| Detalhes | Troca de sessão, descarte de resposta antiga, cancelamento, cache ao navegar, expiração e erro de atualização. |
| Polling | Listagem cancela polling em andamento ao sair; detalhes pausam fora da tela e revalidam no retorno. |
| Idiomas e navegação | Emulador Pixel_9_2, Android 16/API 36: lista → detalhes → lista → configurações → lista em pt-BR, en e es. Contextos de recursos de cada idioma foram fornecidos aos testes; não foi automatizada a troca de idioma pelo seletor do sistema. |
| Atualização | Requisições simultâneas deduplicadas, dispensa do diálogo, falha e nova consulta, além dos testes de versão e URL existentes. |

Validação final aprovada:

```bash
JAVA_HOME='/Applications/Android Studio.app/Contents/jbr/Contents/Home' ./gradlew :app:connectedDebugAndroidTest :app:testDebugUnitTest :app:assembleDebug :app:lint
```

- 61 testes unitários (48 existentes e 13 novos), sem falhas.
- 4 testes instrumentados, sem falhas: três cenários de navegação/localização e o teste de contexto existente.
- APK: `app/build/outputs/apk/debug/app-debug.apk`.
- Lint sem erros; avisos registrados em `app/build/reports/lint-results-debug.html`.
- Testes usam API simulada; não foram executadas chamadas de regressão contra o Render. A integração com o backend real e a comparação visual por screenshots não fazem parte desta evidência.
- Emuladores iniciados para esta execução encerrados após a validação.
