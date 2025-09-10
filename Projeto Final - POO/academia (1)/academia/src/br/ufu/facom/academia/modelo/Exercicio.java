// CÓDIGO COMPLETO PARA: src/br/ufu/facom/academia/modelo/Exercicio.java

package br.ufu.facom.academia.modelo;

public class Exercicio {
    private String nome;
    private String grupoMuscular;

    public Exercicio(String nome, String grupoMuscular) {
        this.nome = nome;
        this.grupoMuscular = grupoMuscular;
    }

    public String getNome() {
        return nome;
    }

    public String getGrupoMuscular() {
        return grupoMuscular;
    }
}