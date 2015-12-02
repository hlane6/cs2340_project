package mule.model.blackjack;

import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

public class Card extends Parent {

	public enum Suit {
		HEARTS, DIAMONDS, CLUBS, SPADES
	};

	public enum Rank {
		TWO(2), THREE(3), FOUR(4), FIVE(5), SIX(6), SEVEN(7), EIGHT(8), NINE(9), TEN(10),
        JACK(10), QUEEN(10), KING(10), ACE(11);

		final int value;
		private Rank(int value) {
			this.value = value;
		}
	};

	public final Suit suit;
	public final Rank rank;
	public final int value;

	public Card(Suit suit, Rank rank) {
		this.suit = suit;
		this.rank = rank;
		this.value = rank.value;
	}

	@Override
	public String toString() {
		return rank.toString() + " of " + suit.toString();
	}

    @Override public boolean equals(Object o) {
        if (o == null) {
            return false;
        } else if (o == this) {
            return true;
        } else if (o instanceof Card) {
            Card that = (Card) o;
            return that.suit.ordinal() == this.suit.ordinal() &&
                    that.rank.ordinal() == this.rank.ordinal();
        }
        return false;
    }

    @Override public int hashCode() {
        return this.rank.ordinal() * 31 + this.suit.ordinal() * 19;
    }
}
