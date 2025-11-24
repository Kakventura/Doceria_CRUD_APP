package com.example.doceria

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

// 1. MODELO DE DADOS: data class Doce
data class Doce(
    var id: String = "", // ID do documento no Firestore
    val nome: String = "",
    val tipo: String = "",
    val quantidade: Int = 0,
    val valor: Double = 0.0,
    val uid_cadastro: String = "" // Quem cadastrou
)

class DoceriaRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val docesCollection = db.collection("doces")

    // Verifica se já existe um usuário logado
    fun isUsuarioLogado(): Boolean = auth.currentUser != null

    // 1. FUNÇÃO DE LOGIN
    fun fazerLogin(email: String, senha: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        auth.signInWithEmailAndPassword(email, senha)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e.message ?: "Erro desconhecido ao logar.") }
    }

    // 2. FUNÇÃO DE CADASTRO
    fun cadastrarUsuario(email: String, senha: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        auth.createUserWithEmailAndPassword(email, senha)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid
                if (uid != null) {
                    val usuarioData = hashMapOf("email" to email)
                    db.collection("usuarios").document(uid).set(usuarioData)
                        .addOnSuccessListener { onSuccess() }
                        .addOnFailureListener { e -> onFailure("Erro ao salvar perfil: ${e.message}") }
                }
            }
            .addOnFailureListener { e -> onFailure(e.message ?: "Falha no cadastro.") }
    }

    // --- 3. FUNÇÃO DE LEITURA (READ) EM TEMPO REAL ---
    fun lerDoces(onListaAtualizada: (List<Doce>) -> Unit) {
        val uidLogado = auth.currentUser?.uid ?: return

        docesCollection
            .whereEqualTo("uid_cadastro", uidLogado)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    println("Erro ao escutar doces: $e")
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    val listaDeDoces = snapshots.documents.mapNotNull { document ->
                        // Mapeia o documento para o objeto Doce, incluindo o ID
                        document.toObject(Doce::class.java)?.copy(id = document.id)
                    }
                    onListaAtualizada(listaDeDoces)
                }
            }
    }

    // --- 4. FUNÇÃO DE CRIAÇÃO (CREATE) ---
    fun salvarNovoDoce(doce: Doce, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val uidLogado = auth.currentUser?.uid

        if (uidLogado == null) {
            onFailure("ERRO: Usuário não está logado.")
            return
        }

        val doceData = hashMapOf(
            "nome" to doce.nome,
            "tipo" to doce.tipo,
            "quantidade" to doce.quantidade,
            "valor" to doce.valor,
            "uid_cadastro" to uidLogado
        )

        docesCollection.add(doceData)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e.message ?: "Erro desconhecido ao salvar doce.") }
    }

    // --- 5. FUNÇÃO DE EXCLUSÃO (DELETE) ---
    fun excluirDoce(doceId: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        if (doceId.isEmpty()) {
            onFailure("ID do doce inválido para exclusão.")
            return
        }

        docesCollection.document(doceId).delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e.message ?: "Erro desconhecido ao excluir doce.") }
    }

    // --- 6. FUNÇÃO DE ATUALIZAÇÃO (UPDATE) ---
    fun atualizarDoce(doce: Doce, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        if (doce.id.isEmpty()) {
            onFailure("ID do doce inválido para atualização.")
            return
        }

        // Dados que podem ser alterados
        val doceData = hashMapOf(
            "nome" to doce.nome,
            "tipo" to doce.tipo,
            "quantidade" to doce.quantidade,
            "valor" to doce.valor
        )

        // Usa o método .update() para atualizar campos específicos do documento
        docesCollection.document(doce.id).update(doceData as Map<String, Any>)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e.message ?: "Erro desconhecido ao atualizar doce.") }
    }
}