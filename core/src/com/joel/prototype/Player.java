package com.joel.prototype;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;

public class Player extends GameObject implements InputProcessor {
    public static final float MAX_VELOCITY = 10f;
    public static final float JUMP_VELOCITY = 40f;
    public static final float DAMPING = 0.87f;
    public static final float GRAVITY = -2.5f;

    private boolean grounded;
    private Vector2 velocity;
    private Vector2 position;

    public Player(Texture texture) {
        super(texture);
        this.grounded = false;
        velocity = new Vector2();
        position = new Vector2();
    }

    public boolean isGrounded() {
        return grounded;
    }

    public void setGrounded(boolean grounded) {
        this.grounded = grounded;
    }

    public Vector2 getVelocity() {
        return velocity;
    }

    public void setVelocity(Vector2 velocity) {
        this.velocity = velocity;
    }

    public Vector2 getPosition() {
        return position;
    }

    public void setPosition(Vector2 position) {
        this.position = position;
    }

    public void update(float delta) {
        position.add(velocity.cpy().scl(delta));
    }

    public void timeStep(float deltaTime) {
        float oldX = this.getX();

        /*if (Keys.isDown(Keys.LEFT)) {
            float newX = this.getX() - 0.3 * deltaTime;
            this.setX(newX);
        } else if (Keys.isDown(Keys.RIGHT)) {
            float newX = this.getX(); + .3 * deltaTime;
            this.setX(newX);
        }
        */

        //this.yVelocity += (0.05 * deltaTime);
        this.setY(this.getY() + velocity.y);

        //set jumping to true or false

        //this.running = (this.getX() != oldX) && !this.jumping;
    }

    @Override
    public void saveState(FrameState fs) {
        //var keys = {};
        //keys[Keys.LEFT] = Keys.isDown(Keys.LEFT);
        //keys[Keys.RIGHT] = Keys.isDown(Keys.RIGHT);
        //keys[Keys.UP] = Keys.isDown(Keys.UP);

        //fs.keys = keys;
        //fs.yVelocity = this.yVelocity; //set yVelocity somehow from framestate

        super.saveState(fs);
    }

    @Override
    public void replayFrame(int frame) {
        if (frame < frameStates.size()) {
            //this.yVelocity = frameStates.get(frame - 1).yVelocity; //get framestates yVelocity somehow
            super.replayFrame(frame);
        }
    }

    @Override
    public boolean keyDown(int keycode) {
        switch (keycode) {
            case Keys.W:
                if (grounded) {
                    velocity.y += JUMP_VELOCITY;
                    grounded = false;
                }
                break;
            case Keys.A:
                velocity.x = -MAX_VELOCITY;
                break;
            case Keys.D:
                velocity.x = MAX_VELOCITY;
                break;
        }
        return true;
    }

    @Override
    public boolean keyUp(int keycode) {
        switch (keycode) {
            case Keys.A:
            case Keys.D:
                velocity.x = 0;
                break;
        }
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
}