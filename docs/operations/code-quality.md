# Qualidade de Codigo - ArenaCode Backend

## Visao geral

O projeto utiliza ferramentas automatizadas para garantir padronizacao de estilo e detectar problemas estaticos antes de adicionar modulos de negocio.

## Ferramentas

| Ferramenta | Finalidade |
|------------|------------|
| **Spotless** | Formatacao automatica de codigo Java (Google Java Format). |
| **SpotBugs** | Analise estatica para detectar bugs potenciais, codigo suspeito e violacoes de boas praticas. |

## Comandos

### Formatacao de codigo

```bash
# Aplicar formatacao automatica a todos os arquivos Java
./gradlew spotlessApply

# Verificar se o codigo esta formatado corretamente (sem modificar arquivos)
./gradlew spotlessCheck
```

**Quando usar:**
- `spotlessApply`: antes de cada commit, para garantir que o codigo esta formatado.
- `spotlessCheck`: em CI/CD ou antes de push, para validar que nenhum arquivo foi esquecido.

**IntelliJ:** o formatador padrao da IDE usa 4 espacos e nao segue o Google Java Format. Instale o plugin `google-java-format` ou rode `./gradlew spotlessApply` antes de commitar.

### Analise estatica (SpotBugs)

```bash
# Executar analise estatica no codigo main e gerar relatorio HTML
./gradlew spotbugsMain

# O relatorio sera gerado em: build/reports/spotbugs/main/spotbugs.html
```

**Interpretando o relatorio:**
- Acesse `build/reports/spotbugs/main/spotbugs.html` no navegador apos a execucao.
- Cada finding e classificado por **confianca** (High, Medium, Low) e **severidade**.
- **High/Medium**: devem ser corrigidos sempre que possivel.
- **Low**: podem ser suprimidos se houver justificativa clara.

### Testes

```bash
# Executar todos os testes
./gradlew test
```

### Verificacao completa

```bash
# Executa spotlessCheck, spotbugsMain, test e todas as verificacoes configuradas
./gradlew check
```

**Nota:** o build **falha** se:
- `spotlessCheck` encontrar arquivos nao formatados.
- `spotbugsMain` encontrar bugs de confianca media ou alta (`reportLevel = MEDIUM`) nao suprimidos.
- `test` falhar em algum teste.

## Supressoes (SpotBugs)

Nao use supressoes genericas. Se um finding do SpotBugs for um falso positivo ou uma excecao justificada:

1. Documente a supressao com um comentario claro explicando o motivo.
2. Use a anotacao `@SuppressFBWarnings` apenas no metodo/classe afetada, nunca no arquivo inteiro.
3. Atualize esta documentacao se criar um arquivo de supressao (`spotbugs-exclude.xml`).

Exemplo:

```java
// SpotBugs: EI_EXPOSE_REP - intencional, este e um DTO imutavel (record)
@SuppressFBWarnings("EI_EXPOSE_REP")
public record MyDto(String value) {
}
```

A anotacao vem da dependencia `spotbugs-annotations` (`compileOnly`), na mesma versao do SpotBugs usada pelo plugin. Prefira o atributo `justification` para registrar o motivo.

**Supressoes atuais:**
- `CT_CONSTRUCTOR_THROW` nos construtores de `Problem` e `TestCase`: entidades JPA nao podem ser `final` e validar no construtor e intencional.
- `EI_EXPOSE_REP` em `TestCase.getProblem()`: navegacao intencional para o agregado raiz.

## Integracao com CI/CD

Em pipelines de CI/CD (GitHub Actions, GitLab CI, etc.):

```bash
# Ordem recomendada
./gradlew spotlessCheck  # Falha rapida se houver formatacao errada
./gradlew spotbugsMain   # Analise estatica
./gradlew test           # Testes unitarios e de integracao
./gradlew check          # Verificacao completa (opcional, ja inclui os anteriores)
```

## Proximos passos

- Adicionar verificacao de formatacao em pre-commit hooks (opcional).
- Integrar relatorios SpotBugs com dashboard de qualidade (ex.: SonarQube) no futuro.
- Expandir Spotless para outros arquivos (Kotlin, Markdown, etc.) se necessario.
