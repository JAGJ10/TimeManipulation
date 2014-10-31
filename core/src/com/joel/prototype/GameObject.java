package com.joel.prototype;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;

import java.util.ArrayList;

public abstract class GameObject extends Sprite {
    //protected double deltaTime;
    protected ArrayList<FrameState> frameStates;
    protected int frameCounter;
    protected int keycode;

    public GameObject(Texture texture) {
        super(texture);
        //deltaTime = 0;
        frameStates = new ArrayList<FrameState>();
        frameCounter = 0;
    }

    public int getKeycode() {
        return keycode;
    }

    public void setKeycode(int keycode) {
        this.keycode = keycode;
    }

    public ArrayList<FrameState> getFrameStates() {
        return frameStates;
    }

    public void setFrameStates(ArrayList<FrameState> frameStates) {
        this.frameStates = frameStates;
    }

    public int getFrameCounter() {
        return frameCounter;
    }

    public void setFrameCounter(int frameCounter) {
        this.frameCounter = frameCounter;
    }

    public void replayFrame(int frame) {
        if (frame < frameStates.size() && frame > 0) {
            FrameState fs;
            fs = frameStates.get(frame);

            this.setX(fs.x);
            this.setY(fs.y);

            if (fs.keycode > 0) {
                keycode = fs.keycode;
                /*for (Key key : fs.keys) {
                    Keys.setKey(key, fs.keys[parseInt(key)]);
                }*/
             }

            //restore animation keyframe here if i want
        }
    }

    public void recalcStates(int frame) {}

    public void resetFrameStates() {
        frameCounter = 0;
        frameStates.clear();
    }

    public void saveStateAt(float deltaTime) {
        this.saveState(new FrameState(this, deltaTime, keycode));
    }

    public void saveState(FrameState fs) {
        frameStates.add(frameCounter, fs);
        frameCounter++;
    }
}
