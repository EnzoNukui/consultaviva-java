-- ConsultaViva | Verificação somente leitura no esquema proprietário das tabelas.
-- Sem linhas no primeiro resultado significa que os itens estruturais abaixo foram encontrados.
SET SERVEROUTPUT ON

WITH colunas_esperadas AS (
    SELECT 'TB_PACIENTE' AS tabela, column_value AS coluna
    FROM TABLE(sys.odcivarchar2list(
        'CD_PACIENTE', 'NM_PACIENTE', 'DT_NASCIMENTO', 'NR_TELEFONE',
        'DS_EMAIL', 'DS_HASH_SENHA'
    ))
    UNION ALL
    SELECT 'TB_MEMBRO_EQUIPE' AS tabela, column_value AS coluna
    FROM TABLE(sys.odcivarchar2list(
        'CD_MEMBRO_EQUIPE', 'NM_MEMBRO_EQUIPE', 'DS_CARGO', 'IN_ATENDIMENTO',
        'IN_APOIO_ADMINISTRATIVO', 'IN_AUTORIZA_MODALIDADE', 'NR_REGISTRO_PROFISSIONAL', 'DS_EMAIL',
        'DS_HASH_SENHA'
    ))
    UNION ALL
    SELECT 'TB_UNIDADE' AS tabela, column_value AS coluna
    FROM TABLE(sys.odcivarchar2list(
        'CD_UNIDADE', 'NM_UNIDADE', 'DS_ENDERECO'
    ))
    UNION ALL
    SELECT 'TB_TIPO_DIFICULDADE' AS tabela, column_value AS coluna
    FROM TABLE(sys.odcivarchar2list(
        'CD_DIFICULDADE', 'NM_DIFICULDADE'
    ))
    UNION ALL
    SELECT 'TB_HORARIO_ATENDIMENTO' AS tabela, column_value AS coluna
    FROM TABLE(sys.odcivarchar2list(
        'CD_HORARIO', 'DT_INICIO', 'DT_FIM', 'CD_UNIDADE',
        'CD_MEMBRO_EQUIPE'
    ))
    UNION ALL
    SELECT 'TB_AGENDAMENTO' AS tabela, column_value AS coluna
    FROM TABLE(sys.odcivarchar2list(
        'CD_AGENDAMENTO', 'DS_MODALIDADE', 'ST_AGENDAMENTO', 'ST_RESPOSTA_PACIENTE',
        'CD_PACIENTE', 'CD_HORARIO'
    ))
    UNION ALL
    SELECT 'TB_SOLICITACAO_AJUDA' AS tabela, column_value AS coluna
    FROM TABLE(sys.odcivarchar2list(
        'CD_SOLICITACAO', 'DT_ABERTURA', 'DT_ENCERRAMENTO', 'DS_RELATO',
        'ST_SOLICITACAO', 'CD_AGENDAMENTO', 'CD_MEMBRO_EQUIPE'
    ))
    UNION ALL
    SELECT 'TB_SOLICITACAO_DIFICULDADE' AS tabela, column_value AS coluna
    FROM TABLE(sys.odcivarchar2list(
        'CD_SOLICITACAO', 'CD_DIFICULDADE'
    ))
    UNION ALL
    SELECT 'TB_DISPONIBILIDADE' AS tabela, column_value AS coluna
    FROM TABLE(sys.odcivarchar2list(
        'CD_DISPONIBILIDADE', 'DT_INICIO', 'DT_FIM', 'DS_MODALIDADE',
        'CD_SOLICITACAO'
    ))
    UNION ALL
    SELECT 'TB_PROPOSTA_ATENDIMENTO' AS tabela, column_value AS coluna
    FROM TABLE(sys.odcivarchar2list(
        'CD_PROPOSTA', 'TP_ALTERNATIVA', 'DS_MODALIDADE', 'DS_JUSTIFICATIVA',
        'DT_CRIACAO', 'ST_PROPOSTA', 'ST_AUTORIZACAO', 'DT_AUTORIZACAO',
        'ST_ACEITE_PACIENTE', 'DT_RESPOSTA', 'DT_INICIO_RESERVA', 'DT_FIM_RESERVA',
        'DT_LIMITE_ACEITE', 'DT_CONFIRMACAO', 'CD_SOLICITACAO', 'CD_HORARIO',
        'CD_ANALISTA', 'CD_AUTORIZADOR'
    ))
    UNION ALL
    SELECT 'TB_PLANO_COMPARECIMENTO' AS tabela, column_value AS coluna
    FROM TABLE(sys.odcivarchar2list(
        'CD_PLANO', 'DT_CALCULO', 'DS_ORIGEM_TRAJETO', 'DS_DESTINO_TRAJETO',
        'DS_MEIO_TRANSPORTE', 'NM_FONTE', 'NR_DURACAO_MIN', 'NR_MARGEM_MIN',
        'DT_ATENDIMENTO_REFERENCIA', 'DT_SAIDA_SUGERIDA', 'IN_SIMULADO', 'CD_AGENDAMENTO',
        'CD_PROPOSTA'
    ))
    UNION ALL
    SELECT 'TB_PREPARO_VIRTUAL' AS tabela, column_value AS coluna
    FROM TABLE(sys.odcivarchar2list(
        'CD_PREPARO', 'DT_VERIFICACAO', 'ST_PERMISSAO_MICROFONE', 'ST_PERMISSAO_CAMERA',
        'ST_CAMERA', 'ST_MICROFONE', 'ST_CONEXAO', 'DS_OBSERVACAO',
        'CD_AGENDAMENTO', 'CD_PROPOSTA'
    ))
    UNION ALL
    SELECT 'TB_HISTORICO_EVENTO' AS tabela, column_value AS coluna
    FROM TABLE(sys.odcivarchar2list(
        'CD_EVENTO', 'DT_EVENTO', 'TP_EVENTO', 'DS_EVENTO',
        'TP_ORIGEM', 'CD_AGENDAMENTO', 'CD_SOLICITACAO', 'CD_PROPOSTA',
        'CD_MEMBRO_EQUIPE'
    ))
    UNION ALL
    SELECT 'TB_SINCRONIZACAO_AGENDA' AS tabela, column_value AS coluna
    FROM TABLE(sys.odcivarchar2list(
        'CD_SINCRONIZACAO', 'NM_PROVEDOR', 'CD_AGENDA_EXTERNA', 'CD_EVENTO_EXTERNO',
        'TP_OPERACAO', 'ST_SINCRONIZACAO', 'NR_TENTATIVAS', 'DT_ULTIMA_TENTATIVA',
        'DT_ULTIMO_SUCESSO', 'DS_ULTIMO_ERRO', 'CD_AGENDAMENTO'
    ))
),
tabelas_esperadas AS (
    SELECT DISTINCT tabela FROM colunas_esperadas
),
identidades_esperadas AS (
    SELECT 'TB_PACIENTE' AS tabela, 'CD_PACIENTE' AS coluna FROM dual
    UNION ALL
    SELECT 'TB_MEMBRO_EQUIPE' AS tabela, 'CD_MEMBRO_EQUIPE' AS coluna FROM dual
    UNION ALL
    SELECT 'TB_UNIDADE' AS tabela, 'CD_UNIDADE' AS coluna FROM dual
    UNION ALL
    SELECT 'TB_TIPO_DIFICULDADE' AS tabela, 'CD_DIFICULDADE' AS coluna FROM dual
    UNION ALL
    SELECT 'TB_HORARIO_ATENDIMENTO' AS tabela, 'CD_HORARIO' AS coluna FROM dual
    UNION ALL
    SELECT 'TB_AGENDAMENTO' AS tabela, 'CD_AGENDAMENTO' AS coluna FROM dual
    UNION ALL
    SELECT 'TB_SOLICITACAO_AJUDA' AS tabela, 'CD_SOLICITACAO' AS coluna FROM dual
    UNION ALL
    SELECT 'TB_DISPONIBILIDADE' AS tabela, 'CD_DISPONIBILIDADE' AS coluna FROM dual
    UNION ALL
    SELECT 'TB_PROPOSTA_ATENDIMENTO' AS tabela, 'CD_PROPOSTA' AS coluna FROM dual
    UNION ALL
    SELECT 'TB_PLANO_COMPARECIMENTO' AS tabela, 'CD_PLANO' AS coluna FROM dual
    UNION ALL
    SELECT 'TB_PREPARO_VIRTUAL' AS tabela, 'CD_PREPARO' AS coluna FROM dual
    UNION ALL
    SELECT 'TB_HISTORICO_EVENTO' AS tabela, 'CD_EVENTO' AS coluna FROM dual
    UNION ALL
    SELECT 'TB_SINCRONIZACAO_AGENDA' AS tabela, 'CD_SINCRONIZACAO' AS coluna FROM dual
),
chave_do_vinculo AS (
    SELECT c.constraint_name
    FROM user_constraints c
    JOIN user_cons_columns cc
      ON cc.constraint_name = c.constraint_name AND cc.table_name = c.table_name
    WHERE c.table_name = 'TB_SOLICITACAO_DIFICULDADE'
      AND c.constraint_type = 'P'
      AND c.status = 'ENABLED'
    GROUP BY c.constraint_name
    HAVING COUNT(*) = 2
       AND COUNT(CASE WHEN cc.column_name IN ('CD_SOLICITACAO', 'CD_DIFICULDADE') THEN 1 END) = 2
)
SELECT e.tabela, CAST(NULL AS VARCHAR2(128)) AS coluna, 'TABELA AUSENTE' AS pendencia
FROM tabelas_esperadas e
LEFT JOIN user_tables t ON t.table_name = e.tabela
WHERE t.table_name IS NULL
UNION ALL
SELECT e.tabela, e.coluna, 'COLUNA AUSENTE'
FROM colunas_esperadas e
JOIN user_tables t ON t.table_name = e.tabela
LEFT JOIN user_tab_columns c ON c.table_name = e.tabela AND c.column_name = e.coluna
WHERE c.column_name IS NULL
UNION ALL
SELECT e.tabela, e.coluna, 'IDENTITY GENERATED ALWAYS AUSENTE'
FROM identidades_esperadas e
JOIN user_tab_columns c ON c.table_name = e.tabela AND c.column_name = e.coluna
LEFT JOIN user_tab_identity_cols i ON i.table_name = e.tabela AND i.column_name = e.coluna
WHERE i.column_name IS NULL OR i.generation_type <> 'ALWAYS'
UNION ALL
SELECT 'TB_SOLICITACAO_DIFICULDADE', NULL, 'CHAVE PRIMARIA COMPOSTA AUSENTE OU DIFERENTE'
FROM user_tables t
WHERE t.table_name = 'TB_SOLICITACAO_DIFICULDADE'
  AND NOT EXISTS (SELECT 1 FROM chave_do_vinculo)
ORDER BY 1, 2, 3;

-- Compare tipos, capacidade e nulabilidade das credenciais com o modelo oficial.
SELECT table_name, column_name, data_type, char_length, char_used, nullable
FROM user_tab_columns
WHERE table_name IN ('TB_PACIENTE', 'TB_MEMBRO_EQUIPE')
  AND column_name IN ('DS_EMAIL', 'DS_HASH_SENHA')
ORDER BY table_name, column_name;

-- Informa a quantidade de grupos duplicados sem imprimir emails ou hashes.
DECLARE
    v_colunas   PLS_INTEGER;
    v_conflitos NUMBER;
BEGIN
    SELECT COUNT(*) INTO v_colunas
    FROM user_tab_columns
    WHERE table_name IN ('TB_PACIENTE', 'TB_MEMBRO_EQUIPE')
      AND column_name = 'DS_EMAIL'
      AND data_type IN ('VARCHAR2', 'NVARCHAR2', 'CHAR', 'NCHAR');

    IF v_colunas = 2 THEN
        EXECUTE IMMEDIATE q'[
            SELECT COUNT(*)
            FROM (
                SELECT email
                FROM (
                    SELECT TO_NCHAR(UPPER(TRIM(ds_email))) AS email FROM tb_paciente
                    UNION ALL
                    SELECT TO_NCHAR(UPPER(TRIM(ds_email))) AS email FROM tb_membro_equipe
                )
                WHERE email IS NOT NULL
                GROUP BY email
                HAVING COUNT(*) > 1
            )
        ]' INTO v_conflitos;
        DBMS_OUTPUT.PUT_LINE('Grupos de emails duplicados: ' || v_conflitos);
    ELSE
        DBMS_OUTPUT.PUT_LINE('Duplicidade: verifique DS_EMAIL nas duas tabelas primeiro.');
    END IF;
END;
/
