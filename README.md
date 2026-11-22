# Controle de gastos pessoais família Lopes

App Android (Kotlin + Jetpack Compose) para o casal controlar o orçamento do ciclo.
Cada um usa o seu celular; tudo sincroniza **em tempo real pela internet** (Firebase Firestore).
Lançamentos feitos sem internet ficam salvos no aparelho e sobem sozinhos quando a conexão volta.

## O que o app faz

- **Início**: saldo do casal no ciclo, quanto ainda dá para gastar por dia, cartão de cada pessoa (você / esposa) com barra de progresso, gastos por categoria e últimos lançamentos. Indicador "Sincronizado / Enviando / Offline".
- **Novo gasto**: valor (máscara de centavos), quem gastou, categoria, descrição e data.
- **Gastos**: histórico do ciclo agrupado por dia, com filtro por pessoa; toque para editar/excluir.
- **Orçamento**: no início do ciclo, define o valor de cada um (com "copiar do ciclo anterior").
- **Ajustes**: código do casal, dia em que o ciclo começa (ex.: dia do pagamento) e sair da conta.
- Navegação entre ciclos (anterior / próximo), tema claro e escuro automático.

## Como colocar para funcionar (uma vez só, ~10 min)

### 1. Criar o projeto Firebase (gratuito)
1. Acesse <https://console.firebase.google.com> → **Adicionar projeto**.
2. **Build → Authentication → Começar → E-mail/senha → Ativar**.
3. **Build → Firestore Database → Criar banco de dados** (modo produção, região `southamerica-east1`).
4. Na aba **Regras** do Firestore, cole o conteúdo de [`firestore.rules`](firestore.rules) e **Publicar**.
5. **Configurações do projeto → Seus apps → Android** (ícone do robô):
   nome do pacote **`app.casal.financas`** → Registrar → baixar o **`google-services.json`**.
6. Substitua o arquivo `app/google-services.json` deste repositório pelo que você baixou.

### 2. Gerar o APK
- **Sem Android Studio**: no GitHub, em *Settings → Secrets and variables → Actions*, crie o segredo
  `GOOGLE_SERVICES_JSON` com o conteúdo do arquivo. Depois vá em *Actions → Gerar APK → Run workflow*
  e baixe `financas-casal-apk` (dentro está o `app-debug.apk`).
- **Com Android Studio** (ou terminal): `./gradlew assembleDebug` →
  `app/build/outputs/apk/debug/app-debug.apk`.

### 3. Instalar nos dois celulares
Envie o `.apk` para os dois aparelhos (WhatsApp, Drive, cabo) e abra. O Android pedirá para permitir
"instalar apps desconhecidos".

### 4. Primeiro uso
1. **Você**: Criar conta → "Criar espaço do casal" → aparece um **código de 6 letras** (também em *Ajustes*).
2. **Sua esposa**: instala, Criar conta → "Já recebi um código" → digita o código.
3. Em **Orçamento**, definam o valor de cada um para o ciclo e comecem a lançar os gastos.

## Estrutura do código

```
app/src/main/java/app/casal/financas/
├── MainActivity.kt          # escolhe a tela conforme a sessão (login → pareamento → app)
├── domain/                  # regras puras, testadas: Models, Cycles (datas do ciclo), Money, Summary, Category
├── data/                    # FinanceRepository (contrato) + FirebaseFinanceRepository (Auth + Firestore)
└── ui/
    ├── SessionViewModel     # login, pareamento do casal
    ├── FinanceViewModel     # ciclo selecionado, gastos e orçamento em tempo real
    ├── AppRoot.kt           # barra inferior + navegação
    ├── theme/  components/  # visual e peças reutilizáveis
    └── auth/ home/ expense/ budget/ settings/   # telas
```

Dados no Firestore: `households/{id}` (casal) → `cycles/{yyyy-MM}` (orçamento) e `expenses/{id}` (gastos).
Valores são guardados em **centavos** (`Long`). Testes: `./gradlew testDebugUnitTest`.

## Segurança
- Cada casal só enxerga os próprios dados (regras em `firestore.rules`); cada casal tem no máximo 2 membros.
- O código de convite dá acesso ao casal enquanto houver vaga. Depois que sua esposa entrar, ninguém mais consegue usá-lo.
  Não publique o código.
