import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;

/**
 * Created by Wen Yang on 14/4/2017.
 */
public class PokerHandAnalyzer {
    int totalRemainingDeck = 52;
    int[] remainingDeck = {1, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4};             //1 for joker, 4 for each card, distribution of cards
    int numberOfCardToDraw = 0;
    ArrayList<Card> finalHand = new ArrayList<>();

    static void listAllHandsProbability(final ArrayList<Card> initialHand) {
        PokerHandAnalyzer original = new PokerHandAnalyzer();
        System.out.println("initial hand" + original.getTwoPairProbability());
//        PokerHandAnalyzer test = new PokerHandAnalyzer(initialHand, new boolean[]{false, true, false,false, false});
//        System.out.println("final hand" + test.getTwoPairProbability());
        for (int i = 0; i < Double.valueOf(Math.pow(2, 5)).intValue(); i++) {
            PokerHandAnalyzer a = new PokerHandAnalyzer(initialHand, binaryToBooleanArray(i, 5));
            System.out.println("Probability after shuffling card#" + new StringBuilder(String.format("%5s", Integer.toBinaryString(i)).replace(' ', '0')).reverse().toString() + ": " + a.getTwoPairProbability()); //referred from Satish answered May 25 '11 at 20:29 in StackOverFlow
        }
    }

    private PokerHandAnalyzer() {

    }

    private PokerHandAnalyzer(ArrayList<Card> initialHand, boolean[] cardToShuffle) {
        initialHand.forEach(card -> remainingDeck[card.RANK]--);
        for (int i = 0; i < cardToShuffle.length; i++) {
            totalRemainingDeck--;                                                     //minus the initial hand from deck
            if (cardToShuffle[i]) {                                                   //draw a card
                numberOfCardToDraw++;
                totalRemainingDeck--;
            } else {
                finalHand.add(initialHand.get(i));                                     // move to final hand
            }
        }

    }

    double getTwoPairProbability() {        //(13-choose-2)(4-choose-2)(4-choose-2)(11-choose-1)(4-choose-1).
        double probability = 0.0;
        int size = this.finalHand.size();
        int numberOfDistinctPattern = 3; // aabbc -> 3, which are a, b, and c
        ArrayList<Card> handPattern = new ArrayList<>();//fulfilled groups
        finalHand.stream().map(PokerRankWrapper::new).distinct().map(PokerRankWrapper::unwrap).forEach(handPattern::add);//remove duplicated rank and add to hand pattern
        if (handPattern.size() > numberOfDistinctPattern){
            return probability;
        }
        for (int i = handPattern.size(); i < numberOfDistinctPattern; i++) {
            handPattern.add(new Card(-1, -1));// add based on remaining empty group
        }
        BigInteger totalChance = C(this.totalRemainingDeck, 5 - size);
        BigInteger possibleCombination = BigInteger.ZERO;
        boolean handDuplicateLookup[][] = new boolean[handPattern.size()][handPattern.size()];
        boolean deckDuplicateLookup[][] = new boolean[14][14];


        if (size == 0) {
            for (int i = 1; i < this.remainingDeck.length - 1; i++) {
                if (this.remainingDeck[i] >= 2) {
                    for (int j = i + 1; j < this.remainingDeck.length; j++) {
                        if (this.remainingDeck[j] >= 2) {
                            for (int k = 1; k < this.remainingDeck.length; k++) {
                                if (k != i && k != j && this.remainingDeck[k] > 0) {
                                    possibleCombination = possibleCombination.add(C(this.remainingDeck[i], 2).multiply(C(this.remainingDeck[j], 2)).multiply(C(this.remainingDeck[k], 1)));
                                }
                            }
                        }
                    }
                }
            }
        } else if (size <= 4) {
            for (int m = 0; m < handPattern.size(); m++) {
                for (int n = 0; n < handPattern.size(); n++) {
                    if (n != m && !handDuplicateLookup[m][n]) {
                        handDuplicateLookup[m][n] = true;
                        handDuplicateLookup[n][m] = true; // marked as visited
                        for (int o = 0; o < handPattern.size(); o++) {
                            if (o != m && o != n) {
                                //
                                for (int i = 1; i < this.remainingDeck.length; i++) {
                                    if (this.remainingDeck[i] >= 2 - this.getPopulation(handPattern.get(m).RANK) && this.getPopulation(handPattern.get(m).RANK) <= 2 && (handPattern.get(m).RANK == -1 || i == handPattern.get(m).RANK)) { //replace 2 with variable
                                        for (int j = 1; j < this.remainingDeck.length; j++) {
                                            boolean test1 = this.remainingDeck[j] >= 2 - this.getPopulation(handPattern.get(n).RANK);
                                            boolean test2 = this.getPopulation(handPattern.get(n).RANK) <= 2;
                                            boolean test3 = handPattern.get(n).RANK == -1;
                                            boolean test4 = j == handPattern.get(n).RANK;
                                            boolean test5 = (!deckDuplicateLookup[i][j]);
                                            if (test1 && test2 && (test3 || test4) && test5) {
                                                //if ((!deckDuplicateLookup[i][j]) && this.remainingDeck[j] >= 2 - this.getPopulation(handPattern.get(n).RANK) && this.getPopulation(handPattern.get(n).RANK) != 2 && (handPattern.get(n).RANK == -1 || i == handPattern.get(n).RANK)) {
                                                deckDuplicateLookup[i][j] = true;
                                                deckDuplicateLookup[j][i] = true;//visited
                                                for (int k = 1; k < this.remainingDeck.length; k++) {
                                                    boolean logicTest1 = k != i && k != j;
                                                    boolean logicTest2 = this.remainingDeck[k] >= 1 - this.getPopulation(handPattern.get(o).RANK);
                                                    boolean logicTest3 = this.getPopulation(handPattern.get(o).RANK) <= 1;
                                                    boolean logicTest4 = (handPattern.get(o).RANK == -1 || k == handPattern.get(o).RANK);
                                                    if (logicTest1 && logicTest2 && logicTest3 && logicTest4) {
                                                        possibleCombination = possibleCombination.add(C(this.remainingDeck[i], 2 - this.getPopulation(handPattern.get(m).RANK)).multiply(C(this.remainingDeck[j], 2 - this.getPopulation(handPattern.get(n).RANK))).multiply(C(this.remainingDeck[k], 1 - this.getPopulation(handPattern.get(o).RANK))));
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

        }

        probability = new BigDecimal(possibleCombination).divide(new BigDecimal(totalChance), 10, RoundingMode.HALF_UP).doubleValue();
        return probability;
    }


    static BigInteger C(final int N, final int K) {  //referred from polygenelubricants answered May 28 '10 at 14:34 in StackOverFlow
        BigInteger combination = BigInteger.ONE;
        for (int i = 0; i < K; i++) {
            combination = combination.multiply(BigInteger.valueOf(N - i))
                    .divide(BigInteger.valueOf(i + 1));
        }
        return combination;
    }

    int getPopulation(int rank) {
        return Long.valueOf(this.finalHand.stream().filter(card -> card.RANK == rank).count()).intValue();
    }


//    static int P(final int N, final int K) {
//        int permutation = 1;
//        for (int i = N - K + 1; i <= N; i++) {
//            permutation *= i;
//        }
//        return permutation;
//    }

    static private boolean[] binaryToBooleanArray(int number, int arrayLength) { //referred to ptomli answered Nov 16 '11 at 12:26 in StackOverFlow
        boolean[] booleanArray = new boolean[arrayLength];
        for (int i = 0; i < arrayLength; i++) {
            booleanArray[i] = (number & (1 << i)) != 0;
        }
        return booleanArray;
    }

    class PokerRankWrapper {     // referred from nosid answered May 16 '14 at 15:47 and Stuart Marks edited Sep 18 '14 at 7:45 in StackOverFlow
        private final Card card;

        public PokerRankWrapper(Card card) {
            this.card = card;
        }

        public Card unwrap() {
            return card;
        }

        public boolean equals(Object other) {
            if (other instanceof PokerRankWrapper) {
                return ((PokerRankWrapper) other).card.RANK == ((PokerRankWrapper) other).card.RANK;
            } else {
                return false;
            }
        }

        public int hashCode() {
            return Integer.valueOf(card.RANK).hashCode();
        }
    }

}
