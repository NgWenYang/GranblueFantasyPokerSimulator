import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.stream.Collectors;

/**
 * Created by Wen Yang on 14/4/2017.
 */

public class PokerHandAnalyzer {
    private int totalRemainingDeck = 53;
    //1 for joker, 4 for each card, distribution of cards
    private int[] remainingCardsInDeck = {0, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4};

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
            if ((!cardToShuffle[i])) {
                // move to final hand
                finalHand.add(initialHand.get(i));
            }
        }

        //build hand pattern by removing duplicated rank
        finalHand.stream()
                .filter(card -> card.RANK != 0)
                .map(PokerRankWrapper::new)
                .distinct()
                .map(PokerRankWrapper::unwrap)
                .collect(Collectors.toList())
                .forEach(handPattern::add);
    }

    //generate all patterns and probability
    static void listAllHandsProbability(ArrayList<Card> fullDeck, final ArrayList<Card> initialHand) {
        //initial hand
//        PokerHandAnalyzer original = new PokerHandAnalyzer();
//        System.out.println("initial hand: " + original.getTwoPairProbability());

        //final hand testing
//        PokerHandAnalyzer test = new PokerHandAnalyzer(initialHand, new boolean[]{false, true, false, false, false});
//        System.out.println("final hand" + test.getTwoPairProbability());

        //all possible shuffles (2^5 = 32 types)
        NumberFormat formatter = new DecimalFormat("#0.0000000000");
        for (int i = 0; i < Double.valueOf(Math.pow(2, 5)).intValue(); i++) {
            PokerHandAnalyzer pattern = new PokerHandAnalyzer(initialHand, binaryToBooleanArray(i, 5));
            System.out.println("Probability after shuffling card#" + new StringBuilder(String.format("%5s", Integer.toBinaryString(i)).replace(' ', '0')).reverse().toString() + ": " + formatter.format(pattern.getTwoPairProbability())); //referred from Satish answered May 25 '11 at 20:29 in StackOverFlow
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
        BigInteger combinationCount = BigInteger.ZERO;
        if (finalHand.stream().noneMatch(card -> card.RANK == 0)) {
            combinationCount = combinationCount.add(getPossibleCombination(new LinkedList<>(Arrays.asList(new Integer[]{2, 2, 1})), new LinkedList<>(Arrays.asList(new Integer[]{2, 1}))));

        }
        if (remainingCardsInDeck[0] == 1 || finalHand.stream().anyMatch(card -> card.RANK == 0)) {
            //if (remainingCardsInDeck[0] == 1 ){totalRemainingDeck --;}
            combinationCount = combinationCount.add(getPossibleCombination(new LinkedList<>(Arrays.asList(new Integer[]{2, 1, 1})), new LinkedList<>(Arrays.asList(new Integer[]{1, 2}))));
            //if (remainingCardsInDeck[0] == 1 ){totalRemainingDeck ++;}
        }
        //s1 initial got joker, didnt change, s2 initial got joker, changed, s3 initial no joker, get no joker, s4 initial no joker, get joker

        return new BigDecimal(combinationCount).divide(new BigDecimal(combination(this.totalRemainingDeck, 5 - this.finalHand.size())), 10, RoundingMode.HALF_UP).doubleValue();

    }
    //brute force for checking
    double getTwoPairProbability2(ArrayList<Card> fullDeck){
            long totalMatchingCombination = 0;
            int[] deckSize = new int[5];
            Arrays.fill(deckSize,fullDeck.size());
            for(int i = 0; i < finalHand.size(); i++){
                deckSize[4 - i] = 1;
            }
            for (int i = deckSize[0] == 1? 0 :  0; i < deckSize[0]; i++) {
                for (int j = deckSize[1] == 1? 0 : i + 1; j < deckSize[1]; j++) {
                    for (int k = deckSize[2] == 1? 0 : j + 1; k < deckSize[2]; k++) {
                        for (int l = deckSize[3] == 1? 0 : k + 1; l < deckSize[3]; l++) {
                            for (int m = deckSize[4] == 1? 0 : l + 1; m < deckSize[4]; m++) {
                                ArrayList<Card> hand = new ArrayList<>(finalHand);
                                if (finalHand.size() < 5)
                                hand.add(fullDeck.get(i));
                                if (finalHand.size() < 4)
                                hand.add(fullDeck.get(j));
                                if (finalHand.size() < 3)
                                hand.add(fullDeck.get(k));
                                if (finalHand.size() < 2)
                                hand.add(fullDeck.get(l));
                                if (finalHand.size() < 1)
                                hand.add(fullDeck.get(m));
                                if (checkPattern(hand, new LinkedList<>(Arrays.asList(new Integer[]{2, 2, 1})))) {
                                    totalMatchingCombination++;
                                }
                            }
                        }
                    }
                }
            }
        return new BigDecimal(totalMatchingCombination).divide(new BigDecimal(combination(this.totalRemainingDeck, 5 - this.finalHand.size())), 10, RoundingMode.HALF_UP).doubleValue();
    }

    BigInteger getPossibleCombination(LinkedList winningHandPattern, LinkedList compactWinningHandPattern) {

        //total possible combination count
        BigInteger combinationCount[] = new BigInteger[]{BigInteger.ZERO};

        //all possible combination pattern before going through the deck
        LinkedList<LinkedList<Integer>> combinationPattern = new LinkedList<>();

        // aabbc -> [2,1], 2 x double group + 1 x dingle group
        //LinkedList<Integer> winningHandPattern = new LinkedList<>(Arrays.asList(new Integer[]{2, 2, 1}));
        //LinkedList<Integer> compactWinningHandPattern = new LinkedList<>(Arrays.asList(new Integer[]{2, 1}));

        //already matches the pattern
        if (checkPattern(winningHandPattern)) {
            return combination(this.totalRemainingDeck, 5 - this.finalHand.size());
        }

        //return zero for impossible hand pattern
        if (handPattern.size() > winningHandPattern.size()) {
            return combinationCount[0];
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

        return combinationCount[0];
    }

//    old messy way
//    double getTwoPairProbability() {        //(13-choose-2)(4-choose-2)(4-choose-2)(11-choose-1)(4-choose-1).
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

    //check pattern
    boolean checkPattern(LinkedList<Integer> winningHandPattern) {
        int[] frequency = new int[14];
        for (int i = 0; i < finalHand.size(); i++) {
            if (finalHand.get(i).RANK != 0) {
                frequency[finalHand.get(i).RANK]++;
            }
        }
        for (int i = 0; i < winningHandPattern.size(); i++) {
            for (int j = 0; j < frequency.length; j++) {
                if (winningHandPattern.get(i) == frequency[j]) {
                    frequency[j] = 0;
                    break;
                }else if( j == frequency.length-1){
                    return false;
                }
            }
        }
        return true;
        //return Arrays.stream(frequency).allMatch(f -> f == 0);
    }


    boolean checkPattern(ArrayList<Card> finalHand, LinkedList<Integer> winningHandPattern) {
        int[] frequency = new int[14];
        for (int i = 0; i < finalHand.size(); i++) {
            if (finalHand.get(i).RANK != 0) {
                frequency[finalHand.get(i).RANK]++;
            }
        }
        for (int i = 0; i < winningHandPattern.size(); i++) {
            for (int j = 0; j < frequency.length; j++) {
                if (winningHandPattern.get(i) == frequency[j]) {
                    frequency[j] = 0;
                    break;
                } else if (j == frequency.length - 1) {
                    return false;
                }
            }
        }
        return true;
    }

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
