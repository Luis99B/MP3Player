//Luis Bodart A01635000

package mp3player;

import java.io.File;
import java.io.FilenameFilter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.Stack;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import javafx.application.Application.Parameters;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Player extends Pane {

	public static final List<String> EXTENSION = Arrays.asList(".mp3");
	public static final int FILE_EXTENSION_LEN = 3;

	private static LinkedList<Node> songsPlaylist;
	public static MediaView mediaView;
	public static List<MediaPlayer> players = new ArrayList<>();
	private static Stack<MediaPlayer> recentlyPlayed = new Stack<>();
	private static Queue<MediaPlayer> nextToPlay = new LinkedList<>();
	private static ChangeListener<Duration> progressChangeListener;

	private static Label title, artist, actualTime, length;
	private static Button shuffle, prev, pause, play, stop, next, repeat, playlist;
	private static ProgressBar progress = new ProgressBar();
	private static ImageView shuffleOFF = new ImageView("/icons/shuffle.png"),
			shuffleON = new ImageView("/icons/shuffleON.png"), repeatOFF = new ImageView("/icons/repeat.png"),
			repeatON = new ImageView("/icons/repeatON.png");

	public Player(Stage primaryStage) {
		setBackground(new Background(new BackgroundFill(Color.rgb(200, 200, 200), CornerRadii.EMPTY, Insets.EMPTY)));
		setPrefSize(385, 100);
		relocate(0, 400);

		createComponents(primaryStage);
		progress.setProgress(0);
	}

	private void createComponents(Stage primaryStage) {
		VBox vbox = new VBox(10);
		HBox hboxSong = new HBox(10);
		HBox hboxBtn = new HBox();
		HBox hboxTime = new HBox(5);

		title = new Label("Title of Song");
		artist = new Label("Artists");
		shuffle = new Button("", shuffleOFF);
		prev = new Button("", new ImageView("/icons/prev.png"));
		pause = new Button("", new ImageView("/icons/pause.png"));
		play = new Button("", new ImageView("/icons/play.png"));
		stop = new Button("", new ImageView("/icons/stop.png"));
		next = new Button("", new ImageView("/icons/next.png"));
		repeat = new Button("", repeatOFF);
		playlist = new Button("", new ImageView("/icons/playlist.png"));
		actualTime = new Label("-:--");
		length = new Label("-:--");

		title.setPrefSize(180, 15);
		title.relocate(20, 10);
		title.setStyle("-fx-font-weight: bold;");

		artist.setPrefSize(150, 15);
		artist.relocate(180, 10);

		shuffle.setPrefSize(32, 32);
		shuffle.relocate(10, 35);

		prev.setPrefSize(32, 32);
		prev.relocate(45, 35);

		pause.setPrefSize(32, 32);
		pause.relocate(80, 35);

		play.setPrefSize(32, 32);
		play.relocate(115, 35);

		stop.setPrefSize(32, 32);
		stop.relocate(150, 35);

		next.setPrefSize(32, 32);
		next.relocate(185, 35);

		repeat.setPrefSize(32, 32);
		repeat.relocate(220, 35);

		playlist.setPrefSize(32, 32);
		playlist.relocate(255, 35);

		actualTime.setPrefSize(30, 10);
		actualTime.relocate(50, 80);

		progress.setPrefSize(100, 15);
		progress.relocate(60, 70);

		length.setPrefSize(40, 10);
		length.relocate(170, 80);

		Label space = new Label();
		space.setPrefSize(30, 15);

		hboxSong.getChildren().add(space);
		hboxSong.getChildren().add(title);
		hboxSong.getChildren().add(artist);

		hboxBtn.getChildren().add(shuffle);
		hboxBtn.getChildren().add(prev);
		hboxBtn.getChildren().add(pause);
		hboxBtn.getChildren().add(play);
		hboxBtn.getChildren().add(stop);
		hboxBtn.getChildren().add(next);
		hboxBtn.getChildren().add(repeat);
		hboxBtn.getChildren().add(playlist);

		Label space2 = new Label();
		space2.setPrefSize(100, 10);

		hboxTime.getChildren().add(space2);
		hboxTime.getChildren().add(actualTime);
		hboxTime.getChildren().add(progress);
		hboxTime.getChildren().add(length);

		hboxSong.relocate(10, 0);
		hboxTime.relocate(10, 0);

		vbox.getChildren().add(hboxSong);
		vbox.getChildren().add(hboxBtn);
		vbox.getChildren().add(hboxTime);

		getChildren().add(vbox);
	}

	public static void start(Stage stage, Parameters parameters, String path) throws Exception {
		songsPlaylist = PlayList.songList;
		// System.out.println(DIR);
		// MusicPlayList.printList(songsPlaylist);
		List<String> params = parameters.getRaw();
		File directory;
		if (params.size() > 0) {
			directory = new File(params.get(0));
		} else {
			directory = new File(path);
		}
		if (!directory.exists() || !directory.isDirectory()) {
			System.out.println("Cannot find audio source directory: " + directory
					+ " please supply a directory as a command line argument");
			Platform.exit();
			return;
		}

		for (String file : directory.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				for (String ext : EXTENSION) {
					if (name.endsWith(ext)) {
						return true;
					}
				}
				return false;
			}
		}))
			players.add(createPlayer("file:///" + (directory + "\\" + file).replace("\\", "/").replaceAll(" ", "%20")));
		if (players.isEmpty()) {
			System.out.println("No audio found in " + directory);
			Platform.exit();
			return;
		}

		mediaView = new MediaView(players.get(0));
		
		for (int i = 0; i < players.size(); i++) {
			MediaPlayer player = players.get(i);
			MediaPlayer nextPlayer = players.get((i + 1) % players.size());
			nextToPlay.add(nextPlayer);
			player.setOnEndOfMedia(new Runnable() {
				@Override
				public void run() {
					mediaView.getMediaPlayer().setVolume(1.0);
					mediaView.getMediaPlayer().setAutoPlay(false);
					player.currentTimeProperty().removeListener(progressChangeListener);
					player.stop();
					mediaView.setMediaPlayer(nextPlayer);
					title.setText(songsPlaylist.get((players.indexOf(nextPlayer))).getTitle());
					artist.setText(songsPlaylist.get((players.indexOf(nextPlayer))).getArtist());
					nextPlayer.play();
				}
			});
		}

		shuffle.setOnAction(e -> {
			if (!songsPlaylist.isEmpty()) {
				if (shuffle.getGraphic().equals(shuffleOFF)) {
					// System.out.println("Shuffle ON");
					shuffle.setGraphic(shuffleON);
					nextToPlay = new LinkedList<>();
					ArrayList<Integer> randoms = new ArrayList<>();
					for (int i = 0; i < players.size(); i++) {
						int r = new Random().nextInt(players.size());
						if (randoms.contains(r)) {
							i--;
						} else {							
							randoms.add(r);
						}
					}
					//System.out.println(randoms.toString());
					LinkedList<Node> newSongPlaylist = new LinkedList<>();
					nextToPlay = new LinkedList<>();
					for (int i = 0; i < randoms.size() - 1; i++) {
						MediaPlayer player = players.get(randoms.get(i));
						MediaPlayer nextPlayer = players.get((randoms.get(i + 1)) % players.size());
						newSongPlaylist.add(songsPlaylist.get(i));
						nextToPlay.add(nextPlayer);
						player.setOnEndOfMedia(new Runnable() {
							@Override
							public void run() {
								mediaView.getMediaPlayer().setVolume(1.0);
								mediaView.getMediaPlayer().setAutoPlay(false);
								player.currentTimeProperty().removeListener(progressChangeListener);
								player.stop();
								mediaView.setMediaPlayer(nextPlayer);
								title.setText(songsPlaylist.get((players.indexOf(nextPlayer))).getTitle());
								artist.setText(songsPlaylist.get((players.indexOf(nextPlayer))).getArtist());
								nextPlayer.play();
							}
						});
					}
					songsPlaylist = newSongPlaylist;
				} else {
					// System.out.println("Shuffle OFF");
					shuffle.setGraphic(shuffleOFF);
				}
				//System.out.println(new Random().nextInt(players.size() + 1));
			}
		});

		prev.setOnAction(e -> {
			if (!songsPlaylist.isEmpty()) {
				if (!recentlyPlayed.isEmpty()) {
					// System.out.println("Previous Song");
					MediaPlayer curPlayer = mediaView.getMediaPlayer();
					curPlayer.currentTimeProperty().removeListener(progressChangeListener);
					curPlayer.stop();

					MediaPlayer prevPlayer = recentlyPlayed.pop();
					// MediaPlayer prevPlayer = players.get((players.indexOf(curPlayer) - 1) %
					// players.size());
					mediaView.setMediaPlayer(prevPlayer);
					prevPlayer.play();
					title.setText(songsPlaylist.get((players.indexOf(prevPlayer))).getTitle());
					artist.setText(songsPlaylist.get((players.indexOf(prevPlayer))).getArtist());
					// title.setText(songsPlaylist.get((players.indexOf(mediaView.getMediaPlayer())
					// - 1)).getTitle());
					// artist.setText(songsPlaylist.get((players.indexOf(mediaView.getMediaPlayer())
					// - 1)).getArtist());
					// title.setText(songsPlaylist.get((players.indexOf(mediaView.getMediaPlayer()))
					// % players.size()).getTitle());
					// artist.setText(songsPlaylist.get((players.indexOf(mediaView.getMediaPlayer()))
					// % players.size()).getArtist());
				}
			}
		});

		pause.setOnAction(e -> {
			if (!songsPlaylist.isEmpty()) {
				// System.out.println("Pause Song");
				mediaView.getMediaPlayer().pause();
			}
		});

		play.setOnAction(e -> {
			if (!songsPlaylist.isEmpty()) {
				// System.out.println("Play Song");
				if (mediaView.getMediaPlayer().getCurrentTime().equals(mediaView.getMediaPlayer().getTotalDuration())) {
					mediaView.getMediaPlayer().seek(mediaView.getMediaPlayer().getStartTime());
				}
				mediaView.getMediaPlayer().play();
				title.setText(songsPlaylist.get((players.indexOf(mediaView.getMediaPlayer()))).getTitle());
				artist.setText(songsPlaylist.get((players.indexOf(mediaView.getMediaPlayer()))).getArtist());
				// title.setText(songsPlaylist.get((players.indexOf(mediaView.getMediaPlayer()))
				// % players.size()).getTitle());
				// artist.setText(songsPlaylist.get((players.indexOf(mediaView.getMediaPlayer()))
				// % players.size()).getArtist());
			}
		});

		stop.setOnAction(e -> {
			if (!songsPlaylist.isEmpty()) {
				// System.out.println("Stop Song");
				mediaView.getMediaPlayer().stop();
			}
		});

		next.setOnAction(e -> {
			if (!songsPlaylist.isEmpty()) {
				if (!nextToPlay.isEmpty()) {
					// System.out.println("Next Song");
					MediaPlayer curPlayer = mediaView.getMediaPlayer();
					curPlayer.currentTimeProperty().removeListener(progressChangeListener);
					curPlayer.stop();

					recentlyPlayed.push(curPlayer);
					MediaPlayer nextPlayer = nextToPlay.poll();
					// MediaPlayer nextPlayer = players.get((players.indexOf(curPlayer) + 1) %
					// players.size());
					mediaView.setMediaPlayer(nextPlayer);
					nextPlayer.play();
					title.setText(songsPlaylist.get((players.indexOf(nextPlayer))).getTitle());
					artist.setText(songsPlaylist.get((players.indexOf(nextPlayer))).getArtist());
					// title.setText(songsPlaylist.get((players.indexOf(mediaView.getMediaPlayer())
					// + 1)).getTitle());
					// artist.setText(songsPlaylist.get((players.indexOf(mediaView.getMediaPlayer())
					// + 1)).getArtist());
				}
			}
		});

		repeat.setOnAction(e -> {
			if (!songsPlaylist.isEmpty()) {
				if (repeat.getGraphic().equals(repeatOFF)) {
					// System.out.println("Repeat ON");
					repeat.setGraphic(repeatON);
					title.setText(songsPlaylist.get((players.indexOf(mediaView.getMediaPlayer()))).getTitle());
					artist.setText(songsPlaylist.get((players.indexOf(mediaView.getMediaPlayer()))).getArtist());
					mediaView.getMediaPlayer().setAutoPlay(true);
				} else {
					// System.out.println("Repeat OFF");
					repeat.setGraphic(repeatOFF);
					mediaView.getMediaPlayer().setAutoPlay(false);
				}
			}
		});

		playlist.setOnAction(e -> {
			if (!songsPlaylist.isEmpty()) {
				// System.out.println("Show Playlist");
				JTextArea area = new JTextArea();
				area.setSize(500, 400);
				area.setEditable(false);
				area.setText("#" + "\t     Artist" + "\t\t     Artist" + "\t\t\t     Album\n");
				for (int i = 0; i < songsPlaylist.size(); i++) {
					area.setText(area.getText() + songsPlaylist.get(i).getN() + "\t" + songsPlaylist.get(i).getTitle()
							+ "\t" + songsPlaylist.get(i).getArtist() + "\t" + songsPlaylist.get(i).getAlbum()
							+ "     \n");
					if (i < 20) {
						area.setRows(i + 1);
					}
				}
				JScrollPane scrollPane = new JScrollPane(area, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
						JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
				scrollPane.setSize(500, 400);
				JOptionPane.showMessageDialog(null, scrollPane, "Current Playlist", JOptionPane.PLAIN_MESSAGE, null);
			}
		});

		mediaView.mediaPlayerProperty().addListener(new ChangeListener<MediaPlayer>() {
			@Override
			public void changed(ObservableValue<? extends MediaPlayer> observableValue, MediaPlayer oldPlayer,
					MediaPlayer newPlayer) {
				setCurrentlyPlaying(newPlayer);
			}
		});
		setCurrentlyPlaying(mediaView.getMediaPlayer());
	}

	public static void setCurrentlyPlaying(MediaPlayer newPlayer) {
		newPlayer.seek(Duration.ZERO);

		progress.setProgress(0);
		progressChangeListener = new ChangeListener<Duration>() {
			@Override
			public void changed(ObservableValue<? extends Duration> observableValue, Duration oldValue,
					Duration newValue) {
				progress.setProgress(
						1.0 * newPlayer.getCurrentTime().toMillis() / newPlayer.getTotalDuration().toMillis());
				DecimalFormatSymbols dfs = new DecimalFormatSymbols();
				dfs.setDecimalSeparator(':');
				DecimalFormat df = new DecimalFormat("0.00", dfs);
				actualTime.setText(df.format(newPlayer.getCurrentTime().toMinutes()));
			}
		};

		newPlayer.currentTimeProperty().addListener(progressChangeListener);

		title.setText(songsPlaylist.get((players.indexOf(newPlayer)) + 1).getTitle());
		artist.setText(songsPlaylist.get((players.indexOf(newPlayer)) + 1).getArtist());

		DecimalFormatSymbols dfs = new DecimalFormatSymbols();
		dfs.setDecimalSeparator(':');
		DecimalFormat df = new DecimalFormat("0.00", dfs);
		if (newPlayer.getStatus().equals(Status.UNKNOWN)) {
			String[] n = songsPlaylist.getFirst().getLength().split(":");
			Double num = Double.parseDouble(n[0] + "." + n[1]);
			actualTime.setText("0:00");
			length.setText("" + df.format(num));
		} else {
			length.setText("" + df.format(newPlayer.getTotalDuration().toMinutes()));
		}
	}

	private static MediaPlayer createPlayer(String mediaSource) {
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
		Media media = new Media(mediaSource);
		MediaPlayer player = new MediaPlayer(media);
		player.setOnError(new Runnable() {
			@Override
			public void run() {
				System.out.println("Media error occurred: " + player.getError());
			}
		});
		return player;
	}

}
