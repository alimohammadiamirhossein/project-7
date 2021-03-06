package client.models.menus;

import client.Client;
import client.view.View;

public class SinglePlayerMenu extends Menu {
    private static final SinglePlayerMenu SINGLE_PLAYER_MENU = new SinglePlayerMenu();

    private SinglePlayerMenu() {
    }

    public static SinglePlayerMenu getInstance() {
        return SINGLE_PLAYER_MENU;
    }

    public void moveToStoryMenu(Client client, String serverName) {
        client.setCurrentMenu(StoryMenu.getInstance(client, serverName));
    }

    public void moveToCustomGameMenu(Client client){
        CustomGameMenu.getInstance().setHelp(client);
        client.setCurrentMenu(CustomGameMenu.getInstance());
    }

    @Override
    public void exit(Client client){
        client.setCurrentMenu(BattleMenu.getInstance());
    }

    @Override
    public void showHelp(){
        String help = "Single Palyer:\n" +
                "\"story\"\n" +
                "\"custom game\"\n" +
                "\"exit\"";
        View.getInstance().showHelp(help);
    }
}