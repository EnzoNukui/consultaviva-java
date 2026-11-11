package br.com.fiap.bo;

import br.com.fiap.dao.ConnectionFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Function;

class TransacaoJdbc {
    private TransacaoJdbc() {
    }

    // Os DAOs retornam null em caso de falha. A operação só é confirmada com um resultado completo.
    static <T> T executar(Function<Connection, T> operacao) {
        Connection conexao = ConnectionFactory.getNewConnection();
        if (conexao == null) {
            return null;
        }

        T confirmado = null;
        try (conexao) {
            conexao.setAutoCommit(false);
            try {
                T resultado = operacao.apply(conexao);
                if (resultado == null) {
                    conexao.rollback();
                } else {
                    conexao.commit();
                    confirmado = resultado;
                }
            } catch (SQLException | RuntimeException e) {
                try {
                    conexao.rollback();
                } catch (SQLException erroRollback) {
                    e.addSuppressed(erroRollback);
                }
                if (e instanceof RuntimeException erroAplicacao) {
                    throw erroAplicacao;
                }
                System.out.println("Erro ao executar transação: " + e.getMessage());
            }
        } catch (SQLException e) {
            System.out.println("Erro na conexão da transação: " + e.getMessage());
        }
        return confirmado;
    }
}
