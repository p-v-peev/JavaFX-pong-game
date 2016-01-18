package application;

import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import javafx.util.Duration;
import networking.NetwokIO;
import networking.Reader;
import networking.UDPReader;
import networking.Writer;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

public class Main extends Application {
	private final static String host = "185.35.108.17";
	private final static int initialPort = 22000;

	private final static int width = 400;
	private final static int height = 600;
	private final static int paddleWidth = 100;
	private final static int paddleHeight = 10;
	private final static int radius = 10;

	private boolean isConnectedAndroid = false;
	public NetwokIO port = null;
	public Reader input = null;
	public Writer output = null;
	public Stage stage = null;
	public Scene currentScene = null;
	public Rectangle upperPaddle = null;
	public Rectangle bottomPaddle = null;
	public Circle ball = null;
	public Text counter = null;
	public EventHandler<KeyEvent> handler = null;
	public UDPReader udpReader = null;
	public int i = 3;
	public Text myScore = null;
	public Text oponentScore = null;

	@Override
	public void start(Stage primaryStage) {

		this.stage = primaryStage;
		this.createInitialScene();
		this.stage.show();
		this.port = new NetwokIO(host, initialPort, this);
		this.port.start();
		this.stage.setResizable(false);
		this.stage.setOnCloseRequest(e -> onClose());
	}

	public static void main(String[] args) {
		launch(args);
	}

	public void createInitialScene() {
		ProgressIndicator indicator = new ProgressIndicator();
		StackPane root = new StackPane(indicator);
		this.currentScene = new Scene(root, 300, 200);
		this.currentScene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
		this.stage.setScene(this.currentScene);
		this.stage.show();
	}

	public void createNamesScene() {
		TextField nameField = new TextField();
		nameField.setPromptText("Enter your name.");
		nameField.setOnMouseClicked(e -> nameField.clear());
		Button logIn = new Button("Log In");
		logIn.setOnAction(e -> {
			String name = nameField.getText();
			if ((name != null) && name.length() < 4 || name.length() > 9) {
				nameField.setText("Enter between 4 and 8 characters.");
			} else if (name != null) {
				this.output.write(":52");
				this.output.write(name);
				this.createRoomFormScene();
			}
		});
		VBox root = new VBox(5, nameField, logIn);
		this.currentScene = new Scene(root, 300, 200);
		this.currentScene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
		Platform.runLater(() -> this.stage.setScene(this.currentScene));
	}

	private void createRoomFormScene() {
		Button createRoom = new Button("Create room");
		createRoom.setOnAction(e -> this.output.write(":55"));
		Button joinRoom = new Button("Join room");
		joinRoom.setOnAction(e -> this.output.write(":56"));
		Button connectAndroid = new Button("Connect Android device");
		connectAndroid.setOnAction(e -> {
			this.output.write(":25");
			this.isConnectedAndroid = true;
		});
		VBox root = new VBox(5, createRoom, joinRoom, connectAndroid);
		this.currentScene = new Scene(root, 300, 200);
		this.currentScene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
		Platform.runLater(() -> this.stage.setScene(this.currentScene));
	}

	public void createRoomsListScene(String... names) {
		VBox roomsName = new VBox(5);
		for (String name : names) {
			roomsName.getChildren().add(new Label(name));
		}
		ScrollPane pane = new ScrollPane(roomsName);
		TextField roomChoise = new TextField();
		roomChoise.setPromptText("Enter name of the room.");
		Button sendRoom = new Button("Send name");
		Button back = new Button("Back");
		back.setOnAction(e -> this.createRoomFormScene());
		sendRoom.setOnAction(e -> {
			String roomName = roomChoise.getText();
			if (roomName != null && roomName.length() > 0) {
				this.output.write(":57");
				this.output.write(roomName);
			}
		});
		VBox root = new VBox(5, pane, roomChoise, sendRoom, back);
		this.currentScene = new Scene(root, 300, 440);
		this.currentScene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
		Platform.runLater(() -> this.stage.setScene(this.currentScene));
	}

	public void createGameScene() {
		this.createShapes();
		this.initShapes();
		this.playAnimation();
		StackPane root = new StackPane(this.upperPaddle, this.bottomPaddle, this.counter, this.myScore,
				this.oponentScore, this.ball);
		// root.setCache(true);
		this.currentScene = new Scene(root, width, height);
		this.currentScene.getStylesheets().add(getClass().getResource("gameStyle.css").toExternalForm());
		this.addHandler();
		Platform.runLater(() -> {
			this.stage.setScene(this.currentScene);
			this.stage.setAlwaysOnTop(true);
			this.stage.setY(30);
		});
	}

	public void createErrorConnectionScene() {
		Text errorText = new Text("Server is down.");
		errorText.setStyle("-fx-fill: firebrick; -fx-font-size: 20");
		VBox root = new VBox(errorText);
		this.currentScene = new Scene(root, 300, 200);
		this.currentScene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
		Platform.runLater(() -> this.stage.setScene(this.currentScene));
	}

	private void addHandler() {
		if (!isConnectedAndroid) {
			this.handler = new EventHandler<KeyEvent>() {
				@Override
				public void handle(KeyEvent event) {
					if (event.getCode() == KeyCode.LEFT) {
						if (output != null) {
							output.write(":40");
							output.write('-');
							output.write(":40");
							output.write('-');
							output.write(":40");
							output.write('-');
							output.write(":40");
							output.write('-');
						}
					}
					if (event.getCode() == KeyCode.RIGHT) {
						if (output != null) {
							output.write(":40");
							output.write('+');
							output.write(":40");
							output.write('+');
							output.write(":40");
							output.write('+');
							output.write(":40");
							output.write('+');
						}
					}

				}
			};
			this.stage.addEventHandler(KeyEvent.KEY_PRESSED, this.handler);
		}
	}

	private void createShapes() {
		this.upperPaddle = new Rectangle(paddleWidth, paddleHeight);
		this.upperPaddle.setFill(Paint.valueOf("8686b6"));
		this.bottomPaddle = new Rectangle(paddleWidth, paddleHeight);
		this.bottomPaddle.setTranslateY(height - paddleHeight);
		this.bottomPaddle.setFill(Paint.valueOf("686766"));
		this.counter = new Text("3");
		this.counter.setId("counter");
		this.ball = new Circle(radius, Paint.valueOf("ffffff"));
		this.ball.setFill(Paint.valueOf("e46a6b"));
		this.ball.setVisible(false);
		this.myScore = new Text("0");
		this.oponentScore = new Text("0");
		this.myScore.setId("myscore");
		this.oponentScore.setId("oponentScore");
	}

	private void initShapes() {
		this.upperPaddle.setTranslateX(width / 2 - paddleWidth / 2);
		this.bottomPaddle.setTranslateX(width / 2 - paddleWidth / 2);
		this.moveBall(width / 2, height / 2);
		this.ball.toFront();
		this.counter.toFront();
		this.counter.setVisible(true);
	}

	public void moveBall(int x, int y) {
		this.ball.setTranslateX(x - radius);
		this.ball.setTranslateY(y - radius);
		System.out.println("Update ball");
	}

	public void movePaddle(Rectangle paddle, int x) {
		paddle.setTranslateX(x);
		System.out.println("Update paddle");
	}

	public void decrementCounter() {
		this.i--;
		if (i > 0) {
			playAnimation();
			this.counter.setText(String.valueOf(this.i));
		} else if (i == 0) {
			this.hideText();
			this.showBall();
		}
	}

	public void fireHandler() {
		if (this.handler != null) {
			// Platform.runLater(() -> this.stage.fireEvent(arg0););
			this.handler = null;
		}
	}

	private void hideText() {
		this.counter.setVisible(false);
	}

	private void showBall() {
		this.ball.setVisible(true);
	}

	private void playAnimation() {
		FadeTransition transition = new FadeTransition(Duration.millis(950), this.counter);
		transition.setFromValue(1.0f);
		transition.setToValue(0.2f);
		transition.play();
	}

	public void setScore(Text player, String score) {
		player.setText(score);
	}

	public void onClose() {
		// All the things before closing the application.
		if (this.port != null && this.port.isAlive()) {
			this.port.interrupt();
			this.port.disconnect();
		}

		if (this.udpReader != null && this.udpReader.isAlive()) {
			this.udpReader.interrupt();
			this.udpReader.disconnect();
		}

		if (this.output != null) {
			this.output.write(":95");
			this.output.disconnect();
		}

		if (this.input != null && this.input.isAlive()) {
			this.input.interrupt();
			this.input.disconnect();
		}
	}
}