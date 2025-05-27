package game.snake.players;

import game.snake.Direction;
import game.snake.utils.SnakeGameState;

import java.util.Random;

public abstract class SnakePlayer {
    protected SnakeGameState gameState;
    protected int color;
    protected Random random;

    public SnakePlayer(SnakeGameState gameState, int color, Random random) {
        this.gameState = gameState;
        this.color = color;
        this.random = random;
    }

    public abstract Direction getAction(long remainingTime);
}
