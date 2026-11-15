-- ConsultaViva | Esquema Oracle de referência para um usuário com esquema vazio.
-- Compare com o modelo relacional oficial antes de adotar tipos e restrições.
WHENEVER SQLERROR EXIT SQL.SQLCODE ROLLBACK

DECLARE
    v_total PLS_INTEGER;
BEGIN
    SELECT COUNT(*) INTO v_total
    FROM user_objects
    WHERE object_type IN ('TABLE', 'VIEW', 'MATERIALIZED VIEW', 'SYNONYM')
      AND object_name IN (
          'TB_PACIENTE', 'TB_MEMBRO_EQUIPE', 'TB_UNIDADE', 'TB_TIPO_DIFICULDADE',
          'TB_HORARIO_ATENDIMENTO', 'TB_AGENDAMENTO', 'TB_SOLICITACAO_AJUDA',
          'TB_SOLICITACAO_DIFICULDADE', 'TB_DISPONIBILIDADE', 'TB_PROPOSTA_ATENDIMENTO',
          'TB_PLANO_COMPARECIMENTO', 'TB_PREPARO_VIRTUAL', 'TB_HISTORICO_EVENTO',
          'TB_SINCRONIZACAO_AGENDA'
      );
    IF v_total > 0 THEN
        RAISE_APPLICATION_ERROR(-20001, 'Ja existem objetos do ConsultaViva neste esquema.');
    END IF;
END;
/

CREATE TABLE tb_paciente (
    cd_paciente     NUMBER(18) GENERATED ALWAYS AS IDENTITY,
    nm_paciente     VARCHAR2(100 CHAR) NOT NULL,
    dt_nascimento   DATE NOT NULL,
    nr_telefone     VARCHAR2(20 CHAR),
    ds_email        VARCHAR2(150 CHAR),
    ds_hash_senha   VARCHAR2(255 CHAR),
    CONSTRAINT pk_paciente PRIMARY KEY (cd_paciente)
);

CREATE TABLE tb_membro_equipe (
    cd_membro_equipe         NUMBER(18) GENERATED ALWAYS AS IDENTITY,
    nm_membro_equipe         VARCHAR2(100 CHAR) NOT NULL,
    ds_cargo                 VARCHAR2(60 CHAR) NOT NULL,
    in_atendimento           CHAR(1) NOT NULL,
    in_apoio_administrativo  CHAR(1) NOT NULL,
    in_autoriza_modalidade   CHAR(1) NOT NULL,
    nr_registro_profissional VARCHAR2(40 CHAR),
    ds_email                 VARCHAR2(150 CHAR),
    ds_hash_senha            VARCHAR2(255 CHAR),
    CONSTRAINT pk_membro_equipe PRIMARY KEY (cd_membro_equipe),
    CONSTRAINT ck_membro_atendimento CHECK (in_atendimento IN ('S', 'N')),
    CONSTRAINT ck_membro_apoio CHECK (in_apoio_administrativo IN ('S', 'N')),
    CONSTRAINT ck_membro_autoriza CHECK (in_autoriza_modalidade IN ('S', 'N'))
);

CREATE TABLE tb_unidade (
    cd_unidade  NUMBER(18) GENERATED ALWAYS AS IDENTITY,
    nm_unidade  VARCHAR2(100 CHAR) NOT NULL,
    ds_endereco VARCHAR2(250 CHAR) NOT NULL,
    CONSTRAINT pk_unidade PRIMARY KEY (cd_unidade)
);

CREATE TABLE tb_tipo_dificuldade (
    cd_dificuldade NUMBER(18) GENERATED ALWAYS AS IDENTITY,
    nm_dificuldade VARCHAR2(60 CHAR) NOT NULL,
    CONSTRAINT pk_tipo_dificuldade PRIMARY KEY (cd_dificuldade)
);

CREATE TABLE tb_horario_atendimento (
    cd_horario       NUMBER(18) GENERATED ALWAYS AS IDENTITY,
    dt_inicio        TIMESTAMP NOT NULL,
    dt_fim           TIMESTAMP NOT NULL,
    cd_unidade       NUMBER(18) NOT NULL,
    cd_membro_equipe NUMBER(18) NOT NULL,
    CONSTRAINT pk_horario_atendimento PRIMARY KEY (cd_horario),
    CONSTRAINT fk_horario_unidade FOREIGN KEY (cd_unidade) REFERENCES tb_unidade (cd_unidade),
    CONSTRAINT fk_horario_membro FOREIGN KEY (cd_membro_equipe) REFERENCES tb_membro_equipe (cd_membro_equipe),
    CONSTRAINT ck_horario_intervalo CHECK (dt_fim > dt_inicio)
);

CREATE TABLE tb_agendamento (
    cd_agendamento       NUMBER(18) GENERATED ALWAYS AS IDENTITY,
    ds_modalidade        VARCHAR2(10 CHAR) NOT NULL,
    st_agendamento       VARCHAR2(20 CHAR) NOT NULL,
    st_resposta_paciente VARCHAR2(25 CHAR) NOT NULL,
    cd_paciente          NUMBER(18) NOT NULL,
    cd_horario           NUMBER(18) NOT NULL,
    CONSTRAINT pk_agendamento PRIMARY KEY (cd_agendamento),
    CONSTRAINT fk_agendamento_paciente FOREIGN KEY (cd_paciente) REFERENCES tb_paciente (cd_paciente),
    CONSTRAINT fk_agendamento_horario FOREIGN KEY (cd_horario) REFERENCES tb_horario_atendimento (cd_horario),
    CONSTRAINT ck_agendamento_modalidade CHECK (ds_modalidade IN ('PRESENCIAL', 'VIRTUAL')),
    CONSTRAINT ck_agendamento_status CHECK (st_agendamento IN ('AGENDADO', 'REALIZADO', 'CANCELADO', 'FALTOU')),
    CONSTRAINT ck_agendamento_resposta CHECK (
        st_resposta_paciente IN ('SEM_RESPOSTA', 'VOU_COMPARECER', 'PRECISO_DE_AJUDA', 'NAO_CONSIGO_COMPARECER')
    )
);

CREATE TABLE tb_solicitacao_ajuda (
    cd_solicitacao   NUMBER(18) GENERATED ALWAYS AS IDENTITY,
    dt_abertura      TIMESTAMP NOT NULL,
    dt_encerramento  TIMESTAMP,
    ds_relato        VARCHAR2(500 CHAR),
    st_solicitacao   VARCHAR2(20 CHAR) NOT NULL,
    cd_agendamento   NUMBER(18) NOT NULL,
    cd_membro_equipe NUMBER(18),
    CONSTRAINT pk_solicitacao_ajuda PRIMARY KEY (cd_solicitacao),
    CONSTRAINT fk_solic_agendamento FOREIGN KEY (cd_agendamento) REFERENCES tb_agendamento (cd_agendamento),
    CONSTRAINT fk_solic_membro FOREIGN KEY (cd_membro_equipe) REFERENCES tb_membro_equipe (cd_membro_equipe),
    CONSTRAINT ck_solic_status CHECK (st_solicitacao IN ('ABERTA', 'EM_ANALISE', 'RESOLVIDA', 'SEM_SOLUCAO', 'CANCELADA'))
);

CREATE TABLE tb_solicitacao_dificuldade (
    cd_solicitacao NUMBER(18) NOT NULL,
    cd_dificuldade NUMBER(18) NOT NULL,
    CONSTRAINT pk_solicitacao_dificuldade PRIMARY KEY (cd_solicitacao, cd_dificuldade),
    CONSTRAINT fk_solic_dif_solicitacao FOREIGN KEY (cd_solicitacao) REFERENCES tb_solicitacao_ajuda (cd_solicitacao),
    CONSTRAINT fk_solic_dif_tipo FOREIGN KEY (cd_dificuldade) REFERENCES tb_tipo_dificuldade (cd_dificuldade)
);

CREATE TABLE tb_disponibilidade (
    cd_disponibilidade NUMBER(18) GENERATED ALWAYS AS IDENTITY,
    dt_inicio          TIMESTAMP NOT NULL,
    dt_fim             TIMESTAMP NOT NULL,
    ds_modalidade      VARCHAR2(10 CHAR) NOT NULL,
    cd_solicitacao     NUMBER(18) NOT NULL,
    CONSTRAINT pk_disponibilidade PRIMARY KEY (cd_disponibilidade),
    CONSTRAINT fk_disp_solicitacao FOREIGN KEY (cd_solicitacao) REFERENCES tb_solicitacao_ajuda (cd_solicitacao),
    CONSTRAINT ck_disp_intervalo CHECK (dt_fim > dt_inicio),
    CONSTRAINT ck_disp_modalidade CHECK (ds_modalidade IN ('PRESENCIAL', 'VIRTUAL', 'AMBAS'))
);

CREATE TABLE tb_proposta_atendimento (
    cd_proposta        NUMBER(18) GENERATED ALWAYS AS IDENTITY,
    tp_alternativa     VARCHAR2(20 CHAR) NOT NULL,
    ds_modalidade      VARCHAR2(10 CHAR) NOT NULL,
    ds_justificativa   VARCHAR2(500 CHAR) NOT NULL,
    dt_criacao         TIMESTAMP NOT NULL,
    st_proposta        VARCHAR2(20 CHAR) NOT NULL,
    st_autorizacao     VARCHAR2(20 CHAR) NOT NULL,
    dt_autorizacao     TIMESTAMP,
    st_aceite_paciente VARCHAR2(20 CHAR) NOT NULL,
    dt_resposta        TIMESTAMP,
    dt_inicio_reserva  TIMESTAMP,
    dt_fim_reserva     TIMESTAMP,
    dt_limite_aceite   TIMESTAMP,
    dt_confirmacao     TIMESTAMP,
    cd_solicitacao     NUMBER(18) NOT NULL,
    cd_horario         NUMBER(18) NOT NULL,
    cd_analista        NUMBER(18),
    cd_autorizador     NUMBER(18),
    CONSTRAINT pk_proposta_atendimento PRIMARY KEY (cd_proposta),
    CONSTRAINT fk_prop_solicitacao FOREIGN KEY (cd_solicitacao) REFERENCES tb_solicitacao_ajuda (cd_solicitacao),
    CONSTRAINT fk_prop_horario FOREIGN KEY (cd_horario) REFERENCES tb_horario_atendimento (cd_horario),
    CONSTRAINT fk_prop_analista FOREIGN KEY (cd_analista) REFERENCES tb_membro_equipe (cd_membro_equipe),
    CONSTRAINT fk_prop_autorizador FOREIGN KEY (cd_autorizador) REFERENCES tb_membro_equipe (cd_membro_equipe),
    CONSTRAINT ck_prop_alternativa CHECK (
        tp_alternativa IN ('MANUTENCAO', 'REMARCACAO', 'CONVERSAO', 'REMARCACAO_CONVERSAO')
    ),
    CONSTRAINT ck_prop_modalidade CHECK (ds_modalidade IN ('PRESENCIAL', 'VIRTUAL')),
    CONSTRAINT ck_prop_status CHECK (st_proposta IN ('EM_ANALISE', 'OFERTADA', 'CONFIRMADA', 'EXPIRADA', 'CANCELADA')),
    CONSTRAINT ck_prop_autorizacao CHECK (st_autorizacao IN ('PENDENTE', 'AUTORIZADA', 'NEGADA', 'NAO_APLICAVEL')),
    CONSTRAINT ck_prop_aceite CHECK (st_aceite_paciente IN ('PENDENTE', 'ACEITA', 'RECUSADA'))
);

CREATE TABLE tb_plano_comparecimento (
    cd_plano                  NUMBER(18) GENERATED ALWAYS AS IDENTITY,
    dt_calculo                TIMESTAMP NOT NULL,
    ds_origem_trajeto          VARCHAR2(250 CHAR) NOT NULL,
    ds_destino_trajeto         VARCHAR2(250 CHAR) NOT NULL,
    ds_meio_transporte         VARCHAR2(60 CHAR) NOT NULL,
    nm_fonte                  VARCHAR2(60 CHAR) NOT NULL,
    nr_duracao_min            NUMBER(4) NOT NULL,
    nr_margem_min             NUMBER(3) NOT NULL,
    dt_atendimento_referencia TIMESTAMP NOT NULL,
    dt_saida_sugerida         TIMESTAMP NOT NULL,
    in_simulado               CHAR(1) NOT NULL,
    cd_agendamento            NUMBER(18) NOT NULL,
    cd_proposta               NUMBER(18),
    CONSTRAINT pk_plano_comparecimento PRIMARY KEY (cd_plano),
    CONSTRAINT fk_plano_agendamento FOREIGN KEY (cd_agendamento) REFERENCES tb_agendamento (cd_agendamento),
    CONSTRAINT fk_plano_proposta FOREIGN KEY (cd_proposta) REFERENCES tb_proposta_atendimento (cd_proposta),
    CONSTRAINT ck_plano_duracao CHECK (nr_duracao_min > 0),
    CONSTRAINT ck_plano_margem CHECK (nr_margem_min >= 0),
    CONSTRAINT ck_plano_simulado CHECK (in_simulado IN ('S', 'N'))
);

CREATE TABLE tb_preparo_virtual (
    cd_preparo             NUMBER(18) GENERATED ALWAYS AS IDENTITY,
    dt_verificacao         TIMESTAMP NOT NULL,
    st_permissao_microfone VARCHAR2(15 CHAR) NOT NULL,
    st_permissao_camera    VARCHAR2(15 CHAR) NOT NULL,
    st_camera              VARCHAR2(15 CHAR) NOT NULL,
    st_microfone           VARCHAR2(15 CHAR) NOT NULL,
    st_conexao             VARCHAR2(15 CHAR) NOT NULL,
    ds_observacao          VARCHAR2(500 CHAR),
    cd_agendamento         NUMBER(18) NOT NULL,
    cd_proposta            NUMBER(18),
    CONSTRAINT pk_preparo_virtual PRIMARY KEY (cd_preparo),
    CONSTRAINT fk_preparo_agendamento FOREIGN KEY (cd_agendamento) REFERENCES tb_agendamento (cd_agendamento),
    CONSTRAINT fk_preparo_proposta FOREIGN KEY (cd_proposta) REFERENCES tb_proposta_atendimento (cd_proposta)
);

CREATE TABLE tb_historico_evento (
    cd_evento        NUMBER(18) GENERATED ALWAYS AS IDENTITY,
    dt_evento        TIMESTAMP NOT NULL,
    tp_evento        VARCHAR2(40 CHAR) NOT NULL,
    ds_evento        VARCHAR2(1000 CHAR) NOT NULL,
    tp_origem        VARCHAR2(10 CHAR) NOT NULL,
    cd_agendamento   NUMBER(18) NOT NULL,
    cd_solicitacao   NUMBER(18),
    cd_proposta      NUMBER(18),
    cd_membro_equipe NUMBER(18),
    CONSTRAINT pk_historico_evento PRIMARY KEY (cd_evento),
    CONSTRAINT fk_hist_agendamento FOREIGN KEY (cd_agendamento) REFERENCES tb_agendamento (cd_agendamento),
    CONSTRAINT fk_hist_solicitacao FOREIGN KEY (cd_solicitacao) REFERENCES tb_solicitacao_ajuda (cd_solicitacao),
    CONSTRAINT fk_hist_proposta FOREIGN KEY (cd_proposta) REFERENCES tb_proposta_atendimento (cd_proposta),
    CONSTRAINT fk_hist_membro FOREIGN KEY (cd_membro_equipe) REFERENCES tb_membro_equipe (cd_membro_equipe)
);

CREATE TABLE tb_sincronizacao_agenda (
    cd_sincronizacao    NUMBER(18) GENERATED ALWAYS AS IDENTITY,
    nm_provedor         VARCHAR2(40 CHAR) NOT NULL,
    cd_agenda_externa   VARCHAR2(255 CHAR),
    cd_evento_externo   VARCHAR2(1024 CHAR),
    tp_operacao         VARCHAR2(10 CHAR) NOT NULL,
    st_sincronizacao    VARCHAR2(20 CHAR) NOT NULL,
    nr_tentativas       NUMBER(9) DEFAULT 0 NOT NULL,
    dt_ultima_tentativa TIMESTAMP,
    dt_ultimo_sucesso   TIMESTAMP,
    ds_ultimo_erro      VARCHAR2(500 CHAR),
    cd_agendamento      NUMBER(18) NOT NULL,
    CONSTRAINT pk_sincronizacao_agenda PRIMARY KEY (cd_sincronizacao),
    CONSTRAINT fk_sinc_agendamento FOREIGN KEY (cd_agendamento) REFERENCES tb_agendamento (cd_agendamento),
    CONSTRAINT ck_sinc_tentativas CHECK (nr_tentativas >= 0)
);

-- A unicidade do email vale por tabela. A aplicação consulta as duas tabelas.
CREATE UNIQUE INDEX ux_paciente_email_ci ON tb_paciente (UPPER(ds_email));
CREATE UNIQUE INDEX ux_membro_email_ci ON tb_membro_equipe (UPPER(ds_email));
