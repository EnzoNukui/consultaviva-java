package br.com.fiap.resource;

import br.com.fiap.dao.ConnectionFactory;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;

@Component
@Order(2)
public class AutenticacaoFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String caminho = request.getRequestURI().substring(request.getContextPath().length());
        if ("POST".equalsIgnoreCase(request.getMethod()) && "/pacientes/cadastro".equals(caminho)) {
            filterChain.doFilter(request, response);
            return;
        }

        String[] credenciais = lerCredenciais(request.getHeader("Authorization"));
        if (credenciais == null) {
            responderNaoAutorizado(response);
            return;
        }

        String[] conta = autenticar(credenciais[0], credenciais[1]);
        if (conta == null) {
            responderNaoAutorizado(response);
            return;
        }

        String perfilNecessario = perfilNecessario(caminho, request.getMethod());
        if (perfilNecessario != null && !perfilNecessario.equals(conta[3])) {
            responderProibido(response);
            return;
        }

        Long idConta = Long.valueOf(conta[0]);
        request.setAttribute("idContaAutenticada", idConta);
        request.setAttribute("nomeContaAutenticada", conta[1]);
        request.setAttribute("emailContaAutenticada", conta[2]);
        request.setAttribute("perfilAutenticado", conta[3]);
        if ("PACIENTE".equals(conta[3])) {
            request.setAttribute("idPacienteAutenticado", idConta);
        } else {
            request.setAttribute("idMembroEquipeAutenticado", idConta);
        }
        filterChain.doFilter(request, response);
    }

    private String[] lerCredenciais(String authorization) {
        if (authorization == null || !authorization.regionMatches(true, 0, "Basic ", 0, 6)) {
            return null;
        }
        try {
            byte[] bytes = Base64.getDecoder().decode(authorization.substring(6).trim());
            String dados = new String(bytes, StandardCharsets.UTF_8);
            int separador = dados.indexOf(':');
            if (separador <= 0 || separador == dados.length() - 1) {
                return null;
            }
            return new String[]{dados.substring(0, separador).trim(), dados.substring(separador + 1)};
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private String[] autenticar(String email, String senha) {
        Connection conexao = ConnectionFactory.getConnection();
        if (conexao == null) {
            return null;
        }
        try {
            String[] contaPaciente = buscarConta(conexao,
                    "select CD_PACIENTE, NM_PACIENTE, DS_EMAIL, DS_HASH_SENHA from tb_paciente "
                            + "where upper(DS_EMAIL) = upper(?)", email, senha, "PACIENTE");
            if (contaPaciente != null) {
                return contaPaciente;
            }
            return buscarConta(conexao,
                    "select CD_MEMBRO_EQUIPE, NM_MEMBRO_EQUIPE, DS_EMAIL, DS_HASH_SENHA "
                            + "from tb_membro_equipe where upper(DS_EMAIL) = upper(?)",
                    email, senha, "EQUIPE");
        } catch (SQLException e) {
            System.out.println("Erro ao verificar as credenciais: " + e.getMessage());
            return null;
        } finally {
            ConnectionFactory.closeConnection();
        }
    }

    private String[] buscarConta(Connection conexao, String sql, String email, String senha, String perfil)
            throws SQLException {
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setString(1, email.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && verificarSenha(senha, rs.getString("DS_HASH_SENHA"))) {
                    return new String[]{String.valueOf(rs.getLong(1)), rs.getString(2), rs.getString(3), perfil};
                }
            }
        }
        return null;
    }

    private boolean verificarSenha(String senha, String hashArmazenado) {
        if (senha == null || hashArmazenado == null) {
            return false;
        }
        try {
            String[] partes = hashArmazenado.split("\\$", -1);
            if (partes.length != 4 || !"pbkdf2-sha256".equals(partes[0])) {
                return false;
            }
            int iteracoes = Integer.parseInt(partes[1]);
            if (iteracoes < 100000 || iteracoes > 1000000) {
                return false;
            }
            byte[] sal = Base64.getDecoder().decode(partes[2]);
            byte[] hashEsperado = Base64.getDecoder().decode(partes[3]);
            PBEKeySpec especificacao = new PBEKeySpec(senha.toCharArray(), sal, iteracoes, 256);
            try {
                byte[] hashInformado = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                        .generateSecret(especificacao).getEncoded();
                return MessageDigest.isEqual(hashEsperado, hashInformado);
            } finally {
                especificacao.clearPassword();
            }
        } catch (Exception e) {
            return false;
        }
    }

    private String perfilNecessario(String caminho, String metodo) {
        String caminhoNormalizado = caminho.toLowerCase();
        String metodoNormalizado = metodo.toUpperCase();

        if (caminhoNormalizado.startsWith("/atendimentos")) {
            return "PACIENTE";
        }
        if (caminhoNormalizado.startsWith("/membros-equipe")) {
            return "EQUIPE";
        }
        if (caminhoNormalizado.equals("/pacientes") || caminhoNormalizado.startsWith("/pacientes/")) {
            return "EQUIPE";
        }
        if (caminhoNormalizado.startsWith("/unidades")) {
            return metodoNormalizado.equals("GET") ? null : "EQUIPE";
        }
        if (caminhoNormalizado.startsWith("/horarios-atendimento")) {
            if (metodoNormalizado.equals("GET") && !caminhoNormalizado.contains("/por-membro/")) {
                return null;
            }
            return "EQUIPE";
        }
        if (caminhoNormalizado.startsWith("/agendamentos")) {
            if (caminhoNormalizado.endsWith("/paciente")
                    || caminhoNormalizado.endsWith("/resposta")
                    || caminhoNormalizado.endsWith("/cancelamento-paciente")) {
                return "PACIENTE";
            }
            return "EQUIPE";
        }
        if (caminhoNormalizado.startsWith("/solicitacoes")) {
            if (caminhoNormalizado.equals("/solicitacoes")) {
                return metodoNormalizado.equals("GET") ? "EQUIPE" : "PACIENTE";
            }
            if (caminhoNormalizado.endsWith("/tipos-dificuldade")) {
                return null;
            }
            if (caminhoNormalizado.endsWith("/analise") || caminhoNormalizado.endsWith("/encerramento")) {
                return "EQUIPE";
            }
            return "PACIENTE";
        }
        if (caminhoNormalizado.startsWith("/propostas")) {
            if (caminhoNormalizado.startsWith("/propostas/por-solicitacao/")
                    && !caminhoNormalizado.endsWith("/gerar")) {
                return "PACIENTE";
            }
            if (caminhoNormalizado.endsWith("/resposta")) {
                return "PACIENTE";
            }
            return "EQUIPE";
        }
        return null;
    }

    private void responderNaoAutorizado(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setHeader("WWW-Authenticate", "Basic realm=\"ConsultaViva\", charset=\"UTF-8\"");
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"mensagem\":\"Credenciais ausentes ou inválidas.\"}");
    }

    private void responderProibido(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"mensagem\":\"Este perfil não tem permissão para esta operação.\"}");
    }
}
