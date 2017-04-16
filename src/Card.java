import org.fusesource.jansi.Ansi;

import java.util.Comparator;

/**
 * Created by Wen Yang on 14/4/2017.
 */
public class Card {
    final int RANK;     //1,2,3,4,5,6,7,8,9,10,11(J),12(Q),13(K),0 (Joker) /// ready to be changed to Ace as biggest
    final int SUIT;  //1 - Spade, 2 - Heart, 3 - Club, 4 - Diamond, 0 - Joker(wildcard)

    public Card(int RANK, int SUIT) {
        this.RANK = RANK;
        this.SUIT = SUIT;
    }

    static public Card[] createFullDeck() {
        final int MAX_CARD = 53;
        Card[] deck = new Card[MAX_CARD];
        for (int i = 0; i < 52; i++) {
            deck[i] = new Card((i / 4) + 1, (i % 4) + 1);
        }
        if (MAX_CARD > 52) {                        //Joker or WildCard
            for (int i = 52; i < MAX_CARD; i++) {
                deck[i] = new Card(0, 0);
            }
        }
        return deck;
    }

    public String toString() {

        String[] rank = {"Joker", "A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K"};
        char[] suit = {'\u2605', '\u2660', '\u2665', '\u2663', '\u2666'}; //suit symbol unicode

        if (this.SUIT == 2 || this.SUIT == 4) {
            return Ansi.ansi().render("@|red " + suit[this.SUIT] + " " + rank[this.RANK] + "|@").toString(); //coloured word using others library
        } else if (this.SUIT == 1 || this.SUIT == 3){
            return Ansi.ansi().render("@|black " + suit[this.SUIT] + " " + rank[this.RANK] + "|@").toString();
        }else {return super.toString();}
    }


    //comparator   *to be edited*
    class PokerRankComparator implements Comparator<Card> {

        @Override
        public int compare(Card c1, Card c2) {
            return Integer.compare(c1.RANK, c2.RANK);
        }
    }

    class PokerSuitComparator implements Comparator<Card> {

        @Override
        public int compare(Card c1, Card c2) {
            return Integer.compare(c1.SUIT, c2.SUIT);
        }
    }
}
