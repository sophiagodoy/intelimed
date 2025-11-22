package br.com.ibm.intelimed.network;

import java.net.*;
import java.io.*;
import java.util.List;
import java.util.Map;

public class Cliente {
    private final String hostPadrao  = "localhost";
    private final int portaPadrao = 3000;
    private Parceiro servidor;
    private String uid;
    private Thread threadReceberMensagens;
    private OnNovaMensagemListener listener;

    public void setListener(OnNovaMensagemListener listener) {
        this.listener = listener;
    }


    public void conectarServidor (String h, int p, String uid) throws Exception {
        Socket conexao = null;
        try {
            String host = this.hostPadrao;
            int porta = this.portaPadrao;
            if (h != null) host = h;
            if (p > 0) porta = p;
            conexao = new Socket (host, porta);
        } catch (Exception erro) {
            throw new Exception ("Indique o servidor e a porta corretos!\n");
        }
        ObjectOutputStream transmissor=null;
        try {
            transmissor = new ObjectOutputStream(conexao.getOutputStream());
            transmissor.flush();
        } catch (Exception erro) {
            throw new Exception("Indique o servidor e a porta corretos!\n");
        }
        ObjectInputStream receptor=null;
        try {
            receptor = new ObjectInputStream(conexao.getInputStream());
        } catch (Exception erro) {
            throw new Exception("Indique o servidor e a porta corretos!\n");
        }
        try {
            this.servidor = new Parceiro (conexao, receptor, transmissor);
        } catch (Exception erro) {
            System.err.println ("Indique o servidor e a porta corretos!\n");
            return;
        }

        this.servidor.recebeUmPedido(new PedidoIdentificacao(uid));
        this.uid = uid;

        receberMensagens();
    }

    public List<Map<String, Object>> usarMongo(PedidoDeUsoMongo pedido) throws Exception {
        if (servidor == null) throw new Exception("Cliente não conectado.");

        System.out.println("Tentando enviar mensagem: $mensagemDigitada para $uidDest");
        servidor.recebeUmPedido(pedido);

        if ("find".equalsIgnoreCase(pedido.getTipoPedido())) {
            Pedido retorno = servidor.enviarUmPedido();
            if (retorno instanceof RetornarDados) {
                RetornarDados resultado = (RetornarDados) retorno;
                return resultado.getDados();
            }
        }
        return null;
    }

    public void enviarMensagem(String conteudo, String uidDest) throws Exception {
        if (servidor == null) throw new Exception("Cliente não conectado.");

        PedidoMensagem msg = new PedidoMensagem(this.uid, conteudo, uidDest);
        servidor.recebeUmPedido(msg);
    }

    private void receberMensagens() {
        threadReceberMensagens = new Thread(() -> {
            try {
                while (true) {
                    Pedido pedido = servidor.enviarUmPedido();

                    if (pedido instanceof PedidoMensagem) {
                        PedidoMensagem msg = (PedidoMensagem) pedido;

                        if (listener != null)
                            listener.aoChegarMensagem(msg);
                    }
                }
            } catch (Exception e) {
                System.out.println("❌ Conexão encerrada: " + e.getMessage());
            }
        });

        threadReceberMensagens.setDaemon(true); // encerra junto com o app
        threadReceberMensagens.start();
    }
}
