package br.com.fiap.bo;

import br.com.fiap.dao.MembroEquipeDAO;
import br.com.fiap.to.MembroEquipeTO;

import java.util.ArrayList;

public class MembroEquipeBO {
    private MembroEquipeDAO membroEquipeDAO = new MembroEquipeDAO();

    public ArrayList<MembroEquipeTO> listarTodos() {
        return membroEquipeDAO.listarTodos();
    }

    public MembroEquipeTO buscarPorId(Long idMembroEquipe) {
        if (idMembroEquipe == null || idMembroEquipe <= 0) {
            return null;
        }
        return membroEquipeDAO.buscarPorId(idMembroEquipe);
    }

    public MembroEquipeTO inserir(MembroEquipeTO membroEquipe) {
        if (!cadastroValido(membroEquipe)) {
            return null;
        }
        return membroEquipeDAO.inserir(membroEquipe);
    }

    public MembroEquipeTO atualizar(MembroEquipeTO membroEquipe) {
        if (!cadastroValido(membroEquipe) || membroEquipe.getIdMembroEquipe() == null
                || membroEquipe.getIdMembroEquipe() <= 0) {
            return null;
        }
        return membroEquipeDAO.atualizar(membroEquipe);
    }

    public boolean excluir(Long idMembroEquipe) {
        return idMembroEquipe != null && idMembroEquipe > 0 && membroEquipeDAO.excluir(idMembroEquipe);
    }

    public boolean podeAtender(Long idMembroEquipe) {
        MembroEquipeTO membroEquipe = buscarPorId(idMembroEquipe);
        return membroEquipe != null && "S".equals(membroEquipe.getAtendimento());
    }

    public boolean podeApoiarAdministrativamente(Long idMembroEquipe) {
        MembroEquipeTO membroEquipe = buscarPorId(idMembroEquipe);
        return membroEquipe != null && "S".equals(membroEquipe.getApoioAdministrativo());
    }

    public boolean podeAutorizarModalidade(Long idMembroEquipe) {
        MembroEquipeTO membroEquipe = buscarPorId(idMembroEquipe);
        return membroEquipe != null && "S".equals(membroEquipe.getAutorizaModalidade());
    }

    private boolean cadastroValido(MembroEquipeTO membroEquipe) {
        return membroEquipe != null && membroEquipe.getNomeMembroEquipe() != null
                && !membroEquipe.getNomeMembroEquipe().isBlank() && membroEquipe.getCargo() != null
                && !membroEquipe.getCargo().isBlank() && indicadorValido(membroEquipe.getAtendimento())
                && indicadorValido(membroEquipe.getApoioAdministrativo())
                && indicadorValido(membroEquipe.getAutorizaModalidade());
    }

    private boolean indicadorValido(String valor) {
        return "S".equals(valor) || "N".equals(valor);
    }
}
