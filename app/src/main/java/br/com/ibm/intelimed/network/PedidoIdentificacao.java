package br.com.ibm.intelimed.network;

public class PedidoIdentificacao extends Pedido{
    private String uid;
    public PedidoIdentificacao(String uid) {
        this.uid = uid;
    }

    public String getUid() {
        return uid;
    }
}