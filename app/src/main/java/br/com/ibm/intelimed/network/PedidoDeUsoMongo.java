package br.com.ibm.intelimed.network;

import java.util.Map;

public class PedidoDeUsoMongo extends Pedido {
    private String db;
    private String collection;
    private String tipoPedido;
    private Map<String, Object> filtro;
    private Map<String, Object> novosDados;
    private Map<String, Object> doc;

    public PedidoDeUsoMongo(
            String database,
            String collection,
            String tipoPedido,
            Map<String, Object> filtro,
            Map<String, Object> novosDados,
            Map<String, Object> documento
    ) {
        this.db = database;
        this.collection = collection;
        this.tipoPedido = tipoPedido.toLowerCase();
        this.filtro = filtro;
        this.novosDados = novosDados;
        this.doc = documento;
    }

    public String getDb() {
        return this.db;
    }

    public String getCollection() {
        return this.collection;
    }

    public String getTipoPedido() {
        return this.tipoPedido;
    }

    public Map<String, Object> getFiltro() {
        return this.filtro;
    }

    public Map<String, Object> getNovosDados() {
        return this.novosDados;
    }

    public Map<String, Object> getDocumento() {
        return this.doc;
    }
}
