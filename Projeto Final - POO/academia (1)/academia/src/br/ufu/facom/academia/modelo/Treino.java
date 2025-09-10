// CÓDIGO COMPLETO PARA: src/br/ufu/facom/academia/modelo/Treino.java

package br.ufu.facom.academia.modelo;

import java.util.ArrayList;
import java.util.List;

public class Treino {
    private String nome;
    private List<ItemTreino> itens;

    public Treino(String nome) {
        this.nome = nome;
        this.itens = new ArrayList<>();
    }

    public void adicionarItem(ItemTreino item) {
        this.itens.add(item);
    }

    // --- MÉTODOS QUE FORAM ADICIONADOS PARA CORRIGIR O ERRO ---
    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public List<ItemTreino> getItens() {
        return itens;
    }
    // --- FIM DOS MÉTODOS ADICIONADOS ---

    public String gerarRelatorio() {
        if (itens.isEmpty()) {
            return "Treino: " + nome + "\n(Nenhum exercício cadastrado)";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Treino: ").append(nome).append("\n");
        sb.append("---------------------------------\n");
        for (ItemTreino item : itens) {
            sb.append(item.gerarRelatorioItem());
        }
        sb.append("---------------------------------");
        return sb.toString();
    }
}