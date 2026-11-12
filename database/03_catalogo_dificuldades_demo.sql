-- ConsultaViva | Opções ilustrativas para um ambiente de demonstração.
-- Use o catálogo oficial se ele já tiver sido definido pela equipe.
WHENEVER SQLERROR EXIT SQL.SQLCODE ROLLBACK

MERGE INTO tb_tipo_dificuldade destino
USING (
    SELECT 'Dificuldade de transporte' AS nm_dificuldade FROM dual
    UNION ALL SELECT 'Conflito de horario' FROM dual
    UNION ALL SELECT 'Dificuldade de mobilidade' FROM dual
    UNION ALL SELECT 'Dificuldade de acesso digital' FROM dual
) origem
ON (UPPER(TRIM(destino.nm_dificuldade)) = UPPER(TRIM(origem.nm_dificuldade)))
WHEN NOT MATCHED THEN
    INSERT (nm_dificuldade) VALUES (origem.nm_dificuldade);

COMMIT;

SELECT cd_dificuldade, nm_dificuldade
FROM tb_tipo_dificuldade
ORDER BY cd_dificuldade;
