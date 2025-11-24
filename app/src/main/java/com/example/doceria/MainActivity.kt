package com.example.doceria

import android.R.attr.fontFamily
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.sp


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
                    // MODIFICADO: Força o início na tela de LOGIN
                    mutableStateOf(TelaAtual.LOGIN)
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

            // Substituído o Icone de coração pela imagem 'logo'
            Image(
                painter = painterResource(id = R.drawable.candy_shop_bro__1_), // Altere 'R.drawable.logo' para o nome do seu arquivo de imagem
                contentDescription = "Logo Doceria",
                modifier = Modifier.size(300.dp) // Ajuste o tamanho conforme necessário para sua logo
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Bem-vindo à Doceria!",
                style = MaterialTheme.typography.headlineMedium,
                color = corRoxo,
                fontSize = 40.sp,
                fontFamily = FontFamily.Cursive,
                fontWeight = FontWeight.Bold

            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email",
                    modifier = Modifier.padding(),
                    fontFamily = FontFamily.Cursive,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp)
            },
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
                label = { Text("Senha",
                    modifier = Modifier.padding(),
                    fontFamily = FontFamily.Cursive,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp) },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = corRoxo,
                    cursorColor = corRoxo
                ),
                modifier = Modifier.fillMaxWidth()
            )

            if (erro.isNotEmpty()) {
                Text(erro, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp), fontFamily = FontFamily.Cursive)
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
                Text("Entrar", modifier = Modifier.padding(vertical = 4.dp),
                    color = corBranco,
                    fontFamily = FontFamily.Cursive,
                    fontWeight = FontWeight.Bold,
                    fontSize = 25.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botão Secundário (Cadastre-se)
            TextButton(onClick = onIrParaCadastro) {
                Text(
                    "Ainda não tem conta? Cadastre-se",
                    color = corAzulEscuro, // <--- AZUL ESCURO APLICADO
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Cursive,
                    fontSize = 20.sp
                )
            }
        }
    }
}

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

    // --- CARREGAMENTO MANUAL DAS CORES ---
    val corRoxo = colorResource(id = R.color.roxo)
    val corBranco = colorResource(id = R.color.branco)

    // Configuração de cores para os OutlinedTextFields
    val coresCampo = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = corRoxo,
        cursorColor = corRoxo
    )

    // Removida a função auxiliar styleCadastroLabel incorreta.
    // O estilo será aplicado diretamente ao Text:

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    // Ajuste no Text e no Modifier do Text
                    Text(
                        "Novo Cadastro",
                        fontFamily = FontFamily.Cursive,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp, // Tenta manter 18.sp, mas pode precisar de 16.sp
                        // Adicionar o alinhamento vertical forçado ao título
                        modifier = Modifier.fillMaxHeight().wrapContentHeight(Alignment.CenterVertically)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onVoltar, enabled = !carregando) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = corRoxo)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = corBranco
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 32.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // --- Imagem ---
            Spacer(modifier = Modifier.height(16.dp))
            Image(
                painter = painterResource(id = R.drawable.login_bro__1_), // Usando a imagem 'login'
                contentDescription = "Imagem de Login/Cadastro",
                modifier = Modifier.size(250.dp)
            )

            // --- Título estilizado ---
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Crie sua conta na Doceria",
                style = MaterialTheme.typography.headlineSmall,
                color = corRoxo,
                fontSize = 32.sp,
                fontFamily = FontFamily.Cursive,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(32.dp))

            // --- Campos de Texto ---
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                // CORREÇÃO: Aplicação correta dos estilos diretamente no composable Text do label
                label = {
                    Text(
                        "Email",
                        fontFamily = FontFamily.Cursive,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                },
                enabled = !carregando,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                colors = coresCampo,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = senha,
                onValueChange = { senha = it },
                // CORREÇÃO: Aplicação correta dos estilos diretamente no composable Text do label
                label = {
                    Text(
                        "Senha (mínimo 6 caracteres)",
                        fontFamily = FontFamily.Cursive,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                },
                visualTransformation = PasswordVisualTransformation(),
                enabled = !carregando,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                colors = coresCampo,
                modifier = Modifier.fillMaxWidth()
            )

            // --- Mensagem de Erro ---
            if (erro.isNotEmpty()) {
                Text(
                    erro,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp),
                    fontFamily = FontFamily.Cursive
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- Botão ou Indicador de Carregamento ---
            if (carregando) {
                CircularProgressIndicator(color = corRoxo)
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
                    enabled = senha.length >= 6 && email.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = corRoxo),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Cadastrar",
                        color = corBranco,
                        fontFamily = FontFamily.Cursive,
                        fontWeight = FontWeight.Bold,
                        fontSize = 25.sp
                    )
                }
            }

            // --- Botão Voltar para Login ---
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = onVoltar, enabled = !carregando) {
                Text(
                    "Já tem conta? Fazer Login",
                    color = corRoxo,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Cursive,
                    fontSize = 20.sp
                )
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

    // Carrega a cor roxa para a TopAppBar, se desejar estilizar
    val corRoxo = colorResource(id = R.color.roxo)
    val corBranco = colorResource(id = R.color.branco)
    val corVerde = colorResource(id = R.color.verde_agua)


    Scaffold(
        topBar = {
            TopAppBar(
                // MODIFICAÇÃO AQUI: Substituímos o Text por uma Row com Image e Text
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth() // Garante que a Row ocupe a largura total para centralização
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.cake__1_), // Sua imagem de cupcake
                            contentDescription = "Logo Cupcake",
                            modifier = Modifier.size(36.dp) // Ajuste o tamanho da imagem conforme necessário
                        )
                        Spacer(modifier = Modifier.width(8.dp)) // Espaço entre a imagem e o texto
                        Text(
                            " (${listaDoces.size} itens)",
                            fontFamily = FontFamily.Cursive, // Mantém a fonte cursiva
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp // Ajuste o tamanho da fonte para ficar bom com a imagem
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onLogout) {
                        Text(
                            "Sair",
                            fontFamily = FontFamily.Cursive, // Estilizando o botão "Sair"
                            fontWeight = FontWeight.Bold,
                            color = corRoxo,
                            fontSize = 20.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = corBranco // Fundo da TopAppBar branco
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    doceSelecionado = null
                    mostrandoDialogAdicionar = true
                },
                containerColor = corRoxo // Cor do FAB
            ) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar Doce", tint = corBranco)
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
                        Text(
                            "Nenhum doce cadastrado ainda.",
                            style = MaterialTheme.typography.bodyLarge,
                            fontFamily = FontFamily.Cursive
                        )
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
    // Carrega cores para o CardItem se precisar de estilização consistente
    val corRoxo = colorResource(id = R.color.roxo)
    val corBranco = colorResource(id = R.color.branco)
    val corVerde = colorResource(id = R.color.verde_agua)

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = corBranco) // Fundo do Card branco
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Informações do Doce
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    doce.nome,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    fontFamily = FontFamily.Cursive, // Estilizando texto do item
                    color = corRoxo,
                    fontSize = 20.sp
                )
                Text(
                    "Tipo: ${doce.tipo}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = FontFamily.Cursive,
                    fontSize = 15.sp
                )
                Text(
                    "Qtd: ${doce.quantidade}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = FontFamily.Cursive,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // 2. Preço e Ações
            Column(horizontalAlignment = Alignment.End) {
                // Preço Formatado
                Text(
                    formatarPreco(doce.valor),
                    style = MaterialTheme.typography.titleMedium,
                    color = corRoxo, // Usando a cor roxa para o preço
                    fontFamily = FontFamily.Cursive // Estilizando preço
                )

                Row {
                    // Botão de Editar
                    IconButton(onClick = onEditar) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = corVerde) // Ícone roxo
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

    // Carrega as cores para o Dialog
    val corRoxo = colorResource(id = R.color.roxo)
    val corBranco = colorResource(id = R.color.branco)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                titulo,
                fontFamily = FontFamily.Cursive,
                fontWeight = FontWeight.Bold,
                color = corRoxo,
                fontSize = 35.sp
            )
        },
        text = {
            // Use o Modifier.imePadding() para garantir que o teclado não cubra os campos
            Column(modifier = Modifier.padding(top = 8.dp).imePadding()) {

                // Campo Nome
                OutlinedTextField(
                    value = nome, onValueChange = { nome = it },
                    label = { Text("Nome do Doce", fontFamily = FontFamily.Cursive, fontSize = 20.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !carregando,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = corRoxo,
                        cursorColor = corRoxo
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Campo Tipo
                OutlinedTextField(
                    value = tipo, onValueChange = { tipo = it },
                    label = { Text("Tipo (Ex: Bolo, Torta)", fontFamily = FontFamily.Cursive, fontSize = 20.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !carregando,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = corRoxo,
                        cursorColor = corRoxo
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Campo Quantidade
                OutlinedTextField(
                    value = quantidadeText,
                    onValueChange = { quantidadeText = it.filter { it.isDigit() } },
                    label = { Text("Quantidade", fontFamily = FontFamily.Cursive, fontSize = 20.sp) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !carregando,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = corRoxo,
                        cursorColor = corRoxo
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Campo Valor/Preço
                OutlinedTextField(
                    value = valorText,
                    onValueChange = { valorText = it.replace(",", ".") },
                    label = { Text("Valor (R$)", fontFamily = FontFamily.Cursive, fontSize = 20.sp) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !carregando,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = corRoxo,
                        cursorColor = corRoxo
                    )
                )

                if (erro.isNotEmpty()) {
                    Text(
                        erro,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp),
                        fontFamily = FontFamily.Cursive
                    )
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
                enabled = nome.isNotEmpty() && tipo.isNotEmpty() && valorText.isNotEmpty() && quantidadeText.isNotEmpty() && !carregando,
                colors = ButtonDefaults.buttonColors(containerColor = corRoxo) // Botão com cor roxa
            ) {
                if (carregando) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = corBranco)
                } else {
                    Text(
                        if (doceParaEditar == null) "Salvar Doce" else "Atualizar",
                        fontFamily = FontFamily.Cursive,
                        color = corBranco
                    )
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    "Cancelar",
                    fontFamily = FontFamily.Cursive,
                    color = corRoxo // Botão Cancelar com cor roxa
                )
            }
        },
        containerColor = corBranco // Fundo do AlertDialog branco
    )
}

// --- DIALOGO DE CONFIRMAÇÃO DE EXCLUSÃO (DELETE) ---
@Composable
fun DialogConfirmacaoExclusao(
    doce: Doce,
    repository: DoceriaRepository,
    onDismiss: () -> Unit
) {
    // Carrega as cores para o Dialog
    val corRoxo = colorResource(id = R.color.roxo)
    val corBranco = colorResource(id = R.color.branco)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Confirmar Exclusão",
                fontFamily = FontFamily.Cursive,
                fontWeight = FontWeight.Bold,
                color = corRoxo
            )
        },
        text = {
            Text(
                "Tem certeza que deseja excluir '${doce.nome}'? Esta ação é irreversível.",
                fontFamily = FontFamily.Cursive
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    repository.excluirDoce(
                        doceId = doce.id,
                        onSuccess = onDismiss,
                        onFailure = { /* Em um app real, aqui mostraria um Toast */ }
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text(
                    "Excluir",
                    fontFamily = FontFamily.Cursive,
                    color = corBranco
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    "Cancelar",
                    fontFamily = FontFamily.Cursive,
                    color = corRoxo
                )
            }
        },
        containerColor = corBranco // Fundo do AlertDialog branco
    )
}

