package at.ac.fhcampuswien.fhmdb.controllers;

import at.ac.fhcampuswien.fhmdb.factoryPattern.MyFactory;
import at.ac.fhcampuswien.fhmdb.enums.UIComponent;
import at.ac.fhcampuswien.fhmdb.statePattern.*;
import at.ac.fhcampuswien.fhmdb.database.*;
import at.ac.fhcampuswien.fhmdb.models.Movie;
import com.jfoenix.controls.*;
import com.jfoenix.transitions.hamburger.HamburgerBasicCloseTransition;
import javafx.animation.TranslateTransition;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.util.Duration;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MainController implements Observer{
    private MovieSorter movieSorter;

    @FXML
    private ListView<Movie> movieListView;

    public MainController() throws DataBaseException {
        this.movieSorter = new MovieSorter();
        WatchlistRepository.getInstance().addObserver((at.ac.fhcampuswien.fhmdb.observerPattern.Observer) this);
    }


    @FXML
    public void sortAscending() {
        movieSorter.sortAscending();
        updateMovieList();
    }

    @FXML
    public void sortDescending() {
        movieSorter.sortDescending();
        updateMovieList();
    }

    @FXML
    public void resetSort() {
        movieSorter.resetSort();
        updateMovieList();
    }

    private void updateMovieList() {
        List<Movie> movies = getMoviesFromRepository(); // Fetch the list of movies
        movieSorter.sort(movies);
        movieListView.setItems(FXCollections.observableArrayList(movies));
    }
    @FXML
    public JFXHamburger hamburgerMenu;

    @FXML
    private JFXDrawer drawer;

    @FXML
    private BorderPane mainPane;

    private boolean isMenuCollapsed = true;

    private final MyFactory myFactory = MyFactory.getInstance();

    private HamburgerBasicCloseTransition transition;

    public void initialize() {
        FXMLLoader loader = new FXMLLoader(MainController.class.getResource(UIComponent.HOME.path));
        loader.setControllerFactory(myFactory);

        transition = new HamburgerBasicCloseTransition(hamburgerMenu);
        transition.setRate(-1);
        drawer.toBack();

        hamburgerMenu.addEventHandler(MouseEvent.MOUSE_CLICKED, (e) -> {
            toggleMenuDrawer();
        });
        // start with home view
        navigateToMovielist();
    }

    private void toggleHamburgerTransitionState(){
        transition.setRate(transition.getRate() * -1);
        transition.play();
    }

    private void toggleMenuDrawer(){
        toggleHamburgerTransitionState();

        if(isMenuCollapsed) {
            TranslateTransition translateTransition=new TranslateTransition(Duration.seconds(0.5), drawer);
            translateTransition.setByX(130);
            translateTransition.play();
            isMenuCollapsed = false;
            drawer.toFront();
        } else {
            TranslateTransition translateTransition=new TranslateTransition(Duration.seconds(0.5), drawer);
            translateTransition.setByX(-130);
            translateTransition.play();
            isMenuCollapsed = true;
            drawer.toBack();
        }
    }

    public void setContent(String fxmlPath){
        FXMLLoader loader = new FXMLLoader(MainController.class.getResource(fxmlPath));
        loader.setControllerFactory(myFactory);
        try {
            mainPane.setCenter(loader.load());
        } catch (Exception e) {
            e.printStackTrace();
        }

        if(!isMenuCollapsed){
            toggleMenuDrawer();
        }
    }

    // count which actor is in the most movies
    public String getMostPopularActor(List<Movie> movies) {
        String actor = movies.stream()
                .flatMap(movie -> movie.getMainCast().stream())
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("");

        return actor;
    }

    public int getLongestMovieTitle(List<Movie> movies) {
        return movies.stream()
                .mapToInt(movie -> movie.getTitle().length())
                .max()
                .orElse(0);
    }

    public long countMoviesFrom(List<Movie> movies, String director) {
        return movies.stream()
                .filter(movie -> movie.getDirectors().contains(director))
                .count();
    }

    public List<Movie> getMoviesBetweenYears(List<Movie> movies, int startYear, int endYear) {
        return movies.stream()
                .filter(movie -> movie.getReleaseYear() >= startYear && movie.getReleaseYear() <= endYear)
                .collect(Collectors.toList());
    }

    @FXML
    public void navigateToWatchlist() {
        setContent(UIComponent.WATCHLIST.path);
    }

    @FXML
    public void navigateToMovielist() {
        setContent(UIComponent.MOVIELIST.path);
    }

    @Override
    public void update(Observable o, Object arg) {

    }
}