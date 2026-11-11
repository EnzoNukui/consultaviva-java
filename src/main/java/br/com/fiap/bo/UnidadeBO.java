package br.com.fiap.bo;

import br.com.fiap.dao.HorarioAtendimentoDAO;
import br.com.fiap.dao.UnidadeDAO;
import br.com.fiap.to.HorarioAtendimentoTO;
import br.com.fiap.to.UnidadeTO;

import java.util.ArrayList;

public class UnidadeBO {
    private UnidadeDAO unidadeDAO = new UnidadeDAO();
    private HorarioAtendimentoDAO horarioDAO = new HorarioAtendimentoDAO();

    public ArrayList<UnidadeTO> listarTodos() {
        return unidadeDAO.listarTodos();
    }

    public UnidadeTO buscarPorId(Long idUnidade) {
        if (idUnidade == null || idUnidade <= 0) {
            return null;
        }
        return unidadeDAO.buscarPorId(idUnidade);
    }

    public UnidadeTO inserir(UnidadeTO unidade) {
        if (!cadastroValido(unidade)) {
            return null;
        }
        return unidadeDAO.inserir(unidade);
    }

    public UnidadeTO atualizar(UnidadeTO unidade) {
        if (!cadastroValido(unidade) || unidade.getIdUnidade() == null || unidade.getIdUnidade() <= 0) {
            return null;
        }
        return unidadeDAO.atualizar(unidade);
    }

    public boolean excluir(Long idUnidade) {
        return idUnidade != null && idUnidade > 0 && unidadeDAO.excluir(idUnidade);
    }

    public ArrayList<HorarioAtendimentoTO> listarHorarios(Long idUnidade) {
        if (idUnidade == null || idUnidade <= 0) {
            return new ArrayList<>();
        }
        return horarioDAO.listarPorUnidade(idUnidade);
    }

    private boolean cadastroValido(UnidadeTO unidade) {
        return unidade != null && unidade.getNomeUnidade() != null && !unidade.getNomeUnidade().isBlank()
                && unidade.getEndereco() != null && !unidade.getEndereco().isBlank();
    }
}
