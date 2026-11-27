# CloudForge: entrega do dia 27/11

Trabalho de Sistemas Paralelos e Distribuídos com **Java e gRPC**: um serviço de nomes, duas instâncias do mesmo servidor e um cliente com tela (JFrame Form).

```
ServidorCloudForge ── 1. registra ──> ServicoNomes <── 2. consulta ── ClienteCloudForge
 (50051 e 50052)                        (50050)                             │
        ^                                                                   │
        └──────────────── 3. envia a tarefa para o servidor escolhido ──────┘
```

| Projeto | Papel |
|---|---|
| `ServicoNomes` | Guarda onde estão os servidores (porta 50050) |
| `ServidorCloudForge` | Executa as tarefas e guarda o próprio contador e histórico |
| `ClienteCloudForge` | Tela para escolher o servidor e enviar tarefas |

A explicação da interface e do código está em [`Explicacao-CloudForge.pdf`](Explicacao-CloudForge.pdf).

## Pré-requisitos

- **Apache NetBeans** com **JDK 26** (o `pom.xml` usa `maven.compiler.release` 26).
- Internet na primeira compilação, para o Maven baixar as bibliotecas.
- Portas **50050**, **50051** e **50052** livres.

## Passo a passo no NetBeans

### 1. Baixar o projeto

Com Git:

```bash
git clone https://github.com/jonasssgabriel/entrega-dia-27-11.git
```

Sem Git: no GitHub, clique em **Code > Download ZIP** e extraia a pasta.

### 2. Abrir os três projetos

**File > Open Project** e abra `ServicoNomes`, `ServidorCloudForge` e `ClienteCloudForge`.

### 3. Compilar

Em **cada um** dos três: botão direito no projeto > **Clean and Build**.
Espere aparecer `BUILD SUCCESS`. É nesse passo que as classes do `.proto` são geradas; antes dele o NetBeans mostra erros em vermelho.

### 4. Executar, nesta ordem

| Ordem | Onde clicar | O que deve aparecer na saída |
|---|---|---|
| 1 | `ServicoNomes`: botão direito no projeto > **Run** | `[NOMES] servico de nomes no ar na porta 50050` |
| 2 | `Servidor50051.java`: botão direito no arquivo > **Run File** | `[srv-50051] registrado no servico de nomes` |
| 3 | `Servidor50052.java`: botão direito no arquivo > **Run File** | `[srv-50052] registrado no servico de nomes` |
| 4 | `ClienteCloudForge`: botão direito no projeto > **Run** | abre a tela, com 2 servidores na lista |

Os arquivos `Servidor50051.java` e `Servidor50052.java` ficam em `ServidorCloudForge > Source Packages > com.mycompany.servidorcloudforge`.

### 5. Usar a tela

| Botão | O que faz |
|---|---|
| **Atualizar lista** | consulta o serviço de nomes de novo |
| **Enviar** | manda uma tarefa para o servidor escolhido |
| **Enviar 20 juntas** | manda 20 tarefas ao mesmo tempo (concorrência) |
| **Ver estado** | mostra o total e as 5 últimas tarefas do servidor |
| **Limpar** | apaga a área de respostas |

| Tipo | Exemplo de entrada | Resposta |
|---|---|---|
| `CALCULO` | `5` (de 0 a 1000) | `5! = 120` |
| `PROCESSAMENTO_DADOS` | `1,2,3` | `soma = 6.0 \| média = 2.0` |
| `HASH` | qualquer texto | SHA-256 do texto |

### 6. Testar as falhas

| Teste | Como fazer | Resultado esperado |
|---|---|---|
| Entrada inválida | `CALCULO` com a entrada `abc` | `INVALID_ARGUMENT` |
| Deadline | na saída de um servidor, digite `atraso 5000` e tecle Enter; envie uma tarefa para ele; depois digite `atraso 0` | `DEADLINE_EXCEEDED` |
| Servidor desligado | pare um servidor no quadrado vermelho e envie para ele | `UNAVAILABLE`; o outro continua respondendo |

### 7. Encerrar

Feche a tela do cliente e clique no quadrado vermelho de cada aba da saída (servidores e serviço de nomes).

## Problemas comuns

| Problema | Solução |
|---|---|
| Erros em vermelho no código | faça **Clean and Build** no projeto |
| `ERRO: ligue o servico de nomes primeiro` | execute o `ServicoNomes` antes dos servidores |
| `Address already in use` | já existe um programa rodando nessa porta; pare a execução anterior |
| Lista vazia na tela | suba os servidores e clique em **Atualizar lista** |
| Servidor parado continua na lista | é uma limitação desta versão; o envio para ele mostra `UNAVAILABLE` |
| Reiniciei o serviço de nomes | reinicie também os servidores, para registrarem de novo |

## O que mostrar na apresentação

| Item pedido | Como mostrar |
|---|---|
| Registro no serviço de nomes | a saída do `ServicoNomes` mostra as duas instâncias |
| Cliente descobre os servidores | a lista da tela vem do serviço de nomes |
| Chamar as duas instâncias | escolher na lista e clicar **Enviar** |
| Qual instância respondeu | início de cada linha da resposta |
| Estado próprio | enviar para cada uma e clicar **Ver estado** |
| Concorrência | **Enviar 20 juntas**: nenhum número repetido ou pulado |
| Deadline e falha | testes do passo 6 |

## Recurso compartilhado

Em cada instância, o contador de tarefas e a lista de histórico são protegidos pelo método `synchronized registrarTarefa`, em `CloudForgeServiceImpl`. O processamento fica fora do bloqueio.

## Situação do projeto

**Pronto (mínimo de 27/11):** serviço de nomes, duas instâncias, cliente com escolha manual, três tipos de tarefa, controle de concorrência, estado próprio, deadline e tratamento de falhas.

**Falta (04/12):** round-robin automático, Health Check antes de cada envio, `NOT_SERVING` e volta, tratamento automático de queda, aviso de nenhum servidor saudável e testes de concorrência completos.
