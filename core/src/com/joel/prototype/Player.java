package com.joel.prototype;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;

public class Player extends GameObject {
    public static final float MAX_VELOCITY = 400f;
    public static float JUMP_VELOCITY = 1000f;
    public static final float GRAVITY = -50f;

    private boolean grounded;
    private boolean moveLeft;
    private boolean moveRight;
    private Vector2 velocity;

    public Player(Texture texture) {
        super(texture);
        this.grounded = true;
        this.moveLeft = false;
        this.moveRight = false;
        velocity = new Vector2();
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

    public void setMoveLeft(boolean moveLeft) {
        this.moveLeft = moveLeft;
    }

    public void setMoveRight(boolean moveRight) {
        this.moveRight = moveRight;
    }

    public void update(float deltaTime) {
        this.getVelocity().y += (Player.GRAVITY);

        if (velocity.y > Player.JUMP_VELOCITY)
            velocity.y = Player.JUMP_VELOCITY;
        else if (velocity.y < -Player.JUMP_VELOCITY)
            velocity.y = -Player.JUMP_VELOCITY;

        // clamp the velocity to the maximum, x-axis only
        if (Math.abs(this.getVelocity().x) > Player.MAX_VELOCITY) {
            this.getVelocity().x = Math.signum(this.getVelocity().x) * Player.MAX_VELOCITY;
        }

        // clamp the velocity to 0 if it's < 1
        if (Math.abs(this.getVelocity().x) < 1) {
            this.getVelocity().x = 0;
        }

        if (moveLeft) {
            velocity.x = -MAX_VELOCITY;
        } else if (moveRight) {
            velocity.x = MAX_VELOCITY;
        } else {
            velocity.x = 0;
        }
    }

    @Override
    public void saveState(FrameState fs) {
        fs.keycode = this.keycode;
        fs.yVelocity = this.velocity.y;
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
        if (frame < frameStates.size() && frame > 0) {
            this.velocity.y = frameStates.get(frame - 1).yVelocity; //get framestates yVelocity somehow
            super.replayFrame(frame);
        }
    }

    @Override
    public void recalcStates(int frame) {
       /* FrameState fs;
        this.frameStates.get(frame).yVelocity = this.velocity.y;
        frame++;
        float prevX = this.getX();
        float prevY = this.getY();
        System.out.println("prevY: " + prevY);
        for (int i = frame; i < this.frameStates.size(); i++) {
            this.replayFrame(i);
            System.out.println("new y: " + this.getY());
            fs = this.frameStates.get(i);
            fs.x = this.getX();
            fs.y = this.getY();
            fs.yVelocity = this.velocity.y;
        }

        this.setX(prevX);
        this.setY(prevY);*/

        FrameState fs = frameStates.get(0);
        int prevKeycode;
        //w keycode is 51
        if (fs.keycode == 51) {
            //if (player.isGrounded()) {
            fs.yVelocity = Player.JUMP_VELOCITY;
                //player.setGrounded(false);
            //}
            fs.y += fs.yVelocity * fs.deltaTime;
        }

        boolean justChanged = false;
        for (int i = 1; i < frameStates.size(); i++) {
            FrameState cur = frameStates.get(i);
            prevKeycode = frameStates.get(i-1).keycode;
            if (cur.keycode != prevKeycode && cur.keycode == 51) {
                cur.yVelocity = Player.JUMP_VELOCITY;
                justChanged = true;
            }

            if (!justChanged) {
                cur.yVelocity = frameStates.get(i - 1).yVelocity + (Player.GRAVITY);
                cur.y = frameStates.get(i - 1).y + cur.yVelocity * cur.deltaTime; //y can be negative because no collision detection
                if (cur.y < 32) cur.y = 32;
                System.out.println("Velocity: " + cur.yVelocity);
                System.out.println("y: " + cur.y);

            }
            justChanged = false;
        }
    }
}