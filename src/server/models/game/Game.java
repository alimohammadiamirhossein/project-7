package server.models.game;

import server.Server;
import server.models.account.Account;
import server.models.card.AttackType;
import server.models.card.Card;
import server.models.card.CardType;
import server.models.card.spell.Spell;
import server.models.card.spell.SpellAction;
import server.models.map.Cell;
import server.models.map.GameMap;
import server.models.map.Position;

import java.util.ArrayList;

public abstract class Game {
    private GameType gameType;
    private Player playerOne;
    private Player playerTwo;
    private ArrayList<Buff> buffs;
    private GameMap gameMap;
    private int turnNumber;
    private int lastTurnChangingTime;
    private boolean finished = false;

    protected Game(GameType gameType, Account accountOne, Account accountTwo, GameMap gameMap) {
        this.gameType = gameType;
        this.gameMap = gameMap;
        this.playerOne = new Player(accountOne);
        this.playerTwo = new Player(accountTwo);
        put(1,playerOne.getHero(),gameMap.getCell(2,0));
        put(2,playerTwo.getHero(),gameMap.getCell(2,8));
        this.turnNumber = 1;
        applyStartSpells(playerOne);
        applyStartSpells(playerTwo);
    }

    private void applyStartSpells(Player player) {
        for (Card card : player.getDeck().getOthers()) {
            for (Spell spell : card.getSpells()) {
                if (spell.getAvailabilityType().isOnStart())
                    applySpell(
                            spell, detectTarget(
                                    spell, gameMap.getCell(0, 0), gameMap.getCell(0, 0), gameMap.getCell(0, 0))
                    );
            }

        }
    }

    public Player getPlayerOne() {
        return playerOne;
    }

    public Player getPlayerTwo() {
        return playerTwo;
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

    private boolean canCommand(String username) {
        return (turnNumber % 2 == 0 && username.equalsIgnoreCase(playerTwo.getUserName()))
                || (turnNumber % 2 == 1 && username.equalsIgnoreCase(playerOne.getUserName()));
    }

    public void changeTurn(String username) throws Exception {
        if (canCommand(username)) {
            getCurrentTurnPlayer().addNextCardToHand();
            revertNotDurableBuffs();
            turnNumber++;
            Server.getInstance().sendChangeTurnMessage(this, turnNumber);
            applyAllBuffs();
            allTroopsCanAttack();
        } else {
            throw new Exception("it isn't your turn!");
        }
    }

    private void allTroopsCanAttack() {
        for (Troop troop : playerOne.getTroops()) {
            troop.setCanAttack(true);
        }
        for (Troop troop : playerTwo.getTroops()) {
            troop.setCanAttack(true);
        }
    }

    private void applyAllBuffs() {
        for (Buff buff : buffs) {
            applyBuff(buff);
        }
    }

    private void revertNotDurableBuffs() {
        for (Buff buff : buffs) {
            if (!buff.getAction().isDurable()) {
                revertBuff(buff);
            }
        }
    }

    private void revertBuff(Buff buff) {
        SpellAction action = buff.getAction();

        for (Troop troop : buff.getTarget().getTroops()) {
            if (!(buff.isPositive() || troop.canGiveBadEffect())) continue;

            troop.changeEnemyHit(-action.getEnemyHitChanges());
            troop.changeCurrentAp(-action.getApChange());
            if (!action.isPoison() || troop.canGetPoison()) {
                troop.changeCurrentHp(-action.getHpChange());
                if (troop.getCurrentHp() <= 0) {
                    killTroop(troop);
                }
            }
            if (action.isMakeStun() && troop.canGetStun()) {
                troop.setCanMove(true);
            }
            if (action.isMakeDisarm() && troop.canGetDisarm()) {
                troop.setDisarm(false);
            }
            if (action.isNoDisarm()) {
                troop.setCantGetDisarm(false);
            }
            if (action.isNoPoison()) {
                troop.setCantGetPoison(false);
            }
            if (action.isNoStun()) {
                troop.setCantGetStun(false);
            }
            if (action.isNoBadEffect()) {
                troop.setDontGiveBadEffect(false);
            }
            if (action.isNoAttackFromWeakerOnes()) {
                troop.setNoAttackFromWeakerOnes(false);
            }
            if (action.isDisableHolyBuff()) {
                troop.setDisableHolyBuff(false);
            }
        }
    }

    public void moveTroop(String username, String cardId, Position position) throws Exception {

    }

    public void insert(String username, String cardId, Position position) throws Exception {
        if (!canCommand(username)) {
            throw new Exception("its not your turn");
        }
        put(2 - (turnNumber % 2), getCurrentTurnPlayer().insert(cardId, gameMap.getCellWithPosition(position)), gameMap.getCellWithPosition(position));
    }

    public void put(int playerNumber, Troop troop, Cell cell) {
        troop.setCell(cell);
        gameMap.addTroop(playerNumber, troop);
        for (Spell spell :
                troop.getCard().getSpells()) {
            if (spell.getAvailabilityType().isOnPut())
                applySpell(spell, detectTarget(spell, cell, gameMap.getCell(0, 0), gameMap.getCell(0, 0)));
        }
    }

    public void attack(String username, String attackerCardId, String defenderCardId) throws Exception {
        if (!canCommand(username)) {
            throw new Exception("its not your turn");
        }

        Troop attackerTroop = getCurrentTurnPlayer().getTroop(attackerCardId);
        if (attackerTroop == null) {
            throw new Exception("attacker id is not valid");
        }

        Troop defenderTroop = getOtherTurnPlayer().getTroop(defenderCardId);
        if (defenderTroop == null) {
            throw new Exception("target id is not valid");
        }

        attack(attackerTroop, defenderTroop);
    }

    public void attack(Troop attackerTroop, Troop defenderTroop) throws Exception {
        if (!attackerTroop.canAttack()) {
            throw new Exception("attacker can not attack");
        }

        if (attackerTroop.getCard().getAttackType() == AttackType.MELEE) {
            if (!attackerTroop.getCell().isNextTo(defenderTroop.getCell())) {
                throw new Exception("can not attack to this target");
            }
        } else if (attackerTroop.getCard().getAttackType() == AttackType.RANGED) {
            if (attackerTroop.getCell().isNextTo(defenderTroop.getCell()) ||
                    attackerTroop.getCell().manhattanDistance(defenderTroop.getCell()) > attackerTroop.getCard().getRange()) {
                throw new Exception("can not attack to this target");
            }
        } else { // HYBRID
            if (attackerTroop.getCell().manhattanDistance(defenderTroop.getCell()) > attackerTroop.getCard().getRange()) {
                throw new Exception("can not attack to this target");
            }
        }

        if (defenderTroop.canGiveBadEffect() &&
                (defenderTroop.canBeAttackedFromWeakerOnes() || attackerTroop.getCurrentAp() > defenderTroop.getCurrentAp())) {
            int attackPower = attackerTroop.getCurrentAp();
            if (!attackerTroop.isHolyBuffDisabling() || defenderTroop.getEnemyHitChanges() > 0) {
                attackPower += defenderTroop.getEnemyHitChanges();
            }
            defenderTroop.changeCurrentHp(attackPower);
            attackerTroop.setCanAttack(false);
            counterAttack(defenderTroop, attackerTroop);
        }
    }

    private void counterAttack(Troop defenderTroop, Troop attackerTroop) throws Exception {
        if (defenderTroop.isDisarm()) {
            throw new Exception("defender is disarm");
        }

        if (defenderTroop.getCard().getAttackType() == AttackType.MELEE) {
            if (!defenderTroop.getCell().isNextTo(attackerTroop.getCell())) {
                throw new Exception("can not counter attack to this target");
            }
        } else if (defenderTroop.getCard().getAttackType() == AttackType.RANGED) {
            if (defenderTroop.getCell().isNextTo(attackerTroop.getCell()) ||
                    defenderTroop.getCell().manhattanDistance(attackerTroop.getCell()) > defenderTroop.getCard().getRange()) {
                throw new Exception("can not counter attack to this target");
            }
        } else { // HYBRID
            if (defenderTroop.getCell().manhattanDistance(attackerTroop.getCell()) > defenderTroop.getCard().getRange()) {
                throw new Exception("can not counter attack to this target");
            }
        }

        if (attackerTroop.canGiveBadEffect() &&
                (attackerTroop.canBeAttackedFromWeakerOnes() || defenderTroop.getCurrentAp() > attackerTroop.getCurrentAp())) {
            int attackPower = defenderTroop.getCurrentAp();
            if (!defenderTroop.isHolyBuffDisabling() || attackerTroop.getEnemyHitChanges() > 0) {
                attackPower += attackerTroop.getEnemyHitChanges();
            }
            attackerTroop.changeCurrentHp(attackPower);
        }
    }

    public void useSpecialPower(String username, String CardId, Position target) throws Exception {

    }

    public void comboAttack(String username, String[] attackerCardIds, String defenderCardId) throws Exception {

    }

    public abstract void finishCheck();

    public Troop[] getAttackableTroops(String cardId) {
        return new Troop[]{};
    }

    public Cell[] getSpellableCells(String cardId, String spellId) {
        return new Cell[]{};
    }

    public Cell[] getMovableCells(String cardId) {
        return new Cell[]{};
    }

    public boolean canAttack(String attackerCardId, String defenderCardId) {
        return false;
    }

    public boolean canSpell(String cardId, Position position) {
        return false;
    }

    public boolean canInsert(String cardId, Position position) {
        return false;
    }

    private void applySpell(Spell spell, TargetData target) {
        spell.setLastTurnUsed(turnNumber);
        Buff buff = new Buff(spell.getAction(), target);
        buffs.add(buff);
        applyBuff(buff);
    }

    private void applyBuff(Buff buff) {
        TargetData target = buff.getTarget();
        if (haveDelay(buff)) return;

        applyBuffOnCards(buff, target.getCards());
        applyBuffOnCellTroops(buff, target.getCells());
        applyBuffOnTroops(buff, target.getTroops());
        applyBuffOnPlayers(buff, target.getPlayers());

        decreaseDuration(buff);
    }

    private void applyBuffOnPlayers(Buff buff, ArrayList<Player> players) {
        SpellAction action = buff.getAction();
        for (Player player : players) {
            player.changeCurrentMP(action.getMpChange());
        }
    }

    private void decreaseDuration(Buff buff) {
        SpellAction action = buff.getAction();
        if (action.getDuration() > 0) {
            action.decreaseDuration();
        }
        if (action.getDuration() == 0) {
            buffs.remove(buff);
        }
    }

    private boolean haveDelay(Buff buff) {
        SpellAction action = buff.getAction();
        if (action.getDelay() > 0) {
            action.decreaseDelay();
            return true;
        }
        return false;
    }

    private void applyBuffOnCards(Buff buff, ArrayList<Card> cards) {
        SpellAction action = buff.getAction();
        for (Card card : cards) {
            if (action.isAddSpell()) {
                card.addSpell(action.getCarryingSpell());
            }
        }
    }

    private void applyBuffOnCellTroops(Buff buff, ArrayList<Cell> cells) {
        ArrayList<Troop> inCellTroops = getInCellTargetTroops(cells);
        Buff troopBuff = new Buff(
                buff.getAction().makeCopyAction(1, 0), new TargetData(inCellTroops)
        );
        buffs.add(troopBuff);
        applyBuffOnTroops(troopBuff, inCellTroops);
    }

    private void applyBuffOnTroops(Buff buff, ArrayList<Troop> targetTroops) {
        SpellAction action = buff.getAction();
        for (Troop troop : targetTroops) {
            if (!(buff.isPositive() || troop.canGiveBadEffect())) continue;

            troop.changeEnemyHit(action.getEnemyHitChanges());
            troop.changeCurrentAp(action.getApChange());
            if (!action.isPoison() || troop.canGetPoison()) {
                troop.changeCurrentHp(action.getHpChange());
                if (troop.getCurrentHp() <= 0) {
                    killTroop(troop);
                }
            }
            if (action.isMakeStun() && troop.canGetStun()) {
                troop.setCanMove(false);
            }
            if (action.isMakeDisarm() && troop.canGetDisarm()) {
                troop.setDisarm(true);
            }
            if (action.isNoDisarm()) {
                troop.setCantGetDisarm(true);
            }
            if (action.isNoPoison()) {
                troop.setCantGetPoison(true);
            }
            if (action.isNoStun()) {
                troop.setCantGetStun(true);
            }
            if (action.isNoBadEffect()) {
                troop.setDontGiveBadEffect(true);
            }
            if (action.isNoAttackFromWeakerOnes()) {
                troop.setNoAttackFromWeakerOnes(true);
            }
            if (action.isDisableHolyBuff()) {
                troop.setDisableHolyBuff(true);
            }
            if (action.isKillsTarget()) {
                killTroop(troop);
            }
            if (action.getRemoveBuffs() > 0) {
                removePositiveBuffs(troop);
            }
            if (action.getRemoveBuffs() < 0) {
                removeNegativeBuffs(troop);
            }
        }
    }

    private void removePositiveBuffs(Troop troop) {
        for (Buff buff : buffs) {
            if (buff.isPositive() && buff.getAction().getDuration() >= 0) {
                buff.getTarget().getTroops().remove(troop);
            }
        }
    }

    private void removeNegativeBuffs(Troop troop) {
        for (Buff buff : buffs) {
            if (!buff.isPositive() && buff.getAction().getDuration() >= 0) {
                buff.getTarget().getTroops().remove(troop);
            }
        }
    }

    private void killTroop(Troop troop) {

    }

    private ArrayList<Troop> getInCellTargetTroops(ArrayList<Cell> cells) {
        ArrayList<Troop> inCellTroops = new ArrayList<>();
        for (Cell cell : cells) {
            Troop troop = playerOne.getTroop(cell);
            if (troop == null) {
                troop = playerTwo.getTroop(cell);
            }
            if (troop != null) {
                inCellTroops.add(troop);
            }
        }
        return inCellTroops;
    }

    private TargetData detectTarget(Spell spell, Cell cardCell, Cell clickCell, Cell heroCell) {
        TargetData targetData = new TargetData();
        Player player = getCurrentTurnPlayer();
        if (spell.getTarget().getCardType().isPlayer()) {
            targetData.getPlayers().add(player);
            return targetData;
        }
        Position centerPosition;
        if (spell.getTarget().isRelatedToCardOwnerPosition()) {
            centerPosition = new Position(cardCell);
        } else if (spell.getTarget().isForAroundOwnHero()) {
            centerPosition = new Position(heroCell);
        } else {
            centerPosition = new Position(clickCell);
        }
        ArrayList<Cell> targetCells = detectCells(centerPosition, spell.getTarget().getDimensions());
        detectTargets(spell, targetData, player, targetCells);
        return targetData;
    }

    private void detectTargets(Spell spell, TargetData targetData, Player player, ArrayList<Cell> targetCells) {
        for (Cell cell : targetCells) {
            if (spell.getTarget().getCardType().isCell()) {
                targetData.getCells().add(cell);
            }
            if (spell.getTarget().getCardType().isHero()) {
                Troop troop = player.getTroop(cell);
                if (troop == null) {
                    troop = getOtherTurnPlayer().getTroop(cell);
                }
                if (troop != null) {
                    if (troop.getCard().getType() == CardType.HERO) {
                        targetData.getTroops().add(troop);
                    }
                }
            }
            if (spell.getTarget().getCardType().isMinion()) {
                Troop troop = player.getTroop(cell);
                if (troop == null) {
                    troop = getOtherTurnPlayer().getTroop(cell);
                }
                if (troop != null) {
                    if (troop.getCard().getType() == CardType.MINION) {
                        targetData.getTroops().add(troop);
                    }
                }
            }
        }
    }

    private ArrayList<Cell> detectCells(Position centerPosition, Position dimensions) {
        int firstRow = centerPosition.getRow() - (dimensions.getRow() - 1) / 2;
        int lastRow = centerPosition.getRow() + dimensions.getRow();
        int firstColumn = centerPosition.getColumn() - (dimensions.getColumn() - 1) / 2;
        int lastColumn = centerPosition.getColumn() + dimensions.getColumn();
        if (firstRow < 0)
            firstRow = 0;
        if (lastRow > GameMap.getRowNumber())
            lastRow = GameMap.getRowNumber();
        if (firstColumn < 0)
            firstColumn = 0;
        if (lastColumn > GameMap.getColumnNumber())
            lastColumn = GameMap.getColumnNumber();
        ArrayList<Cell> targetCells = new ArrayList<>();
        for (int i = firstRow; i <= lastRow; i++) {
            for (int j = firstColumn; j <= lastColumn; j++) {
                targetCells.add(gameMap.getCells()[i][j]);
            }
        }
        return targetCells;
    }
}