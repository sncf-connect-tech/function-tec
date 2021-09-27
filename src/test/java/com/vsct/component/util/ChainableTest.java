package com.vsct.component.util;

import com.vsct.testing.data.Direction;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ChainableTest {

    @Test
    void then_should_chain() {
        // Arrange
        Arrow base = new Arrow(Direction.W);
        Arrow[] captor = { null };

        // Act
        Arrow result = base.then(t -> {
            captor[0] = t;
            return t.turn();
        });

        // Assert
        assertThat(result).isEqualTo(new Arrow(Direction.N));
        assertThat(captor[0]).isSameAs(base);
    }

    private static class Arrow implements Chainable<Arrow> {

        private final Direction direction;
        Arrow(Direction direction) {
            this.direction = direction;
        }

        @Override
        public boolean equals(Object o) {
            return (o instanceof Arrow) && direction == ((Arrow) o).direction;
        }

        Arrow turn() {
            return new Arrow(direction.next());
        }
    }
}