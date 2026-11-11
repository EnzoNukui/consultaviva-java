package br.com.fiap.resource;

import br.com.fiap.bo.SolicitacaoAjudaBO;
import br.com.fiap.exception.AcessoNegadoException;
import br.com.fiap.exception.RegraNegocioException;
import br.com.fiap.to.CadastroSolicitacaoAjudaTO;
import br.com.fiap.to.DisponibilidadeTO;
import br.com.fiap.to.SolicitacaoAjudaTO;
import br.com.fiap.to.SolicitacaoDificuldadeTO;
import br.com.fiap.to.TipoDificuldadeTO;
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
@RequestMapping("/solicitacoes")
public class SolicitacaoAjudaResource {
    private SolicitacaoAjudaBO solicitacaoAjudaBO = new SolicitacaoAjudaBO();

    @GetMapping
    public ResponseEntity<?> listarTodos() {
        List<SolicitacaoAjudaTO> resultado = solicitacaoAjudaBO.listarTodos();
        if (resultado != null) {
            return ResponseEntity.status(HttpStatus.OK).body(resultado);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Nenhuma solicitação encontrada.");
        }
    }

    @GetMapping("/tipos-dificuldade")
    public ResponseEntity<?> listarTiposDificuldade() {
        List<TipoDificuldadeTO> resultado = solicitacaoAjudaBO.listarTiposDificuldade();
        if (resultado != null) {
            return ResponseEntity.status(HttpStatus.OK).body(resultado);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Nenhum tipo de dificuldade encontrado.");
        }
    }

    @GetMapping("/por-agendamento/{idAgendamento}")
    public ResponseEntity<?> listarPorAgendamento(@PathVariable Long idAgendamento,
                                                  @RequestAttribute("idPacienteAutenticado") Long idPacienteAutenticado) {
        try {
            List<SolicitacaoAjudaTO> resultado = solicitacaoAjudaBO.listarPorAgendamento(
                    idAgendamento, idPacienteAutenticado);
            if (resultado != null) {
                return ResponseEntity.status(HttpStatus.OK).body(resultado);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Nenhuma solicitação encontrada.");
            }
        } catch (AcessoNegadoException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erro ao consultar as solicitações.");
        }
    }

    @GetMapping("/{idSolicitacao}")
    public ResponseEntity<?> buscarDoPaciente(@PathVariable Long idSolicitacao,
                                              @RequestAttribute("idPacienteAutenticado") Long idPacienteAutenticado) {
        try {
            SolicitacaoAjudaTO resultado = solicitacaoAjudaBO.buscarDoPaciente(
                    idSolicitacao, idPacienteAutenticado);
            if (resultado != null) {
                return ResponseEntity.status(HttpStatus.OK).body(resultado);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Solicitação não encontrada.");
            }
        } catch (AcessoNegadoException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erro ao buscar a solicitação.");
        }
    }

    @GetMapping("/{idSolicitacao}/dificuldades")
    public ResponseEntity<?> listarDificuldades(@PathVariable Long idSolicitacao,
                                                @RequestAttribute("idPacienteAutenticado") Long idPacienteAutenticado) {
        try {
            List<SolicitacaoDificuldadeTO> resultado = solicitacaoAjudaBO.listarDificuldades(
                    idSolicitacao, idPacienteAutenticado);
            if (resultado != null) {
                return ResponseEntity.status(HttpStatus.OK).body(resultado);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Nenhuma dificuldade encontrada.");
            }
        } catch (AcessoNegadoException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erro ao consultar as dificuldades.");
        }
    }

    @GetMapping("/{idSolicitacao}/disponibilidades")
    public ResponseEntity<?> listarDisponibilidades(@PathVariable Long idSolicitacao,
                                                    @RequestAttribute("idPacienteAutenticado") Long idPacienteAutenticado) {
        try {
            List<DisponibilidadeTO> resultado = solicitacaoAjudaBO.listarDisponibilidades(
                    idSolicitacao, idPacienteAutenticado);
            if (resultado != null) {
                return ResponseEntity.status(HttpStatus.OK).body(resultado);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Nenhuma disponibilidade encontrada.");
            }
        } catch (AcessoNegadoException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erro ao consultar as disponibilidades.");
        }
    }

    @PostMapping
    public ResponseEntity<?> inserir(@RequestBody @Valid CadastroSolicitacaoAjudaTO cadastro,
                                     @RequestAttribute("idPacienteAutenticado") Long idPacienteAutenticado) {
        try {
            SolicitacaoAjudaTO solicitacao = new SolicitacaoAjudaTO();
            solicitacao.setIdAgendamento(cadastro.getIdAgendamento());
            solicitacao.setRelato(cadastro.getRelato());
            SolicitacaoAjudaTO resultado = solicitacaoAjudaBO.inserir(
                    solicitacao, idPacienteAutenticado, cadastro.getIdsDificuldade(), cadastro.getDisponibilidades());
            if (resultado != null) {
                return ResponseEntity.status(HttpStatus.CREATED).body(resultado);
            } else {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Não foi possível abrir a solicitação para este agendamento.");
            }
        } catch (AcessoNegadoException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erro ao inserir o registro.");
        }
    }

    @PutMapping("/{idSolicitacao}")
    public ResponseEntity<?> atualizar(@PathVariable Long idSolicitacao,
                                       @RequestBody SolicitacaoAjudaTO solicitacao,
                                       @RequestAttribute("idPacienteAutenticado") Long idPacienteAutenticado) {
        try {
            solicitacao.setIdSolicitacao(idSolicitacao);
            SolicitacaoAjudaTO resultado = solicitacaoAjudaBO.atualizar(solicitacao, idPacienteAutenticado);
            if (resultado != null) {
                return ResponseEntity.status(HttpStatus.OK).body(resultado);
            } else if (solicitacaoAjudaBO.buscarPorId(idSolicitacao) != null) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("A solicitação não pode ser atualizada no estado atual.");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Solicitação não encontrada para atualização.");
            }
        } catch (AcessoNegadoException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erro ao atualizar o registro.");
        }
    }

    @PostMapping("/{idSolicitacao}/dificuldades")
    public ResponseEntity<?> adicionarDificuldade(@PathVariable Long idSolicitacao,
                                                  @RequestBody @Valid SolicitacaoDificuldadeTO dificuldade,
                                                  @RequestAttribute("idPacienteAutenticado") Long idPacienteAutenticado) {
        try {
            SolicitacaoDificuldadeTO resultado = solicitacaoAjudaBO.adicionarDificuldade(
                    idSolicitacao, idPacienteAutenticado, dificuldade.getIdDificuldade());
            if (resultado != null) {
                return ResponseEntity.status(HttpStatus.CREATED).body(resultado);
            } else if (solicitacaoAjudaBO.buscarPorId(idSolicitacao) != null) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("A dificuldade não pode ser adicionada à solicitação.");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Solicitação não encontrada.");
            }
        } catch (AcessoNegadoException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erro ao adicionar a dificuldade.");
        }
    }

    @DeleteMapping("/{idSolicitacao}/dificuldades/{idDificuldade}")
    public ResponseEntity<String> excluirDificuldade(@PathVariable Long idSolicitacao,
                                                     @PathVariable Long idDificuldade,
                                                     @RequestAttribute("idPacienteAutenticado") Long idPacienteAutenticado) {
        try {
            SolicitacaoDificuldadeTO resultado = solicitacaoAjudaBO.excluirDificuldade(
                    idSolicitacao, idPacienteAutenticado, idDificuldade);
            if (resultado != null) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Dificuldade removida da solicitação.");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Solicitação ou dificuldade vinculada não encontrada.");
            }
        } catch (AcessoNegadoException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (RegraNegocioException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erro ao remover a dificuldade.");
        }
    }

    @PostMapping("/{idSolicitacao}/disponibilidades")
    public ResponseEntity<?> adicionarDisponibilidade(@PathVariable Long idSolicitacao,
                                                      @RequestBody @Valid DisponibilidadeTO disponibilidade,
                                                      @RequestAttribute("idPacienteAutenticado") Long idPacienteAutenticado) {
        try {
            disponibilidade.setIdDisponibilidade(null);
            disponibilidade.setIdSolicitacao(idSolicitacao);
            DisponibilidadeTO resultado = solicitacaoAjudaBO.adicionarDisponibilidade(
                    idSolicitacao, idPacienteAutenticado, disponibilidade);
            if (resultado != null) {
                return ResponseEntity.status(HttpStatus.CREATED).body(resultado);
            } else if (solicitacaoAjudaBO.buscarPorId(idSolicitacao) != null) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("A disponibilidade não pode ser adicionada à solicitação.");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Solicitação não encontrada.");
            }
        } catch (AcessoNegadoException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erro ao adicionar a disponibilidade.");
        }
    }

    @PutMapping("/{idSolicitacao}/disponibilidades/{idDisponibilidade}")
    public ResponseEntity<?> atualizarDisponibilidade(@PathVariable Long idSolicitacao,
                                                      @PathVariable Long idDisponibilidade,
                                                      @RequestBody @Valid DisponibilidadeTO disponibilidade,
                                                      @RequestAttribute("idPacienteAutenticado") Long idPacienteAutenticado) {
        try {
            disponibilidade.setIdDisponibilidade(idDisponibilidade);
            disponibilidade.setIdSolicitacao(idSolicitacao);
            DisponibilidadeTO resultado = solicitacaoAjudaBO.atualizarDisponibilidade(
                    idSolicitacao, idDisponibilidade, idPacienteAutenticado, disponibilidade);
            if (resultado != null) {
                return ResponseEntity.status(HttpStatus.OK).body(resultado);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Solicitação ou disponibilidade vinculada não encontrada.");
            }
        } catch (AcessoNegadoException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (RegraNegocioException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erro ao atualizar a disponibilidade.");
        }
    }

    @DeleteMapping("/{idSolicitacao}/disponibilidades/{idDisponibilidade}")
    public ResponseEntity<String> excluirDisponibilidade(@PathVariable Long idSolicitacao,
                                                         @PathVariable Long idDisponibilidade,
                                                         @RequestAttribute("idPacienteAutenticado") Long idPacienteAutenticado) {
        try {
            DisponibilidadeTO resultado = solicitacaoAjudaBO.excluirDisponibilidade(
                    idSolicitacao, idDisponibilidade, idPacienteAutenticado);
            if (resultado != null) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Disponibilidade removida da solicitação.");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Solicitação ou disponibilidade vinculada não encontrada.");
            }
        } catch (AcessoNegadoException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (RegraNegocioException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erro ao remover a disponibilidade.");
        }
    }

    @PostMapping("/{idSolicitacao}/analise")
    public ResponseEntity<?> iniciarAnalise(@PathVariable Long idSolicitacao,
                                            @RequestAttribute("idMembroEquipeAutenticado") Long idMembroEquipe) {
        try {
            SolicitacaoAjudaTO resultado = solicitacaoAjudaBO.iniciarAnalise(idSolicitacao, idMembroEquipe);
            if (resultado != null) {
                return ResponseEntity.status(HttpStatus.OK).body(resultado);
            } else if (solicitacaoAjudaBO.buscarPorId(idSolicitacao) != null) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Não foi possível iniciar a análise da solicitação.");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Solicitação não encontrada.");
            }
        } catch (AcessoNegadoException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erro ao iniciar a análise.");
        }
    }

    @PostMapping("/{idSolicitacao}/encerramento")
    public ResponseEntity<?> encerrar(@PathVariable Long idSolicitacao,
                                      @RequestParam String situacao,
                                      @RequestAttribute("idMembroEquipeAutenticado") Long idMembroEquipe) {
        try {
            SolicitacaoAjudaTO resultado = solicitacaoAjudaBO.encerrar(idSolicitacao, idMembroEquipe, situacao);
            if (resultado != null) {
                return ResponseEntity.status(HttpStatus.OK).body(resultado);
            } else if (solicitacaoAjudaBO.buscarPorId(idSolicitacao) != null) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Não foi possível encerrar a solicitação no estado atual.");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Solicitação não encontrada.");
            }
        } catch (AcessoNegadoException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erro ao encerrar a solicitação.");
        }
    }

    @DeleteMapping("/{idSolicitacao}")
    public ResponseEntity<String> excluir(@PathVariable Long idSolicitacao,
                                          @RequestAttribute("idPacienteAutenticado") Long idPacienteAutenticado) {
        try {
            if (solicitacaoAjudaBO.excluir(idSolicitacao, idPacienteAutenticado)) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Solicitação cancelada com sucesso.");
            } else if (solicitacaoAjudaBO.buscarPorId(idSolicitacao) != null) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("A solicitação não pode ser cancelada no estado atual.");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Solicitação não encontrada.");
            }
        } catch (AcessoNegadoException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erro ao cancelar a solicitação.");
        }
    }
}
