package com.joel.prototype;

public class FrameState {
    public float x;
    public float y;
    public int keycode;
    public float deltaTime;
    public float yVelocity;

    //need to add keys
    public FrameState(GameObject obj, float deltaTime, int keycode) {
        this.x = obj.getX();
        this.y = obj.getY();
        this.deltaTime = deltaTime;
        this.keycode = keycode;
    }

    public FrameState(Player player, float deltaTime, int keycode) {
        this.x = player.getX();
        this.y = player.getY();
        this.deltaTime = deltaTime;
        this.keycode = keycode;
        this.yVelocity = player.getVelocity().y;
    }
}
