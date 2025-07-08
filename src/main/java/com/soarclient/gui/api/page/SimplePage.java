package com.soarclient.gui.api.page;

import com.soarclient.animation.Animation;
import com.soarclient.animation.other.DummyAnimation;
import com.soarclient.gui.api.SoarGui;

public abstract class SimplePage {

	protected float x, y, width, height;
	private final String title;
    private final String icon;
	protected SoarGui parent;
	private Animation animation;
	private GuiTransition transition;

	public SimplePage(SoarGui parent, String title, String icon, GuiTransition transition) {
		this.parent = parent;
		this.title = title;
		this.icon = icon;
		this.x = 0;
		this.y = 0;
		this.width = 0;
		this.height = 0;
		this.transition = transition;
		this.animation = new DummyAnimation(1);
	}

	public void init() {
	}

	public void draw(double mouseX, double mouseY) {
	}

	public void mousePressed(double mouseX, double mouseY, int button) {
	}

	public void mouseReleased(double mouseX, double mouseY, int button) {
	}

	public void mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
	}

	public void charTyped(char chr, int modifiers) {
	}

	public void keyPressed(int keyCode, int scanCode, int modifiers) {
	}

	public void onClosed() {
	}

	public float getX() {
		return x;
	}

	public void setX(float x) {
		this.x = x;
	}

	public float getY() {
		return y;
	}

	public void setY(float y) {
		this.y = y;
	}

	public float getWidth() {
		return width;
	}

	public void setWidth(float width) {
		this.width = width;
	}

	public float getHeight() {
		return height;
	}

	public void setHeight(float height) {
		this.height = height;
	}

	public String getTitle() {
		return title;
	}

	public String getIcon() {
		return icon;
	}

	public GuiTransition getTransition() {
		return transition;
	}

	public void setTransition(GuiTransition transition) {
		this.transition = transition;
	}

	public Animation getAnimation() {
		return animation;
	}

	public void setAnimation(Animation animation) {
		this.animation = animation;
	}

	public SoarGui getParent() {
		return parent;
	}
}
