package br.com.fiap.resource;

import br.com.fiap.bo.HorarioAtendimentoBO;
import br.com.fiap.to.HorarioAtendimentoTO;
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
@RequestMapping("/horarios-atendimento")
public class HorarioAtendimentoResource {
    private HorarioAtendimentoBO horarioAtendimentoBO = new HorarioAtendimentoBO();

    @GetMapping
    public ResponseEntity<?> listarTodos() {
        List<HorarioAtendimentoTO> resultado = horarioAtendimentoBO.listarTodos();
        if (resultado != null) {
            return ResponseEntity.status(HttpStatus.OK).body(resultado);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Nenhum horário encontrado.");
        }
    }

    @GetMapping("/disponiveis")
    public ResponseEntity<?> listarDisponiveis() {
        List<HorarioAtendimentoTO> resultado = horarioAtendimentoBO.listarDisponiveis();
        if (resultado != null) {
            return ResponseEntity.status(HttpStatus.OK).body(resultado);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Nenhum horário disponível encontrado.");
        }
    }

    @GetMapping("/{idHorario}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long idHorario) {
        HorarioAtendimentoTO resultado = horarioAtendimentoBO.buscarPorId(idHorario);
        if (resultado == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Horário não encontrado.");
        }
        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/por-unidade/{idUnidade}")
    public ResponseEntity<?> listarPorUnidade(@PathVariable Long idUnidade) {
        List<HorarioAtendimentoTO> resultado = horarioAtendimentoBO.listarPorUnidade(idUnidade);
        if (resultado != null) {
            return ResponseEntity.status(HttpStatus.OK).body(resultado);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Nenhum horário encontrado para a unidade.");
        }
    }

    @GetMapping("/por-membro/{idMembroEquipe}")
    public ResponseEntity<?> listarPorMembroEquipe(@PathVariable Long idMembroEquipe) {
        List<HorarioAtendimentoTO> resultado = horarioAtendimentoBO.listarPorMembroEquipe(idMembroEquipe);
        if (resultado != null) {
            return ResponseEntity.status(HttpStatus.OK).body(resultado);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Nenhum horário encontrado para o membro.");
        }
    }

    @PostMapping
    public ResponseEntity<?> inserir(@RequestBody @Valid HorarioAtendimentoTO horario) {
        try {
            horario.setIdHorario(null);
            HorarioAtendimentoTO resultado = horarioAtendimentoBO.inserir(horario);
            if (resultado != null) {
                return ResponseEntity.status(HttpStatus.CREATED).body(resultado);
            } else {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Não foi possível cadastrar o horário. Verifique se há conflito de horários.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erro ao inserir o registro.");
        }
    }

    @PutMapping("/{idHorario}")
    public ResponseEntity<?> atualizar(@PathVariable Long idHorario, @RequestBody @Valid HorarioAtendimentoTO horario) {
        try {
            horario.setIdHorario(idHorario);
            HorarioAtendimentoTO resultado = horarioAtendimentoBO.atualizar(horario);
            if (resultado != null) {
                return ResponseEntity.status(HttpStatus.OK).body(resultado);
            } else if (horarioAtendimentoBO.buscarPorId(idHorario) != null) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("O horário não pode ser alterado porque está ocupado ou reservado.");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Horário não encontrado para atualização.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erro ao atualizar o registro.");
        }
    }

    @DeleteMapping("/{idHorario}")
    public ResponseEntity<String> excluir(@PathVariable Long idHorario) {
        try {
            if (horarioAtendimentoBO.excluir(idHorario)) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Horário excluído com sucesso.");
            } else if (horarioAtendimentoBO.buscarPorId(idHorario) != null) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Horário vinculado a agendamento ou proposta não pode ser excluído.");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Horário não encontrado.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erro ao excluir o registro.");
        }
    }
}
