package br.com.fiap.resource;

import br.com.fiap.bo.MembroEquipeBO;
import br.com.fiap.to.MembroEquipeTO;
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
@RequestMapping("/membros-equipe")
public class MembroEquipeResource {
    private MembroEquipeBO membroEquipeBO = new MembroEquipeBO();

    @GetMapping
    public ResponseEntity<?> listarTodos() {
        List<MembroEquipeTO> resultado = membroEquipeBO.listarTodos();
        if (resultado != null) {
            return ResponseEntity.status(HttpStatus.OK).body(resultado);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Nenhum membro da equipe encontrado.");
        }
    }

    @GetMapping("/{idMembroEquipe}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long idMembroEquipe) {
        MembroEquipeTO resultado = membroEquipeBO.buscarPorId(idMembroEquipe);
        if (resultado == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Membro da equipe não encontrado.");
        }
        return ResponseEntity.ok(resultado);
    }

    @PostMapping
    public ResponseEntity<?> inserir(@RequestBody @Valid MembroEquipeTO membroEquipe) {
        try {
            membroEquipe.setIdMembroEquipe(null);
            MembroEquipeTO resultado = membroEquipeBO.inserir(membroEquipe);
            if (resultado != null) {
                return ResponseEntity.status(HttpStatus.CREATED).body(resultado);
            } else {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Não foi possível cadastrar o membro da equipe. Verifique se os dados já estão cadastrados.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erro ao inserir o registro.");
        }
    }

    @PutMapping("/{idMembroEquipe}")
    public ResponseEntity<?> atualizar(@PathVariable Long idMembroEquipe,
                                       @RequestBody @Valid MembroEquipeTO membroEquipe) {
        try {
            membroEquipe.setIdMembroEquipe(idMembroEquipe);
            MembroEquipeTO resultado = membroEquipeBO.atualizar(membroEquipe);
            if (resultado != null) {
                return ResponseEntity.status(HttpStatus.OK).body(resultado);
            } else if (membroEquipeBO.buscarPorId(idMembroEquipe) != null) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Não foi possível atualizar o membro da equipe devido a um conflito nos dados.");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Membro da equipe não encontrado para atualização.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erro ao atualizar o registro.");
        }
    }

    @DeleteMapping("/{idMembroEquipe}")
    public ResponseEntity<String> excluir(@PathVariable Long idMembroEquipe) {
        try {
            if (membroEquipeBO.excluir(idMembroEquipe)) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Membro da equipe excluído com sucesso.");
            } else if (membroEquipeBO.buscarPorId(idMembroEquipe) != null) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Membro da equipe vinculado a outros registros não pode ser excluído.");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Membro da equipe não encontrado.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erro ao excluir o registro.");
        }
    }
}
