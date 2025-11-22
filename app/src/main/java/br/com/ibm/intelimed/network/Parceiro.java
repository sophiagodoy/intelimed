package br.com.ibm.intelimed.network;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.Semaphore;

/*
    A classe Parceiro pode representar o cliente ou o servidor
    Caso o cliente implemente, a classe representa o servidor,
    ja que o servidor seria o "parceiro" nesse caso.
    No cliente temos apenas um parceiro (servidor).

    Caso o servidor implemente, a classe representa o cliente,
    ja que o cliente seria o "parceiro" nesse caso
    No servidor podemos ter mais de um parceiro (clientes)

*/

public class Parceiro {
    private Socket conexao;
    private ObjectInputStream receptor; //Serve para receber dados do tipo Serializable
    private ObjectOutputStream transmissor; //Serve para enviar dados do tipo Serializable
    private Pedido proximoPedido = null;
    private Semaphore mutex = new Semaphore(1, true);

    public Parceiro(Socket conexao, ObjectInputStream receptor, ObjectOutputStream transmissor) throws Exception {
        if (conexao == null || receptor == null || transmissor == null) {
            throw new Exception("Parametro Ausente");
        }
        this.conexao = conexao;
        this.receptor = receptor;
        this.transmissor = transmissor;
    }

    public void recebeUmPedido(Pedido p) throws Exception {
        try {
            this.transmissor.writeObject(p);
            this.transmissor.flush();
        } catch (IOException erro) {
            throw new Exception("Erro de transmissão"+ erro.getClass().getSimpleName() + " - " + erro.getMessage());
        }
    }

    public Pedido enviarUmPedido() throws Exception {
        try {
            if (this.proximoPedido == null) this.proximoPedido = (Pedido) this.receptor.readObject();
            Pedido retorno = this.proximoPedido;
            this.proximoPedido = null;
            return retorno;
        } catch (Exception erro) {
            throw new Exception("Erro de recebimento"+ erro.getClass().getSimpleName() + " - " + erro.getMessage());
        }
    }

    //Esse metodo serve para ver que pedido esta la para ser recebido, sem consumir, ou seja, "espiar"
    public Pedido espionarPedido() throws Exception {
        try {
            this.mutex.acquireUninterruptibly();
            if (this.proximoPedido == null) this.proximoPedido = (Pedido) this.receptor.readObject();
            this.mutex.release();
            return this.proximoPedido;
        } catch (Exception erro) {
            throw new Exception("Erro de recebimento"+ erro.getClass().getSimpleName() + " - " + erro.getMessage());
        }
    }

    public void fecharConexao() throws Exception {
        try {
            this.conexao.close();
            this.receptor.close();
            this.transmissor.close();
        } catch (Exception erro) {
            throw new Exception("Erro de desconexao"+ erro.getClass().getSimpleName() + " - " + erro.getMessage());
        }
    }
}
