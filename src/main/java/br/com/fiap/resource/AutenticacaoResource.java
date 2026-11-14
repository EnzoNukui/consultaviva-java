package br.com.fiap.resource;

import br.com.fiap.bo.SessaoBO;
import br.com.fiap.bo.SessaoBO.Conta;
import br.com.fiap.exception.AcessoNegadoException;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/autenticacao")
public class AutenticacaoResource {
    private final SessaoBO sessoes;

    public AutenticacaoResource(SessaoBO sessoes) {
        this.sessoes = sessoes;
    }

    @GetMapping("/me")
    public ResponseEntity<ContaAutenticada> consultarConta(@RequestAttribute("contaAutenticada") Conta conta) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .body(new ContaAutenticada(conta.perfil(), primeiroNome(conta)));
    }

    @PostMapping("/sessao")
    public ResponseEntity<SessaoAutenticada> iniciarSessao(
            @RequestAttribute("contaAutenticada") Conta conta,
            @RequestAttribute(value = "tokenSessaoAutenticada", required = false) String token) {
        if (token != null) throw new AcessoNegadoException("Informe o e-mail e a senha para iniciar uma sessão.");
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .body(new SessaoAutenticada(conta.perfil(), primeiroNome(conta), sessoes.iniciar(conta)));
    }

    @DeleteMapping("/sessao")
    public ResponseEntity<Void> encerrarSessao(@RequestAttribute("tokenSessaoAutenticada") String token) {
        sessoes.encerrar(token);
        return ResponseEntity.noContent().build();
    }

    private String primeiroNome(Conta conta) {
        return conta.nome() == null ? null : conta.nome().strip().split("\\s+", 2)[0];
    }

    public record ContaAutenticada(String perfil, String primeiroNome) {}

    public record SessaoAutenticada(String perfil, String primeiroNome, String token) {}
}
