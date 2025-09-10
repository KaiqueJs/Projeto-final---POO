package br.ufu.facom.academia.modelo;

import br.ufu.facom.academia.excecoes.ValidacaoException;

public abstract class Pessoa {
    private final String nome;
    private final String cpf; // somente dígitos

    public Pessoa(String nome, String cpf) throws ValidacaoException {
        if (nome == null || nome.isBlank()) {
            throw new ValidacaoException("O nome não pode ser vazio.");
        }
        if (!cpfValido(cpf)) {
            throw new ValidacaoException("CPF inválido (use 11 dígitos, com ou sem pontuação).");
        }
        this.nome = nome.trim();
        this.cpf = cpf.replaceAll("[^0-9]", "");
    }

    private boolean cpfValido(String cpf) {
        if (cpf == null) return false;
        String digits = cpf.replaceAll("[^0-9]", "");
        return digits.length() == 11;
    }

    public String getNome() { return nome; }
    public String getCpf()  { return cpf;  }

    /** Exibe no padrão 000.000.000-00 */
    public String getCpfFormatado() {
        String d = cpf;
        return d.substring(0,3) + "." + d.substring(3,6) + "." + d.substring(6,9) + "-" + d.substring(9);
    }

    public abstract void seApresentar();

    @Override
    public String toString() {
        return nome + " (CPF: " + getCpfFormatado() + ")";
    }
}