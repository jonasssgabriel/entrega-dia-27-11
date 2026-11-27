package com.mycompany.servidorcloudforge;

import com.mycompany.cloudforge.Instancia;
import com.mycompany.cloudforge.NomesServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.StatusRuntimeException;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

/**
 * Sobe uma instância do servidor e registra no serviço de nomes.
 */
public class ServidorCloudForge {

    public static void iniciar(int porta) throws Exception {
        String id = "srv-" + porta;

        // 1) sobe o servidor gRPC na porta informada
        CloudForgeServiceImpl servico = new CloudForgeServiceImpl(id);
        Server servidor = ServerBuilder.forPort(porta)
                .addService(servico)
                .build()
                .start();
        System.out.println("[" + id + "] servidor no ar na porta " + porta);

        // 2) avisa o serviço de nomes onde esta instância está
        ManagedChannel canalNomes = ManagedChannelBuilder.forAddress("localhost", 50050)
                .usePlaintext()   // sem TLS: só no laboratório
                .build();
        Instancia eu = Instancia.newBuilder()
                .setId(id).setHost("localhost").setPorta(porta).build();
        try {
            NomesServiceGrpc.newBlockingStub(canalNomes)
                    .withDeadlineAfter(3, TimeUnit.SECONDS)
                    .registrar(eu);
            System.out.println("[" + id + "] registrado no servico de nomes");
        } catch (StatusRuntimeException e) {
            System.out.println("[" + id + "] ERRO: ligue o servico de nomes primeiro. "
                    + e.getStatus().getCode());
            servidor.shutdownNow();
            return;
        } finally {
            canalNomes.shutdown();
        }

        // 3) lê comandos digitados no console, para testar o deadline
        System.out.println("Digite: atraso 5000   (para voltar ao normal: atraso 0)");
        Scanner teclado = new Scanner(System.in);
        while (teclado.hasNextLine()) {
            String[] comando = teclado.nextLine().trim().split(" ");
            if (comando.length == 2 && comando[0].equals("atraso") && comando[1].matches("\\d{1,6}")) {
                servico.setAtrasoMs(Long.parseLong(comando[1]));
                System.out.println("[" + id + "] atraso = " + comando[1] + " ms");
            } else {
                System.out.println("comando invalido");
            }
        }
        servidor.awaitTermination();
    }
}
