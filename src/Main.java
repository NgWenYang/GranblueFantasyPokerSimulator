import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class Main {

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        for(int i = 0; i < 1; i++) {
            ArrayList<Card> initialHand = new ArrayList<>();

            ArrayList<Card> fullDeck = new ArrayList<>(Arrays.asList(Card.createFullDeck()));//initialize deck

        Collections.shuffle(fullDeck);//randomized deck
        initialHand.clear();
        fullDeck.stream().limit(5).forEach(card -> {
            System.out.println(card);
            initialHand.add(card);
        });

//--------------------test-----
//            initialHand.add(fullDeck.get(8));
//            initialHand.add(fullDeck.get(1));
//            initialHand.add(fullDeck.get(4));
//            initialHand.add(fullDeck.get(5));
//            initialHand.add(fullDeck.get(0));
//            initialHand.stream().forEach(System.out::println);

            fullDeck.removeAll(initialHand);

            PokerHandAnalyzer.listAllHandsProbability(fullDeck, initialHand);
        }
        System.out.println((System.currentTimeMillis() - start));
    }
}
