import java.util.ArrayList;
import java.util.Arrays;

public class Main {

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        ArrayList<Card> initialHand = new ArrayList<>();

        ArrayList<Card> fullDeck = new ArrayList<>(Arrays.asList(Card.createFullDeck()));//initialize deck

//        Collections.shuffle(fullDeck);//randomized deck
//        initialHand.clear();
//        fullDeck.stream().limit(5).forEach(card -> {
//            System.out.println(card);
//            initialHand.add(card);
//        });

//--------------------test-----
        initialHand.add(new Card(0, 0));
        initialHand.add(new Card(4, 3));
        initialHand.add(new Card(1, 2));
        initialHand.add(new Card(2, 2));
        initialHand.add(new Card(1, 1));
        initialHand.stream().forEach(System.out::println);


        PokerHandAnalyzer.listAllHandsProbability(initialHand);

        System.out.println((System.currentTimeMillis() - start));
    }
}
