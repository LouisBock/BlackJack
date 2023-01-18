package main.java.de.louisbock.BlackJack;


import java.io.FileInputStream;

import javafx.application.Application;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class Table extends Application{
	
	private static TableLogic TL;
	private final int WINDOW_HEIGHT = 700;
	private final int WINDOW_WIDTH = 1000;
	private final static int MAX_PLAYERS = 4;
	
	//FX objects
	
	//MENU
	private Scene menuScene;
	private VBox menuLayout, accountLayout;
	private Button startButton, loginButton, logoutButton;
	private TextField playerNameInput;
	private Label errorLabel, headdingLabel, currentLabel;
	
	//GAME
	private Scene tableScene;
	private BorderPane rootLayout;
	private VBox tabletopLayout, bottomLayout;
	private GridPane playerHandsLayout;
	private HBox gameButtonBox, menuButtonBox, topLayout;
	private Button hitButton, standButton, doubleButton, splitButton, nextButton, menuButton, betButton;
	private Pane[]	playerBoxes;
	private Pane dealerBox;
	private ImageView currentMarker;
	private VBox shoe;
	private Label shoeCount, blackJackLabel;
	private VBox cardCountBox;
	private Label cardCountLabel, cardCount; 
	private TextField[] betInputs;
	
	//images
	private final static Image[] images = new Image[15];
	private static final double CARD_HEIGHT = 90;
	private static final double CARD_WIDTH = (CARD_HEIGHT/3)*2;
	private static final boolean CARD_SMOOTH = false;
	private static final boolean CARD_RATIO = true;
	
	@Override
	public void start(Stage primaryStage) throws Exception {
//------MENU_SCENE-------------------------------------------
		
		//buttons
		startButton = new Button();
		startButton.setText("PLAY");
		startButton.setAlignment(Pos.CENTER);
		startButton.setOnAction(e -> {
			if(TL.getPlayerCount()>0) {
				errorLabel.setText("");
				errorLabel.setVisible(false);
				primaryStage.setScene(tableScene);
				Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
				double x = bounds.getMinX() + bounds.getWidth()/2 - tableScene.getWidth()/2;
				double y = bounds.getMinY() + bounds.getHeight()/2 - tableScene.getHeight()/2;
				primaryStage.setX(x);
				primaryStage.setY(y);
				//start
				startRound();
			} else {
				displayError("There are currently no players!");
			}
		});
		
		loginButton = new Button();
		loginButton.setText("LOGIN");
		loginButton.setOnAction(e -> {
			//TODO check if player is broke
			if(TL.addPlayer(playerNameInput.getText())) {
				errorLabel.setText("");
				errorLabel.setVisible(false);
			}
			else {
				displayError("Invalid name or to many players!\n(length between 1 and 16 | max players: " + MAX_PLAYERS + ")");
			}
			updateAccountLayout();
		});
		
		logoutButton = new Button();
		logoutButton.setText("LOGOUT");
		logoutButton.setOnAction(e -> {
			TL.removePlayer(playerNameInput.getText());
			updateAccountLayout();
		});
		
		//textfield
		playerNameInput = new TextField();
		playerNameInput.setPromptText("Enter your name");
		playerNameInput.setMaxWidth(130);
		
		//lable
		errorLabel = new Label();
		errorLabel.setText("");
		errorLabel.setFont(Font.font(22));
		errorLabel.setStyle("-fx-text-fill: #630000;");
		errorLabel.setVisible(false);
		errorLabel.setAlignment(Pos.CENTER);
		
		headdingLabel = new Label();
		headdingLabel.setText("WELCOME TO BLACK JACK");
		headdingLabel.setFont(Font.font(50));
		headdingLabel.setStyle("-fx-text-fill: black;");
		headdingLabel.setAlignment(Pos.CENTER);
		
		currentLabel = new Label();
		currentLabel.setText("CURRENT PLAYERS:");
		currentLabel.setFont(Font.font(24));
		currentLabel.setStyle("-fx-text-fill: black;");
		currentLabel.setAlignment(Pos.CENTER);
		
		//layouts
		accountLayout = new VBox(20);
		accountLayout.setAlignment(Pos.CENTER);
		updateAccountLayout();
		
		menuButtonBox = new HBox(20);
		menuButtonBox.setAlignment(Pos.CENTER);
		menuButtonBox.getChildren().addAll(loginButton, logoutButton);
		
		menuLayout = new VBox(20);
		menuLayout.getChildren().addAll(headdingLabel, playerNameInput, menuButtonBox, currentLabel, accountLayout, startButton, errorLabel);
		menuLayout.setStyle("-fx-background-color: #14A92B;");
		menuLayout.setAlignment(Pos.TOP_CENTER);
		
		//tableScene
		menuScene = new Scene(menuLayout, 600, 600);
		
//------GAME_SCENE-------------------------------------------	
		
		//buttons
		hitButton = new Button();
		hitButton.setText("HIT");
		hitButton.setOnAction(e -> {
			playerMoved(!TL.playerDrawCard());
		});
		
		standButton = new Button();
		standButton.setText("STAND");
		standButton.setOnAction(e -> {
			playerMoved(true);
		});
		
		doubleButton = new Button();
		doubleButton.setText("DOUBLE");
		doubleButton.setOnAction(e -> {
			TL.playerDoubleHand();
			playerMoved(true);
		});
		
		splitButton = new Button();
		splitButton.setText("SPLIT");
		splitButton.setOnAction(e -> {
			TL.playerSplitHand();
			playerMoved(!TL.getHitable());
		});
		
		betButton = new Button();
		betButton.setText("BET");
		betButton.setOnAction(e -> {
			double bet = checkBetInput(TL.getCurrentPlayerIndex());
			if(bet != -1) {
				errorLabel.setText("");
				errorLabel.setVisible(false);
				TL.payBet(bet);
				playerBet();
			} else {
				displayError("Please enter a nuber > '0' and <= 'your current money count'!");
			}
		});
		
		nextButton = new Button();
		nextButton.setText("NEXT ROUND");
		nextButton.setVisible(false);
		nextButton.setOnAction(e -> {
			startRound();
		});
		
		menuButton = new Button();
		menuButton.setText("MENU");
		menuButton.setVisible(false);
		menuButton.setOnAction(e -> {
			primaryStage.setScene(menuScene);
			Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
			double x = bounds.getMinX() + bounds.getWidth()/2 - menuScene.getWidth()/2;
			double y = bounds.getMinY() + bounds.getHeight()/2 - menuScene.getHeight()/2;
			primaryStage.setX(x);
			primaryStage.setY(y);
		});
		
		
		//buttonBox
		gameButtonBox = new HBox(20);
		gameButtonBox.getChildren().addAll(hitButton, standButton, doubleButton, splitButton, betButton, nextButton, menuButton);
		gameButtonBox.setAlignment(Pos.TOP_CENTER);
		
		//shoe
		shoe = new VBox(20);
		shoe.setAlignment(Pos.BOTTOM_CENTER);
		shoe.setMinWidth(CARD_WIDTH*2.5);
		shoeCount = new Label();
		shoeCount.setAlignment(Pos.CENTER);
		shoeCount.setFont(Font.font(32));
		shoeCount.setText(TL.getShoeCount() + "");
		shoe.getChildren().addAll(new ImageView(images[13]), shoeCount);
		
		//cardCount
		cardCountBox = new VBox(10);
		cardCountBox.setAlignment(Pos.CENTER);
		cardCountBox.setMinWidth(CARD_WIDTH*1);
		cardCountLabel = new Label();
		cardCountLabel.setAlignment(Pos.CENTER);
		cardCountLabel.setFont(Font.font(32));
		cardCountLabel.setText("COUNT: ");
		cardCount = new Label();
		cardCount.setAlignment(Pos.CENTER);
		cardCount.setFont(Font.font(32));
		cardCount.setText(TL.getCardCount() + "");
		cardCountBox.getChildren().addAll(cardCountLabel, cardCount);
		
		//handBoxes
		dealerBox = TL.getDealerBox(true);
		playerBoxes = TL.getPlayerBoxes();
		
		//currentMarker
		currentMarker = new ImageView(images[14]);
		
		//playerHandsLayout
		playerHandsLayout = new GridPane();
		playerHandsLayout.setHgap(CARD_WIDTH/2);
		playerHandsLayout.setVgap(CARD_HEIGHT/2);
		playerHandsLayout.setAlignment(Pos.CENTER);
		for(int i = 0; i < TL.getPlayerCount(); i++) {
			GridPane.setConstraints(playerBoxes[i], i, 0, 1, 1, HPos.CENTER, VPos.CENTER);
		}
		playerHandsLayout.getChildren().addAll(playerBoxes);
		
		GridPane.setConstraints(currentMarker, TL.getCurrentPlayerIndex(), 1, 1, 1, HPos.CENTER, VPos.TOP);
		playerHandsLayout.getChildren().add(currentMarker);
		
		//tabletopLayout
		tabletopLayout = new VBox(CARD_HEIGHT/2);
		tabletopLayout.getChildren().addAll(dealerBox, playerHandsLayout);	
		tabletopLayout.setAlignment(Pos.CENTER);
		
		//lable
		blackJackLabel = new Label();
		blackJackLabel.setText("BLACK JACK");
		blackJackLabel.setFont(Font.font(50));
		blackJackLabel.setStyle("-fx-text-fill: black;");
		blackJackLabel.setAlignment(Pos.CENTER);
		
		//topLayout
		topLayout = new HBox(200);
		topLayout.getChildren().addAll(blackJackLabel, shoe);
		topLayout.setAlignment(Pos.CENTER);
		
		//bottomLayout
		bottomLayout = new VBox(40);
		bottomLayout.getChildren().addAll(gameButtonBox, errorLabel);
		bottomLayout.setAlignment(Pos.CENTER);
		
		//rootLayout
		rootLayout = new BorderPane();
		rootLayout.setTop(topLayout);
		rootLayout.setCenter(tabletopLayout);
		rootLayout.setBottom(bottomLayout);
		rootLayout.setRight(cardCountBox);
		rootLayout.setPrefWidth(WINDOW_WIDTH);
		rootLayout.setPrefHeight(WINDOW_HEIGHT);
		rootLayout.setStyle("-fx-background-color: #14A92B;");

		//tableScene
		tableScene = new Scene(rootLayout, WINDOW_WIDTH, WINDOW_HEIGHT);
		
//------STAGE-------------------------------------------------
		
		primaryStage.setScene(menuScene);
		primaryStage.setTitle("Black Jack");
		primaryStage.show();
		Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
		double x = bounds.getMinX() + bounds.getWidth()/2 - menuScene.getWidth()/2;
		double y = bounds.getMinY() + bounds.getHeight()/2 - menuScene.getHeight()/2;
		primaryStage.setX(x);
		primaryStage.setY(y);
	}
	
	private static void initImages() {
		//images
		try {
			images[0] = new Image(new FileInputStream("res\\card2.png"), CARD_WIDTH, CARD_HEIGHT, CARD_RATIO, CARD_SMOOTH);
			images[1] = new Image(new FileInputStream("res\\card3.png"), CARD_WIDTH, CARD_HEIGHT, CARD_RATIO, CARD_SMOOTH);
			images[2] = new Image(new FileInputStream("res\\card4.png"), CARD_WIDTH, CARD_HEIGHT, CARD_RATIO, CARD_SMOOTH);
			images[3] = new Image(new FileInputStream("res\\card5.png"), CARD_WIDTH, CARD_HEIGHT, CARD_RATIO, CARD_SMOOTH);
			images[4] = new Image(new FileInputStream("res\\card6.png"), CARD_WIDTH, CARD_HEIGHT, CARD_RATIO, CARD_SMOOTH);
			images[5] = new Image(new FileInputStream("res\\card7.png"), CARD_WIDTH, CARD_HEIGHT, CARD_RATIO, CARD_SMOOTH);
			images[6] = new Image(new FileInputStream("res\\card8.png"), CARD_WIDTH, CARD_HEIGHT, CARD_RATIO, CARD_SMOOTH);
			images[7] = new Image(new FileInputStream("res\\card9.png"), CARD_WIDTH, CARD_HEIGHT, CARD_RATIO, CARD_SMOOTH);
			images[8] = new Image(new FileInputStream("res\\card10.png"), CARD_WIDTH, CARD_HEIGHT, CARD_RATIO, CARD_SMOOTH);
			images[9] = new Image(new FileInputStream("res\\cardJ.png"), CARD_WIDTH, CARD_HEIGHT, CARD_RATIO, CARD_SMOOTH);
			images[10] = new Image(new FileInputStream("res\\cardQ.png"), CARD_WIDTH, CARD_HEIGHT, CARD_RATIO, CARD_SMOOTH);
			images[11] = new Image(new FileInputStream("res\\cardK.png"), CARD_WIDTH, CARD_HEIGHT, CARD_RATIO, CARD_SMOOTH);
			images[12] = new Image(new FileInputStream("res\\cardA.png"), CARD_WIDTH, CARD_HEIGHT, CARD_RATIO, CARD_SMOOTH);
			images[13] = new Image(new FileInputStream("res\\cardBlank.png"), CARD_WIDTH, CARD_HEIGHT, CARD_RATIO, CARD_SMOOTH);
			images[14] = new Image(new FileInputStream("res\\currentPlayer.png"), CARD_WIDTH, (CARD_HEIGHT/6), CARD_RATIO, CARD_SMOOTH);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
//--LAYOUT-METHODS-----------------------------------	
	
	/**
	 * updates and reveals dealer
	 */
	private void updateTabletopLayout(boolean hidden) {
		//clean
		tabletopLayout.getChildren().remove(0, tabletopLayout.getChildren().size());
		
		//dealerBox
		dealerBox = TL.getDealerBox(hidden);
		
		//tabletopLayout
		tabletopLayout.getChildren().addAll(dealerBox);
		
		updatePlayerHands();
	}
	
	private void updatePlayerHands() {
		//clean
		tabletopLayout.getChildren().remove(playerHandsLayout);
		playerHandsLayout.getChildren().remove(0, playerHandsLayout.getChildren().size());
		
		//currentMarker
		updateCurrentMarker();
		
		//handBoxes
		playerBoxes = TL.getPlayerBoxes();
		for(int i = 0; i < TL.getPlayerCount(); i++) {
			GridPane.setConstraints(playerBoxes[i], i, 0, 1, 1, HPos.CENTER, VPos.CENTER);
		}
		
		//tabletopLayout
		playerHandsLayout.getChildren().addAll(playerBoxes);
		tabletopLayout.getChildren().add(playerHandsLayout);
		
		//shoe
		shoeCount.setText(TL.getShoeCount() + "");
		cardCount.setText(TL.getCardCount() + "");
	}
	
	private void updateCurrentMarker() {
		//clean
		playerHandsLayout.getChildren().remove(currentMarker);
		
		//currentMarker
		currentMarker.setVisible(TL.getCurrentPlayerIndex()<TL.getPlayerCount());
		GridPane.setConstraints(currentMarker, Math.min(TL.getPlayerCount()-1, TL.getCurrentPlayerIndex()), 1, 1, 1, HPos.CENTER, VPos.CENTER);
		
		//layout
		playerHandsLayout.getChildren().add(currentMarker);
	}
	
	private void hidePlayerButtons() {
		hitButton.setVisible(false);
		standButton.setVisible(false);
		doubleButton.setVisible(false);
		splitButton.setVisible(false);
	}
	
	private void initBettingLayout() {
		//clean
		tabletopLayout.getChildren().remove(0, tabletopLayout.getChildren().size());
		playerHandsLayout.getChildren().remove(0, playerHandsLayout.getChildren().size());
		
		//betInputs
		double[] bets = TL.getPlayerBets();
		betInputs = new TextField[TL.getPlayerCount()];
		for(int i = 0; i < betInputs.length; i++) {
			TextField input = new TextField();
			input.setMaxWidth(CARD_WIDTH);
			input.setText(bets[i] + "");
			input.setAlignment(Pos.CENTER);
			betInputs[i] = input;
		}
		
		
		//layout
		Label[] labels = TL.getPlayerLabels();
		Pane[] betLayout = new VBox[labels.length];
		for(int i = 0; i < betLayout.length; i++) {
			VBox layout = new VBox(20);
			layout.getChildren().addAll(betInputs[i], labels[i]);
			GridPane.setConstraints(layout, i, 0, 1, 1, HPos.CENTER, VPos.CENTER);
			
			betLayout[i] = layout;
		}
		
		playerHandsLayout.getChildren().addAll(betLayout);
		tabletopLayout.getChildren().add(playerHandsLayout);
		
		//currentMarker
		updateCurrentMarker();
	}
	
//--CONTROLLERS---------------------------------	
	
	private void playerBet() {
		if(TL.nextPerson()) {
			updateCurrentMarker();
		} else {
			betButton.setVisible(false);
			cardCountBox.setVisible(false);
			
			TL.startRound();
			nextButton.setVisible(false);
			menuButton.setVisible(false);
			standButton.setVisible(true);
			hitButton.setVisible(TL.getHitable());
			doubleButton.setVisible(TL.getDoubleable());
			splitButton.setVisible(TL.getSplitable());
			updateTabletopLayout(true);
		}
		
	}
	
	private void playerMoved(boolean done) {
		if(done && !TL.nextPerson()) {
			TL.playDealersHand();
			endRound();
			return;
		}
		hitButton.setVisible(TL.getHitable());
		doubleButton.setVisible(TL.getDoubleable());
		splitButton.setVisible(TL.getSplitable());
		updatePlayerHands();
	}
	
	private double checkBetInput(int currentPlayerIndex) {
		double input = -1;
		try {
			input = Double.parseDouble(betInputs[currentPlayerIndex].getText());
		}catch (Exception e) {
			return -1;
		}
		if(input > 0 && input < TL.getPlayerMoney()) {
			return input;
		} else {
			return -1;
		}
	}
	
	private void startRound() {
		TL.clearSplitHands();
		
		hidePlayerButtons();
		nextButton.setVisible(false);
		menuButton.setVisible(false);
		cardCountBox.setVisible(true);
		betButton.setVisible(true);
		initBettingLayout();
	}
	
	private void endRound() {
		//evaluate the round
		TL.evaluateRound();
		
		hidePlayerButtons();
		nextButton.setVisible(true);
		menuButton.setVisible(true);
		updateTabletopLayout(false);
	}

//--MENU_METHODS-----------------------------------
	
	private void displayError(String error) {
		errorLabel.setText(error);
		errorLabel.setVisible(true);
	}
	
	private void updateAccountLayout() {
		accountLayout.getChildren().removeAll(accountLayout.getChildren());
		
		String[] names = TL.getPlayerNames();
		for(int i = 0; i < names.length; i++) {
			Label label = new Label(names[i]);
			label.setFont(Font.font(18));
			label.setStyle("-fx-text-fill: black;");
			accountLayout.getChildren().add(label);
		}
	}
	
	public static void main(String[] args){
		initImages();
		TL = new TableLogic(8);
		
		//start
		launch(args);
	}

//--GETTERS-----------------------------------
	
	public static Image[] getImages() {
		return images;
	}

	public static double getCardWidth() {
		return CARD_WIDTH;
	}
	
	public static double getMaxPlayers() {
		return MAX_PLAYERS;
	}
}
