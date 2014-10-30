package com.joel.prototype;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
//import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

public class SuperKoalio extends Game {
    static class Koala {
        static float WIDTH;
        static float HEIGHT;
        static float MAX_VELOCITY = 32f;
        static float JUMP_VELOCITY = 32f;
        static float DAMPING = 0f;

        enum State {
            Standing, Walking, Jumping
        }

        final Vector2 position = new Vector2();
        final Vector2 velocity = new Vector2();
        State state = State.Walking;
        float stateTime = 0;
        boolean facesRight = true;
        boolean grounded = false;
    }

    private TiledMap map;
    private OrthogonalTiledMapRenderer renderer;
    private OrthographicCamera camera;
    private Texture koalaTexture;
    private Animation stand;
    private Animation walk;
    private Animation jump;
    private Koala koala;
    private Pool<Rectangle> rectPool = new Pool<Rectangle>() {
        @Override
        protected Rectangle newObject () {
            return new Rectangle();
        }
    };
    private Array<Rectangle> tiles = new Array<Rectangle>();

    private static final float GRAVITY = -2.5f;

    @Override
    public void create () {
        // load the koala frames, split them, and assign them to Animations
        koalaTexture = new Texture("player.png");
        TextureRegion[] regions = TextureRegion.split(koalaTexture, 18, 26)[0];
        stand = new Animation(0, regions[0]);
        jump = new Animation(0, regions[0]);
        walk = new Animation(0.15f, regions[0], regions[0], regions[0]);
        walk.setPlayMode(Animation.PlayMode.LOOP_PINGPONG);

        // figure out the width and height of the koala for collision
        // detection and rendering by converting a koala frames pixel
        // size into world units (1 unit == 16 pixels)
        Koala.WIDTH = 1;
        Koala.HEIGHT = 1;

        // load the map, set the unit scale to 1/16 (1 unit == 16 pixels)
        map = new TmxMapLoader().load("prototype.tmx");
        renderer = new OrthogonalTiledMapRenderer(map, 1 / 32f);

        // create an orthographic camera, shows us 30x20 units of the world
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 20, 20);
        camera.update();

        // create the Koala we want to move around the world
        koala = new Koala();
        koala.position.set(0, 0);
    }

    @Override
    public void render () {
        // clear the screen
        Gdx.gl.glClearColor(0f, 0f, 0f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // get the delta time
        float deltaTime = Gdx.graphics.getDeltaTime();

        // update the koala (process input, collision detection, position update)
        updateKoala(deltaTime);

        // let the camera follow the koala, x-axis only
        camera.update();

        // set the tile map rendere view based on what the
        // camera sees and render the map
        renderer.setView(camera);
        renderer.render();

        // render the koala
        renderKoala(deltaTime);
    }

    private Vector2 tmp = new Vector2();

    private void updateKoala (float deltaTime) {
        if (deltaTime == 0) return;
        koala.stateTime += deltaTime;

        // check input and apply to velocity & state
        if ((Gdx.input.isKeyJustPressed(Keys.UP) || isTouched(0.75f, 1)) && koala.grounded) {
            //koala.velocity.y += Koala.JUMP_VELOCITY;
            koala.position.y += 7;
            koala.state = Koala.State.Jumping;
            koala.grounded = false;
        }

        if (Gdx.input.isKeyJustPressed(Keys.LEFT) || Gdx.input.isKeyJustPressed(Keys.A) || isTouched(0, 0.25f)) {
            //koala.velocity.x = -Koala.MAX_VELOCITY;
            koala.position.x -= 1;
            if (koala.grounded) koala.state = Koala.State.Walking;
            koala.facesRight = false;
        }

        if (Gdx.input.isKeyJustPressed(Keys.RIGHT) || Gdx.input.isKeyJustPressed(Keys.D) || isTouched(0.25f, 0.5f)) {
            //koala.velocity.x = Koala.MAX_VELOCITY;
            koala.position.x += 1;
            if (koala.grounded) koala.state = Koala.State.Walking;
            koala.facesRight = true;
        }

        // apply gravity if we are falling
        koala.velocity.add(0, GRAVITY);

        // clamp the velocity to the maximum, x-axis only
        if (Math.abs(koala.velocity.x) > Koala.MAX_VELOCITY) {
            koala.velocity.x = Math.signum(koala.velocity.x) * Koala.MAX_VELOCITY;
        }

        // clamp the velocity to 0 if it's < 1, and set the state to standign
        if (Math.abs(koala.velocity.x) < 1) {
            koala.velocity.x = 0;
            if (koala.grounded) koala.state = Koala.State.Standing;
        }

        // multiply by delta time so we know how far we go
        // in this frame
        koala.velocity.scl(deltaTime);

        // perform collision detection & response, on each axis, separately
        // if the koala is moving right, check the tiles to the right of it's
        // right bounding box edge, otherwise check the ones to the left
        Rectangle koalaRect = rectPool.obtain();
        koalaRect.set(koala.position.x, koala.position.y, Koala.WIDTH, Koala.HEIGHT);
        int startX, startY, endX, endY;
        if (koala.velocity.x > 0) {
            startX = endX = (int)(koala.position.x + Koala.WIDTH + koala.velocity.x);
        } else {
            startX = endX = (int)(koala.position.x + koala.velocity.x);
        }
        startY = (int)(koala.position.y);
        endY = (int)(koala.position.y + Koala.HEIGHT);
        getTiles(startX, startY, endX, endY, tiles);
        koalaRect.x += koala.velocity.x;
        for (Rectangle tile : tiles) {
            if (koalaRect.overlaps(tile)) {
                koala.velocity.x = 0;
                break;
            }
        }
        koalaRect.x = koala.position.x;

        // if the koala is moving upwards, check the tiles to the top of it's
        // top bounding box edge, otherwise check the ones to the bottom
        if (koala.velocity.y > 0) {
            startY = endY = (int)(koala.position.y + Koala.HEIGHT + koala.velocity.y);
        } else {
            startY = endY = (int)(koala.position.y + koala.velocity.y);
        }
        startX = (int)(koala.position.x);
        endX = (int)(koala.position.x + Koala.WIDTH);
        getTiles(startX, startY, endX, endY, tiles);
        koalaRect.y += koala.velocity.y;
        for (Rectangle tile : tiles) {
            if (koalaRect.overlaps(tile)) {
                // we actually reset the koala y-position here
                // so it is just below/above the tile we collided with
                // this removes bouncing :)
                if (koala.velocity.y > 0) {
                    koala.position.y = tile.y - Koala.HEIGHT;
                } else {
                    koala.position.y = tile.y + tile.height;
                    // if we hit the ground, mark us as grounded so we can jump
                    koala.grounded = true;
                }
                koala.velocity.y = 0;
                break;
            }
        }
        rectPool.free(koalaRect);

        // unscale the velocity by the inverse delta time and set
        // the latest position
        koala.position.add(koala.velocity);
        koala.velocity.scl(1 / deltaTime);

        // Apply damping to the velocity on the x-axis so we don't
        // walk infinitely once a key was pressed
        koala.velocity.x *= Koala.DAMPING;

    }

    private boolean isTouched (float startX, float endX) {
        // check if any finge is touch the area between startX and endX
        // startX/endX are given between 0 (left edge of the screen) and 1 (right edge of the screen)
        for (int i = 0; i < 2; i++) {
            float x = Gdx.input.getX() / (float)Gdx.graphics.getWidth();
            if (Gdx.input.isTouched(i) && (x >= startX && x <= endX)) {
                return true;
            }
        }
        return false;
    }

    private void getTiles (int startX, int startY, int endX, int endY, Array<Rectangle> tiles) {
        TiledMapTileLayer layer = (TiledMapTileLayer)map.getLayers().get(0);
        rectPool.freeAll(tiles);
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

    private void renderKoala (float deltaTime) {
        // based on the koala state, get the animation frame
        TextureRegion frame = null;
        switch (koala.state) {
            case Standing:
                frame = stand.getKeyFrame(koala.stateTime);
                break;
            case Walking:
                frame = walk.getKeyFrame(koala.stateTime);
                break;
            case Jumping:
                frame = jump.getKeyFrame(koala.stateTime);
                break;
        }

        // draw the koala, depending on the current velocity
        // on the x-axis, draw the koala facing either right
        // or left
        Batch batch = renderer.getSpriteBatch();
        batch.begin();
        if (koala.facesRight) {
            batch.draw(frame, koala.position.x, koala.position.y, Koala.WIDTH, Koala.HEIGHT);
        } else {
            batch.draw(frame, koala.position.x + Koala.WIDTH, koala.position.y, -Koala.WIDTH, Koala.HEIGHT);
        }
        batch.end();
    }

    @Override
    public void dispose () {
    }
}
