package game.snake.players;

import java.util.Random;

import game.snake.Direction;
import game.snake.SnakeGame;
import game.snake.SnakePlayer;
import game.snake.utils.SnakeGameState;

/**
 * A Snake játékhoz használt véletlenszerű döntéseket hozó ágens.
 * Az ágens minden lépésben véletlenszerűen választ egy irányt a lehetséges irányok közül.
 */
public class SamplePlayer extends SnakePlayer {

    public SamplePlayer(SnakeGameState gameState, int color, Random random) {
        super(gameState, color, random);
    }

    /**
     * A következő lépés irányának meghatározása.
     * Véletlenszerű irányválasztás.
     *
     * @param remainingTime a hátralévő idő (nem használjuk a példában).
     * @return a kiválasztott irány.
     */
    @Override
    public Direction getAction(long remainingTime) {
        Direction action = SnakeGame.DIRECTIONS[random.nextInt(SnakeGame.DIRECTIONS.length)];
        return action;
    }
}
