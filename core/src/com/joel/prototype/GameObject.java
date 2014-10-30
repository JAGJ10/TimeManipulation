package com.joel.prototype;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;

import java.util.ArrayList;

public abstract class GameObject extends Sprite {
    protected double deltaTime;
    protected ArrayList<FrameState> frameStates;
    protected int frameCounter;

    public GameObject(Texture texture) {
        super(texture);
        deltaTime = 0;
        frameStates = new ArrayList<FrameState>();
        frameCounter = 0;
    }

    public void replayFrame(int frame) {
        if (frame < frameStates.size()) {
            FrameState fs = frameStates.get(frame);
            //check if fr exists?

            this.setX(fs.x);
            this.setY(fs.y);

            /*
            if (fs.keys) {
                for (Key key : fs.keys) {
                    Keys.setKey(key, fs.keys[parseInt(key)]);
                }
             }
             */

            //restore animation keyframe here if i want
        }
    }

    public void recalcStates(int frame) {}

    public void resetFrameStates() {
        frameCounter = 0;
        frameStates.clear();
    }

    public void saveState(FrameState fs) {
        frameStates.set(frameCounter, fs);
        frameCounter++;
    }
}
