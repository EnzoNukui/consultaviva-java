package br.com.fiap.resource;

import br.com.fiap.exception.AcessoNegadoException;
import br.com.fiap.exception.RegraNegocioException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<List<String>> tratarValidacao(MethodArgumentNotValidException excecao) {
        List<String> erros = excecao.getBindingResult().getFieldErrors().stream()
                .map(erro -> erro.getField() + ": valor inválido")
                .toList();
        return ResponseEntity.badRequest().body(erros);
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<String> tratarRegraNegocio(RegraNegocioException excecao) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(excecao.getMessage());
    }

    @ExceptionHandler(AcessoNegadoException.class)
    public ResponseEntity<String> tratarAcessoNegado(AcessoNegadoException excecao) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(excecao.getMessage());
    }
}
