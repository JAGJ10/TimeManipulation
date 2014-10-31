package com.joel.prototype;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import java.util.ArrayList;

public class Prototype extends Game implements InputProcessor {
    private OrthogonalTiledMapRenderer renderer;
    private OrthographicCamera camera;

    private boolean recorded;
    private boolean isPaused;
    private ArrayList<GameObject> gameObjects;
    private Player player;
    private Texture texture;
    private Texture textureAlpha;
    private Level level;
    private Batch spriteBatch;

    private Array<Rectangle> tiles;
    private TiledMapTileLayer collisionLayer;

    @Override
    public void create() {
        this.recorded = false;
        this.isPaused = false;
        gameObjects = new ArrayList<GameObject>();
        texture = new Texture("player.png");
        textureAlpha = new Texture("playerAlpha.png");
        player = new Player(texture);
        player.setPosition(0, 32);

        level = new Level("prototype.tmx");
        renderer = new OrthogonalTiledMapRenderer(level.getMap());

        spriteBatch = renderer.getSpriteBatch();

        tiles = new Array<Rectangle>();

        camera = new OrthographicCamera();
        camera.setToOrtho(false, 640, 640);
        camera.update();

        collisionLayer = (TiledMapTileLayer) level.getMap().getLayers().get(0);

        Gdx.input.setInputProcessor(this);
    }

    @Override
    public void render() {
        // clear the screen
        Gdx.gl.glClearColor(0f, 0f, 0f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // get the delta time
        float deltaTime = Gdx.graphics.getDeltaTime();

        camera.update();

        renderer.setView(camera);
        renderer.render();

        if (isPaused) {
            ArrayList<FrameState> fs = player.getFrameStates();
            for (int i = 0; i < fs.size(); i++) {
                spriteBatch.begin();
                spriteBatch.draw(textureAlpha, fs.get(i).x, fs.get(i).y, textureAlpha.getWidth(), textureAlpha.getHeight());
                spriteBatch.end();
            }
        }

        spriteBatch.begin();
        spriteBatch.draw(player, player.getX(), player.getY(), player.getWidth(), player.getHeight());
        spriteBatch.end();

        if (!isPaused) {
            player.update(deltaTime);
            this.checkCollisions(deltaTime);
        }

        if (!recorded) player.saveStateAt(deltaTime);
    }

    public void checkCollisions(float deltaTime) {
        // save old position
        float oldX = player.getX(), oldY = player.getY();
        boolean collisionX = false, collisionY = false;

        player.setX(player.getX() + player.getVelocity().x * deltaTime);

        if (player.getVelocity().x < 0) {
            collisionX = collidesLeft();
        } else if (player.getVelocity().x > 0) {
            collisionX = collidesRight();
        }

        if (collisionX) {
            player.setX(oldX);
            player.getVelocity().x = 0;
        }

        player.setY(player.getY() + player.getVelocity().y * deltaTime);

        if (player.getVelocity().y < 0) { // going down
            collisionY = collidesBottom();
            player.setGrounded(collisionY);
        } else if (player.getVelocity().y > 0) // going up
            collisionY = collidesTop();

        // react to y collision
        if (collisionY) {
            player.setY(oldY);
            player.getVelocity().y = 0;
        }
    }

    private boolean isCellBlocked(float x, float y) {
        Cell cell = collisionLayer.getCell((int) (x / collisionLayer.getTileWidth()), (int) (y / collisionLayer.getTileHeight()));
        return (cell != null && cell.getTile() != null);
    }

    public boolean collidesRight() {
        for (float step = 0; step < player.getHeight(); step += collisionLayer.getTileHeight() / 2)
            if (isCellBlocked(player.getX() + player.getWidth(), player.getY() + step))
                return true;
        return false;
    }

    public boolean collidesLeft() {
        for (float step = 0; step < player.getHeight(); step += collisionLayer.getTileHeight() / 2)
            if (isCellBlocked(player.getX(), player.getY() + step))
                return true;
        return false;
    }

    public boolean collidesTop() {
        for (float step = 0; step < player.getWidth(); step += collisionLayer.getTileWidth() / 2)
            if (isCellBlocked(player.getX() + step, player.getY() + player.getHeight()))
                return true;
        return false;
    }

    public boolean collidesBottom() {
        for (float step = 0; step < player.getWidth(); step += collisionLayer.getTileWidth() / 2)
            if (isCellBlocked(player.getX() + step, player.getY()))
                return true;
        return false;
    }

    @Override
    public boolean keyDown(int keycode) {
        switch (keycode) {
            case Input.Keys.W:
                if (player.isGrounded()) {
                    player.getVelocity().y = Player.JUMP_VELOCITY;
                    player.setGrounded(false);
                }
                break;
            case Input.Keys.A:
                player.setMoveLeft(true);
                break;
            case Input.Keys.D:
                player.setMoveRight(true);
                break;
            case Input.Keys.SPACE:
                isPaused = !isPaused;
                recorded = true;
                break;
            case Input.Keys.P:
                isPaused = !isPaused;
                break;
            case Input.Keys.LEFT:
                if (isPaused) {
                    int fCount = player.getFrameCounter() - 1;
                    player.replayFrame(fCount);
                    player.setFrameCounter(fCount--);
                }
                break;
            case Input.Keys.RIGHT:
                if (isPaused) {
                    int fCount = player.getFrameCounter();
                    player.replayFrame(fCount);
                    player.setFrameCounter(++fCount);
                }
                break;
            case Input.Keys.UP:
                if (isPaused) {
                    player.setVelocity(new Vector2(player.getVelocity().x, player.getVelocity().y + 1000f));
                    int fCount = player.getFrameCounter() - 1;
                    player.recalcStates(fCount);
                }
        }
        player.setKeycode(keycode);
        return true;
    }

    @Override
    public boolean keyUp(int keycode) {
        switch (keycode) {
            case Input.Keys.A:
                player.setMoveLeft(false);
            case Input.Keys.D:
                player.setMoveRight(false);
                break;
            case Input.Keys.SPACE:
                isPaused = !isPaused;
        }
        player.setKeycode(-10);
        return true;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }

    @Override
    public void dispose() {
    }
}
