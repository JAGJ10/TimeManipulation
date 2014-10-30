package com.joel.prototype;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
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

public class Prototype extends Game {
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
    private TiledMapTileLayer collisionLayer;

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

        spriteBatch = renderer.getSpriteBatch();

        tiles = new Array<Rectangle>();

        // create an orthographic camera, shows us 30x20 units of the world
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 640, 640);
        camera.update();

        collisionLayer = (TiledMapTileLayer) level.getMap().getLayers().get(0);

        Gdx.input.setInputProcessor(player);
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
        player.getVelocity().y += Player.GRAVITY;

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
    
        // save old position
        float oldX = player.getX(), oldY = player.getY();
        boolean collisionX = false, collisionY = false;

        // move on x
        player.setX(player.getX() + player.getVelocity().x * deltaTime);

        if (player.getVelocity().x < 0) // going left
            collisionX = collidesLeft();
        else if (player.getVelocity().x > 0) // going right
            collisionX = collidesRight();

        // react to x collision
        if(collisionX) {
            player.setX(oldX);
            player.getVelocity().x = 0;
        }

        // move on y
        player.setY(player.getY() + player.getVelocity().y * deltaTime * 5f);

        if (player.getVelocity().y < 0) // going down
            player.setGrounded(collisionY = collidesBottom());
        else if(player.getVelocity().y > 0) // going up
            collisionY = collidesTop();

        // react to y collision
        if(collisionY) {
            player.setY(oldY);
            player.getVelocity().y = 0;
        }

        player.getPosition().add(player.getVelocity());
        player.getVelocity().scl(1 / deltaTime);
        //player.getVelocity().x *= Player.DAMPING;
    }

    private boolean isCellBlocked(float x, float y) {
        Cell cell = collisionLayer.getCell((int) (x / collisionLayer.getTileWidth()), (int) (y / collisionLayer.getTileHeight()));
        return cell != null && cell.getTile() != null;
    }

    public boolean collidesRight() {
        for(float step = 0; step < player.getHeight(); step += collisionLayer.getTileHeight() / 2)
            if(isCellBlocked(player.getX() + player.getWidth(), player.getY() + step))
                return true;
        return false;
    }

    public boolean collidesLeft() {
        for(float step = 0; step < player.getHeight(); step += collisionLayer.getTileHeight() / 2)
            if(isCellBlocked(player.getX(), player.getY() + step))
                return true;
        return false;
    }

    public boolean collidesTop() {
        for(float step = 0; step < player.getWidth(); step += collisionLayer.getTileWidth() / 2)
            if(isCellBlocked(player.getX() + step, player.getY() + player.getHeight()))
                return true;
        return false;

    }

    public boolean collidesBottom() {
        for(float step = 0; step < player.getWidth(); step += collisionLayer.getTileWidth() / 2)
            if(isCellBlocked(player.getX() + step, player.getY()))
                return true;
        return false;
    }

    @Override
    public void dispose() {
    }
}
