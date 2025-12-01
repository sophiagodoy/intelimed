package br.com.ibm.intelimed.network;
import java.util.List;
import java.util.Map;

public class RetornarDados extends Pedido {
    private List<Map<String, Object>> dados;

    public RetornarDados(List<Map<String, Object>> dados) {
        this.dados = dados;
    }

    public List<Map<String, Object>> getDados() {
        return dados;
    }
}