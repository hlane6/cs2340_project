package mule;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.shape.Rectangle;
import javafx.animation.*;
import javafx.event.*;
import javafx.util.Duration;
import javafx.scene.input.MouseEvent;

import mule.model.*;
import mule.model.map.*;
import mule.model.resources.*;

/**
 * Controlls the map screen, handling plot purchases, mule placement, etc
 */
public class MapController implements Initializable, ControlledScreen {

    private ScreensController controller;

    @FXML private Canvas mapParent;

    @FXML private MenuBar menuBar;
    private static MenuBar menuBarInstance;

    @FXML private TextArea displayText;
    private static TextArea displayTextInstance;

    @FXML private Rectangle selectionRect;

    @Override public final void initialize(URL url, ResourceBundle rb) {
        if (Main.getMap() == null) {
            String type = Main.getMapType();
            switch (type) {
                case "Default":
                    Main.setMap(new DefaultMap(mapParent));
                    break;
                case "River":
                    Main.setMap(new RiverMap(mapParent));
                    break;
                case "Random":
                    Main.setMap(new RandomMap(mapParent));
                    break;
            }
        } else {
            Main.getMap().redraw(mapParent);
        }
        setupInfoBar();
        setupDisplayText();
        startTimer();

        selectionRect.setWidth(75);
        selectionRect.setHeight(75);

        selectionRect.addEventHandler(MouseEvent.MOUSE_PRESSED,
                e -> createLandSelectionHandler(e));

        mapParent.addEventHandler(MouseEvent.MOUSE_MOVED,
                e -> createLandOutlineHandler(e));
    }

    /**
     * Shows the town to the user
     */
    private void goToTownScreen() {
        Main.loadScene(Main.TOWN_ID, Main.TOWN_FILE);
        controller.setScreen(Main.TOWN_ID);
        Main.setMenuBar(TownController.getMenuBar());
        TownController.getDisplayText().setText( "Welcome to the Town. Go to the store to buy resources " +
                "or go to the Pub to end your turn\n" + displayText.getText());

        for (int i = 0; i < Main.getPlayerCount(); i++) {
            TownController.updatePlayerMenu(i);
        }
    }

    /**
     * Sets the main screen controller
     * @param screenParent the screen controller
     */
    public final void setScreenParent(ScreensController screenParent) {
        controller = screenParent;
    }

    /**
     * Attempts to save the game using the Database Controller
     */
    @FXML public final void saveGame() {
        Main.getDBController().saveGame();
    }

    private void incrementTurn() {
        if (Main.getTurn().hasNextPlayer()) {
            Main.getTurn().nextPlayer();
        } else {
            Main.getTurn().nextStage();
            goToTownScreen();
        }
    }

    private void setupInfoBar() {
        menuBarInstance = menuBar;
        Main.setMenuBar(menuBarInstance);
        for (int i = 0; i < Main.getPlayerCount(); i++) {
            updatePlayerMenu(i);        }
        for (int i = Main.getPlayerCount(); i < 4; i++) {
            menuBar.getMenus().get(i).setVisible(false);
        }

    }

    private void setupDisplayText() {
        displayText.setEditable(false);
        displayText.setText("Welcome to M.U.L.E! Select a plot " +
                "of land to continue or select the town to pass.\n");
        displayTextInstance = displayText;
    }

    private void startTimer() {
        EventHandler onFinished = new EventHandler<ActionEvent>() {
            public void handle(ActionEvent t) {
                if (Main.getCurrentPlayer().getTimer().outOfTime()) {
                    Main.getCurrentPlayer().getTimer().reset();
                    updateDisplayText(Main.getCurrentPlayer().getName() +
                            " ran out of time, skipping to next player");
                    incrementTurn();
                } else {
                    Main.getMenuBar().getMenus().get(4).setText("Time: "
                            + Main.getCurrentPlayer().getTimer().getTime());
                    Main.getCurrentPlayer().getTimer().tick();
                }
            }
        };
        KeyFrame keyFrame = new KeyFrame(Duration.millis(1000), onFinished);
        Main.getTimeline().getKeyFrames().addAll(keyFrame);
        Main.getTimeline().play();
    }

    /**
     * Updates the players information in the menubar
     * @param i The rank of the player to update
     */
    public static void updatePlayerMenu(int i) {
        menuBarInstance.getMenus().get(i).setText(Main.getPlayer(i).getName());
        menuBarInstance.getMenus().get(i).getItems().get(0).setText(
                "Money: " + Main.getPlayer(i).getMoney());
        menuBarInstance.getMenus().get(i).getItems().get(1).setText(
                "Energy: " + Main.getPlayer(i).getResource(new Energy()));
        menuBarInstance.getMenus().get(i).getItems().get(2).setText(
                "Food: " + Main.getPlayer(i).getResource(new Food()));
        menuBarInstance.getMenus().get(i).getItems().get(3).setText(
                "Smithore: " + Main.getPlayer(i).getResource(new Smithore()));
        menuBarInstance.getMenus().get(i).getItems().get(4).setText(
                "Score: " + Main.getPlayer(i).getScore());
    }

    private void createLandSelectionHandler(MouseEvent event) {
        System.out.println("MOUSE CLICKED");
        int x = (int) ((event.getSceneX()) / 75);
        int y = (int) ((event.getSceneY() - 30) / 75);

        if (isTown(x, y)) {
            processTownClick();
        } else {
            Plot selected = Main.getMap().getPlot(x, y);
            if (selected.hasOwner()) {
                processLandOwnedClick(selected);
            } else {
                processLandNotOwnedClick(selected);
            }
        }
    }

    private void createLandOutlineHandler(MouseEvent event) {
        int x = (int) ((event.getX()) / 75);
        int y = (int) ((event.getY()) / 75);

        selectionRect.setTranslateX(x * 75);
        selectionRect.setTranslateY(y * 75);
    }

    private boolean isTown(int x, int y) {
        return x == Map.MAP_WIDTH / 2 && y == Map.MAP_HEIGHT / 2;
    }

    private void processTownClick() {
        if (Main.getTurn().getCurrentStage() == Turn.LAND) {
            updateDisplayText(Main.getCurrentPlayer().getName() +
                    " passes, no land purchased.");
            incrementTurn();
        } else if (Main.getTurn().getCurrentStage() == Turn.TOWN) {
            Main.getCurrentPlayer().removeMule();
            updateDisplayText("Mule's can't go on the town, " +
                    Main.getCurrentPlayer().getName() +
                    ". Your mule was lost.");
            goToTownScreen();
        }
    }

    private void processLandOwnedClick(Plot selected) {
        if (Main.getTurn().getCurrentStage() == Turn.TOWN) {
            if (selected.getOwner().equals(Main.getCurrentPlayer())
                    && !selected.outfitted()) {
                Mule temp = Main.getCurrentPlayer().removeMule();
                selected.outfit(temp);
                goToTownScreen();
            } else {
                Main.getCurrentPlayer().removeMule();
                updateDisplayText("You do not own that piece of land. " +
                        "Your mule was lost.");
                goToTownScreen();
            }
        } else {
            updateDisplayText("That land was already purchased!");
            goToTownScreen();
        }
    }

    private void processLandNotOwnedClick(Plot selected) {
        if (Main.getTurn().getCurrentStage() == Turn.TOWN) {
            Main.getCurrentPlayer().removeMule();
            updateDisplayText("You can't place a mule on land " +
                    "you don't own. Your mule was lost.");
            goToTownScreen();
        } else if (Main.getTurn().getCurrentTurn() > 1) {
            if (selected.buy(Main.getCurrentPlayer())) {
                updateDisplayText(Main.getCurrentPlayer().getName() +
                        " bought a plot of land.");
                incrementTurn();
            } else {
                updateDisplayText("You do not have enough funds to " +
                        "buy this plot of land.");
            }
        } else {
            Main.getCurrentPlayer().addPlot(selected);
            updateDisplayText(Main.getCurrentPlayer().getName() +
                    " granted a plot of land");
            incrementTurn();
        }
    }

    /**
     * Updates the text displayed to the user
     * @param text the string to add to the display
     */
    public static void updateDisplayText(String text) {
        displayTextInstance.setText(text + "\n" + displayTextInstance.getText());
    }

    public static MenuBar getMenuBar() { return menuBarInstance; }

    public static TextArea getDisplayText() { return displayTextInstance; }

}
