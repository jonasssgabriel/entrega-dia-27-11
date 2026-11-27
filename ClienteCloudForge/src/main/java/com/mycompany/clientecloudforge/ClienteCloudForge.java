package com.mycompany.clientecloudforge;

import java.awt.EventQueue;

/**
 * Ponto de entrada do cliente: Run no projeto abre a tela.
 */
public class ClienteCloudForge {

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> new TelaCliente().setVisible(true));
    }
}
