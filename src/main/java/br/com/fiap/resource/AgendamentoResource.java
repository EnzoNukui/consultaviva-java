package br.com.fiap.resource;

import br.com.fiap.bo.AgendamentoBO;
import br.com.fiap.bo.PacienteBO;
import br.com.fiap.exception.AcessoNegadoException;
import br.com.fiap.to.AgendamentoTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/agendamentos")
public class AgendamentoResource {
    private AgendamentoBO agendamentoBO = new AgendamentoBO();
    private PacienteBO pacienteBO = new PacienteBO();

    @GetMapping
    public ResponseEntity<?> listarTodos() {
        List<AgendamentoTO> resultado = agendamentoBO.listarTodos();
        if (resultado != null) {
            return ResponseEntity.status(HttpStatus.OK).body(resultado);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Nenhum agendamento encontrado.");
        }
    }

    @GetMapping("/paciente")
    public ResponseEntity<List<AgendamentoTO>> listarDoPaciente(
            @RequestAttribute("idPacienteAutenticado") Long idPacienteAutenticado) {
        List<AgendamentoTO> resultado = pacienteBO.listarAgendamentos(idPacienteAutenticado, idPacienteAutenticado);
        return ResponseEntity.status(HttpStatus.OK).body(resultado);
    }

    @GetMapping("/{idAgendamento}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long idAgendamento) {
        AgendamentoTO resultado = agendamentoBO.buscarPorId(idAgendamento);
        if (resultado != null) {
            return ResponseEntity.status(HttpStatus.OK).body(resultado);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Agendamento não encontrado.");
        }
    }

    @GetMapping("/{idAgendamento}/paciente")
    public ResponseEntity<?> buscarDoPaciente(@PathVariable Long idAgendamento,
                                              @RequestAttribute("idPacienteAutenticado") Long idPacienteAutenticado) {
        try {
            AgendamentoTO resultado = agendamentoBO.buscarDoPaciente(idAgendamento, idPacienteAutenticado);
            if (resultado != null) {
                return ResponseEntity.status(HttpStatus.OK).body(resultado);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Agendamento não encontrado.");
            }
        } catch (AcessoNegadoException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erro ao buscar o agendamento.");
        }
    }

    @PostMapping
    public ResponseEntity<?> inserir(@RequestBody @Valid AgendamentoTO agendamento,
                                     @RequestAttribute("idMembroEquipeAutenticado") Long idMembroEquipe) {
        try {
            agendamento.setIdAgendamento(null);
            AgendamentoTO resultado = agendamentoBO.inserir(agendamento, idMembroEquipe);
            if (resultado != null) {
                return ResponseEntity.status(HttpStatus.CREATED).body(resultado);
            } else {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Não foi possível cadastrar o agendamento. Verifique o paciente e o horário.");
            }
        } catch (AcessoNegadoException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erro ao inserir o registro.");
        }
    }

    @PutMapping("/{idAgendamento}")
    public ResponseEntity<?> atualizar(@PathVariable Long idAgendamento,
                                       @RequestBody AgendamentoTO agendamento,
                                       @RequestAttribute("idMembroEquipeAutenticado") Long idMembroEquipe) {
        try {
            agendamento.setIdAgendamento(idAgendamento);
            AgendamentoTO resultado = agendamentoBO.atualizar(agendamento, idMembroEquipe);
            if (resultado != null) {
                return ResponseEntity.status(HttpStatus.OK).body(resultado);
            } else if (agendamentoBO.buscarPorId(idAgendamento) != null) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("O agendamento não pode ser atualizado no estado atual ou no horário informado.");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Agendamento não encontrado para atualização.");
            }
        } catch (AcessoNegadoException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erro ao atualizar o registro.");
        }
    }

    @DeleteMapping("/{idAgendamento}")
    public ResponseEntity<String> excluir(@PathVariable Long idAgendamento,
                                          @RequestAttribute("idMembroEquipeAutenticado") Long idMembroEquipe) {
        try {
            if (agendamentoBO.excluir(idAgendamento, idMembroEquipe)) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Agendamento cancelado com sucesso.");
            } else if (agendamentoBO.buscarPorId(idAgendamento) != null) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("O agendamento não pode ser cancelado no estado atual.");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Agendamento não encontrado.");
            }
        } catch (AcessoNegadoException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erro ao cancelar o agendamento.");
        }
    }

    @PostMapping("/{idAgendamento}/resposta")
    public ResponseEntity<?> registrarResposta(@PathVariable Long idAgendamento,
                                              @RequestParam String resposta,
                                              @RequestAttribute("idPacienteAutenticado") Long idPacienteAutenticado) {
        try {
            AgendamentoTO resultado = agendamentoBO.registrarResposta(idAgendamento, idPacienteAutenticado, resposta);
            if (resultado != null) {
                return ResponseEntity.status(HttpStatus.OK).body(resultado);
            } else if (agendamentoBO.buscarPorId(idAgendamento) != null) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Não foi possível registrar a resposta para este agendamento.");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Agendamento não encontrado.");
            }
        } catch (AcessoNegadoException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erro ao registrar a resposta.");
        }
    }

    @PostMapping("/{idAgendamento}/cancelamento")
    public ResponseEntity<?> cancelar(@PathVariable Long idAgendamento,
                                      @RequestAttribute("idMembroEquipeAutenticado") Long idMembroEquipe) {
        try {
            AgendamentoTO resultado = agendamentoBO.cancelar(idAgendamento, idMembroEquipe);
            if (resultado != null) {
                return ResponseEntity.status(HttpStatus.OK).body(resultado);
            } else if (agendamentoBO.buscarPorId(idAgendamento) != null) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("O agendamento não pode ser cancelado.");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Agendamento não encontrado.");
            }
        } catch (AcessoNegadoException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erro ao cancelar o agendamento.");
        }
    }

    @PostMapping("/{idAgendamento}/cancelamento-paciente")
    public ResponseEntity<?> cancelarPeloPaciente(@PathVariable Long idAgendamento,
                                                  @RequestAttribute("idPacienteAutenticado") Long idPacienteAutenticado) {
        try {
            AgendamentoTO resultado = agendamentoBO.cancelarPeloPaciente(idAgendamento, idPacienteAutenticado);
            if (resultado != null) {
                return ResponseEntity.status(HttpStatus.OK).body(resultado);
            } else if (agendamentoBO.buscarPorId(idAgendamento) != null) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("O agendamento não pode ser cancelado.");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Agendamento não encontrado.");
            }
        } catch (AcessoNegadoException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erro ao cancelar o agendamento.");
        }
    }

    @PostMapping("/{idAgendamento}/realizacao")
    public ResponseEntity<?> registrarRealizacao(@PathVariable Long idAgendamento,
                                                 @RequestAttribute("idMembroEquipeAutenticado") Long idMembroEquipe) {
        return registrarResultado(idAgendamento, idMembroEquipe, true);
    }

    @PostMapping("/{idAgendamento}/falta")
    public ResponseEntity<?> registrarFalta(@PathVariable Long idAgendamento,
                                            @RequestAttribute("idMembroEquipeAutenticado") Long idMembroEquipe) {
        return registrarResultado(idAgendamento, idMembroEquipe, false);
    }

    private ResponseEntity<?> registrarResultado(Long idAgendamento, Long idMembroEquipe, boolean realizado) {
        try {
            AgendamentoTO resultado = realizado
                    ? agendamentoBO.registrarRealizacao(idAgendamento, idMembroEquipe)
                    : agendamentoBO.registrarFalta(idAgendamento, idMembroEquipe);
            if (resultado != null) {
                return ResponseEntity.status(HttpStatus.OK).body(resultado);
            } else if (agendamentoBO.buscarPorId(idAgendamento) != null) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Não foi possível registrar o resultado para este agendamento.");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Agendamento não encontrado.");
            }
        } catch (AcessoNegadoException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erro ao registrar o resultado.");
        }
    }
}
