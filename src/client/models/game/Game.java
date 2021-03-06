package client.models.game;

import client.models.account.Account;
import client.models.card.Card;
import client.models.card.spell.Spell;
import client.models.map.Cell;
import client.models.map.GameMap;

public class Game {
    private GameType gameType;
    private Player playerOne;
    private Player playerTwo;
    private GameMap gameMap;
    private int turnNumber;
    private int lastTurnChangingTime;
    private boolean finished = false;

    protected Game(GameType gameType, Account account1, GameMap gameMap) {

    }

    public GameType getGameType() {
        return gameType;
    }

    public int getLastTurnChangingTime() {
        return lastTurnChangingTime;
    }

    public Player getPlayerOne() {
        return this.playerOne;
    }

    public Player getPlayerTwo() {
        return playerTwo;
    }

    public boolean isFinished() {
        return finished;
    }

    public GameMap getGameMap() {
        return this.gameMap;
    }

    public void addTurnNum() {

    }

    public int getTurnNumber() {
        return this.turnNumber;
    }

    public void receiveMessage(String[] message) {

    }

    public void move(Troop troop, Cell targetCell) {

    }

    public void insert(Card card, Cell targetCell) {

    }

    public void attack(Troop attacker, Troop other) {

    }

    public void useSpell(Troop troop, Spell spell, Cell targetCell) {

    }

    public void nextTurn() {

    }

    public Troop[] getAttackableTroops() {
        return new Troop[]{};
    }

    public Cell[] getSpellableCells(Troop troop, Spell spell) {
        return new Cell[]{};
    }

    public Cell[] getMovableCells(Troop troop) {
        return new Cell[]{};
    }

    public boolean canAttack(Troop myTroop, Troop enemyTroop) {
        return false;
    }

    public boolean canSpell(Troop myTroop, Cell cell) {
        return false;
    }

    public boolean canInsert(Card card, Cell cell) {
        return false;
    }

    public Player getCurrentTurnPlayer() {
        if (turnNumber % 2 == 1) {
            return playerOne;
        } else {
            return playerTwo;
        }
    }

    public Player getOtherTurnPlayer() {
        if (turnNumber % 2 == 0) {
            return playerOne;
        } else {
            return playerTwo;
        }
    }
}