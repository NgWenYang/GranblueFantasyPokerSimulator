import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class Main {

    public static void main(String[] args) {
        ArrayList<Card> initialHand = new ArrayList<>();
        ArrayList<Card> finalHand = new ArrayList<>();
        int numberOfCardToReshuffle = 0; // 0-5

        ArrayList<Card> fullDeck = new ArrayList<>(Arrays.asList(Card.createFullDeck()));//initialize deck

        Collections.shuffle(fullDeck);//randomized deck

        fullDeck.stream().limit(5).forEach(card -> {
            System.out.println(card);
            initialHand.add(card);
        });
//        initialHand.add(new Card(5, 2));
//        initialHand.add(new Card(4, 1));
//        initialHand.add(new Card(3, 1));
//        initialHand.add(new Card(1, 2));
//        initialHand.add(new Card(1, 1));
//        initialHand.stream().limit(5).forEach(card -> {
//            System.out.println(card);
//        });
        PokerHandAnalyzer.listAllHandsProbability(initialHand);

//        System.out.println();
        //fullDeck.stream().skip(5).limit(numberOfCardToReshuffle).forEach(System.out::println); // the following reshuffled cards
    }
}
