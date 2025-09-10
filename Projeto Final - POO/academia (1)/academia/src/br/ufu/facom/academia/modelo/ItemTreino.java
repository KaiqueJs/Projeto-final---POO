// CÓDIGO COMPLETO PARA: src/br/ufu/facom/academia/modelo/ItemTreino.java

package br.ufu.facom.academia.modelo;

public class ItemTreino {
    private Exercicio exercicio;
    private int series;
    private String observacoes;

    public ItemTreino(Exercicio exercicio, int series, String observacoes) {
        this.exercicio = exercicio;
        this.series = series;
        this.observacoes = observacoes;
    }

    // --- MÉTODO ADICIONADO PARA CORRIGIR O ERRO ---
    public Exercicio getExercicio() {
        return exercicio;
    }

    public int getSeries() {
        return series;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public String gerarRelatorioItem() {
        StringBuilder sb = new StringBuilder();
        sb.append("    - Exercício: ").append(exercicio.getNome()).append("\n");
        sb.append("      Grupo: ").append(exercicio.getGrupoMuscular()).append("\n");
        sb.append("      Séries: ").append(series).append("\n");
        if (observacoes != null && !observacoes.isBlank()) {
            sb.append("      Obs: ").append(observacoes).append("\n");
        }
        return sb.toString();
    }
}