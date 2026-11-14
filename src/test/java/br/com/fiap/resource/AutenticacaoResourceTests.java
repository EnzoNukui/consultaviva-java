package br.com.fiap.resource;

import br.com.fiap.bo.SessaoBO;
import br.com.fiap.dao.ConnectionFactory;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Base64;

import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AutenticacaoResourceTests {
    @Test
    void loginRecuperaNomeMantemPermissoesERevogaSessaoAoSair() throws Exception {
        SessaoBO sessoes = new SessaoBO();
        var mvc = MockMvcBuilders.standaloneSetup(new AutenticacaoResource(sessoes))
                .setControllerAdvice(new ApiExceptionHandler())
                .addFilters(new AutenticacaoFilter(sessoes)).build();

        mvc.perform(get("/autenticacao/me")).andExpect(status().isUnauthorized());

        Connection conexao = mock(Connection.class);
        PreparedStatement consulta = mock(PreparedStatement.class);
        ResultSet paciente = mock(ResultSet.class);
        when(conexao.prepareStatement(startsWith("select CD_PACIENTE"))).thenReturn(consulta);
        when(consulta.executeQuery()).thenReturn(paciente);
        when(paciente.next()).thenReturn(true);
        when(paciente.getLong(1)).thenReturn(42L);
        when(paciente.getString(2)).thenReturn("  Lúcia   Souza  ");
        when(paciente.getString("DS_HASH_SENHA")).thenReturn(hashDeTeste());

        String emailESenha = "paciente@example.com:senha-de-teste";
        String basic = "Basic " + Base64.getEncoder().encodeToString(emailESenha.getBytes(StandardCharsets.UTF_8));
        String resposta;
        try (var fabrica = mockStatic(ConnectionFactory.class)) {
            fabrica.when(ConnectionFactory::getConnection).thenReturn(conexao);
            resposta = mvc.perform(post("/autenticacao/sessao").header(HttpHeaders.AUTHORIZATION, basic))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.primeiroNome").value("Lúcia"))
                    .andExpect(jsonPath("$.perfil").value("PACIENTE"))
                    .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        }

        String bearer = "Bearer " + JsonPath.<String>read(resposta, "$.token");
        mvc.perform(get("/autenticacao/me").header(HttpHeaders.AUTHORIZATION, bearer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.primeiroNome").value("Lúcia"))
                .andExpect(jsonPath("$.token").doesNotExist());
        mvc.perform(get("/membros-equipe").header(HttpHeaders.AUTHORIZATION, bearer))
                .andExpect(status().isForbidden());
        mvc.perform(post("/autenticacao/sessao").header(HttpHeaders.AUTHORIZATION, bearer))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/autenticacao/sessao").header(HttpHeaders.AUTHORIZATION, bearer))
                .andExpect(status().isNoContent());
        mvc.perform(get("/autenticacao/me").header(HttpHeaders.AUTHORIZATION, bearer))
                .andExpect(status().isUnauthorized());
    }

    private String hashDeTeste() throws Exception {
        byte[] sal = new byte[16];
        var especificacao = new PBEKeySpec("senha-de-teste".toCharArray(), sal, 100000, 256);
        byte[] hash = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                .generateSecret(especificacao).getEncoded();
        var encoder = Base64.getEncoder();
        return "pbkdf2-sha256$100000$" + encoder.encodeToString(sal) + "$" + encoder.encodeToString(hash);
    }
}
