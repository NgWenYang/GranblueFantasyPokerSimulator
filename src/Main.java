import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class Main {

    public static void main(String[] args) {

        ArrayList<Card> fullDeck = new ArrayList<>(Arrays.asList(Card.createFullDeck()));
        Collections.shuffle(fullDeck);
        fullDeck.stream().limit(5).forEach(System.out::println);
        System.out.println();
        fullDeck.stream().skip(5).limit(5).forEach(System.out::println); // next 5 cards
    }
}
