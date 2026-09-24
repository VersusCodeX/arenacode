-- A renumeracao dos test cases pode criar ordinais repetidos intermediarios dentro da
-- mesma transacao. Restaura a constraint deferida sem alterar migrations ja aplicadas.
ALTER TABLE test_cases DROP CONSTRAINT test_cases_problem_ordinal_unique;
ALTER TABLE test_cases ADD CONSTRAINT test_cases_problem_ordinal_unique
    UNIQUE (problem_id, ordinal) DEFERRABLE INITIALLY DEFERRED;
