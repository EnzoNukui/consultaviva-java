package br.com.fiap.resource;

import br.com.fiap.bo.PropostaAtendimentoBO;
import br.com.fiap.exception.AcessoNegadoException;
import br.com.fiap.to.PropostaAtendimentoTO;
import org.springframework.format.annotation.DateTimeFormat;
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

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/propostas")
public class PropostaAtendimentoResource {
    private PropostaAtendimentoBO propostaAtendimentoBO = new PropostaAtendimentoBO();

    @GetMapping
    public ResponseEntity<?> listarTodos() {
        List<PropostaAtendimentoTO> resultado = propostaAtendimentoBO.listarTodos();
        if (resultado != null) {
            return ResponseEntity.status(HttpStatus.OK).body(resultado);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Nenhuma proposta encontrada.");
        }
    }

    @GetMapping("/{idProposta}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long idProposta) {
        PropostaAtendimentoTO resultado = propostaAtendimentoBO.buscarPorId(idProposta);
        if (resultado != null) {
            return ResponseEntity.status(HttpStatus.OK).body(resultado);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Proposta não encontrada.");
        }
    }

    @GetMapping("/por-solicitacao/{idSolicitacao}")
    public ResponseEntity<?> listarPorSolicitacao(@PathVariable Long idSolicitacao,
                                                  @RequestAttribute("idPacienteAutenticado") Long idPacienteAutenticado) {
        try {
            List<PropostaAtendimentoTO> resultado = propostaAtendimentoBO.listarPorSolicitacao(
                    idSolicitacao, idPacienteAutenticado);
            if (resultado != null) {
                return ResponseEntity.status(HttpStatus.OK).body(resultado);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Nenhuma proposta encontrada.");
            }
        } catch (AcessoNegadoException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erro ao consultar as propostas.");
        }
    }

    @PostMapping("/por-solicitacao/{idSolicitacao}/gerar")
    public ResponseEntity<?> gerarPropostas(@PathVariable Long idSolicitacao,
                                            @RequestAttribute("idMembroEquipeAutenticado") Long idAnalista) {
        try {
            List<PropostaAtendimentoTO> resultado = propostaAtendimentoBO.gerarPropostas(idSolicitacao, idAnalista);
            if (resultado != null) {
                return ResponseEntity.status(HttpStatus.OK).body(resultado);
            } else {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Não foi possível gerar propostas para esta solicitação.");
            }
        } catch (AcessoNegadoException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erro ao gerar as propostas.");
        }
    }

    @PutMapping("/{idProposta}")
    public ResponseEntity<?> atualizar(@PathVariable Long idProposta,
                                       @RequestBody PropostaAtendimentoTO proposta,
                                       @RequestAttribute("idMembroEquipeAutenticado") Long idAnalista) {
        try {
            proposta.setIdProposta(idProposta);
            PropostaAtendimentoTO resultado = propostaAtendimentoBO.atualizar(proposta, idAnalista);
            if (resultado != null) {
                return ResponseEntity.status(HttpStatus.OK).body(resultado);
            } else if (propostaAtendimentoBO.buscarPorId(idProposta) != null) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("A proposta não pode ser atualizada no estado atual.");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Proposta não encontrada para atualização.");
            }
        } catch (AcessoNegadoException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erro ao atualizar o registro.");
        }
    }

    @PostMapping("/{idProposta}/autorizacao")
    public ResponseEntity<?> autorizar(@PathVariable Long idProposta,
                                       @RequestParam boolean autorizada,
                                       @RequestAttribute("idMembroEquipeAutenticado") Long idAutorizador) {
        try {
            PropostaAtendimentoTO resultado = propostaAtendimentoBO.autorizar(
                    idProposta, idAutorizador, autorizada);
            if (resultado != null) {
                return ResponseEntity.status(HttpStatus.OK).body(resultado);
            } else if (propostaAtendimentoBO.buscarPorId(idProposta) != null) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Não foi possível registrar a autorização da proposta.");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Proposta não encontrada.");
            }
        } catch (AcessoNegadoException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erro ao registrar a autorização.");
        }
    }

    @PostMapping("/{idProposta}/oferta")
    public ResponseEntity<?> ofertar(@PathVariable Long idProposta,
                                     @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                     LocalDateTime dataLimiteAceite,
                                     @RequestAttribute("idMembroEquipeAutenticado") Long idAnalista) {
        try {
            PropostaAtendimentoTO resultado = propostaAtendimentoBO.ofertar(
                    idProposta, idAnalista, dataLimiteAceite);
            if (resultado != null) {
                return ResponseEntity.status(HttpStatus.OK).body(resultado);
            } else if (propostaAtendimentoBO.buscarPorId(idProposta) != null) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("A proposta não pode ser ofertada.");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Proposta não encontrada.");
            }
        } catch (AcessoNegadoException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erro ao ofertar a proposta.");
        }
    }

    @PostMapping("/{idProposta}/resposta")
    public ResponseEntity<?> registrarResposta(@PathVariable Long idProposta,
                                               @RequestParam boolean aceita,
                                               @RequestAttribute("idPacienteAutenticado") Long idPacienteAutenticado) {
        try {
            PropostaAtendimentoTO resultado = propostaAtendimentoBO.registrarResposta(
                    idProposta, idPacienteAutenticado, aceita);
            if (resultado != null) {
                return ResponseEntity.status(HttpStatus.OK).body(resultado);
            } else if (propostaAtendimentoBO.buscarPorId(idProposta) != null) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Não foi possível registrar a resposta para esta proposta.");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Proposta não encontrada.");
            }
        } catch (AcessoNegadoException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erro ao registrar a resposta.");
        }
    }

    @PostMapping("/{idProposta}/confirmacao")
    public ResponseEntity<?> confirmar(@PathVariable Long idProposta,
                                       @RequestAttribute("idMembroEquipeAutenticado") Long idMembroEquipe) {
        try {
            PropostaAtendimentoTO resultado = propostaAtendimentoBO.confirmar(idProposta, idMembroEquipe);
            if (resultado != null) {
                return ResponseEntity.status(HttpStatus.OK).body(resultado);
            } else if (propostaAtendimentoBO.buscarPorId(idProposta) != null) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("A proposta não pode ser confirmada nas condições atuais.");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Proposta não encontrada.");
            }
        } catch (AcessoNegadoException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erro ao confirmar a proposta.");
        }
    }

    @PostMapping("/{idProposta}/expiracao")
    public ResponseEntity<?> expirar(@PathVariable Long idProposta,
                                     @RequestAttribute("idMembroEquipeAutenticado") Long idMembroEquipe) {
        try {
            PropostaAtendimentoTO resultado = propostaAtendimentoBO.expirar(idProposta, idMembroEquipe);
            if (resultado != null) {
                return ResponseEntity.status(HttpStatus.OK).body(resultado);
            } else if (propostaAtendimentoBO.buscarPorId(idProposta) != null) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("A proposta não pode ser expirada.");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Proposta não encontrada.");
            }
        } catch (AcessoNegadoException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erro ao expirar a proposta.");
        }
    }

    @DeleteMapping("/{idProposta}")
    public ResponseEntity<String> excluir(@PathVariable Long idProposta,
                                          @RequestAttribute("idMembroEquipeAutenticado") Long idMembroEquipe) {
        try {
            if (propostaAtendimentoBO.excluir(idProposta, idMembroEquipe)) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Proposta cancelada com sucesso.");
            } else if (propostaAtendimentoBO.buscarPorId(idProposta) != null) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("A proposta não pode ser excluída.");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Proposta não encontrada.");
            }
        } catch (AcessoNegadoException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erro ao excluir a proposta.");
        }
    }
}
