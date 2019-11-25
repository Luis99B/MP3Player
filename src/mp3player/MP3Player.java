//Luis Bodart A01635000

package mp3player;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class MP3Player extends Application {

	public void start(Stage primaryStage) throws Exception {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException cnfe) {
			cnfe.printStackTrace();
		} catch (InstantiationException ie) {
			ie.printStackTrace();
		} catch (IllegalAccessException iae) {
			iae.printStackTrace();
		} catch (UnsupportedLookAndFeelException ulfe) {
			ulfe.printStackTrace();
		}
		primaryStage.setTitle("MP3 Player");
		primaryStage.getIcons().add(new Image("/icons/music.png"));

		PlayList pl = new PlayList(primaryStage, getParameters());
		Player p = new Player(primaryStage);

		Group group = new Group();

		group.getChildren().add(pl);
		group.getChildren().add(p);

		Scene scene = new Scene(group, 374, 490);
		primaryStage.setScene(scene);
		primaryStage.setResizable(false);
		primaryStage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}

}
