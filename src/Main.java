import game.engine.Engine;

public class Main {
    public static void main(String[] args) {
        String[] gameArgs = {
                "10",
                "game.snake.SnakeGame",
                "1234567890",
                "15",
                "25",
                "10000",
                "game.snake.players.Agent"
        };

        try {
            Engine.main(gameArgs);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
