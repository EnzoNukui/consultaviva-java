package br.com.fiap.resource;

import br.com.fiap.bo.PacienteBO;
import br.com.fiap.to.CadastroPacienteTO;
import br.com.fiap.to.PacienteTO;
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
@RequestMapping("/pacientes")
public class PacienteResource {
    private PacienteBO pacienteBO = new PacienteBO();

    @PostMapping("/cadastro")
    public ResponseEntity<?> cadastrar(@RequestBody @Valid CadastroPacienteTO cadastro) {
        try {
            PacienteTO resultado = pacienteBO.cadastrar(cadastro);
            if (resultado != null) {
                return ResponseEntity.status(HttpStatus.CREATED).body(resultado);
            }
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Não foi possível cadastrar a conta. Verifique se o e-mail já está em uso.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erro ao cadastrar a conta do paciente.");
        }
    }

    @GetMapping
    public ResponseEntity<?> listarTodos() {
        List<PacienteTO> resultado = pacienteBO.listarTodos();
        if (resultado != null) {
            return ResponseEntity.status(HttpStatus.OK).body(resultado);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Nenhum paciente encontrado.");
        }
    }

    @GetMapping("/{idPaciente}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long idPaciente) {
        PacienteTO resultado = pacienteBO.buscarPorId(idPaciente);
        if (resultado == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Paciente não encontrado.");
        }
        return ResponseEntity.ok(resultado);
    }

    @PostMapping
    public ResponseEntity<?> inserir(@RequestBody @Valid PacienteTO paciente) {
        try {
            paciente.setIdPaciente(null);
            PacienteTO resultado = pacienteBO.inserir(paciente);
            if (resultado != null) {
                return ResponseEntity.status(HttpStatus.CREATED).body(resultado);
            } else {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Não foi possível cadastrar o paciente. Verifique se os dados já estão cadastrados.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erro ao inserir o registro.");
        }
    }

    @PutMapping("/{idPaciente}")
    public ResponseEntity<?> atualizar(@PathVariable Long idPaciente, @RequestBody @Valid PacienteTO paciente) {
        try {
            paciente.setIdPaciente(idPaciente);
            PacienteTO resultado = pacienteBO.atualizar(paciente);
            if (resultado != null) {
                return ResponseEntity.status(HttpStatus.OK).body(resultado);
            } else if (pacienteBO.buscarPorId(idPaciente) != null) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Não foi possível atualizar o paciente devido a um conflito nos dados.");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Paciente não encontrado para atualização.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erro ao atualizar o registro.");
        }
    }

    @DeleteMapping("/{idPaciente}")
    public ResponseEntity<String> excluir(@PathVariable Long idPaciente) {
        try {
            if (pacienteBO.excluir(idPaciente)) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Paciente excluído com sucesso.");
            } else if (pacienteBO.buscarPorId(idPaciente) != null) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Paciente vinculado a outros registros não pode ser excluído.");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Paciente não encontrado.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erro ao excluir o registro.");
        }
    }
}
