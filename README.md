# 🎮 Esports Hub - Android Client

<div align="center">

🇧🇷 Português | 🇺🇸 [English Version](./README.en.md)

### O seu portal de bolso para acompanhar partidas de eSports em tempo real.

[![Licença](https://img.shields.io/github/license/FilipeLacerda738/esports-pro-api?style=flat-square&logo=gnu&color=2B3137&labelColor=161B22)](LICENSE)
[![Android](https://img.shields.io/badge/Android-8.0%2B-3DDC84?style=for-the-badge&logo=android&logoColor=white&labelColor=161B22)](https://www.android.com/)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9%2B-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white&labelColor=161B22)](https://kotlinlang.org/)

<a href="https://github.com/FilipeLacerda738/EsportsNewsAppAndroid/releases/latest">
    <img src="https://img.shields.io/github/v/release/FilipeLacerda738/EsportsNewsAppAndroid?label=Baixar%20APK&logo=android&style=for-the-badge&color=3DDC84&labelColor=161B22" alt="Baixar APK">
</a>

<br><br>
<img src="assets/Demo_app.gif" width="200" />

---

> 🚀 **Ecossistema Completo (Fullstack)**
> Este aplicativo não consome APIs de terceiros diretamente. Ele é alimentado por um **Backend Próprio em Python (FastAPI)**, construído para proteger o aplicativo contra limites de requisição e entregar dados em milissegundos.
> 🔗 [Conheça a Engenharia do Backend aqui](https://github.com/FilipeLacerda738/esports-pro-api.git)

---

## 🎯 O Desafio e o Produto

O **Esports Hub** nasceu para resolver um problema claro: a maioria dos aplicativos de resultados esportivos são lentos, poluídos e gastam muitos dados móveis. 

Este projeto foi construído utilizando o estado da arte do desenvolvimento Android nativo (**Jetpack Compose, MVVM, Coroutines**) para entregar uma experiência fluida, leve e com foco total na retenção do usuário.

## ⚡ Engenharia e Otimizações de Destaque

* 📉 **App Size (De 90MB para 15MB):** Implementação rigorosa de regras do `R8/ProGuard` e `shrinkResources`. O aplicativo final teve seu tamanho reduzido em **mais de 80%**, otimizando a taxa de conversão sem quebrar a desserialização do Retrofit via anotações `@Keep`.
* 🛡️ **Segurança e Prevenção contra Vazamentos:** Tratamento de erros "cego" na camada do `ViewModel`. Em caso de falha de rede, o aplicativo intercepta exceções do OkHttp/Retrofit para garantir que IPs do servidor ou URLs de API jamais sejam expostos na tela do usuário.
* 🎨 **Design System "Cinza Chumbo":** Transição completa para um tema escuro focado em leitura dinâmica e ergonomia visual, inspirado em Strafe/HLTV.

---

## 🛠 Stack Tecnológico

| Interface Gráfica | Arquitetura & Estado | Conectividade |
| :--- | :--- | :--- |
| **Jetpack Compose** (Material 3) | **MVVM** (Model-View-ViewModel) | **Retrofit 2** + Gson |
| **Coil** (Cache e Imagens Assíncronas) | **StateFlow** (Reatividade) | **OkHttp** (Interceptação) |
| **Componentes Customizados** | **Coroutines** (Concorrência) | **Injeção Dinâmica de URLs** |

---

## 🗺️ Roadmap

O aplicativo está em constante evolução. Aqui estão os próximos passos de engenharia e funcionalidades:

* **[Fase Atual] v1.4.0 (Fluidez e Retenção):** Implementação de Cache Offline (Room Database) para abertura instantânea e Paginação (Infinite Scroll) para economia de dados.
* **[Fase 2] Visão Analítica:** Páginas de detalhes com status de mapas individuais (MD3), acompanhamento de chaves (Brackets) e gráficos de momento.
* **[Fase 3] Monetização e Engajamento:** Integração de *Odds* em tempo real, painel de afiliados para plataformas regulamentadas e sistema de *Pick'ems*.

---


# ⚙️ Instalação

## Pré-requisitos

* Android Studio Iguana+
* JDK 17
* Android SDK 26+

---

## 1. Clone o repositório

```bash
git clone https://github.com/FilipeLacerda738/EsportsNewsAppAndroid.git
cd EsportsNewsAppAndroid
```

## 2. Configure sua API KEY

Crie um arquivo `local.properties` na raiz do projeto:

```properties
API_ACCESS_KEY="sua_chave_de_acesso_aqui"
```

---

## 3. Execute o projeto

* Abra no Android Studio
* Aguarde o Gradle sincronizar
* Clique em **Run 'app'**

---

# 🌐 Arquitetura de Rede

O aplicativo alterna automaticamente entre ambiente local e produção através do `BuildConfig.DEBUG`.

| Ambiente | URL                                     |
| -------- | --------------------------------------- |
| Debug    | `http://10.0.2.2:8000/`                 |
| Release  | `https://seuDeploy.com/` |

---

Todos os requests utilizam autenticação via header:

```http
API_ACCESS_KEY: SUA_API_KEY
```

---

# 🤝 Contribuições

Contribuições são muito bem-vindas.

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

# 🗺 Roadmap

* [ ] Tela de detalhes da partida
* [ ] Integração com WebSockets
* [ ] Cache offline com Room
* [ ] Push notifications
* [ ] Favoritos
* [ ] Tema AMOLED

---

# 📄 Licença

Distribuído sob a licença MIT.

Veja `LICENSE` para mais informações.

---

<div align="center">

Feito com ❤️ utilizando Kotlin + Jetpack Compose

</div>
