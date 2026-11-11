package br.com.fiap.bo;

import br.com.fiap.dao.AgendamentoDAO;
import br.com.fiap.dao.PacienteDAO;
import br.com.fiap.to.AgendamentoTO;
import br.com.fiap.to.CadastroPacienteTO;
import br.com.fiap.to.PacienteTO;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;

public class PacienteBO {
    private static final ZoneId ZONA_NEGOCIO = ZoneId.of("America/Sao_Paulo");
    private static int ITERACOES_HASH = 210000;
    private static SecureRandom ALEATORIO = new SecureRandom();

    private PacienteDAO pacienteDAO = new PacienteDAO();
    private AgendamentoDAO agendamentoDAO = new AgendamentoDAO();

    public ArrayList<PacienteTO> listarTodos() {
        return pacienteDAO.listarTodos();
    }

    public PacienteTO buscarPorId(Long idPaciente) {
        if (idPaciente == null || idPaciente <= 0) {
            return null;
        }
        return pacienteDAO.buscarPorId(idPaciente);
    }

    public PacienteTO inserir(PacienteTO paciente) {
        if (!cadastroValido(paciente)) {
            return null;
        }
        return pacienteDAO.inserir(paciente);
    }

    public PacienteTO cadastrar(CadastroPacienteTO cadastro) {
        if (cadastro == null || cadastro.getNomePaciente() == null || cadastro.getNomePaciente().isBlank()
                || cadastro.getDataNascimento() == null
                || !cadastro.getDataNascimento().isBefore(LocalDate.now(ZONA_NEGOCIO))
                || cadastro.getEmail() == null || cadastro.getEmail().isBlank()
                || cadastro.getSenha() == null || cadastro.getSenha().isBlank()) {
            return null;
        }
        if (pacienteDAO.emailEmUso(cadastro.getEmail())) {
            return null;
        }
        return pacienteDAO.cadastrar(cadastro, gerarHashSenha(cadastro.getSenha()));
    }

    public PacienteTO atualizar(PacienteTO paciente) {
        if (!cadastroValido(paciente) || paciente.getIdPaciente() == null || paciente.getIdPaciente() <= 0) {
            return null;
        }
        return pacienteDAO.atualizar(paciente);
    }

    public boolean excluir(Long idPaciente) {
        return idPaciente != null && idPaciente > 0 && pacienteDAO.excluir(idPaciente);
    }

    public ArrayList<AgendamentoTO> listarAgendamentos(Long idPaciente, Long idPacienteAutenticado) {
        if (idPaciente == null || idPaciente <= 0 || !idPaciente.equals(idPacienteAutenticado)) {
            return new ArrayList<>();
        }
        return agendamentoDAO.listarPorPaciente(idPacienteAutenticado);
    }

    private boolean cadastroValido(PacienteTO paciente) {
        return paciente != null && paciente.getNomePaciente() != null && !paciente.getNomePaciente().isBlank()
                && paciente.getDataNascimento() != null
                && paciente.getDataNascimento().isBefore(LocalDate.now(ZONA_NEGOCIO));
    }

    private String gerarHashSenha(String senha) {
        byte[] sal = new byte[16];
        ALEATORIO.nextBytes(sal);
        PBEKeySpec especificacao = new PBEKeySpec(senha.toCharArray(), sal, ITERACOES_HASH, 256);
        try {
            byte[] hash = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(especificacao).getEncoded();
            return "pbkdf2-sha256$" + ITERACOES_HASH + "$" + Base64.getEncoder().encodeToString(sal)
                    + "$" + Base64.getEncoder().encodeToString(hash);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Não foi possível processar a senha.", e);
        } finally {
            especificacao.clearPassword();
        }
    }
}
