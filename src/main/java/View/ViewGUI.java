package View;

import Model.Fields.Owneble;
import Model.Fields.Street;
import Model.Spiller;
import Model.SpillerListe;
import gui_fields.*;
import gui_main.GUI;

import java.awt.*;

import static gui_fields.GUI_Car.Pattern.*;
import static gui_fields.GUI_Car.Type.*;

public class ViewGUI {
    private final GUI gui;
    private GUI_Player[] gui_players;
    private GUI_Car[] gui_cars;
    private GUI_Field[] gui_fields;
    public ViewGUI(GUI gui){
        this.gui = gui;
        this.gui_fields = gui.getFields();
    }

    GUI_Car.Type[] carTypes = {CAR, RACECAR, TRACTOR, UFO};
    GUI_Car.Pattern[] carPatterns = {FILL, HORIZONTAL_GRADIANT, DIAGONAL_DUAL_COLOR,
            HORIZONTAL_DUAL_COLOR, HORIZONTAL_LINE, CHECKERED, DOTTED, ZEBRA};
    Color[] colors = {Color.BLUE, Color.ORANGE,Color.RED, Color.GREEN,Color.YELLOW, Color.WHITE };