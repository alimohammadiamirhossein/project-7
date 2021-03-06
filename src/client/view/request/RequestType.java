package client.view.request;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum RequestType {
    SUDO("^sudo .+$"),
    //Account menu:
    CREATE_ACCOUNT("^create account (\\w+)$"),
    LOGIN("^login (\\w+)$"),
    SHOW_LEADER_BOARD("^show leaderboard$"),
    HELP("^help$"),
    //Main menu:
    ENTER_MENU("^enter (\\w+)$"),
    SAVE("^save$"),
    LOGOUT("^logout$"),
    EXIT("^exit$"),
    START_GAME("^start game (\\w+) ([123])\\s?(\\d+)?$"),
    START_MULTIPLAYER_GAME("^start multiplayer game ([123])\\s?(\\d+)?$"),
    SELECT_USER("^select user (\\w+)$"),
    SHOW_ACCOUNT("show account"),
    //collection
    SHOW("^show$"),
    CREATE_DECK("^create deck (\\w+)$"),
    DELETE_DECK("^delete deck (\\w+)$"),
    SEARCH("^search (.+)$"),
    ADD_TO_DECK("^add (\\w+) to deck (\\w+)$"),
    REMOVE_FROM_DECK("^remove (\\w+) from deck (\\w+)$"),
    VALIDATE_DECK("^validate deck (\\w+)$"),
    SELECT_MAIN_DECK("^select deck (\\w+)$"),
    SHOW_ALL_DECKS("^show all decks$"),
    SHOW_DECK("^show deck (\\w+)"),
    //battle menu
    SINGLE_PLAYER("^single player$"),
    MULTI_PLAYER("^multi player$"),
    //single player menu
    STORY("^story$"),
    START_GAME_IN_STORY_MENU("^start game (\\d)$"),
    CUSTOM_GAME("^custom game$"),
    //shop:
    SHOW_COLLECTION("^show collection$"),
    SEARCH_COLLECTION("^search collection (\\w+)$"),
    BUY("^buy (.+)$"),
    SELL("sell (\\w+)$")
    ;
    private Pattern pattern;
    private Matcher matcher;

    RequestType(String pattern) {
        this.pattern = Pattern.compile(pattern);
    }

    public Matcher setMatcher(String command) {
        this.matcher = pattern.matcher(command);
        return matcher;
    }

    public Matcher getMatcher() {
        return matcher;
    }
}