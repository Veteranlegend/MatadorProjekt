package Controler;

import Model.*;
import Model.Chance.ChanceCard;
import Model.Fields.*;
import View.BoardGUI;
import View.ViewGUI;
import gui_main.GUI;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

import static Model.Account.*;

public class  Spil {
    final int JAILFIELD = 10;

    public void play() {

        Dice dice1 = new Dice(6, 1);
        Dice dice2 = new Dice(6, 1);
        BoardGUI boardGUI = new BoardGUI();
        FieldList fl = new FieldList();
        Field[] fields = fl.CreateFieldList();
        ChanceDeck deck = new ChanceDeck();

        GUI gui = new GUI(boardGUI.guiFields(fields), Color.WHITE);
        ViewGUI viewGUI = new ViewGUI(gui);
        Spiller spiller;

        String amountPlayers = gui.getUserButtonPressed("Vælg antallet af spillere", "3", "4", "5", "6");

        SpillerListe sl = new SpillerListe(Integer.parseInt(amountPlayers));

        sl.setCurrentPlayer(0);

        viewGUI.addPlayers(sl);

        boolean endGame = false;
        Spiller winner = null;

        while (!endGame) {
            spiller = sl.getCurrentPlayer();
            spiller.setSetOutofJailCard(true);

            int totalBankRupts = 0;
            for (int i = 0; i < Integer.parseInt(amountPlayers); i++) {
                if (sl.getPlayerList(i).getAccount().getBalance() <= 0) {
                    totalBankRupts++;
                }
            }
            if (totalBankRupts == Integer.parseInt(amountPlayers) - 1) {
                endGame = true;
                gui.showMessage("Game ends");
            }
            if (spiller.isBankRupt()) {
                pantsætning(gui, viewGUI, spiller, fl);
                if (spiller.hasLost()) {
                    sl.getNextPlayer();
                }
                continue;
            }
            spiller.setPassingMoney(true);

            if (spiller.isJail()) {
                spiller.setJailTurns(spiller.getJailTurns() + 1);

                String jailString = gui.getUserButtonPressed("Vælg den måde du vil komme ud af fængslet på", "Betal en bøde på 1000", "Benyt et løsladelseskort", "Forsøg at kaste 2 ens terninger");

                if (jailString.equals("Betal en bøde på 1000")) {
                    if (spiller.getAccount().getBalance() > 1000) {
                        withdraw(spiller.getAccount(), 1000, spiller);
                        viewGUI.updateBalance(sl);
                        spiller.setJail(false);
                    }
                }

                if (jailString.equals("Forsøg at kaste 2 ens terninger")) {
                    viewGUI.setDice(dice1.roll(), dice2.roll());
                    if (dice1.getFaceValue() == dice2.getFaceValue()) {
                        spiller.setJail(false);
                        OnOwneble(dice1, dice2, fl, gui, viewGUI, spiller, sl);
                    }
                }

                if (jailString.equals("Benyt løsladelseskort") && spiller.isSetOutofJailCard()) {
                    spiller.setSetOutofJailCard(false);
                    spiller.setJail(false);
                }

                if (spiller.getJailTurns() >= 3) {
                    spiller.setJail(false);
                }
            } else {
                spiller.extraTurns = 0;

                takeTurn(gui, viewGUI, spiller, dice1, dice2, fl, sl, deck);

                while (spiller.getExtraTurn()) {
                    if (spiller.extraTurns < 2) {
                        takeTurn(gui, viewGUI, spiller, dice1, dice2, fl, sl, deck);
                    } else {
                        gui.showMessage("Du går til fængslet");
                        spiller.setPassingMoney(false);
                        spiller.setJail(true);
                        viewGUI.moveCarToField(spiller, JAILFIELD);
                        break;
                    }
                }
                winner = spiller;
            }
            sl.getNextPlayer();
        }
        assert winner != null;
        viewGUI.showMessage(winner.getName() + "has won the game");
    }private void takeTurn(GUI gui, ViewGUI viewGUI, Spiller spiller, Dice dice1, Dice dice2, FieldList fl, SpillerListe sl, ChanceDeck deck) {
        gui.showMessage("Kast med terninger " + spiller.getName());
        viewGUI.setDice(dice1.roll(), dice2.roll());
        spiller.setExtraTurn(dice1.getFaceValue() == dice2.getFaceValue());
        OnOwneble(dice1, dice2, fl, gui, viewGUI, spiller, sl);
        Field currentField = fl.getField(spiller.getPosition());

        pantsætning(gui, viewGUI, spiller, fl);

        if (currentField instanceof Tax) {
            withdraw(spiller.getAccount(), ((Tax) currentField).getTax(), spiller);
            viewGUI.updateBalance(sl);
        }

        if (currentField instanceof Chance) {
            String drawCard = gui.getUserButtonPressed("Træk et hvilket som helst kort", "Træk kort");
            ChanceCard chanceCard = deck.drawCard();
            chanceCard.doCard(spiller, viewGUI);
            viewGUI.showChanceCard(chanceCard.getDescription());
        }

        if (spiller.getPosition() == 30) {
            gui.showMessage("Du går til fængslet");
            spiller.setPassingMoney(false);
            spiller.setJail(true);
            viewGUI.moveCarToField(spiller, JAILFIELD);
        }

        if (spiller.isPassingMoney() && spiller.previousPosition > spiller.getPosition()) {
            deposit(spiller.getAccount(), 4000);
        }

        // For houses, will appear if you have all the fields in a color
        if (currentField instanceof Street) {
            if (ownedGroupHouses(((Street) currentField).getColor(), spiller, fl)) {
                String buyHouse = gui.getUserButtonPressed("Vil du købe et hus til " + currentField.getName(), "Ja", "Nej");
                if (buyHouse.equals("Ja")) {
                    if (spiller.getAccount().getBalance() > ((Street) currentField).getHousePrice()) {
                        withdraw(spiller.getAccount(), ((Street) currentField).getHousePrice(), spiller);
                        ((Street) currentField).setHouseAmount(((Street) currentField).getHouseAmount() + 1);
                        viewGUI.updateBalance(sl);
                        viewGUI.buyHouseHotel((Street) currentField, spiller.getPosition());
                    }
                }
            }

            // if you have all houses, then this option will appear
            if (ownedGroupHotels(((Street) currentField).getColor(), spiller, fl)) {
                String buyHotel = gui.getUserButtonPressed("Vil du købe et hotel til " + currentField.getName(), "Ja", "Nej");
                if (buyHotel.equals("Ja")) {
                    if (spiller.getAccount().getBalance() > ((Street) currentField).getHousePrice()) {
                        withdraw(spiller.getAccount(), ((Street) currentField).getHousePrice(), spiller);
                        viewGUI.updateBalance(sl);
                        viewGUI.buyHouseHotel((Street) currentField, spiller.getPosition());
                    }
                }
            }
        }
        viewGUI.updateBalance(sl);
        spiller.extraTurns += 1;
    }
    private void pantsætning(GUI gui, ViewGUI viewGUI, Spiller spiller, FieldList fl) {
        Object[] ownedFields = getStrings(spiller, fl);

        String[] names = (String[]) ownedFields[0];

        String pantsæt = gui.getUserButtonPressed("Vil du pantsætte nogle af dine grunde", "Nej", "Ja");
        if (pantsæt.equals("Ja") && names.length != 0) {
            String grund = gui.getUserSelection("Hvilken grund vil du sælge", names);
            Owneble fieldToSell = null;
            int index = 0;

            for (int i = 0; i < fl.getFields().length; i++) {
                if (fl.getField(i).getName().equals(grund)) {
                    fieldToSell = (Owneble) fl.getField(i);
                    index = i;
                }
            }

            int amount = 0;
            if (fieldToSell instanceof Street) {
                amount += fieldToSell.getHouseAmount() * ((Street) fieldToSell).getHousePrice();
            }
            assert fieldToSell != null;
            amount += fieldToSell.getPrice();

            deposit(spiller.getAccount(), amount);
            fieldToSell.setOwner(null);
            viewGUI.removeOwneble(index);

            assert fieldToSell instanceof Street;
            viewGUI.buyHouseHotel((Street) fieldToSell, index);
        }

        if (pantsæt.equals("Nej") && names.length == 0 && spiller.isBankRupt()) {
            spiller.setHasLost(true);
        }
    }
    @NotNull
    private Object[] getStrings(Spiller spiller, FieldList fl) {
        int j = 0;
        String[] ownedFieldsNames;
        Owneble[] ownedFields;

        for (int i = 0; i < fl.getFields().length; i++) {
            Field f = fl.getField(i);
            if (f instanceof Street && ((Street) f).getOwner() == spiller) {
                j++;
            }
        }
        ownedFieldsNames = new String[j];
        ownedFields = new Owneble[j];
        int k = 0;
        for (int i = 0; i < fl.getFields().length; i++) {
            Field f = fl.getField(i);
            if (f instanceof Street && ((Street) f).getOwner() == spiller) {
                ownedFieldsNames[k] = f.getName();
                ownedFields[k] = (Owneble) f;
                k++;
            }
        }
        return new Object[]{ownedFieldsNames, ownedFields};
    }