-- A tarefa exige unicidade por problema/ordinal, sem necessidade de reordenacao deferida.
-- Substitui a constraint DEFERRABLE herdada da V3 por uma UNIQUE imediata, tornando
-- violacoes observaveis no INSERT/flush e simplificando o contrato de persistencia.
ALTER TABLE test_cases DROP CONSTRAINT test_cases_problem_ordinal_unique;
ALTER TABLE test_cases ADD CONSTRAINT test_cases_problem_ordinal_unique UNIQUE (problem_id, ordinal);
