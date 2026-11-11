package br.com.fiap.resource;

import br.com.fiap.bo.UnidadeBO;
import br.com.fiap.to.HorarioAtendimentoTO;
import br.com.fiap.to.UnidadeTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/unidades")
public class UnidadeResource {
    private UnidadeBO unidadeBO = new UnidadeBO();

    @GetMapping
    public ResponseEntity<?> listarTodos() {
        List<UnidadeTO> resultado = unidadeBO.listarTodos();
        if (resultado != null) {
            return ResponseEntity.status(HttpStatus.OK).body(resultado);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Nenhuma unidade encontrada.");
        }
    }

    @GetMapping("/{idUnidade}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long idUnidade) {
        UnidadeTO resultado = unidadeBO.buscarPorId(idUnidade);
        if (resultado == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Unidade não encontrada.");
        }
        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/{idUnidade}/horarios")
    public ResponseEntity<?> listarHorarios(@PathVariable Long idUnidade) {
        List<HorarioAtendimentoTO> resultado = unidadeBO.listarHorarios(idUnidade);
        if (resultado != null) {
            return ResponseEntity.status(HttpStatus.OK).body(resultado);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Nenhum horário encontrado para a unidade.");
        }
    }

    @PostMapping
    public ResponseEntity<?> inserir(@RequestBody @Valid UnidadeTO unidade) {
        try {
            unidade.setIdUnidade(null);
            UnidadeTO resultado = unidadeBO.inserir(unidade);
            if (resultado != null) {
                return ResponseEntity.status(HttpStatus.CREATED).body(resultado);
            } else {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Não foi possível cadastrar a unidade. Verifique se os dados já estão cadastrados.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erro ao inserir o registro.");
        }
    }

    @PutMapping("/{idUnidade}")
    public ResponseEntity<?> atualizar(@PathVariable Long idUnidade, @RequestBody @Valid UnidadeTO unidade) {
        try {
            unidade.setIdUnidade(idUnidade);
            UnidadeTO resultado = unidadeBO.atualizar(unidade);
            if (resultado != null) {
                return ResponseEntity.status(HttpStatus.OK).body(resultado);
            } else if (unidadeBO.buscarPorId(idUnidade) != null) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Não foi possível atualizar a unidade devido a um conflito nos dados.");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Unidade não encontrada para atualização.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erro ao atualizar o registro.");
        }
    }

    @DeleteMapping("/{idUnidade}")
    public ResponseEntity<String> excluir(@PathVariable Long idUnidade) {
        try {
            if (unidadeBO.excluir(idUnidade)) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Unidade excluída com sucesso.");
            } else if (unidadeBO.buscarPorId(idUnidade) != null) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Unidade vinculada a horários e atendimentos não pode ser excluída.");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Unidade não encontrada.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erro ao excluir o registro.");
        }
    }
}
