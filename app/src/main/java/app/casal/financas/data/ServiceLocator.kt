package app.casal.financas.data

/** Ponto único de criação das dependências (suficiente para um app deste tamanho, sem framework de DI). */
object ServiceLocator {
    val repository: FinanceRepository by lazy { FirebaseFinanceRepository() }
}
