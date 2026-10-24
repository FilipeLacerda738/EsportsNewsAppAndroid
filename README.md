# 🎮 Esports Hub - Android Client

<div align="center">

🇧🇷 Português | 🇺🇸 [English Version](./README.en.md)

### O seu portal para acompanhar partidas de eSports em tempo real.

[![Licença](https://img.shields.io/github/license/FilipeLacerda738/EsportsNewsAppAndroid?style=flat-square&logo=gnu&color=2B3137&labelColor=161B22)](LICENSE)
[![Android](https://img.shields.io/badge/Android-8.0%2B-3DDC84?style=for-the-badge&logo=android&logoColor=white&labelColor=161B22)](https://www.android.com/)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white&labelColor=161B22)](https://kotlinlang.org/)

<a href="https://github.com/FilipeLacerda738/EsportsNewsAppAndroid/releases/latest">
    <img src="https://img.shields.io/github/v/release/FilipeLacerda738/EsportsNewsAppAndroid?label=Baixar%20APK&logo=android&style=for-the-badge&color=3DDC84&labelColor=161B22" alt="Baixar APK">
</a>

<br><br>
<img src="assets/Demo_app.gif" width="200" alt="Demonstração do Esports Hub no Android" />

</div>

---

> 🚀 **Ecossistema Completo (Fullstack)**
> As consultas de partidas são atendidas por um **Backend Próprio em Python (FastAPI)**, responsável por sincronizar a PandaScore com PostgreSQL e fornecer um catálogo próprio ao Android.
> 🔗 [Conheça a Engenharia do Backend aqui](https://github.com/FilipeLacerda738/esports-pro-api.git)

---

## 🎯 O Desafio e o Produto

O **Esports Hub** reúne agenda, placares e resultados de **Counter-Strike 2 e VALORANT** em um aplicativo Android nativo. A proposta é facilitar o acompanhamento de equipes e campeonatos em uma interface escura, com busca, filtros e detalhes de cada confronto.

O projeto utiliza **Jetpack Compose, MVVM e Coroutines**, com interface em português, inglês e espanhol. Não é necessário criar uma conta para acompanhar as partidas.

### 🎮 O que você encontra no aplicativo

* **Agenda e resultados:** partidas ao vivo, futuras e finalizadas, com campeonato, equipes, horário e placar.
* **Busca e filtros combinados:** encontre equipes por parte do nome e filtre por jogo, status, tier (S, A, B ou C) e dia.
* **Calendário no seu fuso:** oito dias a partir de ontem, com horários convertidos para o fuso do celular.
* **Detalhes do confronto:** formato, premiação, jogadores das equipes e situação dos mapas quando informados pela fonte. Os elencos não representam necessariamente a escalação daquela partida.
* **Transmissões:** links identificados por idioma e plataforma, abertos em um aplicativo externo.
* **Configurações:** seleção de idioma e consulta de novas versões do app.

## ⚡ Engenharia e Otimizações de Destaque

* 📉 **Otimização do APK:** release habilita `R8/ProGuard` e `shrinkResources`; DTOs usam `@Keep` para preservar a desserialização Gson. O tamanho final depende da variante e dos recursos incluídos.
* 🛡️ **Segurança e Tratamento de Erros:** mensagens localizadas para falhas de rede e HTTPS obrigatório em release. Chaves administrativas, credenciais do banco e tokens PandaScore permanecem no servidor.
* 🎨 **Design System "Cinza Chumbo":** Transição completa para um tema escuro focado em leitura dinâmica e ergonomia visual, inspirado em Strafe/HLTV.
* 🔄 **Atualizações sem perder o contexto:** cache em memória e estados separados de carregamento inicial, refresh e próxima página preservam dados válidos quando uma nova consulta falha.
* ⏱️ **Polling por tela:** consultas de 30 segundos acompanham o lifecycle. A lista consulta apenas a primeira página em Todos/Ao Vivo; o detalhe consulta apenas partidas `running`, sem requisição ativa ou erro pendente.

---

## 🛠 Stack Tecnológico

| Interface Gráfica | Arquitetura & Estado | Conectividade |
| :--- | :--- | :--- |
| **Jetpack Compose** (Material 3) | **MVVM** (Model-View-ViewModel) | **Retrofit 3** + Gson |
| **Coil** (Cache e Imagens Assíncronas) | **StateFlow** (Reatividade) | **OkHttp** (Interceptação) |
| **Componentes Customizados** | **Coroutines** (Concorrência) | **Injeção Dinâmica de URLs** |

### 🧩 Organização e fluxo de estado

Módulo único `:app`, pacote/applicationId `com.example.esportsnews`, com organização por funcionalidade. `MatchesViewModel`, `MatchDetailViewModel` e `AppUpdatesViewModel` pertencem à Activity e usam repositórios que recebem a API por construtor.

```text
Route → Screen / eventos → ViewModel → Repository → Retrofit → API
                            ↓
                        StateFlow → UI
```

Caminhos relativos a `app/src/main/java/com/example/esportsnews/`:

| Diretório | Responsabilidade |
| :--- | :--- |
| `app/` | Activity, composição, navegação e menu lateral |
| `feature/matches/` | Lista, busca, filtros, calendário, paginação e polling |
| `feature/matchdetail/` | Detalhe, jogadores, mapas e transmissões |
| `feature/settings/` e `feature/updates/` | Idioma, configurações e atualização do app |
| `data/remote/`, `data/repository/` e `data/cache/` | Contratos/DTOs, acesso à API e cache em memória |
| `core/` | Datas/fuso, erros, links, tema e componentes compartilhados |

Versões em [gradle/libs.versions.toml](gradle/libs.versions.toml) e [app/build.gradle.kts](app/build.gradle.kts): Kotlin/Compose 2.2.10, Retrofit 3.0.0, Coil 2.7.0, AGP 9.2.1 e Gradle 9.4.1. Textos em `res/values/`, `values-en/` e `values-es/`.

---

# ⚙️ Instalação

### 📱 Para instalar e usar

Você precisa de Android 8.0 ou superior e internet para consultar dados atualizados. Use o botão **Baixar APK** no topo ou a [página de releases](https://github.com/FilipeLacerda738/EsportsNewsAppAndroid/releases/latest), abra o APK e autorize a instalação pelo navegador/gerenciador de arquivos se solicitado pelo Android.

Na tela inicial, combine os filtros e toque em uma partida para ver os detalhes. O menu lateral abre as Configurações. A verificação de atualização abre a página da versão; o download e a instalação são feitos pelo usuário. O código pode estar à frente do APK publicado e o GIF pode representar uma versão anterior.

## Pré-requisitos

* Android Studio compatível com AGP 9.2.1 / Gradle 9.4.1
* JDK 21 para o daemon Gradle; compatibilidade de código Java 11
* Android SDK 35; Android 8.0 (API 26) mínimo

---

## 1. Clone o repositório

```bash
git clone https://github.com/FilipeLacerda738/EsportsNewsAppAndroid.git
cd EsportsNewsAppAndroid
```

## 2. Configure sua API KEY

No `local.properties` da raiz, preserve o `sdk.dir` gerado pelo Android Studio e adicione:

```properties
API_KEY=sua_chave_de_acesso_aqui
DEBUG_API_BASE_URL=http://127.0.0.1:8000/
```

---

## 3. Execute o projeto

* Abra no Android Studio
* Aguarde o Gradle sincronizar
* Clique em **Run 'app'**

Para gerar um APK debug pelo PowerShell, com o ambiente preparado:

```powershell
.\gradlew.bat :app:assembleDebug
```

Saída: `app/build/outputs/apk/debug/app-debug.apk`. No Linux/macOS, use `./gradlew`. `minSdk` é 26 e `compileSdk`/`targetSdk` são 35. `versionName` está no Gradle; `versionCode` usa o maior valor entre 59 e a contagem de commits (contagem com fallback para 1). A assinatura de distribuição deve ser configurada separadamente pelo mantenedor.

---

# 🌐 Arquitetura de Rede

A URL é definida por variante no build. Debug lê `DEBUG_API_BASE_URL`; sem a propriedade, usa `http://127.0.0.1:8000/`. Não deixe o valor vazio: remova a propriedade para usar o padrão. **No seu fork, configure `API_BASE_URL` da variante release em `app/build.gradle.kts` com a URL HTTPS do seu próprio deploy**, por exemplo `https://sua-api.example.com/`. Essa configuração é obrigatória antes de distribuir seu APK.

A URL deve usar HTTP(S), terminar em `/` e não conter credenciais, query ou fragmento. Para backend local no emulador, use `http://10.0.2.2:8000/`. Em um celular conectado por USB, encaminhe a porta e mantenha o endereço de loopback:

```bash
adb reverse tcp:8000 tcp:8000
```

O backend precisa ser iniciado separadamente, conforme seu README. Debug permite HTTP local; release bloqueia tráfego sem TLS.

Em `local.properties`, mantenha `sdk.dir` configurado pelo Android Studio. Valores de propriedades não usam aspas. `API_ACCESS_KEY` continua aceito como alternativa quando `API_KEY` estiver ausente. A chave deve corresponder à `API_ACCESS_KEY` do backend. Não versione esse arquivo.

Todos os requests usam o header:

```http
X-API-Key: SUA_API_KEY
```

O cache de partidas é apenas em memória, sem Room ou persistência offline: até 40 páginas e 20 detalhes, frescos por 30 segundos e retidos por até 10 minutos. Dados já exibidos continuam disponíveis quando uma atualização falha. A listagem e os detalhes possuem seus próprios caches e jobs; os ViewModels pertencem à Activity, preservando o histórico ao navegar. Polling de 30 segundos acompanha o lifecycle de cada destino e pausa quando ele sai da tela.

As telas recebem estado imutável e callbacks; as rotas observam o estado com lifecycle. Repositórios recebem a API por construtor, e os ViewModels de partidas recebem relógios substituíveis nos testes.

### 🔌 Contratos consumidos

| Método e caminho | Uso |
| :--- | :--- |
| `GET /api/v1/matches/` | Lista com envelope `total`, `page`, `size`, `has_more` e `items` |
| `GET /api/v1/matches/{match_id}/details` | Detalhe com equipes, jogadores e mapas |
| `GET /api/v1/system/app-version` | `version`, `download_url` e `release_notes` |

CS2 é enviado como `CSGO`; VALORANT, como `VALORANT`. Status: `running`, `not_started` e `finished`. Busca por equipe usa `team_query` com debounce de 350 ms. O dia local vira um intervalo UTC com `begin_at_gte` inclusivo e `begin_at_lt` exclusivo. Com equipe ou dia ativo, o app exige **`X-Match-Search-Version: 1`** na resposta para confirmar suporte aos filtros.

A paginação usa 15 itens por página, deduplica IDs e reinicia pela página 1 no refresh. Os limites do cache são por entradas, não por bytes. Tokens de geração/sessão impedem que respostas antigas substituam os filtros ou o detalhe atuais. A chave de leitura embutida no APK é extraível e não autentica um usuário; nunca configure uma chave administrativa no app.

### ℹ️ Disponibilidade e limitações

* Os placares são atualizados por polling, sujeitos à sincronização do backend e à cobertura da PandaScore; não há garantia de atualização instantânea.
* A sincronização atual importa tiers S, A e B. O filtro C existe na interface, mas pode não encontrar dados no catálogo sincronizado.
* O backend remove partidas finalizadas com início anterior a dez dias; o histórico não é completo.
* O cache desaparece quando o processo é encerrado. A última consulta bem-sucedida não indica o momento da atualização na fonte.
* Streams abrem serviços externos; disponibilidade e reprodução dependem desses serviços. Campos opcionais podem não ser informados.
* O backend consulta a versão do app no GitHub e retorna `1.0.0` se essa consulta falhar; isso pode impedir um aviso de atualização.

### 🧪 Validação local

Com JDK, SDK e dependências disponíveis, os testes JVM usam API simulada, sem consultar o Render:

```powershell
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:lintDebug
```

Os testes cobrem datas/fusos, contratos, paginação, cache, erros, links, streams, vencedores, atualizações e transições dos ViewModels. Eles não substituem verificações visuais, de lifecycle em dispositivo ou de serialização em release.

### 🛠️ Problemas comuns

| Sintoma | Verificação |
| :--- | :--- |
| Lista vazia | Limpe os filtros e confira conexão, cobertura e sincronização do backend. |
| HTTP 403 em desenvolvimento | Confira a chave de leitura e gere novamente o app após alterar `local.properties`. |
| Backend local inacessível | Verifique servidor/porta, URL com `/` final e encaminhamento USB. |
| Busca por equipe/dia incompatível | Atualize o backend para o contrato `X-Match-Search-Version: 1`. |
| Falha ao atualizar dados | Dados anteriores podem permanecer visíveis; tente novamente após recuperar a conexão. |

---

# 🤝 Contribuições

Contribuições são muito bem-vindas. Informe comportamento esperado, reprodução, versão do Android e verificações realizadas. Preserve traduções, contratos da API e Android 8.0; não inclua chaves, configurações pessoais ou arquivos de assinatura.

## Fluxo de contribuição

```bash
# Fork do projeto

# Crie uma branch
git checkout -b feature/minha-feature

# Commit
git commit -m "feat: nova funcionalidade"

# Push
git push origin feature/minha-feature
```

Depois disso, basta abrir um Pull Request 🚀

---

# 📄 Licença

Distribuído sob a licença GNU General Public License v3.0.

Veja [LICENSE](LICENSE) para mais informações. Os avisos do código permitem GPL v3 ou posterior. Dados esportivos, imagens, marcas e transmissões têm seus próprios titulares; a licença do código não concede direitos sobre esses conteúdos.

---

<div align="center">

Feito utilizando Kotlin + Jetpack Compose

</div>
