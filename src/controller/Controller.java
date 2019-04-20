package controller;

import client.Client;
import client.models.menus.AccountMenu;
import server.Server;
import client.view.View;
import client.view.request.InputException;
import client.view.request.Request;

import java.util.ArrayList;
import java.util.Scanner;

public class Controller {
    private View view = View.getInstance();
    private Scanner input;
    private ArrayList<Client> clients = new ArrayList<>();
    private Server server;
    private Client thisClient;

    public void main() {
        do {
            Request request = new Request();
            request.getNewCommand();
            try {
                request.handleRequset(thisClient, server.getServerName());
            } catch (InputException e) {
                view.printError(e);
            }
        } while (true);
    }

    public void preProcess() {
        clients.add(
                new Client(java.lang.System.getProperty("user.name"), AccountMenu.getInstance(), Server.getInstance())
        );
        thisClient = clients.get(0);
    }
}