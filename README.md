# 🎮 Esports Hub - Cliente Android

<div align="center">

### O seu portal para acompanhar partidas de CS2 e VALORANT em tempo real.

[![Licença](https://img.shields.io/github/license/FilipeLacerda738/EsportsNewsAppAndroid?style=for-the-badge\&logo=gnu\&color=2B3137\&labelColor=161B22)](LICENSE)
[![Android](https://img.shields.io/badge/Android-8.0%2B-3DDC84?style=for-the-badge\&logo=android\&logoColor=white\&labelColor=161B22)](https://www.android.com/)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9%2B-7F52FF?style=for-the-badge\&logo=kotlin\&logoColor=white\&labelColor=161B22)](https://kotlinlang.org/)

</div>

---

> 🚀 **O backend também é Open Source!**
> Este aplicativo consome uma API dedicada construída em **Python (FastAPI)** e **PostgreSQL**.
>
> 🔗 [Repositório da API do Esports Hub](https://github.com/FilipeLacerda738/esports-pro-api.git)

---

# 📚 Tabela de Conteúdos

* [📱 Visão Geral](#-visão-geral)
* [🛠 Stack Tecnológico](#-stack-tecnológico)
* [✨ Características](#-características)
* [⚙️ Instalação](#️-instalação)
* [🌐 Arquitetura de Rede](#-arquitetura-de-rede)
* [🤝 Contribuições](#-contribuições)
* [📄 Licença](#-licença)

---

# 📱 Visão Geral

O **Esports Hub** é um aplicativo Android nativo desenvolvido para entregar:

* Resultados ao vivo
* Calendários de partidas
* Status em tempo real
* Filtros por jogo
* Atualizações automáticas

Tudo isso utilizando o ecossistema moderno Android com:

* Jetpack Compose
* Coroutines
* StateFlow
* MVVM
* Material Design 3

O projeto serve tanto como aplicativo funcional quanto como base de estudos para arquitetura Android moderna.

---

# 🛠 Stack Tecnológico

| Interface         | Arquitetura & Dados | Rede     |
| ----------------- | ------------------- | -------- |
| Jetpack Compose   | MVVM                | Retrofit |
| Material Design 3 | StateFlow           | OkHttp   |
| Coil              | Coroutines          | Gson     |

---

# ✨ Características

## 🔴 Atualizações em Tempo Real

Sistema de polling inteligente para atualização automática de partidas ao vivo.

## 📅 Calendário Dinâmico

Filtragem rápida por dias da semana diretamente na interface.

## 🎮 Filtros por Jogo

Alternância instantânea entre:

* Counter-Strike 2
* VALORANT

## 🎨 UI Moderna

Interface construída totalmente em Compose utilizando Material Design 3.

## 🔄 Pull To Refresh

Atualização manual utilizando gestos nativos do Android.

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
API_KEY="sua_chave_de_acesso_aqui"
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
| Release  | `https://esports-pro-api.onrender.com/` |

---

Todos os requests utilizam autenticação via header:

```http
X-API-Key: SUA_API_KEY
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
