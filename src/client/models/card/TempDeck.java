package client.models.card;

import java.util.ArrayList;

public class TempDeck {
    private String deckName;
    private String heroId;
    private String itemId;
    private ArrayList<String> othersIds = new ArrayList<>();

    public TempDeck(Deck deck) {
        this.deckName = deck.getName();
        this.heroId = deck.getHero().getCardId();
        this.itemId = deck.getItem().getCardId();
        for (Card card : deck.getOthers()) {
            this.othersIds.add(card.getCardId());
        }
    }

    public String getDeckName() {
        return deckName;
    }

    public String getHeroId() {
        return heroId;
    }

    public String getItemId() {
        return itemId;
    }

    public ArrayList<String> getOthersIds() {
        return othersIds;
    }
}
