/*
 * VERSÃO COMPATÍVEL COM A CLASSE ALUNO ORIGINAL
 * Esta versão funciona com a classe Aluno existente (que usa apenas um Treino)
 * e simula múltiplos treinos através de uma estrutura auxiliar interna.
 */

package br.ufu.facom.academia.main;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import br.ufu.facom.academia.excecoes.ValidacaoException;
import br.ufu.facom.academia.modelo.Aluno;
import br.ufu.facom.academia.modelo.Exercicio;
import br.ufu.facom.academia.modelo.ItemTreino;
import br.ufu.facom.academia.modelo.Plano;
import br.ufu.facom.academia.modelo.PlanoAnual;
import br.ufu.facom.academia.modelo.PlanoMensal;
import br.ufu.facom.academia.modelo.Treino;

public class Principal {

    // Estrutura auxiliar para gerenciar múltiplos treinos por aluno
    private static Map<String, List<Treino>> treinosAlunos = new HashMap<>();

    public static void main(String[] args) {
        System.out.println("--- Sistema de Academia ---");

        Aluno aluno = null;
        try (Scanner sc = new Scanner(System.in)) {
            while (true) {
                System.out.println("\n=== MENU ===");
                System.out.println("1) Cadastrar aluno e escolher plano");
                System.out.println("2) Montar/editar treinos do aluno");
                System.out.println("3) Mostrar resumo e relatório");
                System.out.println("4) Salvar em arquivo (.txt)");
                System.out.println("5) Consultar e gerenciar alunos");
                System.out.println("0) Sair");
                int opc = lerIntNoIntervalo(sc, "Opção: ", 0, 5);

                if (opc == 0) {
                    System.out.println("Saindo. Até mais!");
                    break;
                }

                switch (opc) {
                    case 1 -> {
                        String nome = lerNaoVazio(sc, "Nome do aluno: ");
                        String cpf = lerCpf(sc, "CPF (11 dígitos, com ou sem pontuação): ");

                        // Verificar se o CPF já existe no arquivo
                        Path arquivoAluno = Paths.get("aluno_" + cpf + ".txt");
                        if (Files.exists(arquivoAluno)) {
                            System.out.println("\n*** ATENÇÃO: Já existe um aluno cadastrado com este CPF! ***");
                            System.out.println("Use a opção 5 do menu para consultar os dados do aluno.");
                            System.out.println("Ou use um CPF diferente para cadastrar um novo aluno.");
                            break;
                        }

                        System.out.println("\nPlanos disponíveis:");
                        System.out.println("1 - Plano Básico (R$ 99,90/mês)");
                        System.out.println("2 - Plano Completo Anual (R$ 1200,00/ano)");
                        int p = lerIntNoIntervalo(sc, "Escolha (1-2): ", 1, 2);

                        Plano planoEscolhido;
                        try {
                            if (p == 1) {
                                planoEscolhido = new PlanoMensal("Plano Básico", 99.90);
                            } else {
                                planoEscolhido = new PlanoAnual("Plano Completo Anual", 1200.00);
                            }
                        } catch (ValidacaoException e) {
                            System.out.println("Falha ao criar plano: " + e.getMessage());
                            break;
                        }

                        try {
                            aluno = new Aluno(nome, cpf, planoEscolhido);
                            // Inicializar lista de treinos para este aluno
                            treinosAlunos.put(cpf, new ArrayList<>());
                            System.out.println("Aluno criado com sucesso!");
                            aluno.seApresentar();
                        } catch (ValidacaoException e) {
                            System.out.println("Falha de validação: " + e.getMessage());
                            aluno = null;
                        }
                    }
                    case 2 -> {
                        if (aluno == null) {
                            System.out.println("Cadastre um aluno antes (opção 1) ou consulte um existente (opção 5).");
                            break;
                        }
                        gerenciarTreinos(sc, aluno);
                    }
                    case 3 -> {
                        if (aluno == null) {
                            System.out.println("Cadastre um aluno antes (opção 1) ou consulte um existente (opção 5).");
                            break;
                        }
                        System.out.println("\n=== RESUMO ===");
                        aluno.seApresentar();
                        System.out.println(aluno.getPlano().gerarRelatorio());

                        List<Treino> treinos = obterTreinosAluno(aluno.getCpf());
                        if (treinos != null && !treinos.isEmpty()) {
                            System.out.println("\n=== TREINOS DO ALUNO ===");
                            for (int i = 0; i < treinos.size(); i++) {
                                System.out.println("\n--- Treino " + (i + 1) + " ---");
                                System.out.println(treinos.get(i).gerarRelatorio());
                            }
                        } else {
                            System.out.println("(Aluno ainda não possui treinos.)");
                        }
                    }
                    case 4 -> {
                        if (aluno == null) {
                            System.out.println("Cadastre um aluno antes (opção 1) ou consulte um existente (opção 5).");
                            break;
                        }
                        try {
                            Path destino = salvarEmArquivo(aluno);
                            System.out.println("Arquivo salvo em: " + destino.toAbsolutePath());
                        } catch (IOException e) {
                            System.out.println("Falha ao salvar arquivo: " + e.getMessage());
                        }
                    }
                    case 5 -> {
                        aluno = gerenciarAlunos(sc, aluno);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Erro inesperado: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ================== GERENCIAMENTO DE ALUNOS (OPÇÃO 5 MESCLADA) ==================

    private static Aluno gerenciarAlunos(Scanner sc, Aluno alunoAtual) {
        System.out.println("\n=== CONSULTAR E GERENCIAR ALUNOS ===");

        // Listar todos os alunos
        List<DadosAluno> listaAlunos = listarTodosAlunos();

        if (listaAlunos.isEmpty()) {
            System.out.println("\nNenhum aluno cadastrado no sistema.");
            return alunoAtual;
        }

        // Exibir lista de alunos
        System.out.println("\nAlunos cadastrados:");
        System.out.println("----------------------------------------");
        for (int i = 0; i < listaAlunos.size(); i++) {
            DadosAluno dados = listaAlunos.get(i);
            System.out.printf("%d. Nome: %s | CPF: %s\n",
                    i + 1, dados.nome, formatarCpf(dados.cpf));
        }
        System.out.println("----------------------------------------");
        System.out.println("Total de alunos: " + listaAlunos.size());

        // Perguntar se deseja escolher algum aluno
        String escolher = lerNaoVazio(sc, "\nDeseja escolher algum aluno? (s/n): ").trim().toLowerCase();
        if (!escolher.equals("s")) {
            return alunoAtual;
        }

        // Solicitar CPF do aluno
        String cpfEscolhido = lerCpf(sc, "Digite o CPF do aluno que deseja consultar: ");

        // Verificar se o aluno existe
        Path arquivoAluno = Paths.get("aluno_" + cpfEscolhido + ".txt");
        if (!Files.exists(arquivoAluno)) {
            System.out.println("\n*** Aluno não encontrado! ***");
            return alunoAtual;
        }

        // Carregar dados completos do aluno
        Aluno alunoCarregado = carregarAlunoCompleto(arquivoAluno);
        if (alunoCarregado == null) {
            System.out.println("Erro ao carregar dados do aluno.");
            return alunoAtual;
        }

        // Mostrar resumo completo dos dados
        System.out.println("\n=== RESUMO GERAL DO ALUNO ===");
        System.out.println("Nome: " + alunoCarregado.getNome());
        System.out.println("CPF: " + alunoCarregado.getCpfFormatado());
        System.out.println("\n" + alunoCarregado.getPlano().gerarRelatorio());

        System.out.println("\n=== TREINOS ===");
        List<Treino> treinos = obterTreinosAluno(alunoCarregado.getCpf());
        if (treinos != null && !treinos.isEmpty()) {
            for (int i = 0; i < treinos.size(); i++) {
                System.out.println("\n--- Treino " + (i + 1) + " ---");
                System.out.println(treinos.get(i).gerarRelatorio());
            }
        } else {
            System.out.println("(Aluno ainda não possui treinos.)");
        }

        // Perguntar se deseja selecionar este aluno para carregamento
        String carregar = lerNaoVazio(sc, "\nDeseja selecionar este aluno para carregamento e edição? (s/n): ").trim().toLowerCase();
        if (!carregar.equals("s")) {
            return alunoAtual;
        }

        System.out.println("\n*** Aluno carregado no sistema! ***");

        // Menu de opções para o aluno carregado
        while (true) {
            System.out.println("\n=== OPÇÕES PARA O ALUNO CARREGADO ===");
            System.out.println("1) Editar dados cadastrais");
            System.out.println("2) Editar treinos");
            System.out.println("3) Salvar alterações no arquivo");
            System.out.println("4) Ver resumo do aluno");
            System.out.println("0) Voltar ao menu principal");

            int opcao = lerIntNoIntervalo(sc, "Escolha uma opção: ", 0, 4);

            if (opcao == 0) {
                break;
            }

            switch (opcao) {
                case 1 -> {
                    alunoCarregado = editarDadosCadastrais(sc, alunoCarregado);
                }
                case 2 -> {
                    gerenciarTreinos(sc, alunoCarregado);
                }
                case 3 -> {
                    try {
                        Path destino = salvarEmArquivo(alunoCarregado);
                        System.out.println("Alterações salvas em: " + destino.toAbsolutePath());
                    } catch (IOException e) {
                        System.out.println("Erro ao salvar arquivo: " + e.getMessage());
                    }
                }
                case 4 -> {
                    System.out.println("\n=== RESUMO DO ALUNO ===");
                    alunoCarregado.seApresentar();
                    System.out.println(alunoCarregado.getPlano().gerarRelatorio());
                    List<Treino> treinosResumo = obterTreinosAluno(alunoCarregado.getCpf());
                    if (treinosResumo != null && !treinosResumo.isEmpty()) {
                        System.out.println("\n=== TREINOS DO ALUNO ===");
                        for (int i = 0; i < treinosResumo.size(); i++) {
                            System.out.println("\n--- Treino " + (i + 1) + " ---");
                            System.out.println(treinosResumo.get(i).gerarRelatorio());
                        }
                    } else {
                        System.out.println("(Aluno ainda não possui treinos.)");
                    }
                }
            }
        }

        return alunoCarregado;
    }

    // ================== FUNÇÕES AUXILIARES PARA MÚLTIPLOS TREINOS ==================

    private static List<Treino> obterTreinosAluno(String cpf) {
        return treinosAlunos.get(cpf);
    }

    private static void adicionarTreinoAluno(String cpf, Treino treino) {
        List<Treino> treinos = treinosAlunos.get(cpf);
        if (treinos == null) {
            treinos = new ArrayList<>();
            treinosAlunos.put(cpf, treinos);
        }
        treinos.add(treino);
    }

    private static void removerTreinoAluno(String cpf, int indice) {
        List<Treino> treinos = treinosAlunos.get(cpf);
        if (treinos != null && indice >= 0 && indice < treinos.size()) {
            treinos.remove(indice);
        }
    }

    // ================== GERENCIAMENTO DE TREINOS ==================

    private static void gerenciarTreinos(Scanner sc, Aluno aluno) {
        while (true) {
            System.out.println("\n=== GERENCIAR TREINOS ===");
            System.out.println("Aluno: " + aluno.getNome());

            List<Treino> treinos = obterTreinosAluno(aluno.getCpf());
            if (treinos != null && !treinos.isEmpty()) {
                System.out.println("Treinos cadastrados: " + treinos.size());
                for (int i = 0; i < treinos.size(); i++) {
                    System.out.println("  " + (i + 1) + ". " + treinos.get(i).getNome() +
                            " (" + treinos.get(i).getItens().size() + " exercícios)");
                }
            } else {
                System.out.println("Nenhum treino cadastrado.");
            }

            System.out.println("\nOpções:");
            System.out.println("1) Cadastrar novo treino");
            System.out.println("2) Editar treino existente");
            System.out.println("3) Remover treino");
            System.out.println("4) Ver todos os treinos");
            System.out.println("0) Voltar");

            int opcao = lerIntNoIntervalo(sc, "Escolha uma opção: ", 0, 4);

            if (opcao == 0) break;

            switch (opcao) {
                case 1 -> {
                    cadastrarNovoTreino(sc, aluno);
                }
                case 2 -> {
                    editarTreinoExistente(sc, aluno);
                }
                case 3 -> {
                    removerTreino(sc, aluno);
                }
                case 4 -> {
                    verTodosTreinos(aluno);
                }
            }
        }
    }

    private static void cadastrarNovoTreino(Scanner sc, Aluno aluno) {
        System.out.println("\n=== CADASTRAR NOVO TREINO ===");
        String nomeTreino = lerNaoVazio(sc, "Nome do novo treino (ex.: Full Body A, Push, Pull): ");

        // Verificar se já existe treino com esse nome
        List<Treino> treinos = obterTreinosAluno(aluno.getCpf());
        if (treinos != null) {
            for (Treino t : treinos) {
                if (t.getNome().equalsIgnoreCase(nomeTreino)) {
                    System.out.println("*** ATENÇÃO: Já existe um treino com este nome! ***");
                    String continuar = lerNaoVazio(sc, "Deseja continuar mesmo assim? (s/n): ").trim().toLowerCase();
                    if (!continuar.equals("s")) {
                        return;
                    }
                    break;
                }
            }
        }

        Treino novoTreino = new Treino(nomeTreino);

        System.out.println("\nAgora vamos adicionar exercícios ao treino:");
        while (true) {
            adicionarExercicio(sc, novoTreino);

            String mais = lerNaoVazio(sc, "Adicionar outro exercício? (s/n): ").trim().toLowerCase();
            if (!mais.equals("s")) break;
        }

        adicionarTreinoAluno(aluno.getCpf(), novoTreino);
        System.out.println("\n*** Treino '" + nomeTreino + "' cadastrado com sucesso! ***");
        System.out.println("Total de exercícios: " + novoTreino.getItens().size());
    }

    private static void editarTreinoExistente(Scanner sc, Aluno aluno) {
        List<Treino> treinos = obterTreinosAluno(aluno.getCpf());

        if (treinos == null || treinos.isEmpty()) {
            System.out.println("\nO aluno não possui treinos cadastrados.");
            String criar = lerNaoVazio(sc, "Deseja cadastrar um novo treino? (s/n): ").trim().toLowerCase();
            if (criar.equals("s")) {
                cadastrarNovoTreino(sc, aluno);
            }
            return;
        }

        System.out.println("\n=== SELECIONAR TREINO PARA EDITAR ===");
        for (int i = 0; i < treinos.size(); i++) {
            Treino t = treinos.get(i);
            System.out.println((i + 1) + ". " + t.getNome() + " (" + t.getItens().size() + " exercícios)");
        }

        int indice = lerIntNoIntervalo(sc, "\nQual treino deseja editar? (1-" + treinos.size() + "): ", 1, treinos.size());
        Treino treinoSelecionado = treinos.get(indice - 1);

        editarTreino(sc, treinoSelecionado);
    }

    private static void removerTreino(Scanner sc, Aluno aluno) {
        List<Treino> treinos = obterTreinosAluno(aluno.getCpf());

        if (treinos == null || treinos.isEmpty()) {
            System.out.println("\nO aluno não possui treinos para remover.");
            return;
        }

        System.out.println("\n=== REMOVER TREINO ===");
        for (int i = 0; i < treinos.size(); i++) {
            Treino t = treinos.get(i);
            System.out.println((i + 1) + ". " + t.getNome() + " (" + t.getItens().size() + " exercícios)");
        }

        int indice = lerIntNoIntervalo(sc, "\nQual treino deseja remover? (1-" + treinos.size() + "): ", 1, treinos.size());
        Treino treinoRemover = treinos.get(indice - 1);

        String confirmar = lerNaoVazio(sc, "Tem certeza que deseja remover o treino '" +
                treinoRemover.getNome() + "'? (s/n): ").trim().toLowerCase();

        if (confirmar.equals("s")) {
            removerTreinoAluno(aluno.getCpf(), indice - 1);
            System.out.println("*** Treino '" + treinoRemover.getNome() + "' removido com sucesso! ***");
        } else {
            System.out.println("Remoção cancelada.");
        }
    }

    private static void verTodosTreinos(Aluno aluno) {
        List<Treino> treinos = obterTreinosAluno(aluno.getCpf());

        if (treinos == null || treinos.isEmpty()) {
            System.out.println("\nO aluno não possui treinos cadastrados.");
            return;
        }

        System.out.println("\n=== TODOS OS TREINOS DO ALUNO ===");
        for (int i = 0; i < treinos.size(); i++) {
            System.out.println("\n--- TREINO " + (i + 1) + " ---");
            System.out.println(treinos.get(i).gerarRelatorio());
        }
    }

    // ================== EDIÇÃO DE TREINO (INDIVIDUAL) ==================

    private static void editarTreino(Scanner sc, Treino treino) {
        while (true) {
            System.out.println("\n=== EDITAR TREINO: " + treino.getNome() + " ===");
            System.out.println("Exercícios cadastrados: " + treino.getItens().size());
            System.out.println();
            System.out.println("1) Ver treino completo");
            System.out.println("2) Adicionar exercício");
            System.out.println("3) Remover exercício");
            System.out.println("4) Alterar nome do treino");
            System.out.println("0) Voltar");

            int opcao = lerIntNoIntervalo(sc, "Escolha uma opção: ", 0, 4);

            if (opcao == 0) break;

            switch (opcao) {
                case 1 -> {
                    System.out.println("\n" + treino.gerarRelatorio());
                }
                case 2 -> {
                    adicionarExercicio(sc, treino);
                    System.out.println("*** Exercício adicionado com sucesso! ***");
                }
                case 3 -> {
                    removerExercicio(sc, treino);
                }
                case 4 -> {
                    String novoNome = lerNaoVazio(sc, "Digite o novo nome do treino: ");
                    treino.setNome(novoNome);
                    System.out.println("*** Nome do treino alterado com sucesso! ***");
                }
            }
        }
    }

    private static void adicionarExercicio(Scanner sc, Treino treino) {
        System.out.println("\nAdicionar exercício:");
        String nomeEx = lerNaoVazio(sc, "Nome do exercício: ");
        String grupo = lerNaoVazio(sc, "Grupo muscular (ex.: Peito/Costas/Perna): ");
        int series = lerIntMin(sc, "Séries (>=1): ", 1);
        String obs = lerOpcional(sc, "Observações (ex.: 10-12 reps, 60s descanso, 20kg): ");

        Exercicio ex = new Exercicio(nomeEx, grupo);
        treino.adicionarItem(new ItemTreino(ex, series, obs));
    }

    private static void removerExercicio(Scanner sc, Treino treino) {
        if (treino.getItens().isEmpty()) {
            System.out.println("O treino não possui exercícios para remover.");
            return;
        }

        System.out.println("\n=== EXERCÍCIOS NO TREINO ===");
        List<ItemTreino> itens = treino.getItens();
        for (int i = 0; i < itens.size(); i++) {
            ItemTreino item = itens.get(i);
            System.out.printf("%d. %s (%s) - %d séries\n",
                    i + 1,
                    item.getExercicio().getNome(),
                    item.getExercicio().getGrupoMuscular(),
                    item.getSeries());
        }

        int indice = lerIntNoIntervalo(sc, "Qual exercício deseja remover? (1-" + itens.size() + "): ", 1, itens.size());

        ItemTreino itemRemovido = itens.get(indice - 1);
        itens.remove(indice - 1);

        System.out.println("Exercício '" + itemRemovido.getExercicio().getNome() + "' removido com sucesso!");
    }

    // ================== EDIÇÃO DE DADOS CADASTRAIS ==================

    private static Aluno editarDadosCadastrais(Scanner sc, Aluno aluno) {
        boolean dadosAlterados = false;
        Aluno alunoEditado = aluno;

        while (true) {
            System.out.println("\n=== EDITAR DADOS CADASTRAIS ===");
            System.out.println("1) Alterar Nome (atual: " + alunoEditado.getNome() + ")");
            System.out.println("2) Alterar CPF (atual: " + formatarCpf(alunoEditado.getCpf()) + ")");
            System.out.println("3) Alterar Plano (atual: " + alunoEditado.getPlano().getNome() + ")");
            System.out.println("0) Voltar");

            int opcEdit = lerIntNoIntervalo(sc, "Opção: ", 0, 3);

            if (opcEdit == 0) break;

            switch (opcEdit) {
                case 1 -> {
                    String novoNome = lerNaoVazio(sc, "Digite o novo nome: ");
                    try {
                        Aluno novoAluno = new Aluno(novoNome, alunoEditado.getCpf(), alunoEditado.getPlano());
                        // Manter treino original se existir
                        if (alunoEditado.getTreino() != null) {
                            novoAluno.setTreino(alunoEditado.getTreino());
                        }
                        alunoEditado = novoAluno;
                        dadosAlterados = true;
                        System.out.println("Nome alterado com sucesso!");
                    } catch (ValidacaoException e) {
                        System.out.println("Erro ao alterar nome: " + e.getMessage());
                    }
                }
                case 2 -> {
                    String novoCpf = lerCpf(sc, "Digite o novo CPF: ");

                    Path novoArquivo = Paths.get("aluno_" + novoCpf + ".txt");
                    if (Files.exists(novoArquivo) && !novoCpf.equals(alunoEditado.getCpf())) {
                        System.out.println("\n*** ERRO: Já existe um aluno com este CPF! ***");
                        break;
                    }

                    try {
                        String cpfAntigo = alunoEditado.getCpf();

                        Aluno novoAluno = new Aluno(alunoEditado.getNome(), novoCpf, alunoEditado.getPlano());
                        // Manter treino original se existir
                        if (alunoEditado.getTreino() != null) {
                            novoAluno.setTreino(alunoEditado.getTreino());
                        }

                        // Transferir treinos auxiliares
                        List<Treino> treinosAntigos = obterTreinosAluno(cpfAntigo);
                        if (treinosAntigos != null) {
                            treinosAlunos.put(novoCpf, new ArrayList<>(treinosAntigos));
                            treinosAlunos.remove(cpfAntigo);
                        }

                        Path destinoNovo = salvarEmArquivo(novoAluno);

                        if (!cpfAntigo.equals(novoCpf)) {
                            Path arquivoAntigo = Paths.get("aluno_" + cpfAntigo + ".txt");
                            try {
                                Files.deleteIfExists(arquivoAntigo);
                                System.out.println("Arquivo antigo removido.");
                            } catch (IOException e) {
                                System.out.println("Aviso: Não foi possível remover o arquivo antigo.");
                            }
                        }

                        alunoEditado = novoAluno;
                        dadosAlterados = true;
                        System.out.println("CPF alterado com sucesso!");
                        System.out.println("Novo arquivo salvo em: " + destinoNovo.toAbsolutePath());
                    } catch (ValidacaoException e) {
                        System.out.println("Erro ao alterar CPF: " + e.getMessage());
                    } catch (IOException e) {
                        System.out.println("Erro ao salvar arquivo: " + e.getMessage());
                    }
                }
                case 3 -> {
                    System.out.println("\nPlanos disponíveis:");
                    System.out.println("1 - Plano Básico (R$ 99,90/mês)");
                    System.out.println("2 - Plano Completo Anual (R$ 1200,00/ano)");
                    int p = lerIntNoIntervalo(sc, "Escolha o novo plano (1-2): ", 1, 2);

                    try {
                        Plano novoPlano;
                        if (p == 1) {
                            novoPlano = new PlanoMensal("Plano Básico", 99.90);
                        } else {
                            novoPlano = new PlanoAnual("Plano Completo Anual", 1200.00);
                        }

                        Aluno novoAluno = new Aluno(alunoEditado.getNome(), alunoEditado.getCpf(), novoPlano);
                        // Manter treino original se existir
                        if (alunoEditado.getTreino() != null) {
                            novoAluno.setTreino(alunoEditado.getTreino());
                        }
                        alunoEditado = novoAluno;
                        dadosAlterados = true;
                        System.out.println("Plano alterado com sucesso!");
                    } catch (ValidacaoException e) {
                        System.out.println("Erro ao alterar plano: " + e.getMessage());
                    }
                }
            }
        }

        if (dadosAlterados) {
            System.out.println("\n*** Dados cadastrais alterados! Não esqueça de salvar as alterações (opção 3). ***");
        }

        return alunoEditado;
    }

    // ================== CLASSE AUXILIAR PARA DADOS DO ALUNO ==================

    private static class DadosAluno {
        String nome;
        String cpf;

        DadosAluno(String nome, String cpf) {
            this.nome = nome;
            this.cpf = cpf;
        }
    }

    // ================== FUNÇÃO PARA LISTAR TODOS OS ALUNOS ==================

    private static List<DadosAluno> listarTodosAlunos() {
        List<DadosAluno> alunos = new ArrayList<>();

        try {
            Path diretorioAtual = Paths.get(".");

            try (Stream<Path> arquivos = Files.list(diretorioAtual)) {
                List<Path> arquivosAlunos = arquivos
                        .filter(Files::isRegularFile)
                        .filter(p -> p.getFileName().toString().startsWith("aluno_"))
                        .filter(p -> p.getFileName().toString().endsWith(".txt"))
                        .collect(Collectors.toList());

                for (Path arquivo : arquivosAlunos) {
                    try {
                        String nomeArquivo = arquivo.getFileName().toString();
                        String cpf = nomeArquivo.replace("aluno_", "").replace(".txt", "");

                        String conteudo = Files.readString(arquivo);
                        String[] linhas = conteudo.split("\n");

                        String nomeAluno = null;
                        for (String linha : linhas) {
                            if (linha.startsWith("Nome: ")) {
                                nomeAluno = linha.substring(6).trim();
                                break;
                            }
                        }

                        if (nomeAluno != null && !nomeAluno.isEmpty()) {
                            alunos.add(new DadosAluno(nomeAluno, cpf));
                        }
                    } catch (IOException e) {
                        System.out.println("Aviso: Não foi possível ler o arquivo " + arquivo.getFileName());
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Erro ao listar arquivos: " + e.getMessage());
        }

        return alunos;
    }

    // ================== UTILITÁRIOS DE ENTRADA ==================

    private static String lerNaoVazio(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            String s = sc.nextLine();
            if (s != null && !s.isBlank()) return s.trim();
            System.out.println("Valor não pode ser vazio. Tente novamente.");
        }
    }

    private static String lerOpcional(Scanner sc, String prompt) {
        System.out.print(prompt);
        String s = sc.nextLine();
        return s == null ? "" : s.trim();
    }

    private static int lerIntNoIntervalo(Scanner sc, String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String s = sc.nextLine();
            try {
                int v = Integer.parseInt(s.trim());
                if (v < min || v > max) {
                    System.out.printf("Digite um número entre %d e %d.%n", min, max);
                    continue;
                }
                return v;
            } catch (NumberFormatException e) {
                System.out.println("Entrada inválida. Digite um número inteiro.");
            }
        }
    }

    private static int lerIntMin(Scanner sc, String prompt, int min) {
        while (true) {
            System.out.print(prompt);
            String s = sc.nextLine();
            try {
                int v = Integer.parseInt(s.trim());
                if (v < min) {
                    System.out.printf("O valor deve ser >= %d.%n", min);
                    continue;
                }
                return v;
            } catch (NumberFormatException e) {
                System.out.println("Entrada inválida. Digite um número inteiro.");
            }
        }
    }

    private static String lerCpf(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            String s = sc.nextLine();
            if (s == null) continue;
            String digits = s.replaceAll("[^0-9]", "");
            if (digits.length() == 11) return digits;
            System.out.println("CPF precisa ter 11 dígitos. Tente novamente.");
        }
    }

    private static String formatarCpf(String cpf) {
        if (cpf.length() != 11) return cpf;
        return cpf.substring(0, 3) + "." + cpf.substring(3, 6) + "." +
                cpf.substring(6, 9) + "-" + cpf.substring(9);
    }

    // ================== SALVAMENTO EM ARQUIVO ==================

    private static Path salvarEmArquivo(Aluno aluno) throws IOException {
        String nomeArquivo = "aluno_" + aluno.getCpf() + ".txt";
        Path path = Path.of(nomeArquivo);

        StringBuilder sb = new StringBuilder();
        sb.append("=== DADOS DO ALUNO ===\n");
        sb.append("Nome: ").append(aluno.getNome()).append("\n");
        sb.append("CPF: ").append(aluno.getCpfFormatado()).append("\n\n");

        sb.append("=== PLANO ===\n");
        sb.append(aluno.getPlano().gerarRelatorio()).append("\n\n");

        sb.append("=== TREINOS ===\n");
        List<Treino> treinos = obterTreinosAluno(aluno.getCpf());
        if (treinos != null && !treinos.isEmpty()) {
            for (int i = 0; i < treinos.size(); i++) {
                sb.append("--- TREINO ").append(i + 1).append(" ---\n");
                sb.append(treinos.get(i).gerarRelatorio()).append("\n\n");
            }
        } else {
            sb.append("(Aluno ainda não possui treinos.)\n");
        }

        Files.writeString(path, sb.toString());
        return path;
    }

    // ================== CARREGAR ALUNO COMPLETO ==================

    private static Aluno carregarAlunoCompleto(Path arquivo) {
        try {
            String conteudo = Files.readString(arquivo);
            String[] linhas = conteudo.split("\n");

            String cpf = arquivo.getFileName().toString().replace("aluno_", "").replace(".txt", "");

            String nome = null;
            for (String linha : linhas) {
                if (linha.startsWith("Nome: ")) {
                    nome = linha.substring(6).trim();
                    break;
                }
            }

            if (nome == null || nome.isEmpty()) {
                nome = "Nome não encontrado";
            }

            boolean planoAnual = false;
            double valor = 99.90;

            for (String linha : linhas) {
                if (linha.contains("/ano")) {
                    planoAnual = true;
                    valor = 1200.00;
                    break;
                }
            }

            Plano plano;
            try {
                if (planoAnual) {
                    plano = new PlanoAnual("Plano Completo Anual", valor);
                } else {
                    plano = new PlanoMensal("Plano Básico", valor);
                }
            } catch (ValidacaoException e) {
                try {
                    plano = new PlanoMensal("Plano Básico", 99.90);
                } catch (ValidacaoException ex) {
                    return null;
                }
            }

            Aluno aluno;
            try {
                aluno = new Aluno(nome, cpf, plano);

                // Carregar múltiplos treinos
                List<Treino> treinos = new ArrayList<>();
                Treino treinoAtual = null;

                for (int i = 0; i < linhas.length; i++) {
                    String linha = linhas[i];

                    // Detectar início de um novo treino
                    if (linha.trim().startsWith("--- TREINO") && linha.contains("---")) {
                        if (treinoAtual != null) {
                            treinos.add(treinoAtual); // Adicionar treino anterior
                        }
                        // Procurar nome do treino na próxima linha que começa com "Treino:"
                        for (int j = i + 1; j < linhas.length; j++) {
                            if (linhas[j].startsWith("Treino:")) {
                                String nomeTreino = linhas[j].substring(7).trim();
                                treinoAtual = new Treino(nomeTreino);
                                break;
                            }
                        }
                    }

                    // Adicionar exercícios ao treino atual
                    if (treinoAtual != null && linha.trim().startsWith("- Exercício:")) {
                        String nomeEx = linha.substring(linha.indexOf(":") + 1).trim();
                        String grupo = "Não especificado";
                        int series = 3;
                        String obs = "";

                        if (i + 1 < linhas.length && linhas[i + 1].trim().startsWith("Grupo:")) {
                            grupo = linhas[i + 1].substring(linhas[i + 1].indexOf(":") + 1).trim();
                        }
                        if (i + 2 < linhas.length && linhas[i + 2].trim().startsWith("Séries:")) {
                            try {
                                series = Integer.parseInt(linhas[i + 2].substring(linhas[i + 2].indexOf(":") + 1).trim());
                            } catch (NumberFormatException e) {
                                series = 3;
                            }
                        }
                        if (i + 3 < linhas.length && linhas[i + 3].trim().startsWith("Observações:")) {
                            obs = linhas[i + 3].substring(linhas[i + 3].indexOf(":") + 1).trim();
                        }

                        Exercicio ex = new Exercicio(nomeEx, grupo);
                        treinoAtual.adicionarItem(new ItemTreino(ex, series, obs));
                    }
                }

                // Adicionar último treino se existir
                if (treinoAtual != null) {
                    treinos.add(treinoAtual);
                }

                // Armazenar treinos na estrutura auxiliar
                if (!treinos.isEmpty()) {
                    treinosAlunos.put(cpf, treinos);
                    // Definir o primeiro treino como treino principal (compatibilidade)
                    aluno.setTreino(treinos.get(0));
                }

                return aluno;

            } catch (ValidacaoException e) {
                System.out.println("Erro na validação ao carregar aluno: " + e.getMessage());
                return null;
            }

        } catch (IOException e) {
            System.out.println("Erro ao ler arquivo: " + e.getMessage());
            return null;
        }
    }
}