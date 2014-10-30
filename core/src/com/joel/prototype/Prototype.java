package com.joel.prototype;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

import java.util.ArrayList;

public class Prototype extends Game {
    private TiledMap map;
    private OrthogonalTiledMapRenderer renderer;
    private OrthographicCamera camera;

    private boolean recorded;
    private boolean isPaused;
    private ArrayList<GameObject> gameObjects;
    private Player player;
    private Texture texture;
    private Level level;
    private Batch spriteBatch;

    private Array<Rectangle> tiles;

    private Pool<Rectangle> rectPool = new Pool<Rectangle>() {
        @Override
        protected Rectangle newObject () {
            return new Rectangle();
        }
    };

    @Override
    public void create() {
        this.recorded = false;
        this.isPaused = false;
        gameObjects = new ArrayList<GameObject>();
        texture = new Texture("player.png");
        player = new Player(texture);
        player.setPosition(new Vector2(0, 33));

        // load the map, set the unit scale to 1/32 (1 unit == 32 pixels)
        level = new Level("prototype.tmx");
        renderer = new OrthogonalTiledMapRenderer(level.getMap());

        //spriteBatch = new SpriteBatch();
        spriteBatch = renderer.getSpriteBatch();

        tiles = new Array<Rectangle>();

        // create an orthographic camera, shows us 30x20 units of the world
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 640, 640);
        camera.update();
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

        spriteBatch.begin();
        spriteBatch.draw(player, player.getPosition().x, player.getPosition().y, player.getWidth(), player.getHeight());
        spriteBatch.end();

        this.updatePlayer(deltaTime);
        player.update(deltaTime);
    }

    public void updatePlayer(float deltaTime) {
        if ((Gdx.input.isKeyPressed(Input.Keys.UP)) && player.isGrounded()) {
            player.getVelocity().y += Player.JUMP_VELOCITY;
            player.setGrounded(false);
        }

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A)) {
            player.getVelocity().x = -Player.MAX_VELOCITY;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)) {
            player.getVelocity().x = Player.MAX_VELOCITY;
        }

        player.getVelocity().add(0, Player.GRAVITY);

        // clamp the velocity to the maximum, x-axis only
        if (Math.abs(player.getVelocity().x) > Player.MAX_VELOCITY) {
            player.getVelocity().x = Math.signum(player.getVelocity().x) * Player.MAX_VELOCITY;
        }

        // clamp the velocity to 0 if it's < 1
        if (Math.abs(player.getVelocity().x) < 1) {
            player.getVelocity().x = 0;
        }

        // multiply by delta time so we know how far we go
        // in this frame
        player.getVelocity().scl(deltaTime);

        //Rectangle playerRect = player.getBoundingRectangle();
        Rectangle playerRect = rectPool.obtain();
        playerRect.set(player.getPosition().x, player.getPosition().y, player.getWidth(), player.getHeight());
        int startX, startY, endX, endY;
        if (player.getVelocity().x > 0) {
            startX = endX = (int)(player.getPosition().x + player.getWidth() + player.getVelocity().x);
        } else {
            startX = endX = (int)(player.getPosition().x + player.getVelocity().x);
        }
        startY = (int)(player.getPosition().y);
        endY = (int)(player.getPosition().y + player.getHeight());
        getTiles(startX, startY, endX, endY, tiles);

        playerRect.x += player.getVelocity().x;
        for (Rectangle tile : tiles) {
            if (playerRect.overlaps(tile)) {
                player.getVelocity().x = 0;
                break;
            }
        }

        playerRect.set(player.getPosition().x, player.getPosition().y - player.getHeight(), player.getWidth(), player.getHeight());

        if (player.getVelocity().y > 0) {
            startY = endY = (int)(player.getPosition().y + player.getHeight() + player.getVelocity().y);
        } else {
            startY = endY = (int)(player.getPosition().y + player.getVelocity().y);
        }

        startX = (int)(player.getPosition().x);
        endX = (int)(player.getPosition().x + player.getWidth());
        getTiles(startX, startY, endX, endY, tiles);
        playerRect.y += player.getVelocity().y;
        for (Rectangle tile : tiles) {
            if (playerRect.overlaps(tile)) {
                // we actually reset the koala y-position here
                // so it is just below/above the tile we collided with
                // this removes bouncing :)
                if (player.getVelocity().y > 0) {
                    player.getVelocity().y = tile.y - player.getHeight();
                } else {
                    player.getPosition().y = tile.y + tile.height;
                    // if we hit the ground, mark us as grounded so we can jump
                    player.setGrounded(true);
                }
                player.getVelocity().y = 0;
                break;
            }
        }

        player.getPosition().add(player.getVelocity());
        player.getVelocity().scl(1 / deltaTime);
        player.getVelocity().x *= Player.DAMPING;
    }

    private void getTiles (int startX, int startY, int endX, int endY, Array<Rectangle> tiles) {
        TiledMapTileLayer layer = (TiledMapTileLayer)level.getMap().getLayers().get(0);
        tiles.clear();
        for (int y = startY; y <= endY; y++) {
            for (int x = startX; x <= endX; x++) {
                Cell cell = layer.getCell(x, y);
                if (cell != null) {
                    Rectangle rect = rectPool.obtain();
                    rect.set(x, y, 1, 1);
                    tiles.add(rect);
                }
            }
        }
    }

    @Override
    public void dispose() {
    }
}
