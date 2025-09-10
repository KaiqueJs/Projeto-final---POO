package br.ufu.facom.academia.modelo;

import br.ufu.facom.academia.excecoes.ValidacaoException;
import java.util.ArrayList;
import java.util.List;

public class Instrutor extends Pessoa {
    private String cref;
    private List<Aluno> alunosSupervisionados;

    // A assinatura do construtor precisa do "throws ValidacaoException"
    // porque a chamada "super(nome, cpf)" pode lançar essa exceção.
    public Instrutor(String nome, String cpf, String cref) throws ValidacaoException {
        super(nome, cpf); // Esta chamada exige o tratamento ou a declaração da exceção
        this.cref = cref;
        this.alunosSupervisionados = new ArrayList<>();
    }

    public String getCref() {
        return cref;
    }

    public void adicionarAlunoParaSupervisionar(Aluno aluno) {
        this.alunosSupervisionados.add(aluno);
    }

    public void montarTreino(Aluno aluno, Treino treino) {
        aluno.setTreino(treino);
        System.out.println("Treino '" + treino.getNome() + "' montado for o aluno " + aluno.getNome() + " pelo instrutor " + getNome());
    }

    @Override
    public void seApresentar() {
        System.out.println("Olá, sou o instrutor(a) " + getNome() + " (CREF: " + cref + ")");
    }
}