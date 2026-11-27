package com.mycompany.servicodenomes;

import com.mycompany.cloudforge.Instancia;
import com.mycompany.cloudforge.ListaInstancias;
import com.mycompany.cloudforge.NomesServiceGrpc;
import com.mycompany.cloudforge.Resposta;
import com.mycompany.cloudforge.Vazio;
import io.grpc.stub.StreamObserver;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Guarda onde está cada servidor: eles se registram aqui e o cliente consulta a lista.
 */
public class NomesServiceImpl extends NomesServiceGrpc.NomesServiceImplBase {

    // lista compartilhada entre as threads: todo acesso é synchronized
    private final Map<String, Instancia> instancias = new LinkedHashMap<>();

    // chamado por um servidor quando ele sobe
    @Override
    public synchronized void registrar(Instancia req, StreamObserver<Resposta> resposta) {
        instancias.put(req.getId(), req);
        System.out.println("[NOMES] registrou " + req.getId() + " em " + req.getHost() + ":" + req.getPorta());
        responder(resposta, "registrado");
    }

    @Override
    public synchronized void desregistrar(Instancia req, StreamObserver<Resposta> resposta) {
        instancias.remove(req.getId());
        System.out.println("[NOMES] removeu " + req.getId());
        responder(resposta, "removido");
    }

    // chamado pelo cliente para descobrir os servidores
    @Override
    public synchronized void listar(Vazio req, StreamObserver<ListaInstancias> resposta) {
        System.out.println("[NOMES] cliente pediu a lista (" + instancias.size() + " instancia(s))");
        resposta.onNext(ListaInstancias.newBuilder().addAllInstancias(instancias.values()).build());
        resposta.onCompleted();
    }

    private void responder(StreamObserver<Resposta> resposta, String mensagem) {
        resposta.onNext(Resposta.newBuilder().setOk(true).setMensagem(mensagem).build());
        resposta.onCompleted();
    }
}
