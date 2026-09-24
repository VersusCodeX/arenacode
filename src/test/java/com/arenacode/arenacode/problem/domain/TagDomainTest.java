package com.arenacode.arenacode.problem.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class TagDomainTest {

    @Test
    void constructorShouldNormalizeName() {
        Tag tag = new Tag("  Dinamica  ");

        assertThat(tag.getName()).isEqualTo("Dinamica");
        assertThat(tag.getNormalizedName()).isEqualTo("dinamica");
    }

    @Test
    void constructorShouldRejectBlankName() {
        assertThatThrownBy(() -> new Tag(null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Tag("   ")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void constructorShouldRejectNameLongerThanLimit() {
        String tooLong = "a".repeat(Tag.MAX_NAME_LENGTH + 1);

        assertThatThrownBy(() -> new Tag(tooLong)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void normalizeShouldLowercaseAndTrim() {
        assertThat(Tag.normalize("  Grafos  ")).isEqualTo("grafos");
        assertThat(Tag.normalize("GRAFOS")).isEqualTo("grafos");
    }

    @Test
    void differentCasingShouldProduceSameNormalizedName() {
        Tag a = new Tag("Grafos");
        Tag b = new Tag("grafos");
        Tag c = new Tag("GRAFOS");

        assertThat(a.getNormalizedName()).isEqualTo(b.getNormalizedName()).isEqualTo(c.getNormalizedName());
    }
}
