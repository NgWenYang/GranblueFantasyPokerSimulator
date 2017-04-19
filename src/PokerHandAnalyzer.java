import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.stream.Collectors;

/**
 * Created by Wen Yang on 14/4/2017.
 */

public class PokerHandAnalyzer {
    private int totalRemainingDeck = 52;
    private int[] remainingCardsInDeck = {0, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4};             //1 for joker, 4 for each card, distribution of cards

    private ArrayList<Card> finalHand = new ArrayList<>();
    private ArrayList<Card> handPattern = new ArrayList<>();

    //BigInteger combinationCount = BigInteger.ZERO;

    //empty constructor
    private PokerHandAnalyzer() {
    }

    //default constructor
    private PokerHandAnalyzer(ArrayList<Card> initialHand, boolean[] cardToShuffle) {
        //remove drawn cards from deck
        initialHand.forEach(card -> remainingCardsInDeck[card.RANK]--);

        //build final hand based on remaining cards from initial hand after shuffle
        for (int i = 0; i < cardToShuffle.length; i++) {
            //minus the initial hand from deck
            totalRemainingDeck--;
            //draw a card
            if (cardToShuffle[i]) {
                totalRemainingDeck--;
            } else {
                // move to final hand
                finalHand.add(initialHand.get(i));
            }
        }

        //build hand pattern by removing duplicated rank
        finalHand.stream().map(PokerRankWrapper::new).distinct().map(PokerRankWrapper::unwrap).forEach(handPattern::add);

    }

    //generate all patterns and probability
    static void listAllHandsProbability(final ArrayList<Card> initialHand) {
        //initial hand
        PokerHandAnalyzer original = new PokerHandAnalyzer();
        System.out.println("initial hand: " + original.getTwoPairProbability());

        //final hand testing
//        PokerHandAnalyzer test = new PokerHandAnalyzer(initialHand, new boolean[]{false, true, false, false, false});
//        System.out.println("final hand" + test.getTwoPairProbability());

        //all possible shuffles (2^5 = 32 types)
        for (int i = 0; i < Double.valueOf(Math.pow(2, 5)).intValue(); i++) {
            PokerHandAnalyzer pattern = new PokerHandAnalyzer(initialHand, binaryToBooleanArray(i, 5));
            System.out.println("Probability after shuffling card#" + new StringBuilder(String.format("%5s", Integer.toBinaryString(i)).replace(' ', '0')).reverse().toString() + ": " + pattern.getTwoPairProbability()); //referred from Satish answered May 25 '11 at 20:29 in StackOverFlow
        }


    }

    //create all possible subsets of pattern, referred from the question answered Feb 15 '11 at 5:16 by paxdiablo on StackOverFlow
    void subset(LinkedList<LinkedList<Integer>> combinationPattern, LinkedList<Integer> elements, LinkedList<Integer> prefix, LinkedList<Integer> list, LinkedList<Integer> count) { //mutable? immutable? pass by value...? all are confusing
        if (count.getFirst() == 0) {
            if (count.size() == 1) {
                combinationPattern.add(prefix);
            } else {
                count = new LinkedList<>(count.subList(1, count.size()));
                LinkedList<Integer> newList = new LinkedList<>(elements);
                LinkedList<Integer> tempPrefix = new LinkedList<>(prefix);
                newList.removeIf(element -> {
                    if (tempPrefix.contains(element)) {
                        if (element == -1) {
                            tempPrefix.remove(element);
                        }
                        return true;
                    } else {
                        return false;
                    }
                });
                subset(combinationPattern, elements, prefix, newList, count);
            }
        } else {
            for (int i = 0; i < list.size(); i++) {
                LinkedList<Integer> newPrefix = new LinkedList<>(prefix);
                newPrefix.add(list.get(i));
                LinkedList<Integer> newCount = new LinkedList<>(count);
                newCount.set(0, newCount.get(0) - 1);
                subset(combinationPattern, elements, newPrefix, new LinkedList<>(list.subList(i + 1, list.size())), newCount);
            }
        }
    }

    void fillPossibleCard(BigInteger[] combinationCount, LinkedList<Integer> visitedRank, LinkedList<Integer> combinationPattern, LinkedList<Integer> winningHandPattern, int count) {
        if (winningHandPattern.size() == visitedRank.size()) {
            BigInteger tempCombination = BigInteger.ONE;
            for (int i = 0; i < visitedRank.size(); i++) {
                tempCombination = tempCombination.multiply(combination(remainingCardsInDeck[visitedRank.get(i)], winningHandPattern.get(i) - getPopulation(visitedRank.get(i))));
            }
            combinationCount[0] = combinationCount[0].add(tempCombination);
        } else {
            int i = 0;
            if (visitedRank.size() != 0) {
                if ((winningHandPattern.get(visitedRank.size()) == winningHandPattern.get(visitedRank.size() - 1)) && (combinationPattern.get(count - 1) == -1) && (combinationPattern.get(count) == -1)) {
                    i = visitedRank.getLast() + 1;
                }
            }
            while (i < remainingCardsInDeck.length) {
                if ((!visitedRank.contains(i)) && (combinationPattern.get(count) == i || combinationPattern.get(count) == -1) && (getPopulation(i) <= winningHandPattern.get(visitedRank.size())) && (remainingCardsInDeck[i] >= winningHandPattern.get(visitedRank.size()) - getPopulation(i))) {
                    LinkedList<Integer> newVisitedRank = new LinkedList<>(visitedRank);
                    newVisitedRank.add(i);
                    fillPossibleCard(combinationCount, newVisitedRank, combinationPattern, winningHandPattern, count + 1);
                }
                i++;
            }
        }
    }

    //new way of generating probability
    double getTwoPairProbability() {
        //final probability
        double probability = 0.0;

        //total possible combination count
        BigInteger combinationCount[] = new BigInteger[]{BigInteger.ZERO};

        //number of all possible combinationPattern from the pattern
        BigInteger totalDrawingChance = combination(this.totalRemainingDeck, 5 - this.finalHand.size());

        //all possible combination pattern before going through the deck
        LinkedList<LinkedList<Integer>> combinationPattern = new LinkedList<>();

        // aabbc -> [2,1], 2 x double group + 1 x dingle group
        LinkedList<Integer> winningHandPattern = new LinkedList<>(Arrays.asList(new Integer[]{2, 2, 1}));
        LinkedList<Integer> compactWinningHandPattern = new LinkedList<>(Arrays.asList(new Integer[]{2, 1}));

        //return zero for impossible hand pattern
        if (handPattern.size() > winningHandPattern.size()) {
            return probability;
        }

        // add empty space slot for drawing card to pattern
        for (int i = handPattern.size(); i < winningHandPattern.size(); i++) {
            handPattern.add(new Card(-1, -1));//
        }

        // transfer cards pattern to integer pattern before generating combinations
        LinkedList<Integer> elements = new LinkedList<>();
        handPattern.forEach(card -> elements.add(card.RANK));

        //generating patterns before going through the deck
        subset(combinationPattern, elements, new LinkedList<>(), elements, compactWinningHandPattern);
        combinationPattern = new LinkedList<>(combinationPattern.stream().distinct().collect(Collectors.toList()));

        //
        for (int i = 0; i < combinationPattern.size(); i++) {
            fillPossibleCard(combinationCount, new LinkedList<>(), combinationPattern.get(i), winningHandPattern, 0);
        }

        probability = new BigDecimal(combinationCount[0]).divide(new BigDecimal(totalDrawingChance), 10, RoundingMode.HALF_UP).doubleValue();
        return probability;
    }

//    old messy way
//    double getTwoPairProbability2() {        //(13-choose-2)(4-choose-2)(4-choose-2)(11-choose-1)(4-choose-1).
//        double probability = 0.0;
//        int numberOfDistinctGroup = 3; // aabbc -> 3 groups, which are a, b, and c
//        ArrayList<Card> handPattern = new ArrayList<>();//fulfilled groups
//        finalHand.stream().map(PokerRankWrapper::new).distinct().map(PokerRankWrapper::unwrap).forEach(handPattern::add);//remove duplicated rank and then add to hand pattern
//        if (handPattern.size() > numberOfDistinctGroup) {
//            return probability;
//        }
//        for (int i = handPattern.size(); i < numberOfDistinctGroup; i++) {
//            handPattern.add(new Card(-1, -1));// add based on remaining empty group
//        }
//        BigInteger totalChance = combination(this.totalRemainingDeck, 5 - this.finalHand.size());
//        BigInteger possibleCombination = BigInteger.ZERO;
//        boolean handDuplicateLookup[][] = new boolean[handPattern.size()][handPattern.size()];
//        boolean deckDuplicateLookup[][] = new boolean[remainingCardsInDeck.length][remainingCardsInDeck.length];
//
//        for (int m = 0; m < handPattern.size(); m++) {
//            for (int n = 0; n < handPattern.size(); n++) {
//                if (n != m && !handDuplicateLookup[m][n]) {
//                    handDuplicateLookup[m][n] = true;
//                    handDuplicateLookup[n][m] = true; // marked as visited
//                    for (int o = 0; o < handPattern.size(); o++) {
//                        if (o != m && o != n) {
//                            for (int i = 1; i < this.remainingCardsInDeck.length; i++) {
//                                if (this.remainingCardsInDeck[i] >= 2 - this.getPopulation(handPattern.get(m).RANK) && this.getPopulation(handPattern.get(m).RANK) <= 2 && (handPattern.get(m).RANK == -1 || i == handPattern.get(m).RANK)) { //replace 2 with variable
//                                    for (int j = 1; j < this.remainingCardsInDeck.length; j++) {
//                                        if ((this.remainingCardsInDeck[j] >= 2 - this.getPopulation(handPattern.get(n).RANK)) && (this.getPopulation(handPattern.get(n).RANK) <= 2) && ((handPattern.get(n).RANK == -1) || (j == handPattern.get(n).RANK)) && (!deckDuplicateLookup[i][j]) && (i != j)) {
//                                            deckDuplicateLookup[i][j] = true;
//                                            deckDuplicateLookup[j][i] = true;//visited
//                                            for (int k = 1; k < this.remainingCardsInDeck.length; k++) {
//                                                if ((k != i && k != j) && (this.remainingCardsInDeck[k] >= 1 - this.getPopulation(handPattern.get(o).RANK)) && (this.getPopulation(handPattern.get(o).RANK) <= 1) && (handPattern.get(o).RANK == -1 || k == handPattern.get(o).RANK)) {
//                                                    possibleCombination = possibleCombination.add(combination(this.remainingCardsInDeck[i], 2 - this.getPopulation(handPattern.get(m).RANK)).multiply(combination(this.remainingCardsInDeck[j], 2 - this.getPopulation(handPattern.get(n).RANK))).multiply(combination(this.remainingCardsInDeck[k], 1 - this.getPopulation(handPattern.get(o).RANK))));
//                                                }
//                                            }
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
//        probability = new BigDecimal(possibleCombination).divide(new BigDecimal(totalChance), 10, RoundingMode.HALF_UP).doubleValue();
//        return probability;
//    }

    //calculate the number of combination
    static BigInteger combination(final int N, final int K) {  //referred from polygenelubricants answered May 28 '10 at 14:34 in StackOverFlow
        BigInteger combination = BigInteger.ONE;
        for (int i = 0; i < K; i++) {
            combination = combination.multiply(BigInteger.valueOf(N - i))
                    .divide(BigInteger.valueOf(i + 1));
        }
        return combination;
    }

    //get number of cards of a certain rank in remaining final hand
    int getPopulation(int rank) {
        return Long.valueOf(this.finalHand.stream().filter(card -> card.RANK == rank).count()).intValue();
    }

    //turns a number to binary array
    static private boolean[] binaryToBooleanArray(int number, int arrayLength) { //referred from ptomli answered Nov 16 '11 at 12:26 in StackOverFlow
        boolean[] booleanArray = new boolean[arrayLength];
        for (int i = 0; i < arrayLength; i++) {
            booleanArray[i] = (number & (1 << i)) != 0;
        }
        return booleanArray;
    }

    //wrap the pokers to another class to compare rank
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

    //generate number of permutation
//    static int permutation(final int N, final int K) {
//        int permutation = 1;
//        for (int i = N - K + 1; i <= N; i++) {
//            permutation *= i;
//        }
//        return permutation;
//    }

    //generate all subsets of combination
//    static void subset(LinkedList<Integer> prefix, List<Integer> list, int count) {
//        if (count == 0) {
//            combinationPattern.add(prefix);
//            return;
//        } else {
//            for (int i = 0; i < list.size(); i++) {
//                LinkedList<Integer> newPrefix = new LinkedList<>(prefix);
//                newPrefix.add(list.get(i));
//                subset(newPrefix, list.subList(i + 1, list.size()), count - 1);
//            }
//        }
//    }
//


// for the joker's sake
// https://poker.stackexchange.com/questions/2/how-does-a-wild-card-53rd-affect-the-odds-of-making-the-standard-poker-hands
}
