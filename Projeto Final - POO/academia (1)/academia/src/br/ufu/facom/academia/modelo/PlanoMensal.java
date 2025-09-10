// CÓDIGO COMPLETO PARA: src/br/ufu/facom/academia/modelo/PlanoMensal.java

package br.ufu.facom.academia.modelo;

import br.ufu.facom.academia.excecoes.ValidacaoException;

public class PlanoMensal implements Plano {
    private String nome;
    private double valor;

    public PlanoMensal(String nome, double valor) throws ValidacaoException {
        if (valor <= 0) {
            throw new ValidacaoException("Valor do plano deve ser positivo.");
        }
        this.nome = nome;
        this.valor = valor;
    }

    @Override
    public String getNome() {
        return nome;
    }

    @Override
    public double getValor() {
        return valor;
    }

    @Override
    public String gerarRelatorio() {
        return "Plano: " + nome + "\nValor: R$ " + String.format("%.2f", valor) + "/mês";
    }
}