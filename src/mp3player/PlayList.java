//Luis Bodart A01635000

package mp3player;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;

import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.id3.ID3v24Frames;

import javafx.application.Application.Parameters;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

public class PlayList extends Pane {

	protected static String path;
	protected static LinkedList<Node> songList;
	private LinkedList<Pane> songPanes;
	private ScrollPane scrollP;
	private VBox vbox;
	private static Stage primaryStage;
	private static Parameters parameters;
	
	public PlayList(Stage primaryStage, Parameters parameters) {
		setBackground(new Background(new BackgroundFill(Color.rgb(220, 220, 220), CornerRadii.EMPTY, Insets.EMPTY)));
		setPrefSize(385, 400);
		relocate(0, 0);

		Button dir = new Button("Select Directory");
		dir.relocate(150, 200);
		dir.setAlignment(Pos.CENTER);
		
		PlayList.primaryStage = primaryStage;
		PlayList.parameters = parameters;
		
		dir.setOnAction(e -> {
			askPath(PlayList.primaryStage);
			createList();
			// printList(songList);
			createSongPanes();
			try {
				Player.start(PlayList.primaryStage, PlayList.parameters, path);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		});

		getChildren().add(dir);
	}

	private String askPath(Stage stage) {
		DirectoryChooser dChooser = new DirectoryChooser();
		dChooser.setInitialDirectory(
				new File(System.getProperty("user.home") + System.getProperty("file.separator") + "Music"));
		try {
			File sDirectory = dChooser.showDialog(stage);
			path = sDirectory.getAbsolutePath();
		} catch (NullPointerException npe) {
			npe.printStackTrace();
		}
		return path;
	}

	private void createList() {
		songList = new LinkedList<>();
		File folder = new File(path);
		FilenameFilter filter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith("mp3");
			}
		};
		File[] files = folder.listFiles(filter);
		for (int i = 0; i < files.length; i++) {
			String[] meta = readMetaData(files[i].getAbsolutePath());
			Node current = new Node(files[i], "" + (i + 1), meta[0], meta[1], meta[2], meta[3]);
			songList.add(current);
		}
	}

	private String[] readMetaData(String path) {
		File f = new File(path);
		String[] meta = { "", "", "", "" };// title, artist, album, length
		try {
			MP3File mf = (MP3File) AudioFileIO.read(f);
			meta[0] = mf.getID3v2TagAsv24().getFirst(ID3v24Frames.FRAME_ID_TITLE);
			meta[1] = mf.getID3v2TagAsv24().getFirst(ID3v24Frames.FRAME_ID_ARTIST);
			meta[2] = mf.getID3v2TagAsv24().getFirst(ID3v24Frames.FRAME_ID_ALBUM);
			meta[3] = mf.getMP3AudioHeader().getTrackLengthAsString();
		} catch (CannotReadException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TagException e) {
			e.printStackTrace();
		} catch (ReadOnlyFileException e) {
			e.printStackTrace();
		} catch (InvalidAudioFrameException e) {
			e.printStackTrace();
		}
		if (meta[0].equals("")) {
			meta[0] = f.getName();
		}
		if (meta[1].equals("")) {
			meta[1] = "Unknown";
		}
		if (meta[2].equals("")) {
			meta[2] = "Unknown";
		}
		return meta;
	}

	public static void printList(LinkedList<Node> songlist) {
		for (Iterator<Node> it = songlist.iterator(); it.hasNext();) {
			Node node = it.next();
			System.out.println(node.getN() + " | " + node.getFile() + " | " + node.getTitle() + " | " + node.getArtist()
					+ " | " + node.getAlbum() + " | " + node.getLength());
		}
	}

	private void createSongPanes() {
		scrollP = new ScrollPane();
		scrollP.setBackground(getBackground());
		scrollP.setPrefSize(375, 400);
		scrollP.fitToWidthProperty().set(true);
		scrollP.setVbarPolicy(ScrollBarPolicy.ALWAYS);
		scrollP.pannableProperty().set(true);
		scrollP.relocate(10, 10);

		songPanes = new LinkedList<>();

		vbox = new VBox(10);
		vbox.setBackground(getBackground());
		vbox.relocate(10, 10);

		for (Iterator<Node> it = songList.iterator(); it.hasNext();) {
			Node node = it.next();
			Pane info = new Pane();
			info.setBackground(getBackground());
			info.setPrefSize(370, 50);
			info.setBorder(new Border(
					new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
			info.relocate(10, 10);

			createMeta(info, node);

			songPanes.add(info);
		}
		reloadPanes();
	}

	private void createMeta(Pane info, Node node) {
		Label n = new Label(node.getN());
		Label title = new Label(node.getTitle());
		Label artist = new Label(node.getArtist());
		Label album = new Label(node.getAlbum());
		Label length = new Label(node.getLength());
		MenuButton options = new MenuButton("", new ImageView("/icons/options.png"));

		MenuItem play = new MenuItem("Play");
		MenuItem del = new MenuItem("Delete");
		MenuItem playN = new MenuItem("Play Next");
		MenuItem addP = new MenuItem("Add to Playlist");
		options.getItems().add(play);
		options.getItems().add(del);
		options.getItems().add(playN);
		options.getItems().add(addP);

		n.setPrefSize(15, 15);
		n.relocate(5, 15);

		title.setPrefSize(300, 15);
		title.relocate(25, 5);

		artist.setPrefSize(100, 15);
		artist.relocate(25, 25);

		album.setPrefSize(100, 15);
		album.relocate(150, 25);

		length.setPrefSize(30, 15);
		length.relocate(260, 25);

		options.setPrefSize(25, 25);
		options.relocate(300, 10);

		info.getChildren().add(n);
		info.getChildren().add(title);
		info.getChildren().add(artist);
		info.getChildren().add(album);
		info.getChildren().add(length);
		info.getChildren().add(options);

		play.setOnAction(e -> {
			// System.out.println("Play " + title.getText());
			Player.mediaView.getMediaPlayer().stop();
			Player.mediaView.setMediaPlayer(Player.players.get(Integer.parseInt(node.getN()) - 1));
			// Player.mediaView.setMediaPlayer(Player.players.get(Integer.parseInt(node.getN()) - 1));
			// Player.setCurrentlyPlaying(Player.mediaView.getMediaPlayer());
			if (Player.players.indexOf(Player.mediaView.getMediaPlayer()) == 0) {
				Player.setCurrentlyPlaying(Player.mediaView.getMediaPlayer());
				// Player.setCurrentlyPlaying(Player.players.get(Integer.parseInt(node.getN()) - 1));
			} else {
				Player.setCurrentlyPlaying(Player.players.get(Integer.parseInt(node.getN()) - 2));
			}
			Player.mediaView.getMediaPlayer().play();
		});

		del.setOnAction(e -> {
			// System.out.println("Delete " + title.getText());
			for (Pane pane : songPanes) {
				vbox.getChildren().remove(pane);
			}
			songPanes.remove(info);
			songList.remove(node);
			createList();
			reloadPanes();
			Player.mediaView.getMediaPlayer().stop();
			try {
				Player.start(primaryStage, parameters, path);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		});
		playN.setOnAction(e -> {
			// System.out.println("Play Next " + title.getText());

		});

		addP.setOnAction(e -> {
			// System.out.println("Add to Playlist " + title.getText());

		});
	}

	private void reloadPanes() {
		for (Pane pane : songPanes) {
			vbox.getChildren().add(pane);
		}
		scrollP.setContent(vbox);
		getChildren().setAll(scrollP);
	}
}

class Node {

	private File file;
	private String n, title, artist, album, length;

	public Node(File file, String n, String title, String artist, String album, String length) {
		this.file = file;
		this.n = n;
		this.title = title;
		this.artist = artist;
		this.album = album;
		this.length = length;
	}

	public File getFile() {
		return file;
	}

	public String getN() {
		return n;
	}

	public String getTitle() {
		return title;
	}

	public String getArtist() {
		return artist;
	}

	public String getAlbum() {
		return album;
	}

	public String getLength() {
		return length;
	}

}
