package com.joel.prototype;

public class FrameState {
    public float x;
    public float y;
    //keys
    private float deltaTime;

    //need to add keys
    public FrameState(GameObject obj, float deltaTime) {
        this.x = obj.getX();
        this.y = obj.getY();
        this.deltaTime = deltaTime;
        //this.keys = keys;
    }

}
