package Model.Chance;

import Model.Fields.Chance;
import Model.Spiller;
import View.ViewGUI;
import gui_main.GUI;

public class MoveToField extends ChanceCard {

    int moveFieldTo;

    public MoveToField(String description,int moveFieldTo) {
        super(description);
        this.moveFieldTo = moveFieldTo;
    }

    @Override
    public void doCard(Spiller player, ViewGUI gui) {
        super.doCard(player, gui);
        player.setPosition(moveFieldTo);
        gui.moveCarToField(player,moveFieldTo);
    }
}
