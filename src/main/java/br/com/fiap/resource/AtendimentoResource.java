package br.com.fiap.resource;

import br.com.fiap.bo.AtendimentoBO;
import br.com.fiap.exception.AcessoNegadoException;
import br.com.fiap.to.HistoricoEventoTO;
import br.com.fiap.to.PlanoComparecimentoTO;
import br.com.fiap.to.PreparoVirtualTO;
import br.com.fiap.to.SincronizacaoAgendaTO;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/atendimentos")
public class AtendimentoResource {
    private AtendimentoBO atendimentoBO = new AtendimentoBO();

    @GetMapping("/agendamentos/{idAgendamento}/planos")
    public ResponseEntity<?> listarPlanos(@PathVariable Long idAgendamento,
                                          @RequestAttribute("idPacienteAutenticado") Long idPacienteAutenticado) {
        try {
            List<PlanoComparecimentoTO> resultado = atendimentoBO.listarPlanos(idAgendamento, idPacienteAutenticado);
            if (resultado != null) {
                return ResponseEntity.status(HttpStatus.OK).body(resultado);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Nenhum plano de comparecimento encontrado.");
            }
        } catch (AcessoNegadoException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erro ao consultar os planos.");
        }
    }

    @GetMapping("/planos/{idPlano}")
    public ResponseEntity<?> buscarPlano(@PathVariable Long idPlano,
                                         @RequestAttribute("idPacienteAutenticado") Long idPacienteAutenticado) {
        try {
            PlanoComparecimentoTO resultado = atendimentoBO.buscarPlano(idPlano, idPacienteAutenticado);
            if (resultado != null) {
                return ResponseEntity.status(HttpStatus.OK).body(resultado);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Plano de comparecimento não encontrado.");
            }
        } catch (AcessoNegadoException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erro ao buscar o plano.");
        }
    }

    @PostMapping("/planos")
    public ResponseEntity<?> inserirPlano(@RequestBody @Valid PlanoComparecimentoTO plano,
                                          @RequestAttribute("idPacienteAutenticado") Long idPacienteAutenticado) {
        try {
            plano.setIdPlano(null);
            PlanoComparecimentoTO resultado = atendimentoBO.inserirPlano(plano, idPacienteAutenticado);
            if (resultado != null) {
                return ResponseEntity.status(HttpStatus.CREATED).body(resultado);
            } else {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Não foi possível registrar o plano para este agendamento.");
            }
        } catch (AcessoNegadoException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erro ao inserir o plano.");
        }
    }

    @PutMapping("/planos/{idPlano}")
    public ResponseEntity<?> atualizarPlano(@PathVariable Long idPlano,
                                            @RequestBody PlanoComparecimentoTO plano,
                                            @RequestAttribute("idPacienteAutenticado") Long idPacienteAutenticado) {
        try {
            plano.setIdPlano(idPlano);
            PlanoComparecimentoTO resultado = atendimentoBO.atualizarPlano(plano, idPacienteAutenticado);
            if (resultado != null) {
                return ResponseEntity.status(HttpStatus.OK).body(resultado);
            } else if (atendimentoBO.buscarPlano(idPlano, idPacienteAutenticado) != null) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("O plano não pode ser atualizado para o agendamento atual.");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Plano não encontrado para atualização.");
            }
        } catch (AcessoNegadoException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erro ao atualizar o plano.");
        }
    }

    @DeleteMapping("/planos/{idPlano}")
    public ResponseEntity<String> excluirPlano(@PathVariable Long idPlano,
                                               @RequestAttribute("idPacienteAutenticado") Long idPacienteAutenticado) {
        try {
            if (atendimentoBO.excluirPlano(idPlano, idPacienteAutenticado)) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Plano excluído com sucesso.");
            } else if (atendimentoBO.buscarPlano(idPlano, idPacienteAutenticado) != null) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("O plano não pode ser excluído.");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Plano não encontrado.");
            }
        } catch (AcessoNegadoException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erro ao excluir o plano.");
        }
    }

    @GetMapping("/agendamentos/{idAgendamento}/preparos")
    public ResponseEntity<?> listarPreparos(@PathVariable Long idAgendamento,
                                            @RequestAttribute("idPacienteAutenticado") Long idPacienteAutenticado) {
        try {
            List<PreparoVirtualTO> resultado = atendimentoBO.listarPreparos(idAgendamento, idPacienteAutenticado);
            if (resultado != null) {
                return ResponseEntity.status(HttpStatus.OK).body(resultado);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Nenhum preparo virtual encontrado.");
            }
        } catch (AcessoNegadoException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erro ao consultar os preparos.");
        }
    }

    @GetMapping("/preparos/{idPreparo}")
    public ResponseEntity<?> buscarPreparo(@PathVariable Long idPreparo,
                                           @RequestAttribute("idPacienteAutenticado") Long idPacienteAutenticado) {
        try {
            PreparoVirtualTO resultado = atendimentoBO.buscarPreparo(idPreparo, idPacienteAutenticado);
            if (resultado != null) {
                return ResponseEntity.status(HttpStatus.OK).body(resultado);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Preparo virtual não encontrado.");
            }
        } catch (AcessoNegadoException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erro ao buscar o preparo.");
        }
    }

    @PostMapping("/preparos")
    public ResponseEntity<?> registrarPreparoVirtual(@RequestBody @Valid PreparoVirtualTO preparo,
                                                     @RequestAttribute("idPacienteAutenticado") Long idPacienteAutenticado) {
        try {
            preparo.setIdPreparo(null);
            PreparoVirtualTO resultado = atendimentoBO.registrarPreparoVirtual(preparo, idPacienteAutenticado);
            if (resultado != null) {
                return ResponseEntity.status(HttpStatus.CREATED).body(resultado);
            } else {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Não foi possível registrar o preparo virtual para este agendamento.");
            }
        } catch (AcessoNegadoException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erro ao registrar o preparo virtual.");
        }
    }

    @PutMapping("/preparos/{idPreparo}")
    public ResponseEntity<?> atualizarPreparo(@PathVariable Long idPreparo,
                                              @RequestBody PreparoVirtualTO preparo,
                                              @RequestAttribute("idPacienteAutenticado") Long idPacienteAutenticado) {
        try {
            preparo.setIdPreparo(idPreparo);
            PreparoVirtualTO resultado = atendimentoBO.atualizarPreparo(preparo, idPacienteAutenticado);
            if (resultado != null) {
                return ResponseEntity.status(HttpStatus.OK).body(resultado);
            } else if (atendimentoBO.buscarPreparo(idPreparo, idPacienteAutenticado) != null) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("O preparo virtual não pode ser atualizado nas condições atuais.");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Preparo virtual não encontrado.");
            }
        } catch (AcessoNegadoException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erro ao atualizar o preparo virtual.");
        }
    }

    @DeleteMapping("/preparos/{idPreparo}")
    public ResponseEntity<String> excluirPreparo(@PathVariable Long idPreparo,
                                                 @RequestAttribute("idPacienteAutenticado") Long idPacienteAutenticado) {
        try {
            if (atendimentoBO.excluirPreparo(idPreparo, idPacienteAutenticado)) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Preparo virtual excluído com sucesso.");
            } else if (atendimentoBO.buscarPreparo(idPreparo, idPacienteAutenticado) != null) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("O preparo virtual não pode ser excluído.");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Preparo virtual não encontrado.");
            }
        } catch (AcessoNegadoException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erro ao excluir o preparo virtual.");
        }
    }

    @GetMapping("/agendamentos/{idAgendamento}/historico")
    public ResponseEntity<?> listarHistorico(@PathVariable Long idAgendamento,
                                             @RequestAttribute("idPacienteAutenticado") Long idPacienteAutenticado) {
        try {
            List<HistoricoEventoTO> resultado = atendimentoBO.listarHistorico(idAgendamento, idPacienteAutenticado);
            if (resultado != null) {
                return ResponseEntity.status(HttpStatus.OK).body(resultado);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Nenhum evento de histórico encontrado.");
            }
        } catch (AcessoNegadoException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erro ao consultar o histórico.");
        }
    }

    @GetMapping("/agendamentos/{idAgendamento}/sincronizacoes")
    public ResponseEntity<?> listarSincronizacoes(@PathVariable Long idAgendamento,
                                                  @RequestAttribute("idPacienteAutenticado") Long idPacienteAutenticado) {
        try {
            List<SincronizacaoAgendaTO> resultado = atendimentoBO.listarSincronizacoes(
                    idAgendamento, idPacienteAutenticado);
            if (resultado != null) {
                return ResponseEntity.status(HttpStatus.OK).body(resultado);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Nenhuma sincronização encontrada.");
            }
        } catch (AcessoNegadoException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erro ao consultar as sincronizações.");
        }
    }
}
