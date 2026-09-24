-- A renumeracao de test cases pertence ao agregado Problem e pode criar estados
-- intermediarios com ordinais repetidos dentro da mesma transacao. A constraint
-- deferida permite concluir a operacao e validar a unicidade no COMMIT.
ALTER TABLE test_cases DROP CONSTRAINT test_cases_problem_ordinal_unique;
ALTER TABLE test_cases ADD CONSTRAINT test_cases_problem_ordinal_unique UNIQUE (problem_id, ordinal) DEFERRABLE INITIALLY DEFERRED;
