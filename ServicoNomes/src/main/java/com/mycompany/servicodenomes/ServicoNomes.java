package com.mycompany.servicodenomes;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import java.io.IOException;

/**
 * Ponto de partida: ligue este projeto antes dos servidores e do cliente.
 */
public class ServicoNomes {

    public static void main(String[] args) throws IOException, InterruptedException {
        // 50050 é a única porta que o cliente e os servidores precisam conhecer
        Server servidor = ServerBuilder.forPort(50050)
                .addService(new NomesServiceImpl())
                .build()
                .start();
        System.out.println("[NOMES] servico de nomes no ar na porta 50050");
        servidor.awaitTermination();
    }
}
