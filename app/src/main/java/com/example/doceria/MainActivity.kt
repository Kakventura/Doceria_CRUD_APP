package com.example.doceria

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.doceria.ui.theme.DoceriaTheme
import java.text.NumberFormat
import java.util.Locale
import androidx.compose.ui.res.colorResource // ESSENCIAL PARA LER AS CORES
import com.example.doceria.R // Para acessar R.color.roxo, R.color.verde_agua, etc.

// --- IMPORTS CRUCIAIS PARA OS CAMPOS DE TEXTO ---
import androidx.compose.ui.text.input.* // Importa ImeAction, KeyboardType, PasswordVisualTransformation


// Enum para controlar qual tela está visível (Navegação simples)
enum class TelaAtual {
    LOGIN,
    CADASTRO,
    PRINCIPAL
}

class MainActivity : ComponentActivity() {

    // Instancia o Repositório aqui
    private val repository = DoceriaRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DoceriaTheme() {
                // Estado para controlar qual tela mostrar
                var telaAtual by remember {
                    mutableStateOf(if (repository.isUsuarioLogado()) TelaAtual.PRINCIPAL else TelaAtual.LOGIN)
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(modifier = Modifier.padding(innerPadding)) {

                        when (telaAtual) {
                            TelaAtual.LOGIN -> TelaLogin(
                                onLoginSucesso = { telaAtual = TelaAtual.PRINCIPAL },
                                onIrParaCadastro = { telaAtual = TelaAtual.CADASTRO },
                                repository = repository
                            )
                            TelaAtual.CADASTRO -> TelaCadastro(
                                onCadastroSucesso = { telaAtual = TelaAtual.PRINCIPAL },
                                onVoltar = { telaAtual = TelaAtual.LOGIN },
                                repository = repository
                            )
                            TelaAtual.PRINCIPAL -> TelaPrincipal(
                                onLogout = { telaAtual = TelaAtual.LOGIN },
                                repository = repository
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- FUNÇÃO DE TELA DE LOGIN ---
@Composable
fun TelaLogin(
    onLoginSucesso: () -> Unit,
    onIrParaCadastro: () -> Unit,
    repository: DoceriaRepository
) {
    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var erro by remember { mutableStateOf("") }

    // --- CARREGAMENTO MANUAL DAS CORES ---
    val corRoxo = colorResource(id = R.color.roxo)
    val corVerdeAgua = colorResource(id = R.color.verde_agua)
    val corBranco = colorResource(id = R.color.branco)
    // NOVO: Carregamos a cor azul escuro
    val corAzulEscuro = colorResource(id = R.color.azul_escuro)


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 64.dp)
            .imePadding(),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ... (código do topo e campos de texto permanece o mesmo)
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            Icon(
                Icons.Filled.Favorite,
                contentDescription = "Logo Doceria",
                tint = corRoxo,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Bem-vindo à Doceria!",
                style = MaterialTheme.typography.headlineMedium,
                color = corRoxo
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = corRoxo,
                    cursorColor = corRoxo
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = senha,
                onValueChange = { senha = it },
                label = { Text("Senha") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = corRoxo,
                    cursorColor = corRoxo
                ),
                modifier = Modifier.fillMaxWidth()
            )

            if (erro.isNotEmpty()) {
                Text(erro, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
            }
        }

        // --- 3. Base: Botões ---
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {

            // Botão Principal (Entrar)
            Button(
                onClick = {
                    erro = ""
                    repository.fazerLogin(email, senha, onLoginSucesso) { msgErro -> erro = msgErro }
                },
                colors = ButtonDefaults.buttonColors(containerColor = corRoxo),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Entrar", modifier = Modifier.padding(vertical = 4.dp), color = corBranco)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botão Secundário (Cadastre-se)
            TextButton(onClick = onIrParaCadastro) {
                Text(
                    "Ainda não tem conta? Cadastre-se",
                    color = corAzulEscuro, // <--- AZUL ESCURO APLICADO
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// --- FUNÇÃO DE TELA DE CADASTRO ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaCadastro(
    onCadastroSucesso: () -> Unit,
    onVoltar: () -> Unit,
    repository: DoceriaRepository
) {
    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var erro by remember { mutableStateOf("") }
    var carregando by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Novo Cadastro") },
                navigationIcon = {
                    IconButton(onClick = onVoltar) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            Text("Crie sua conta na Doceria", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                enabled = !carregando
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = senha,
                onValueChange = { senha = it },
                label = { Text("Senha (mínimo 6 caracteres)") },
                visualTransformation = PasswordVisualTransformation(),
                enabled = !carregando
            )

            if (erro.isNotEmpty()) {
                Text(erro, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (carregando) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = {
                        erro = ""
                        carregando = true
                        repository.cadastrarUsuario(email, senha,
                            onSuccess = { carregando = false; onCadastroSucesso() },
                            onFailure = { msgErro -> carregando = false; erro = msgErro }
                        )
                    },
                    enabled = senha.length >= 6 && email.isNotEmpty()
                ) {
                    Text("Cadastrar")
                }
            }
        }
    }
}

// --- TELA PRINCIPAL (CRUD - READ/LIST & DELETE) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaPrincipal(onLogout: () -> Unit, repository: DoceriaRepository) {

    var listaDoces by remember { mutableStateOf<List<Doce>>(emptyList()) }
    var mostrandoDialogAdicionar by remember { mutableStateOf(false) }
    var doceSelecionado by remember { mutableStateOf<Doce?>(null) }
    var doceParaExcluir by remember { mutableStateOf<Doce?>(null) } // Estado para Delete

    // CARREGAR DADOS EM TEMPO REAL
    LaunchedEffect(Unit) {
        repository.lerDoces { novaLista ->
            listaDoces = novaLista
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Menu de Doces (${listaDoces.size} itens)") },
                actions = {
                    IconButton(onClick = onLogout) {
                        Text("Sair")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                doceSelecionado = null
                mostrandoDialogAdicionar = true
            }) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar Doce")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 8.dp),
            contentPadding = PaddingValues(top = 8.dp)
        ) {
            if (listaDoces.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("Nenhum doce cadastrado ainda.", style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
            items(listaDoces, key = { it.id }) { doce ->
                DoceCardItem(
                    doce = doce,
                    onEditar = {
                        doceSelecionado = doce
                        mostrandoDialogAdicionar = true
                    },
                    onExcluir = {
                        doceParaExcluir = doce // Prepara o diálogo de exclusão
                    }
                )
            }
        }
    }

    // CHAMADA CONDICIONAL DO DIALOG DE CRIAÇÃO/EDIÇÃO
    if (mostrandoDialogAdicionar) {
        DialogFormularioDoce(
            onDismiss = { mostrandoDialogAdicionar = false; doceSelecionado = null },
            repository = repository,
            doceParaEditar = doceSelecionado
        )
    }

    // CHAMADA CONDICIONAL DO DIALOG DE EXCLUSÃO (DELETE)
    if (doceParaExcluir != null) {
        DialogConfirmacaoExclusao(
            doce = doceParaExcluir!!,
            repository = repository,
            onDismiss = { doceParaExcluir = null }
        )
    }
}

// --- ITEM VISUAL DE CADA DOCE ---
fun formatarPreco(valor: Double): String {
    return NumberFormat.getCurrencyInstance(Locale("pt", "BR")).format(valor)
}

@Composable
fun DoceCardItem(doce: Doce, onEditar: () -> Unit, onExcluir: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Informações do Doce
            Column(modifier = Modifier.weight(1f)) {
                Text(doce.nome, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Text("Tipo: ${doce.tipo}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Qtd: ${doce.quantidade}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(modifier = Modifier.width(16.dp))

            // 2. Preço e Ações
            Column(horizontalAlignment = Alignment.End) {
                // Preço Formatado
                Text(
                    formatarPreco(doce.valor),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Row {
                    // Botão de Editar
                    IconButton(onClick = onEditar) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.secondary)
                    }
                    // Botão de Excluir
                    IconButton(onClick = onExcluir) {
                        Icon(Icons.Default.Delete, contentDescription = "Excluir", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

// --- DIALOGO DE FORMULÁRIO (CREATE/UPDATE) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogFormularioDoce(
    onDismiss: () -> Unit,
    repository: DoceriaRepository,
    doceParaEditar: Doce? = null
) {
    val titulo = if (doceParaEditar == null) "Adicionar Novo Doce" else "Editar Doce"

    // Estados com inicialização condicional para edição
    var nome by remember { mutableStateOf(doceParaEditar?.nome ?: "") }
    var tipo by remember { mutableStateOf(doceParaEditar?.tipo ?: "") }
    var quantidadeText by remember { mutableStateOf(doceParaEditar?.quantidade?.toString() ?: "") }
    var valorText by remember { mutableStateOf(doceParaEditar?.valor?.toString() ?: "") }

    var erro by remember { mutableStateOf("") }
    var carregando by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(titulo) },
        text = {
            // Use o Modifier.imePadding() para garantir que o teclado não cubra os campos
            Column(modifier = Modifier.padding(top = 8.dp).imePadding()) {

                // Campo Nome
                OutlinedTextField(
                    value = nome, onValueChange = { nome = it }, label = { Text("Nome do Doce") },
                    modifier = Modifier.fillMaxWidth(), enabled = !carregando
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Campo Tipo
                OutlinedTextField(
                    value = tipo, onValueChange = { tipo = it }, label = { Text("Tipo (Ex: Bolo, Torta)") },
                    modifier = Modifier.fillMaxWidth(), enabled = !carregando
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Campo Quantidade
                OutlinedTextField(
                    value = quantidadeText,
                    onValueChange = { quantidadeText = it.filter { it.isDigit() } },
                    label = { Text("Quantidade") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.fillMaxWidth(), enabled = !carregando
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Campo Valor/Preço
                OutlinedTextField(
                    value = valorText,
                    onValueChange = { valorText = it.replace(",", ".") },
                    label = { Text("Valor (R$)") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal, // CORRIGIDO
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier.fillMaxWidth(), enabled = !carregando
                )

                if (erro.isNotEmpty()) {
                    Text(erro, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val valor = valorText.toDoubleOrNull()
                    val quantidade = quantidadeText.toIntOrNull()

                    if (nome.isEmpty() || tipo.isEmpty() || valor == null || valor <= 0 || quantidade == null || quantidade <= 0) {
                        erro = "Preencha todos os campos corretamente (Nome, Tipo, Quantidade e Valor)."
                        return@Button
                    }

                    // Objeto Doce com os dados atualizados
                    val doceAEnviar = doceParaEditar?.copy(
                        nome = nome, tipo = tipo, quantidade = quantidade, valor = valor
                    ) ?: Doce(
                        nome = nome, tipo = tipo, quantidade = quantidade, valor = valor
                    )

                    carregando = true

                    // --- CHAMA UPDATE OU CREATE ---
                    if (doceParaEditar != null) {
                        // UPDATE
                        repository.atualizarDoce(doceAEnviar,
                            onSuccess = { carregando = false; onDismiss() },
                            onFailure = { msgErro -> carregando = false; erro = msgErro }
                        )
                    } else {
                        // CREATE
                        repository.salvarNovoDoce(doceAEnviar,
                            onSuccess = { carregando = false; onDismiss() },
                            onFailure = { msgErro -> carregando = false; erro = msgErro }
                        )
                    }
                },
                enabled = nome.isNotEmpty() && tipo.isNotEmpty() && valorText.isNotEmpty() && quantidadeText.isNotEmpty() && !carregando
            ) {
                if (carregando) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text(if (doceParaEditar == null) "Salvar Doce" else "Atualizar")
                }
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

// --- DIALOGO DE CONFIRMAÇÃO DE EXCLUSÃO (DELETE) ---
@Composable
fun DialogConfirmacaoExclusao(
    doce: Doce,
    repository: DoceriaRepository,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirmar Exclusão") },
        text = { Text("Tem certeza que deseja excluir '${doce.nome}'? Esta ação é irreversível.") },
        confirmButton = {
            Button(
                onClick = {
                    repository.excluirDoce(
                        doceId = doce.id,
                        onSuccess = onDismiss, // Fecha após sucesso
                        onFailure = { /* Em um app real, aqui mostraria um Toast */ }
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Excluir")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}