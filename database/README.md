# Banco Oracle do ConsultaViva

Os arquivos desta pasta ajudam a preparar o esquema para o backend Java. A referência foi construída a partir dos SQLs dos DAOs e dos campos usados pela autenticação. Os tipos, tamanhos e restrições propostos devem ser conferidos com o modelo relacional oficial antes de adotar o DDL.

## Qual script executar

| Situação | Script | Resultado |
| --- | --- | --- |
| Criar um ambiente de desenvolvimento vazio | [01_criar_esquema_referencia.sql](01_criar_esquema_referencia.sql) | Cria as 14 tabelas com chaves, relacionamentos e IDs gerados pelo Oracle. |
| Aproveitar o esquema que já está sendo criado | [02_ajustar_credenciais.sql](02_ajustar_credenciais.sql) | Adiciona as colunas de email e hash que estiverem faltando nas duas tabelas de contas. |
| Popular o catálogo para uma demonstração | [03_catalogo_dificuldades_demo.sql](03_catalogo_dificuldades_demo.sql) | Insere quatro opções ilustrativas e lista os códigos gerados. Em execuções sucessivas, preserva as opções já cadastradas com o mesmo nome. |
| Conferir o esquema do backend | [04_validar_esquema.sql](04_validar_esquema.sql) | Verifica presença de tabelas e colunas, configuração das identidades, chave composta e duplicidade de emails. |

### Esquema já existente

1. Conecte-se como proprietário das tabelas e rode o script 04 para levantar as diferenças.
2. Se faltarem as colunas de credenciais, revise e execute o script 02. Se uma coluna já existir, compare seu tipo e capacidade com o modelo oficial. O script preserva a definição existente.
3. Rode novamente o script 04. O primeiro resultado fica vazio quando as verificações de estrutura listadas ali passam. A seguir, o script exibe os metadados das credenciais e a contagem de grupos de emails duplicados no painel **DBMS Output**.
4. Use o catálogo oficial. Para um ambiente de demonstração, o script 03 pode fornecer os primeiros registros.

### Esquema vazio

Rode o script 01 em um esquema de desenvolvimento vazio, seguido do script 03 se precisar de dados de demonstração. Rode o script 04 ao final. No SQL Developer, use **Executar Script (F5)**. No SQLcl ou SQL*Plus, use `@arquivo.sql`. O Oracle confirma alterações de DDL implicitamente, então revise o esquema selecionado antes de executar os scripts 01 e 02.

## Pontos de integração com o Java

- Os inserts das entidades deixam o Oracle gerar o código com `GENERATED ALWAYS AS IDENTITY`. `TB_SOLICITACAO_DIFICULDADE` usa chave composta por `CD_SOLICITACAO` e `CD_DIFICULDADE`.
- As credenciais ficam em `DS_EMAIL` e `DS_HASH_SENHA` de `TB_PACIENTE` e `TB_MEMBRO_EQUIPE`. No esquema de referência, as colunas aceitam nulo porque os cadastros administrativos criam registros antes de habilitar o acesso. Para entrar na API, a conta precisa de email e hash.
- Para provisionar uma conta de equipe, associe um email e um hash ao membro correto no Oracle. O formato aceito é `pbkdf2-sha256$iterações$salEmBase64$hashEmBase64`. O cadastro de paciente gera o hash com PBKDF2-HMAC-SHA-256, 210.000 iterações, sal aleatório de 16 bytes e saída de 256 bits. [Geração no Java](../src/main/java/br/com/fiap/bo/PacienteBO.java)
- O esquema de referência usa um índice único por tabela em `UPPER(DS_EMAIL)`. Cada índice cobre somente sua tabela. No provisionamento manual, confira o email nas duas tabelas antes de gravar. O script 04 ajuda a identificar colisões dentro de uma tabela e entre pacientes e equipe.
- Para demonstrar o fluxo de resgate, prepare um membro com `IN_APOIO_ADMINISTRATIVO = 'S'`, um profissional com `IN_ATENDIMENTO = 'S'`, uma unidade e horários futuros. A conversão de modalidade também exige um membro com `IN_AUTORIZA_MODALIDADE = 'S'`. Uma pessoa pode ter mais de uma atribuição.
- Configure `DB_URL`, `DB_USER` e `DB_PASSWORD` no ambiente da aplicação. [Configuração do backend](../README.md#configuração-do-banco)

Use o modelo oficial para revisar tipos, nulabilidade, chaves estrangeiras e demais restrições. A documentação da Oracle descreve a sintaxe para colunas geradas pelo banco. [Oracle SQL Language Reference](https://docs.oracle.com/en/database/oracle/oracle-database/19/sqlrf/CREATE-TABLE.html)
