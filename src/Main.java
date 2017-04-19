import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class Main {

    public static void main(String[] args) {
        ArrayList<Card> initialHand = new ArrayList<>();

        ArrayList<Card> fullDeck = new ArrayList<>(Arrays.asList(Card.createFullDeck()));//initialize deck

        Collections.shuffle(fullDeck);//randomized deck

        fullDeck.stream().limit(5).forEach(card -> {
            System.out.println(card);
            initialHand.add(card);
        });

//--------------------test-----
//
//        initialHand.add(new Card(3, 2));
//        initialHand.add(new Card(1, 3));
//        initialHand.add(new Card(1, 2));
//        initialHand.add(new Card(2, 2));
//        initialHand.add(new Card(1, 1));
//        initialHand.stream().limit(5).forEach(System.out::println);


        
        PokerHandAnalyzer.listAllHandsProbability(initialHand);
  }
}
