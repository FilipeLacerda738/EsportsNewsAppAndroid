# Plano de próximas atualizações

Partindo da **1.5.4**, propomos versões menores para cada funcionalidade, mantendo a ordem combinada. A numeração abaixo é uma proposta; correções pontuais entre essas entregas podem usar versões patch, como `1.5.5` ou `1.6.1`.

**Estado da 1.6.0:** implementação Android/API concluída e backend implantado no Render, com autorização explícita para commit/push/deploy somente do backend. Inclui busca remota combinada, dia local, debounce, paginação, ações de filtros, contagem, horário da consulta e verificação de compatibilidade da busca. `versionName` é 1.6.0; `versionCode` mantém a regra existente, sem commits/tags artificiais no Android. Regressões foram escritas; revisão de diff, sintaxe Python, XML/formatos das traduções e geração offline da consulta SQLAlchemy realizadas. Não foram executados Gradle, build Android ou suítes de testes. Nenhuma migração, SQL direto, alteração de schema/configuração do banco ou publicação Android foi feita. Build de release e teste no celular ficam com o usuário.

**Implantação verificada em 21/10/2026:** commit backend `57ffe619f78952a6c64217088aa6cabd395ac94e` (`feat: support team search and match date intervals`) enviado a `origin/main`; deploy Render `dep-dbca1brncjis738rm0a0` confirmado `live` no serviço `esports-pro-api`. Somente `app/api/v1/matches.py`, `app/services/match_search.py` e `isolated_tests/test_match_search.py` foram commitados. Consultas autenticadas de leitura à API pública confirmaram HTTP 200 e header `X-Match-Search-Version: 1` em CS2/VALORANT, busca de equipe conhecida, combinação de equipe/intervalo/jogo/status/tier, vazio para equipe inexistente e páginas 1/2 de finalizados com IDs distintos, ordem descendente por data/ID e metadados coerentes. Intervalo incompleto retornou 422. Isso verifica o contrato implantado nas amostras consultadas, não substitui testes completos ou validação Android/release. Nenhuma credencial ou corpo de resposta foi exposto no relatório. Não foi necessário usar o Supabase CLI.

## Escopo

- **Incluído:** busca completa, catálogo de equipes, favoritos, lembretes, modo sem spoilers, páginas de equipes e campeonatos e preferências de transmissão.
- **Fora desta sequência:** login, sincronização entre dispositivos e estatísticas avançadas por jogador/mapa.
- Mudanças necessárias no backend serão apresentadas separadamente para autorização. Este plano não autoriza implementação, publicação de versões ou operações Git.

## Etapas

- [x] **Preparar o contrato da busca completa.** Contratos da base reconfirmados estaticamente; decisões detalhadas abaixo para equipe, dia no fuso do celular, combinações e ordenação determinística. Implementação Android/backend autorizada e concluída localmente; validação executável permanece pendente. Não enviar a data local como dia UTC.

- [ ] **1.6.0 — Busca completa e filtros mais claros (código implementado; validação pendente).** Busca remota sobre as partidas disponíveis no catálogo, com paginação e filtros combinados, “Limpar filtros”, “Hoje” e contagem pelo `total` retornado pela API. Equipe/data não são mais refiltradas localmente. Exibir horário da última consulta bem-sucedida, sem apresentá-lo como horário de atualização da fonte.
  **Critério de conclusão:** encontrar uma equipe cuja partida esteja fora da primeira página; tratar datas próximas da meia-noite, respostas antigas e falhas sem perder resultados válidos. Não exibir contagem remota como total de um filtro aplicado somente no aparelho. Depende do novo contrato de busca; o total e o horário da consulta podem ser aproveitados no Android com a API atual.

- [ ] **1.7.0 — Catálogo de equipes, favoritas e “Meus jogos”.** Consumir o `GET /api/v1/teams/` existente para selecionar equipes mesmo quando elas não aparecem nas partidas carregadas. Exibir nome, sigla, jogo, imagem e país quando informados. Permitir favoritar pelo catálogo, lista ou detalhe, salvar no aparelho e oferecer remoção simples. Busca por nome/jogo no catálogo pode começar local, indicando quando o carregamento ainda estiver incompleto.
  **Critério de conclusão:** favoritos permanecem após fechar o app; a agenda consulta o catálogo completo de partidas usando os filtros da 1.6.0. Usar páginas com limite explícito moderado, por exemplo 100, em vez do padrão de 5.000 equipes. A resposta de equipes é uma lista sem `total`/`has_more`: tratar fim de páginas e erros sem inventar um total. Definir a identidade usando IDs, jogo e `pandascore_id` quando disponível, sem associar equipes apenas pelo nome. Começar sem conta.

- [ ] **1.8.0 — Lembretes de partidas.** Começar com “Adicionar ao calendário”, preenchendo equipes, campeonato e horário local. O usuário confirma o evento no aplicativo de calendário.
  **Critério de conclusão:** horário correto em diferentes fusos e mensagem útil quando não houver aplicativo compatível. Notificações próprias, reagendamento e alertas de início real ficam para uma evolução posterior, pois exigem uma solução adicional de sincronização.

- [ ] **1.9.0 — Modo sem spoilers.** Adicionar preferência persistente para ocultar placares e vencedores na lista e no detalhe, com revelação por partida.
  **Critério de conclusão:** ocultar também vencedores dos mapas e descrições de acessibilidade que revelem resultados; preservar a escolha ao navegar e reabrir o app.

- [ ] **1.10.0 — Página da equipe.** Abrir uma página ao tocar na equipe, reunindo identidade, elenco informado, próximos jogos, resultados e ação de favorito.
  **Critério de conclusão:** manter equipe e jogo corretamente associados, paginar partidas e tratar informações ausentes. A identidade básica já está disponível no catálogo, mas não existe `GET /teams/{id}` nem endpoint próprio de elenco. Para um elenco independente de uma partida, propor contrato específico; o elenco obtido pelo detalhe de uma partida não comprova titulares nem atualização atual. Mostrar resultados recentes disponíveis, sem prometer histórico completo: o backend contém rotina de remoção de partidas finalizadas com início há mais de dez dias.

- [ ] **1.11.0 — Página do campeonato.** Reunir identidade, tier, premiação informada e partidas organizadas por data/status. Acrescentar etapas e classificação somente quando existirem dados confiáveis.
  **Critério de conclusão:** consultar todas as partidas disponíveis daquele campeonato, com paginação e navegação consistente para os detalhes. Atualmente só há a liga aninhada nas partidas; propor consulta/filtro de liga e distinguir liga, torneio e edição antes de agrupar dados. Tier e premiação vêm na partida, sem comprovar um único valor para toda a liga. Chaves, etapas, classificação e estatísticas não estão expostas no contrato atual.

- [ ] **1.12.0 — Preferências de transmissão.** Permitir escolher idioma preferido de transmissão independentemente do idioma da interface, filtrar canais oficiais e apresentar plataforma/canal de forma consistente. Usar “Assistir ao vivo” para partidas em andamento e “Abrir canal” nos demais estados, sem afirmar que o canal está transmitindo naquele instante.
  **Critério de conclusão:** preservar a preferência, manter uma alternativa clara quando não houver canal no idioma escolhido e diferenciar metadados ausentes de `false`. Reutilizar os campos `language`, `main`, `official` e `raw_url` já retornados e a validação de links existente. Continuar abrindo links externos; `embed_url` não exige implementação de player/WebView.

- [ ] **Validar cada entrega antes de avançar.** Executar verificações JVM e estáticas pertinentes conforme o ambiente disponível. Revisar offline, timeout, navegação rápida, idioma e restauração de estado; validar no celular telas pequenas, fonte ampliada e acessibilidade. Em cada versão, revisar também serialização com R8 e alterações de permissões ou persistência. A validação no celular será feita pelo usuário, respeitando as restrições do projeto.

## Contrato da 1.6.0 — decisões da implementação

### Base confirmada antes da implementação

Preparação estática em **21/10/2026**, preservando as alterações locais. Na base, `app/build.gradle.kts:41–42` tinha `versionName=1.5.4` e `versionCode` por contagem de commits, sem exigir tag. A tabela abaixo registra a base anterior às alterações da 1.6.0. Os registros de execução na seção de evidências anteriores não foram repetidos nem revalidados na preparação ou implementação.

| Contrato | Android atual | API atual e consequência |
| --- | --- | --- |
| Lista | `Network.kt:25–43`: envelope e queries; `MatchesViewModel.kt:236–267`: envia jogo/status/tier, sem armazenar `total` | `app/api/v1/matches.py:22–68`: mesmos campos `total/page/size/has_more/items`; página 1, limite padrão 15/máximo 100, página máxima 10000 e offset máximo 100000 |
| Equipe e dia | `MatchesViewModel.kt:348–368`: substring de nome e data local apenas nas páginas carregadas | Sem filtro de equipe/intervalo; `matches.py:50–53` usa dia UTC inclusivo em `data_calendario`. Não atende à busca completa no fuso do celular |
| Ordenação | `MatchRequests.kt:35–49`: valida página e deduplica por ID interno | `matches.py:43–48`: só `begin_at`, ascendente em `not_started`, descendente nos demais; falta desempate explícito |
| Dados opcionais | `model/Match.kt` e `MatchDates.kt`: equipes/data opcionais e conversão por offset | `app/schemas/match.py:20–40` e `app/models/match.py:17–31`: equipes/data opcionais; preservar esses casos no filtro e payload |
| Detalhe | `Network.kt:46–50`, `MatchDetail.kt`: ID, equipes/jogadores, mapas e streams | `matches.py:71–89` e `schemas/match.py`: contrato já disponível; não exige nova rota para a 1.6.0 |
| Versão e equipes | Consulta de versão existente; sem método de catálogo de equipes | `system.py` mantém contrato de versão; `teams.py` retorna lista paginada sem envelope. Nenhum deles resolve filtro completo de partidas; sem alterações propostas nesta versão |

Caminhos Android da tabela relativos a `app/src/main/java/com/example/esportsnews/`; caminhos API relativos a `../esports-pro-api/`. Código disponível comprova o contrato local, não o deploy público ou dados atuais. O Git do backend recusou a leitura de status por propriedade divergente; nenhuma configuração Git foi alterada. Não se afirma que o backend esteja sem alterações locais.

### API: parâmetros e semântica implementados localmente

Estender **`GET /api/v1/matches/`**, mantendo autenticação, payload de partidas e envelope existentes. Nomes escolhidos nesta proposta: `team_query`, `begin_at_gte`, `begin_at_lt`.

| Parâmetro | Regra proposta |
| --- | --- |
| `team_query` | Opcional; remover espaços nas extremidades, vazio equivale a ausente, máximo 100 caracteres após trim. Substring literal sem distinguir maiúsculas em `team_a.name OR team_b.name`. Não incluir sigla, nome da partida ou busca aproximada implicitamente. Acentos continuam significativos; não introduzir remoção de acentos/normalização adicional nesta entrega |
| `begin_at_gte` | Início inclusivo, timestamp RFC 3339 com `Z` ou offset explícito; normalizar para UTC |
| `begin_at_lt` | Fim exclusivo, mesmo formato. Exigir ambos os limites ou nenhum, com início estritamente anterior ao fim |
| `game/status/tier` | Preservar semântica existente: jogo convertido a maiúsculas (`CS2` da UI vira `CSGO`), status exato, tier case insensitive conforme filtro atual. Sem status, excluir `canceled`. Não alterar os identificadores internos dos filtros |
| `page/limit` | Preservar limites atuais e envelope; aplicar todos os filtros antes de contar e paginar. `size` é o número de itens desta página, não o limite solicitado |
| `data_calendario` | Preservar para clientes antigos como dia UTC. Com qualquer novo limite de intervalo, retornar 422; nenhuma precedência implícita |

Combinação: `(nome da equipe A OU nome da equipe B) E intervalo E jogo E status E tier`. Ausência de um filtro não restringe aquele critério; equipes nulas não impedem correspondência na outra equipe. Data nula é excluída quando há intervalo e permanece elegível sem intervalo. Uma partida só aparece uma vez mesmo quando ambas as equipes correspondem. Preferir predicados por relacionamento/EXISTS para não multiplicar itens e `total`.

Usar parâmetros vinculados e escapar `%`, `_` e o caractere de escape para serem texto literal, nunca curingas fornecidos pelo usuário. Retornar **422** para busca acima do limite, timestamp inválido/sem offset, intervalo incompleto, igual/invertido ou conflito com `data_calendario`. Reutilizar o formato de erro de validação FastAPI, sem detalhes internos. Não restringir novos filtros a ID PandaScore nem misturar identidades. Filtro por ID de equipe e filtro de liga não são necessários à 1.6.0; ficam para as entregas que deles dependem.

**Dia local:** no Android, capturar a data e `ZoneId.systemDefault()` como parte dos critérios. Converter separadamente `date.atStartOfDay(zone)` e `date.plusDays(1).atStartOfDay(zone)` para instantes UTC e enviá-los como `Z`. Não somar 24 horas: transições de fuso podem gerar dias de 23/25 horas. Mudança de fuso, seleção de outro dia ou remoção automática de data que saiu do calendário invalida a consulta e reinicia a página 1. Um dia local sem duração por salto de calendário deve ser tratado como dia inexistente, sem enviar intervalo inválido.

**Ordenação determinística proposta:** `not_started`: `begin_at ASC NULLS LAST, id ASC`; demais status ou ausência: `begin_at DESC NULLS LAST, id DESC`. `id` é o ID interno único. `NULLS LAST` é uma decisão explícita, inclusive para a ordem descendente, e deve ser validada como mudança de apresentação para partidas sem horário.

**Compatibilidade da busca:** respostas 200 da API atualizada incluem `X-Match-Search-Version: 1`, também documentado no OpenAPI, sem alterar o envelope JSON. Com nome ou dia selecionado, o Android exige esse valor antes de publicar dados/metadados; header ausente ou incompatível mostra mensagem localizada de busca indisponível e permite limpar filtros, sem fallback para busca parcial. Sem nome/dia ativos, uma API anterior continua utilizável. Erros HTTP mantêm classificação própria, antes da verificação do header. Essa sinalização confirma a versão de contrato declarada pelo servidor, não é controle de segurança nem prova de resultados/implantação corretos.

**Limite de estabilidade:** manter paginação por offset nesta entrega, com ordem total sobre um conjunto sem alterações. Inserções, exclusões e mudanças de horário/status entre páginas podem deslocar posições e provocar omissões/repetições; deduplicar no Android não recupera omissões. Refresh recompõe a consulta desde a página 1, sem prometer snapshot de toda a navegação. `total` representa a consulta respondida, não um total imutável entre páginas. A implementação API usa CTE compartilhada e uma única consulta para contagem e itens, sem alterar o isolamento global do banco; a base anterior usava consultas separadas. Snapshot entre páginas/cursor fica fora do escopo; se for exigida ausência de omissões sob mutações, será necessário ampliar e aprovar esse contrato. Cursor sozinho também não congela horários/status mutáveis.

### Exemplos sintéticos — não executados

Dia **21/10/2026 em America/Sao_Paulo**, equipe fictícia Aurora, CS2 finalizado, tier A. A URL abaixo usa valores legíveis; o cliente deve codificar os parâmetros normalmente:

```http
GET /api/v1/matches/?team_query=Aurora&begin_at_gte=2026-10-21T03:00:00Z&begin_at_lt=2026-10-22T03:00:00Z&game=CSGO&status=finished&tier=A&page=1&limit=1
```

Exemplo de resposta 200, com dois resultados fictícios no conjunto:

```http
HTTP/1.1 200 OK
X-Match-Search-Version: 1
Content-Type: application/json
```

```json
{
  "total": 2,
  "page": 1,
  "size": 1,
  "has_more": true,
  "items": [{
    "id": 9002,
    "pandascore_id": 990002,
    "game": "CSGO",
    "status": "finished",
    "number_of_games": 3,
    "begin_at": "2026-10-21T23:30:00Z",
    "team_a_score": 2,
    "team_b_score": 1,
    "tier": "A",
    "team_a": {"id": 301, "name": "Aurora"},
    "team_b": {"id": 302, "name": "Horizonte"},
    "league": null
  }]
}
```

Mantendo os critérios e trocando somente `page=2`, outro resultado no mesmo horário usa o desempate descendente por ID:

```json
{
  "total": 2,
  "page": 2,
  "size": 1,
  "has_more": false,
  "items": [{
    "id": 9001,
    "pandascore_id": 990001,
    "game": "CSGO",
    "status": "finished",
    "number_of_games": 1,
    "begin_at": "2026-10-21T23:30:00Z",
    "team_a_score": 0,
    "team_b_score": 1,
    "tier": "A",
    "team_a": {"id": 303, "name": "Litoral"},
    "team_b": {"id": 301, "name": "Aurora"},
    "league": null
  }]
}
```

Campos opcionais omitidos usam o schema atual; não se propõe mudar o DTO de partida. Sem correspondências: `{"total":0,"page":1,"size":0,"has_more":false,"items":[]}`. Página além do fim (dentro dos limites de paginação) retorna itens vazios e preserva o total filtrado. Exemplo inválido: `GET /api/v1/matches/?begin_at_gte=2026-10-21T03:00:00Z` → 422 por ausência de `begin_at_lt`. Limites exatamente em `03:00Z`: início incluído e fim excluído. Sem data selecionada, omitir ambos os limites; sem nome, omitir `team_query`.

### Android: decisões de implementação e dependência de implantação

**Possível com o contrato atual:** “Limpar filtros” com um único reset/recarga, “Hoje” com seleção explícita (sem alternar para desmarcado ao tocar novamente), contagem e instante da última consulta bem-sucedida. Sem filtros locais, mostrar total remoto; com equipe/dia ainda locais, mostrar separadamente “N resultados entre M partidas carregadas; T no filtro remoto”. Nunca apresentar T como total da busca local. Nomear o horário como “Última consulta bem-sucedida”, sem afirmar atualização da fonte ou de todas as páginas.

**Integração implementada localmente; uso exige API atualizada:** três queries em `Network.kt`; equipe/intervalo integram a identidade da consulta do `MatchesViewModel.kt`; refiltragem local e aviso de alcance parcial removidos; paginação remota mantém critérios em todas as páginas. Debounce de 350 ms para digitação, com cancelamento e invalidação imediatos de geração antes da espera; limpar texto/filtros e mudar data/jogo/status/tier recarregam imediatamente. Paginação/polling suspensos durante critérios pendentes; resposta ou `finally` antigos não podem alterar lista, total, horário e flags.

Capturar critérios/fuso por requisição; atualizar `total` e horário somente depois de validar e aceitar a resposta da geração atual. O horário é o recebimento bem-sucedido de qualquer página aceita, inclusive polling, não o instante de atualização dos placares. Limpar metadados ao trocar critérios; preservar dados/metadados válidos no erro de refresh/próxima página da mesma consulta, com aviso de falha. Preservar avanço de página só no sucesso, retry e deduplicação por ID.

Preservar o comportamento atual entre Ao Vivo e data: Ao Vivo limpa dia; escolher dia ou Hoje durante Ao Vivo muda para Todos e consulta uma única vez. “Limpar filtros” define jogo/status/tier como Todos, texto vazio e nenhuma data; “Hoje” seleciona o dia local mantendo jogo/tier/texto. Manter calendário de oito dias, lifecycle da lista, polling de 30 s apenas na página 1 em Todos/Ao Vivo e sem erro/requisição ativa; não ampliar polling para settings/background.

Arquivos Android alterados: `Network.kt`, `MatchesViewModel.kt`, `MatchDates.kt` (limites do dia), `MatchRequests.kt` (validação de resposta/paginação), `RequestErrors.kt` (incompatibilidade da busca), `MatchesScreen.kt`, strings em `res/values`, `values-en` e `values-es`, testes de datas/requisições/erros e `app/build.gradle.kts` (somente versionName). Sem nova camada, persistência, dependência, permissão ou mudança de SDK. No backend: `app/api/v1/matches.py` delega a construção da consulta a `app/services/match_search.py`, isolada de sessões/jobs para permitir testes sem importar a aplicação; `isolated_tests/test_match_search.py` cobre dados sintéticos em memória, fora do `tests/conftest.py` existente que prepara banco/migrations. Esses testes não foram executados.

### Critérios de aprovação e validação futura

- **Busca:** localizar equipe cuja partida antes estava além da primeira página; combinação equipe/dia/jogo/status/tier; equipe A ou B nula, ambas correspondentes sem duplicação; vazio/espaços, nomes longos, caixa/acentos e `%`, `_`, barra invertida literais. Não afirmar cobertura além do catálogo retido no backend.
- **Datas:** início incluído/fim excluído, meia-noite local diferente de UTC, offsets equivalentes e frações; dias de 23/25 horas, mudança de fuso enquanto a tela está ativa/inativa e data saindo do calendário. Campos de horário nulos não quebram a consulta.
- **API:** 422 para os novos parâmetros inválidos; mesmos predicados em itens/contagem, visão consistente por resposta, limites atuais preservados, página vazia/fim e `size/has_more/total` coerentes. Requisições antigas sem novos filtros continuam funcionais; ordem com empates/nulos é documentada e testada.
- **Compatibilidade:** header de versão reconhecido para busca ativa, ausência/versão desconhecida sem publicar sucesso, headers sem distinção de caixa, preservação de classificação HTTP e erro de body ausente. API antiga funciona somente sem os filtros novos; limpar filtros recupera a listagem.
- **Paginação:** conjunto sintético estático com horários empatados e nulos atravessa páginas sem omissões/sobreposição; ambos os sentidos de ordem. Cenários com mutações demonstram o limite do offset e refresh desde a página 1, sem prometer consistência entre páginas.
- **Estado Android:** digitação rápida, limpar durante debounce, troca de filtros, respostas/`finally` obsoletos, retry sem avanço indevido e disputa refresh/polling/próxima página. Total/horário não pertencem a critérios antigos nem indicam sucesso após falha. Retorno de detalhe/settings não multiplica jobs.
- **UI:** tradução pt/en/es, carregamento inicial/refresh/página, vazio/erro com e sem dados, fonte ampliada, TalkBack e telas pequenas. Testes JVM/estáticos não substituem validação visual no celular pelo usuário; revisar Gson/R8 sem declarar release validada por debug.

Esta etapa não executou esses critérios. As regressões escritas usam funções puras no Android e banco sintético em memória no backend, sem importar a aplicação ou iniciar lifespan/jobs. Não executar a suíte antiga de backend como validação desta entrega: seu conftest contém preparação de banco/migrations. Nenhuma migração, alteração de dados externos, configuração global, provisionamento ou deploy foi feito; avaliar índices somente com evidência posterior e autorização própria.

**Escopo autorizado e implementado:** (A) Android: quatro melhorias de filtros/metadados acima e regressões; (B) backend: três parâmetros, validação, ordem total, consistência de contagem/itens por resposta, header de compatibilidade e testes isolados, preservando envelope e rotas; (C) Android integrado: queries, debounce/gerações, intervalo local, paginação/contagem, verificação de contrato, UI/strings, regressões e versionName 1.6.0. Commit/push/deploy do backend foram autorizados separadamente e concluídos; o cliente rejeita respostas sem suporte declarado em vez de mostrar filtros ignorados como sucesso. Build/publicação Android, operações Git no Android e demais versões do roadmap continuam fora desta entrega.

## Evidências anteriores de execução e comparação com o Android

As tabelas e observações desta seção registram a base **1.5.4 anterior à implementação**. A ausência de busca remota e o uso parcial do envelope descritos aqui foram tratados no código local da 1.6.0, conforme a seção de decisões acima; as consultas históricas não validam o contrato novo.

Inspeção realizada em **21/10/2026**, sem consultar a pasta `docs`. Foram executadas as rotas reais FastAPI via transporte ASGI em processo, com as dependências reais de banco e autenticação da aplicação. O lifespan não foi acionado, e o agendador permaneceu desligado antes e depois das consultas. Não foi iniciado servidor Uvicorn, nem executados jobs, migrations, endpoints administrativos ou operações de escrita. Essa verificação não valida o deploy público nem o ciclo normal de inicialização.

A primeira execução no sandbox foi bloqueada por `PermissionError`; após aprovação para acesso externo ao sandbox, cinco consultas autenticadas retornaram **HTTP 200**:

| Consulta real | Resultado observado | Alcance da evidência |
| --- | --- | --- |
| `GET /api/v1/matches/?game=CSGO&page=1&limit=3` | `total=34`, `size=3`, `has_more=true`; amostras com estados `not_started`, `running` e `finished` | Catálogo daquele momento; não representa todo o histórico do provedor |
| `GET /api/v1/matches/?game=VALORANT&page=1&limit=3` | `total=10`, `size=3`, `has_more=true`; três partidas futuras na amostra | Apenas a primeira página foi inspecionada |
| `GET /api/v1/teams/?page=1&limit=3` | Lista com três equipes, incluindo nome, sigla, jogo, país, imagens, IDs e datas de criação/alteração | Não foi obtido o total de equipes |
| `GET /api/v1/matches/814/details` | Detalhe CS2 com equipe contendo cinco jogadores e campos de mapas/transmissões | Amostra de uma partida; não comprova titulares |
| `GET /api/v1/matches/792/details` | Detalhe VALORANT com equipe contendo sete jogadores e campos de mapas/transmissões | Amostra de uma partida; não comprova titulares |

Os números e estados são fotografias da consulta e podem mudar. O endpoint raiz também respondeu `{"status":"ok","message":"Ready"}` na execução inicial, o que isoladamente não comprovava conexão com o banco. Não foi necessário consultar o endpoint de versão, que contata o GitHub, nem a saúde do banco separadamente.

### Recursos existentes que podem ser melhor aproveitados

| Recurso do backend | Cobertura atual no Android | Proposta |
| --- | --- | --- |
| Listagem paginada de equipes | Não existe método correspondente em `ApiService` | Catálogo e seleção de favoritas na 1.7.0; identidade básica da página de equipe na 1.10.0 |
| `total`, `page`, `size`, `has_more` nas partidas | DTO contém os campos; o fluxo usa paginação, mas não expõe `total` ao usuário | Quantidade de resultados remotos na 1.6.0, diferenciando filtros locais |
| `data_calendario` | Declarado no Retrofit, mas não enviado pelo ViewModel | Busca completa por dia na 1.6.0 após resolver a diferença entre dia UTC e local |
| Sigla e IDs PandaScore de equipes/ligas | Siglas são modeladas, sem uso visível nos componentes revistos; `pandascore_id` existe no detalhe de equipe, mas não nos DTOs básicos de equipe/liga | Usar sigla como complemento opcional no catálogo e mapear IDs quando necessários à identificação de favoritas e navegação |
| Idioma e marcadores de transmissão | O detalhe já ordena pelo idioma da interface, canal principal e oficial | Escolha independente de idioma/filtro na 1.12.0 |
| Datas `created_at`/`updated_at` no catálogo de equipes | Não são consumidas | Avaliar para reconciliação de cache do catálogo; não representam atualização dos placares |

### Recursos já cobertos e limites confirmados

- **Já cobertos:** formato BO, tier, premiação no detalhe, imagens para tema escuro, bandeiras, jogadores, status/nome/vencedor de mapas e múltiplas transmissões. Não tratar esses dados como funcionalidades ausentes.
- **Busca completa pendente de backend:** não existem parâmetros de nome/ID de equipe, liga ou intervalo de instantes em `get_matches`. Um catálogo de equipes não resolve sozinho a agenda completa de favoritas.
- **Dia UTC comprovado no código:** `data_calendario` é convertido em início e fim de dia UTC. O equivalente ao dia do aparelho precisa de um contrato adicional ou de outra estratégia explicitamente validada.
- **Histórico limitado por rotina existente:** `cleanup_old_matches_task` remove partidas finalizadas cujo `begin_at` é anterior a dez dias. A rotina não foi executada nesta análise; não foi medida a aplicação dessa política no ambiente consultado.
- **Atualização da fonte indisponível no payload de partidas:** existe `updated_at` no modelo de banco, mas os schemas de lista/detalhe não o expõem. O Android pode mostrar quando consultou a API; para mostrar atualização da fonte, seria necessário um contrato com semântica definida.
- **Estatísticas avançadas indisponíveis:** mapas retornam `id`, `position`, `status`, `map_name` e `winner_id`; não retornam placar de rounds, K/D, ADR, ACS, vetos ou estatísticas individuais. Não inferir esses dados dos placares da série.
- **Sem rotas próprias de ligas, torneios ou detalhe de equipe:** a existência desses modelos/relacionamentos não significa que há endpoints utilizáveis pelo Android.
- **Rotas administrativas fora do app:** criação de equipe e sincronização manual não são propostas para o cliente. A chave administrativa não deve ser incorporada ao APK.

### Referências de código inspecionadas

- Backend: `../esports-pro-api/app/main.py`, `app/api/v1/{matches,teams,system}.py`, `app/schemas/{match,team,league}.py`, `app/models/{match,team}.py` e `app/jobs/tasks.py` (os caminhos após o primeiro são relativos à raiz do backend).
- Android: `app/src/main/java/com/example/esportsnews/Network.kt`, `MatchesViewModel.kt`, `MatchComponents.kt`, `MatchDetailScreen.kt`, `MatchDetail.kt`, `MatchStreams.kt` e `model/{Match,Team,League,Stream}.kt` (os nomes após o primeiro são relativos ao mesmo pacote).

## Dependências pendentes para implementação

- Gerar o build de release e testar o Android 1.6.0 no celular. Backend atualizado já implantado e verificado por consultas de leitura; validações executáveis abrangentes continuam pendentes. Filtro por ID de equipe e liga fica para versões posteriores, sem bloquear o recorte 1.6.0.
- Definir a identidade persistida das favoritas e seu comportamento se o backend recriar IDs internos ou remover equipes.
- Definir contrato de detalhe de equipe/elenco e de liga/torneio/edição; o catálogo básico de equipes já foi confirmado.
- Decidir separadamente se haverá histórico além da retenção atual e novos dados de etapas/classificação/estatísticas.

**A 1.6.0 está implementada, com backend publicado e verificado; build, teste no celular e publicação Android ficam pendentes com o usuário.**
