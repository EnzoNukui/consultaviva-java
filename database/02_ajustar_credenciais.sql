-- ConsultaViva | Ajuste para um esquema Oracle existente.
-- Executar com o usuário proprietário das tabelas.
WHENEVER SQLERROR EXIT SQL.SQLCODE ROLLBACK
SET SERVEROUTPUT ON

DECLARE
    v_total PLS_INTEGER;

    PROCEDURE adicionar_se_ausente (
        p_tabela IN VARCHAR2,
        p_coluna IN VARCHAR2,
        p_tipo   IN VARCHAR2
    ) IS
        v_existe PLS_INTEGER;
    BEGIN
        SELECT COUNT(*) INTO v_existe
        FROM user_tab_columns
        WHERE table_name = p_tabela
          AND column_name = p_coluna;

        IF v_existe = 0 THEN
            EXECUTE IMMEDIATE 'ALTER TABLE ' || p_tabela || ' ADD (' || p_coluna || ' ' || p_tipo || ')';
            DBMS_OUTPUT.PUT_LINE(p_tabela || '.' || p_coluna || ' adicionada.');
        ELSE
            DBMS_OUTPUT.PUT_LINE(p_tabela || '.' || p_coluna || ' ja existe.');
        END IF;
    END;
BEGIN
    SELECT COUNT(*) INTO v_total
    FROM user_tables
    WHERE table_name IN ('TB_PACIENTE', 'TB_MEMBRO_EQUIPE');

    IF v_total <> 2 THEN
        RAISE_APPLICATION_ERROR(-20002, 'TB_PACIENTE e TB_MEMBRO_EQUIPE devem existir no esquema.');
    END IF;

    adicionar_se_ausente('TB_PACIENTE', 'DS_EMAIL', 'VARCHAR2(150 CHAR)');
    adicionar_se_ausente('TB_PACIENTE', 'DS_HASH_SENHA', 'VARCHAR2(255 CHAR)');
    adicionar_se_ausente('TB_MEMBRO_EQUIPE', 'DS_EMAIL', 'VARCHAR2(150 CHAR)');
    adicionar_se_ausente('TB_MEMBRO_EQUIPE', 'DS_HASH_SENHA', 'VARCHAR2(255 CHAR)');
END;
/
