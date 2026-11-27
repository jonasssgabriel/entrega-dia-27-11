package com.mycompany.clientecloudforge;

import com.mycompany.cloudforge.CloudForgeServiceGrpc;
import com.mycompany.cloudforge.Estado;
import com.mycompany.cloudforge.Instancia;
import com.mycompany.cloudforge.NomesServiceGrpc;
import com.mycompany.cloudforge.TarefaRequest;
import com.mycompany.cloudforge.TarefaResponse;
import com.mycompany.cloudforge.TipoTarefa;
import com.mycompany.cloudforge.Vazio;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class TelaCliente extends javax.swing.JFrame {

    // prazo (deadline) que o cliente dá para o servidor responder
    private static final long PRAZO_SEGUNDOS = 3;

    // único endereço fixo do cliente: o do serviço de nomes
    private final ManagedChannel canalNomes = ManagedChannelBuilder
            .forAddress("localhost", 50050)
            .usePlaintext()
            .build();

    // servidores descobertos; a posição na lista é a mesma nas três listas
    private final List<Instancia> instancias = new ArrayList<>();
    private final List<ManagedChannel> canais = new ArrayList<>();
    private final List<CloudForgeServiceGrpc.CloudForgeServiceBlockingStub> stubs = new ArrayList<>();

    /**
     * Creates new form TelaCliente
     */
    public TelaCliente() {
        initComponents();
        setLocationRelativeTo(null);   // abre no centro da tela
        atualizarLista();
    }

    // consulta o serviço de nomes e cria um canal e um stub para cada servidor
    private void atualizarLista() {
        try {
            List<Instancia> lista = NomesServiceGrpc.newBlockingStub(canalNomes)
                    .withDeadlineAfter(PRAZO_SEGUNDOS, TimeUnit.SECONDS)
                    .listar(Vazio.getDefaultInstance())
                    .getInstanciasList();

            // fecha os canais antigos antes de montar a lista nova
            for (ManagedChannel canal : canais) {
                canal.shutdown();
            }
            instancias.clear();
            canais.clear();
            stubs.clear();
            cbxServidor.removeAllItems();

            for (Instancia inst : lista) {
                ManagedChannel canal = ManagedChannelBuilder
                        .forAddress(inst.getHost(), inst.getPorta())
                        .usePlaintext()
                        .build();
                instancias.add(inst);
                canais.add(canal);
                stubs.add(CloudForgeServiceGrpc.newBlockingStub(canal));
                cbxServidor.addItem(inst.getId() + " (" + inst.getHost() + ":" + inst.getPorta() + ")");
            }
            log("Serviço de nomes informou " + lista.size() + " servidor(es).");
        } catch (StatusRuntimeException e) {
            log(mensagemDeErro("serviço de nomes", e));
        }
    }

    // devolve a posição do servidor escolhido, ou -1 se a lista estiver vazia
    private int servidorEscolhido() {
        int i = cbxServidor.getSelectedIndex();
        if (i < 0) {
            JOptionPane.showMessageDialog(this, "Nenhum servidor na lista. Suba um e clique em Atualizar lista.");
        }
        return i;
    }

    private TipoTarefa tipoEscolhido() {
        return TipoTarefa.valueOf((String) cbxTipo.getSelectedItem());
    }

    // chama o servidor com deadline e trata a falha
    private void enviarTarefa(int i, TipoTarefa tipo, String entrada) {
        TarefaRequest pedido = TarefaRequest.newBuilder()
                .setTipo(tipo)
                .setEntrada(entrada)
                .build();
        try {
            TarefaResponse r = stubs.get(i)
                    .withDeadlineAfter(PRAZO_SEGUNDOS, TimeUnit.SECONDS)
                    .executarTarefa(pedido);
            log("Respondeu " + r.getInstancia() + " | tarefa #" + r.getIdTarefa() + " | " + r.getResultado());
        } catch (StatusRuntimeException e) {
            // o erro é mostrado e o cliente continua funcionando
            log(mensagemDeErro(instancias.get(i).getId(), e));
        }
    }

    // cada código de erro do gRPC vira uma mensagem explicando o que houve
    private String mensagemDeErro(String quem, StatusRuntimeException e) {
        String explicacao;
        switch (e.getStatus().getCode()) {
            case DEADLINE_EXCEEDED:
                explicacao = "demorou mais que o prazo de " + PRAZO_SEGUNDOS + "s";
                break;
            case UNAVAILABLE:
                explicacao = "está indisponível, escolha outro servidor";
                break;
            case INVALID_ARGUMENT:
                explicacao = e.getStatus().getDescription();
                break;
            default:
                explicacao = "falha inesperada";
        }
        return "FALHA em " + quem + ": " + e.getStatus().getCode() + " - " + explicacao;
    }

    // escreve na tela (invokeLater: pode ser chamado de qualquer thread)
    private void log(String texto) {
        SwingUtilities.invokeLater(() -> {
            taResposta.append(texto + "\n");
            taResposta.setCaretPosition(taResposta.getDocument().getLength());
        });
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        painelTopo = new javax.swing.JPanel();
        painelServidor = new javax.swing.JPanel();
        lblServidor = new javax.swing.JLabel();
        cbxServidor = new javax.swing.JComboBox<>();
        btnAtualizar = new javax.swing.JButton();
        painelTarefa = new javax.swing.JPanel();
        lblTipo = new javax.swing.JLabel();
        cbxTipo = new javax.swing.JComboBox<>();
        lblEntrada = new javax.swing.JLabel();
        tfEntrada = new javax.swing.JTextField();
        painelBotoes = new javax.swing.JPanel();
        btnEnviar = new javax.swing.JButton();
        btnVarias = new javax.swing.JButton();
        btnEstado = new javax.swing.JButton();
        scrollResposta = new javax.swing.JScrollPane();
        taResposta = new javax.swing.JTextArea();
        painelRodape = new javax.swing.JPanel();
        btnLimpar = new javax.swing.JButton();
        lblStatus = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("CloudForge - Cliente");
        painelTopo.setLayout(new java.awt.BorderLayout());
        painelServidor.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        lblServidor.setText("Servidor:");
        painelServidor.add(lblServidor);

        painelServidor.add(cbxServidor);

        btnAtualizar.setText("Atualizar lista");
        btnAtualizar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAtualizarActionPerformed(evt);
            }
        });
        painelServidor.add(btnAtualizar);

        painelTopo.add(painelServidor, java.awt.BorderLayout.NORTH);

        painelTarefa.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        lblTipo.setText("Tipo:");
        painelTarefa.add(lblTipo);

        cbxTipo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "CALCULO", "PROCESSAMENTO_DADOS", "HASH" }));
        painelTarefa.add(cbxTipo);

        lblEntrada.setText("Entrada:");
        painelTarefa.add(lblEntrada);

        tfEntrada.setColumns(10);
        tfEntrada.setText("5");
        painelTarefa.add(tfEntrada);

        painelTopo.add(painelTarefa, java.awt.BorderLayout.CENTER);

        painelBotoes.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        btnEnviar.setText("Enviar");
        btnEnviar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEnviarActionPerformed(evt);
            }
        });
        painelBotoes.add(btnEnviar);

        btnVarias.setText("Enviar 20 juntas");
        btnVarias.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnVariasActionPerformed(evt);
            }
        });
        painelBotoes.add(btnVarias);

        btnEstado.setText("Ver estado");
        btnEstado.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEstadoActionPerformed(evt);
            }
        });
        painelBotoes.add(btnEstado);

        painelTopo.add(painelBotoes, java.awt.BorderLayout.SOUTH);

        getContentPane().add(painelTopo, java.awt.BorderLayout.NORTH);

        scrollResposta.setPreferredSize(new java.awt.Dimension(860, 380));
        taResposta.setEditable(false);
        taResposta.setColumns(20);
        taResposta.setLineWrap(true);
        taResposta.setRows(5);
        scrollResposta.setViewportView(taResposta);

        getContentPane().add(scrollResposta, java.awt.BorderLayout.CENTER);

        painelRodape.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        btnLimpar.setText("Limpar");
        btnLimpar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLimparActionPerformed(evt);
            }
        });
        painelRodape.add(btnLimpar);

        lblStatus.setText("Serviço de nomes: localhost:50050");
        painelRodape.add(lblStatus);

        getContentPane().add(painelRodape, java.awt.BorderLayout.SOUTH);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnAtualizarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAtualizarActionPerformed
        // pede ao serviço de nomes a lista de servidores
        atualizarLista();
    }//GEN-LAST:event_btnAtualizarActionPerformed

    private void btnEnviarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEnviarActionPerformed
        // envia UMA tarefa para o servidor escolhido na lista
        int i = servidorEscolhido();
        if (i >= 0) {
            enviarTarefa(i, tipoEscolhido(), tfEntrada.getText());
        }
    }//GEN-LAST:event_btnEnviarActionPerformed

    private void btnVariasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnVariasActionPerformed
        // 20 threads ao mesmo tempo no mesmo servidor: nenhum número pode se repetir
        int i = servidorEscolhido();
        if (i < 0) {
            return;
        }
        TipoTarefa tipo = tipoEscolhido();
        String entrada = tfEntrada.getText();
        for (int n = 0; n < 20; n++) {
            new Thread(() -> enviarTarefa(i, tipo, entrada)).start();
        }
    }//GEN-LAST:event_btnVariasActionPerformed

    private void btnEstadoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEstadoActionPerformed
        // mostra o contador e o histórico do servidor escolhido
        int i = servidorEscolhido();
        if (i < 0) {
            return;
        }
        try {
            Estado estado = stubs.get(i)
                    .withDeadlineAfter(PRAZO_SEGUNDOS, TimeUnit.SECONDS)
                    .obterEstado(Vazio.getDefaultInstance());
            log("Estado de " + estado.getInstancia() + ": total processado = " + estado.getTotalProcessado());
            for (String linha : estado.getHistoricoList()) {
                log("      " + linha);
            }
        } catch (StatusRuntimeException e) {
            log(mensagemDeErro(instancias.get(i).getId(), e));
        }
    }//GEN-LAST:event_btnEstadoActionPerformed

    private void btnLimparActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLimparActionPerformed
        // limpa a área de respostas
        taResposta.setText("");
    }//GEN-LAST:event_btnLimparActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAtualizar;
    private javax.swing.JButton btnEnviar;
    private javax.swing.JButton btnEstado;
    private javax.swing.JButton btnLimpar;
    private javax.swing.JButton btnVarias;
    private javax.swing.JComboBox<String> cbxServidor;
    private javax.swing.JComboBox<String> cbxTipo;
    private javax.swing.JLabel lblEntrada;
    private javax.swing.JLabel lblServidor;
    private javax.swing.JLabel lblStatus;
    private javax.swing.JLabel lblTipo;
    private javax.swing.JPanel painelBotoes;
    private javax.swing.JPanel painelRodape;
    private javax.swing.JPanel painelServidor;
    private javax.swing.JPanel painelTarefa;
    private javax.swing.JPanel painelTopo;
    private javax.swing.JScrollPane scrollResposta;
    private javax.swing.JTextArea taResposta;
    private javax.swing.JTextField tfEntrada;
    // End of variables declaration//GEN-END:variables
}
