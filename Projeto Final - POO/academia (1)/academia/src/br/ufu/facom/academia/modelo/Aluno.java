package br.ufu.facom.academia.modelo;

import br.ufu.facom.academia.excecoes.ValidacaoException;

public class Aluno {

    private String nome;
    private String cpf;
    private Plano plano;
    private Treino treino;  // ← Mantém apenas UM treino (estrutura original)

    // Construtor
    public Aluno(String nome, String cpf, Plano plano) throws ValidacaoException {
        // Validações necessárias
        if (nome == null || nome.trim().isEmpty()) {
            throw new ValidacaoException("Nome não pode ser vazio");
        }
        if (cpf == null || cpf.trim().isEmpty()) {
            throw new ValidacaoException("CPF não pode ser vazio");
        }
        if (plano == null) {
            throw new ValidacaoException("Plano não pode ser nulo");
        }

        this.nome = nome.trim();
        this.cpf = cpf.trim();
        this.plano = plano;
        this.treino = null; // Inicializado como null
    }

    // Getters
    public String getNome() {
        return nome;
    }

    public String getCpf() {
        return cpf;
    }

    public String getCpfFormatado() {
        if (cpf.length() != 11) return cpf;
        return cpf.substring(0, 3) + "." + cpf.substring(3, 6) + "." +
                cpf.substring(6, 9) + "-" + cpf.substring(9);
    }

    public Plano getPlano() {
        return plano;
    }

    public Treino getTreino() {
        return treino;
    }

    // Setters
    public void setTreino(Treino treino) {
        this.treino = treino;
    }

    // Método para apresentação
    public void seApresentar() {
        System.out.println("Nome: " + nome);
        System.out.println("CPF: " + getCpfFormatado());
        System.out.println("Plano: " + plano.getNome());
    }

    @Override
    public String toString() {
        return "Aluno{" +
                "nome='" + nome + '\'' +
                ", cpf='" + cpf + '\'' +
                ", plano=" + plano +
                ", treino=" + treino +
                '}';
    }
}