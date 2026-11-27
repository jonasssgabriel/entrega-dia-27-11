package com.mycompany.servidorcloudforge;

import com.mycompany.cloudforge.CloudForgeServiceGrpc;
import com.mycompany.cloudforge.Estado;
import com.mycompany.cloudforge.TarefaRequest;
import com.mycompany.cloudforge.TarefaResponse;
import com.mycompany.cloudforge.TipoTarefa;
import com.mycompany.cloudforge.Vazio;
import io.grpc.Context;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;

/**
 * Implementação do serviço CloudForge: recebe uma tarefa, processa e devolve o resultado.
 * Cada instância (processo) tem o seu próprio contador e o seu próprio histórico.
 */
public class CloudForgeServiceImpl extends CloudForgeServiceGrpc.CloudForgeServiceImplBase {

    // nome desta instância, por exemplo srv-50051
    private final String instancia;

    // Estado compartilhado: todas as threads desta instância usam o mesmo contador e a mesma lista
    private int proximoNumero = 1;
    private final List<String> historico = new ArrayList<>();

    // atraso artificial para testar o deadline (volatile: todas as threads enxergam o valor novo)
    private volatile long atrasoMs = 0;

    public CloudForgeServiceImpl(String instancia) {
        this.instancia = instancia;
    }

    public void setAtrasoMs(long atrasoMs) {
        this.atrasoMs = atrasoMs;
    }

    // synchronized: duas tarefas nunca recebem o mesmo número (evita condição de corrida)
    private synchronized int registrarTarefa(String descricao) {
        int numero = proximoNumero++;
        historico.add("#" + numero + " " + descricao);
        return numero;
    }

    @Override
    public void executarTarefa(TarefaRequest req, StreamObserver<TarefaResponse> observador) {
        // espera o atraso configurado no console (0 = sem atraso)
        try {
            Thread.sleep(atrasoMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // se o cliente já desistiu (prazo esgotado), não processa nem gasta um número
        if (Context.current().isCancelled()) {
            System.out.println("[" + instancia + "] cliente desistiu, tarefa descartada");
            return;
        }

        String resultado;
        try {
            // o processamento fica FORA do synchronized: várias tarefas podem calcular ao mesmo tempo
            resultado = processar(req.getTipo(), req.getEntrada().trim());
        } catch (IllegalArgumentException e) {
            // entrada errada: devolve o erro para o cliente
            observador.onError(Status.INVALID_ARGUMENT
                    .withDescription("Entrada inválida: " + req.getEntrada())
                    .asRuntimeException());
            return;
        }

        // aqui acontece o acesso ao recurso compartilhado
        int numero = registrarTarefa(req.getTipo() + " " + req.getEntrada());
        System.out.println("[" + instancia + "] tarefa #" + numero + " " + req.getTipo());

        TarefaResponse resposta = TarefaResponse.newBuilder()
                .setIdTarefa(numero)
                .setInstancia(instancia)
                .setResultado(resultado)
                .build();
        observador.onNext(resposta);   // envia a resposta ao cliente
        observador.onCompleted();      // encerra a chamada
    }

    // devolve o estado desta instância (também synchronized, porque lê a lista compartilhada)
    @Override
    public synchronized void obterEstado(Vazio req, StreamObserver<Estado> observador) {
        // manda só as 5 últimas tarefas
        int inicio = Math.max(0, historico.size() - 5);
        Estado estado = Estado.newBuilder()
                .setInstancia(instancia)
                .setTotalProcessado(historico.size())
                .addAllHistorico(historico.subList(inicio, historico.size()))
                .build();
        observador.onNext(estado);
        observador.onCompleted();
    }

    // funcionalidade principal: escolhe o que fazer pelo tipo da tarefa
    private String processar(TipoTarefa tipo, String entrada) {
        switch (tipo) {
            case CALCULO:
                return fatorial(entrada);
            case PROCESSAMENTO_DADOS:
                return somaEMedia(entrada);
            case HASH:
                return sha256(entrada);
            default:
                throw new IllegalArgumentException("tipo desconhecido");
        }
    }

    // fatorial de um número de 0 a 1000
    private String fatorial(String entrada) {
        int n = Integer.parseInt(entrada);   // texto que não é número gera erro
        if (n < 0 || n > 1000) {
            throw new IllegalArgumentException("fora do limite");
        }
        BigInteger total = BigInteger.ONE;   // BigInteger aguenta números enormes
        for (int i = 2; i <= n; i++) {
            total = total.multiply(BigInteger.valueOf(i));
        }
        return n + "! = " + total;
    }

    // soma e média de uma lista separada por vírgula, por exemplo 1,2,3
    private String somaEMedia(String entrada) {
        String[] partes = entrada.split(",");
        double soma = 0;
        for (String parte : partes) {
            soma += Double.parseDouble(parte.trim());
        }
        return "soma = " + soma + " | média = " + (soma / partes.length);
    }

    // resumo SHA-256 do texto, escrito em hexadecimal
    private String sha256(String entrada) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(entrada.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
