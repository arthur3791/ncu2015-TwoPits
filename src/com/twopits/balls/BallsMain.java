package com.twopits.balls;

import com.twopits.balls.libs.Utils;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

/**
 * Main class of game "Balls"
 * Created by hiking on 2015/10/26.
 */
public class BallsMain extends JPanel {

	// App configurations
	private static final String APP_NAME = "Balls";
	private static final int PLAYER_SPEED = 300;
	private static final float FPS_CAP = 60f;
	private static final float MAX_VISIBLE_BLOCKS_IN_HEIGHT = 3.0f;
	private static final float MAX_VISIBLE_BLOCKS_IN_WIDTH = 5.0f;

	private static BallsKeyManager mKeyManager;
	private static Font mGameFont;

	public static void main(String[] args) {
		initValues();
		startRenderThread();
	}

	private static void startRenderThread() {
		JFrame frame = new JFrame(APP_NAME);
		frame.getContentPane().setPreferredSize(new Dimension(500, 300));
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.pack();
		Utils.setWindowsToCenter(frame);
		Utils.enableOsxFullscreen(frame, APP_NAME);

		BallsMain game = new BallsMain();
		frame.add(game);
		frame.setVisible(true);
		mKeyManager = new BallsKeyManager();
		frame.addKeyListener(mKeyManager);

		long lastFrameTime = System.currentTimeMillis();
		// noinspection InfiniteLoopStatement
		while (true) {
			long currentTime = System.currentTimeMillis();
			long dt = currentTime - lastFrameTime;
			lastFrameTime = currentTime;
			if (dt > 0) {
				game.update(dt);
			}
		}
	}

	private static void initValues() {
		try {
			mGameFont = Font.createFont(Font.TRUETYPE_FONT, new File("res/supercell_magic.ttf"));
		} catch (Exception e) {
			e.printStackTrace();
			mGameFont = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
		}
	}

	public Position getVirtualCharacterXY() {
		return new Position(mPlayerX, mPlayerY);
	}

	static class BallsKeyManager implements KeyListener {

		private List<Integer> mPressedKeys;

		public BallsKeyManager() {
			mPressedKeys = new ArrayList<Integer>();
		}

		public boolean isKeyPressed(int keyCode) {
			return mPressedKeys.contains(keyCode);
		}

		@Override
		public void keyTyped(KeyEvent e) {
		}

		@Override
		public void keyPressed(KeyEvent e) {
			if (!mPressedKeys.contains(e.getKeyCode())) {
				mPressedKeys.add(e.getKeyCode());
			}
		}

		@Override
		public void keyReleased(KeyEvent e) {
			mPressedKeys.remove((Integer) e.getKeyCode());
		}
	}

	private static final int MAP_WIDTH = 50, MAP_HEIGHT = 20;
	private double mPlayerX = 2500, mPlayerY = 1000;

	private static final int BLOCK_SIZE = 100;

	private enum BasicBlock {
		GRASS, ROCK, DIRT, WATER, LAVA;

	}

	private static Image[] mBlockImages;

	private static BasicBlock[][] mBlocks;

	public BallsMain() {
		loadMap(getRandomMap());
		mBlockImages = initBlockResources();
	}

	public void loadMap(BasicBlock[][] map) {
		mBlocks = map;
	}

	private BasicBlock[][] getRandomMap() {
		BasicBlock[][] scene = new BasicBlock[MAP_WIDTH][];
		for (int row = 0; row < MAP_WIDTH; row++) {
			scene[row] = new BasicBlock[MAP_HEIGHT];
			for (int col = 0; col < MAP_HEIGHT; col++) {
				scene[row][col] =
						BasicBlock.values()[((int) (Math.random() * BasicBlock.values().length))];
			}
		}
		return scene;
	}

	private Image[] initBlockResources() {
		Image[] images = new Image[BasicBlock.values().length];
		images[BasicBlock.GRASS.ordinal()] = Toolkit.getDefaultToolkit().getImage("res/grass.png");
		images[BasicBlock.ROCK.ordinal()] = Toolkit.getDefaultToolkit().getImage("res/rock.png");
		images[BasicBlock.DIRT.ordinal()] = Toolkit.getDefaultToolkit().getImage("res/dirt.png");
		images[BasicBlock.WATER.ordinal()] = Toolkit.getDefaultToolkit().getImage("res/water.png");
		images[BasicBlock.LAVA.ordinal()] = Toolkit.getDefaultToolkit().getImage("res/lava.png");
		return images;
	}

	private float getZoomFactor() {
		if (getWidth() / (float) getHeight() >
				MAX_VISIBLE_BLOCKS_IN_WIDTH / MAX_VISIBLE_BLOCKS_IN_HEIGHT) {
			return getWidth() / (MAX_VISIBLE_BLOCKS_IN_WIDTH * BLOCK_SIZE);
		} else {
			return getHeight() / (MAX_VISIBLE_BLOCKS_IN_HEIGHT * BLOCK_SIZE);
		}
	}

	// For the update cycle
	private float mSmoothedDt = 1000 / FPS_CAP;
	private long mSleepDuration;

	private void update(long dt) {
		updatePlayerPosition(dt);
		repaint();

		mSmoothedDt = (mSmoothedDt * 9f + dt - mSleepDuration) / 10f;
		mSleepDuration = (long) (1000 / FPS_CAP - mSmoothedDt);
		if (mSleepDuration > 0l) {
			// Cap the screen rate
			try {
				Thread.sleep(mSleepDuration);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} else {
			mSleepDuration = 0;
		}
	}

	private void updatePlayerPosition(long dt) {
		double playerWayX = 0, playerWayY = 0;

		if (mKeyManager.isKeyPressed(KeyEvent.VK_LEFT)) {
			playerWayX -= 1.0;
		}
		if (mKeyManager.isKeyPressed(KeyEvent.VK_RIGHT)) {
			playerWayX += 1.0;
		}
		if (mKeyManager.isKeyPressed(KeyEvent.VK_UP)) {
			playerWayY -= 1.0;
		}
		if (mKeyManager.isKeyPressed(KeyEvent.VK_DOWN)) {
			playerWayY += 1.0;
		}

		double unitFactor = Math.sqrt(playerWayX * playerWayX + playerWayY * playerWayY);
		if (unitFactor != 0) {
			playerWayX /= unitFactor;
			playerWayY /= unitFactor;
		}

		mPlayerX += playerWayX * PLAYER_SPEED * dt / 1000.0;
		mPlayerY += playerWayY * PLAYER_SPEED * dt / 1000.0;

		if (playerWayX != 0 || playerWayY != 0) {
			if (mPlayerX < 0 || mPlayerX > MAP_WIDTH * BLOCK_SIZE) {
				mPlayerX = Utils.floorMod(mPlayerX, MAP_WIDTH * BLOCK_SIZE);
			}
			if (mPlayerY < 0 || mPlayerY > MAP_HEIGHT * BLOCK_SIZE) {
				mPlayerY = Utils.floorMod(mPlayerY, MAP_HEIGHT * BLOCK_SIZE);
			}
		}
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);

		renderScene((Graphics2D) g);
	}

	@SuppressWarnings("SuspiciousNameCombination")
	private void renderScene(Graphics2D g2d) {
		Position playerPosition = getVirtualCharacterXY();
		float zoom = getZoomFactor();

		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setFont(mGameFont.deriveFont(10 * zoom));

		// Map
		int screenPositionX = (int) (playerPosition.x - this.getWidth() / zoom / 2.0);
		int screenPositionY = (int) (playerPosition.y - this.getHeight() / zoom / 2.0);
		int screenOffsetX = Math.floorMod(screenPositionX, BLOCK_SIZE);
		int screenOffsetY = Math.floorMod(screenPositionY, BLOCK_SIZE);
		int visibleBlockX = Math.floorDiv(screenPositionX, BLOCK_SIZE);
		int visibleBlockY = Math.floorDiv(screenPositionY, BLOCK_SIZE);
		int visibleBlockW = (int) (this.getWidth() / zoom) / BLOCK_SIZE + 2;
		int visibleBlockH = (int) (this.getHeight() / zoom) / BLOCK_SIZE + 2;

		g2d.setStroke(new BasicStroke(2 * zoom));
		for (int row = 0; row < visibleBlockW; row++) {
			for (int col = 0; col < visibleBlockH; col++) {
				int drawingBlockX = Math.floorMod(row + visibleBlockX, MAP_WIDTH);
				int drawingBlockY = Math.floorMod(col + visibleBlockY, MAP_HEIGHT);
				BasicBlock block = mBlocks[drawingBlockX][drawingBlockY];

				int drawPositionX = (int) ((row * BLOCK_SIZE - screenOffsetX) * zoom);
				int drawPositionY = (int) ((col * BLOCK_SIZE - screenOffsetY) * zoom);
				int drawBlockSize = (int) Math.ceil(BLOCK_SIZE * zoom);

				g2d.drawImage(mBlockImages[block.ordinal()], drawPositionX, drawPositionY,
						drawBlockSize, drawBlockSize, null);
				g2d.setColor(Color.BLACK);
				g2d.drawRect(drawPositionX, drawPositionY, drawBlockSize, drawBlockSize);
				g2d.drawString(String.format("(%d,%d)", drawingBlockX, drawingBlockY),
						drawPositionX + (10 * zoom), drawPositionY + (20 * zoom));
			}
		}

		// TODO Draw players
		int fakePlayerRadius = (int) (4 * zoom);
		g2d.setColor(Color.LIGHT_GRAY);
		g2d.fillOval(this.getWidth() / 2 - fakePlayerRadius,
				this.getHeight() / 2 - fakePlayerRadius, 2 * fakePlayerRadius,
				2 * fakePlayerRadius);
		g2d.setColor(Color.BLACK);
		g2d.drawOval(this.getWidth() / 2 - fakePlayerRadius,
				this.getHeight() / 2 - fakePlayerRadius, 2 * fakePlayerRadius,
				2 * fakePlayerRadius);

		boolean DEBUG = true;
		boolean DEBUG_PLAYER = true;
		// noinspection ConstantConditions
		if (DEBUG) {
			// Debug messages
			int drawTextPositionX = (int) (10 * zoom);
			int drawTextPositionY = this.getHeight() - (int) (10 * zoom);
			int lineHeight = (int) (20 * zoom);

			g2d.setColor(Color.WHITE);
			g2d.drawString(String.format("FPS: %.0f (%d)", 1000 / (mSmoothedDt + mSleepDuration),
					mSleepDuration), drawTextPositionX, drawTextPositionY);

			// noinspection ConstantConditions
			if (DEBUG_PLAYER) {
				drawTextPositionY -= lineHeight;
				g2d.drawString(
						String.format("Player offset: (%d,%d)", (int) playerPosition.x % BLOCK_SIZE,
								(int) playerPosition.y % BLOCK_SIZE), drawTextPositionX,
						drawTextPositionY);

				drawTextPositionY -= lineHeight;
				g2d.drawString(String.format("Player block: (%d,%d)",
								(int) playerPosition.x / BLOCK_SIZE % MAP_WIDTH,
								(int) playerPosition.y / BLOCK_SIZE % MAP_HEIGHT),
						drawTextPositionX, drawTextPositionY);
			} else {
				drawTextPositionY -= lineHeight;
				g2d.drawString(
						String.format("Screen offset: (%d,%d)", screenOffsetX, screenOffsetY),
						drawTextPositionX, drawTextPositionY);

				drawTextPositionY -= lineHeight;
				g2d.drawString(String.format("Screen block: (%d,%d)", visibleBlockX, visibleBlockY),
						drawTextPositionX, drawTextPositionY);
			}

			drawTextPositionY -= lineHeight;
			g2d.drawString(String.format("Player position: (%d,%d)", (int) playerPosition.x,
					(int) playerPosition.y), drawTextPositionX, drawTextPositionY);
		}
	}

	class Position {

		public double x, y;

		public Position(double x, double y) {
			this.x = x;
			this.y = y;
		}
	}
}
