package br.com.fiap.bo;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SessaoBO {
    private static final Duration DURACAO = Duration.ofHours(8);
    private final SecureRandom aleatorio = new SecureRandom();
    private final Map<String, Sessao> sessoes = new ConcurrentHashMap<>();

    public String iniciar(Conta conta) {
        Instant agora = Instant.now();
        sessoes.values().removeIf(sessao -> !sessao.expiraEm().isAfter(agora));
        byte[] bytes = new byte[32];
        aleatorio.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        sessoes.put(token, new Sessao(conta, agora.plus(DURACAO)));
        return token;
    }

    public Conta consultar(String token) {
        Sessao sessao = sessoes.get(token);
        if (sessao == null) return null;
        if (!sessao.expiraEm().isAfter(Instant.now())) {
            sessoes.remove(token, sessao);
            return null;
        }
        return sessao.conta();
    }

    public void encerrar(String token) {
        sessoes.remove(token);
    }

    public record Conta(Long id, String nome, String email, String perfil) {}

    private record Sessao(Conta conta, Instant expiraEm) {}
}
