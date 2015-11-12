package com.hao.tetris;
	
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
	
	/*
	 * 俄罗斯方块
	 */
public class Tetris extends JPanel {
	/** 游戏的当前状态: RUNNING PAUSE GAME_OVER */
	private int state;
	public static final int RUNNING = 0;
	public static final int PAUSE = 1;
	public static final int GAME_OVER = 2;
	private int score;// 分数
	private int lines;// 销毁的行数
	private Cell[][] wall;// 背景墙
	private Tetromino tetromino;// 正在下落的四格方块
	private Tetromino nextOne;// 下一个四格方块
	/** 背景图片 */
	private static BufferedImage background;
	private static BufferedImage gameOver;
	private static BufferedImage pause;
	public static BufferedImage T;
	public static BufferedImage S;
	public static BufferedImage I;
	public static BufferedImage L;
	public static BufferedImage J;
	public static BufferedImage O;
	public static BufferedImage Z;
	public static final int ROWS = 20;// 背景墙的行数
	public static final int COLS = 10;// 背景墙的列数
	/** 在 Tetris 类中增加定时器 */
	private Timer timer;
	/** 速度 */
	private int speed;
	/** 难度级别 */
	private int level;
	/** 下落计数器 当 index%speed==0 时候下落一次 */
	private int index;
	// 将图片素材, 复制到 com.tarena.tetris 包中.
	/** 使用静态代码块加载静态的图片 */
	static {
		try {
			// Tetris.class 的同一个包中找 "tetris.png"
			background = ImageIO.read(Tetris.class.getResource("tetris.png"));
			gameOver =ImageIO.read(Tetris.class.getResource("game-over.png"));
			pause = ImageIO.read(Tetris.class.getResource("pause.png"));
			T = ImageIO.read(Tetris.class.getResource("T.png"));
			I = ImageIO.read(Tetris.class.getResource("I.png"));
			S = ImageIO.read(Tetris.class.getResource("S.png"));
			Z = ImageIO.read(Tetris.class.getResource("Z.png"));
			J = ImageIO.read(Tetris.class.getResource("J.png"));
			L = ImageIO.read(Tetris.class.getResource("L.png"));
			O = ImageIO.read(Tetris.class.getResource("O.png"));
			} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	* JPanel paint() paint 画 重写 paint() 修改原有的绘制方法
	**/
	@Override
	public void paint(Graphics g) {
		// 画背景, 画墙, 画正在下落的方块 画下一个方块...
		g.drawImage(background, 0, 0, null);
		g.translate(15, 15);// 坐标系平移
		paintWall(g);// 画墙
		paintTetromino(g);// 绘制正在下落的方块
		paintNextOne(g);//绘制下一个要下落的方块
		paintScore(g);//绘制分数
		paintState(g);// 绘制游戏的状态
	}
	private void paintState(Graphics g) {
		switch (state) {
		case PAUSE:
		g.drawImage(pause, -15, -15, null);
		break;
		case GAME_OVER:
		g.drawImage(gameOver, -15, -15, null);
		break;
		}
	}
	/** 在 Tetris 添加启动方法 action() */
	public void action() {
		wall = new Cell[ROWS][COLS];
		//wall[2][2] = new Cell(2, 2, T);
		tetromino = Tetromino.randomOne();
		nextOne = Tetromino.randomOne();
		state = RUNNING;
		// 处理键盘按下事件, 在按下按键时候执行下落方法
		KeyAdapter l = new KeyAdapter() {
			@Override
			// key 按键 Pressed 按下了
			public void keyPressed(KeyEvent e) {
				int key = e.getKeyCode();
				switch (state) {
				case GAME_OVER:
				processGameoverKey(key);
				break;
				case PAUSE:
				processPauseKey(key);
				break;
				case RUNNING:
				processRunningKey(key);
				}
				repaint();
				}
	};
		// 下落流程: 监听键盘事件->如果下箭头按下->
		// 执行下落算法 tetromino.softDrop() ->
		// 修改每个格子对象的数据->调用 repaint() ->
		// 尽快调用 paint() ->paint 方法会根据当前的数据
		// 重新绘制界面 -> 看到移动以后的方块了
		// 绑定事件到当前面板
		this.requestFocus();
		this.addKeyListener(l);
		// 在 Action 方法中添加, 定时计划任务
		timer = new Timer();
		timer.schedule(new TimerTask() {
			public void run() {
			speed = 40 - (lines / 100);
			speed = speed < 1 ? 1 : speed;
			level = 41 - speed;
			if (state == RUNNING && index % speed == 0) {
			softDropAction();
			}
			index++;
			repaint();
			}
		}, 10, 10);
		}
	private void processPauseKey(int key) {
		switch (key) {
		case KeyEvent.VK_Q:
		System.exit(0);
		break;
		case KeyEvent.VK_C:
		index = 0;
		state = RUNNING;
		break;
		}
	}
	protected void processRunningKey(int key) {
		switch (key) {
		case KeyEvent.VK_Q:
		System.exit(0);
		break;
		case KeyEvent.VK_RIGHT:
		Tetris.this.moveRightAction();
		break;
		case KeyEvent.VK_LEFT:
		Tetris.this.moveLeftAction();
		break;
		case KeyEvent.VK_DOWN:
		softDropAction();
		break;
		case KeyEvent.VK_SPACE:
		hardDropAction();
		break;
		case KeyEvent.VK_UP:
		rotateRightAction();
		break;
		case KeyEvent.VK_P:
		state = PAUSE;
		break;
		}
	}
	protected void processGameoverKey(int key) {
		switch (key) {
		case KeyEvent.VK_Q:
		System.exit(0);
		break;
		case KeyEvent.VK_S:
		/** 游戏重新开始 */
		this.lines = 0;
		this.score = 0;
		this.wall = new Cell[ROWS][COLS];
		this.tetromino = Tetromino.randomOne();
		this.nextOne = Tetromino.randomOne();
		this.state = RUNNING;
		this.index = 0;
		break;
		}
	}
	public static final int FONT_COLOR = 0x667799;
	public static final int FONT_SIZE = 30;
	 /**
	 * 绘制分数
	 * @param g
	 */
	private void paintScore(Graphics g) {
		int x = 290;
		int y = 160;
		g.setColor(new Color(FONT_COLOR));
		Font font = g.getFont();// 取得 g 当前字体
		font = new Font(font.getName(), font.getStyle(), FONT_SIZE);
		g.setFont(font);// 更改了 g 的字体
		String str = "SCORE:" + score;
		g.drawString(str, x, y);
		y += 56;
		str = "LINES:" + lines;
		g.drawString(str, x, y);
		y += 56;
		g.drawString("LEVEL:" + level, x, y);
	}
	/**
	* 绘制下一个要下落的方块
	*
	* @param g
	*/
	private void paintNextOne(Graphics g) {
		if (nextOne == null) {
		return;
		}
		// 将每个格子的 row,col 换算为 x,y 然后贴图
		Cell[] cells = nextOne.cells;
		for (int i = 0; i < cells.length; i++) {
		// i = 0 1 2 3
		Cell cell = cells[i];
		// cell 每个格子
		int x = (cell.getCol() + 10) * CELL_SIZE;
		int y = (cell.getRow() + 1) * CELL_SIZE;
		g.drawImage(cell.getImage(), x - 1, y - 1, null);
		}
	}
	/**
	* 绘制正在下落的方块
	*
	* @param g
	*/
	public void paintTetromino(Graphics g) {
		if (tetromino == null) {
		return;
		}
		// 将每个格子的 row,col 换算为 x,y 然后贴图
		Cell[] cells = tetromino.cells;
		for (int i = 0; i < cells.length; i++) {
		// i = 0 1 2 3
		Cell cell = cells[i];
		// cell 每个格子
		int x = cell.getCol() * CELL_SIZE;
		int y = cell.getRow() * CELL_SIZE;
		g.drawImage(cell.getImage(), x - 1, y - 1, null);
		}
	}
	public static final int CELL_SIZE = 26;
	/** 画墙 */
	private void paintWall(Graphics g) {
		for (int row = 0; row < wall.length; row++) {
		Cell[] line = wall[row];
		// line 代表墙上的每一行
		for (int col = 0; col < line.length; col++) {
			Cell cell = line[col];
			// cell 代表墙上的每个格子
			int x = col * CELL_SIZE;
			int y = row * CELL_SIZE;
			if (cell == null) {
			g.drawRect(x, y, CELL_SIZE, CELL_SIZE);
		} else {
			g.drawImage(cell.getImage(), x - 1, y - 1, null);
		}
		// g.drawString(row+","+col, x,y+CELL_SIZE);
		}
		}
	}
	/** 检查当前正在下落的方块是否出界了 */
	private boolean outOfBounds() {
	Cell[] cells = tetromino.cells;
	for (int i = 0; i < cells.length; i++) {
	Cell cell = cells[i];
	int col = cell.getCol();
	if (col < 0 || col >= COLS) {
	return true;
	}
	}
	return false;
	}
	/** 检查正在下落的方块是否与墙上的砖块重叠 */
	private boolean coincide() {
		Cell[] cells = tetromino.cells;
		for (int i = 0; i < cells.length; i++) {
		Cell cell = cells[i];
		int row = cell.getRow();
		int col = cell.getCol();
		// 如果墙的 row,col 位置上有格子, 就重叠了!
	if (row >= 0 && row < ROWS && col >= 0 && col <= COLS
		&& wall[row][col] != null) {
		return true;// 重叠
		}
	}
	return false;
	}
	/** 在 Tetris 类上添加方法, 向右移动的流程控制 */
	public void moveRightAction() {
		// 尝试先向右移动, 如果发现超出了边界, 就
		// 向左移动, 修正回来.
		tetromino.moveRight();// coincide 重叠
		if (outOfBounds() || coincide()) {
		tetromino.moveLeft();
		}
	}
	public void moveLeftAction() {
		tetromino.moveLeft();
		if (outOfBounds() || coincide()) {
		tetromino.moveRight();
		}
	}
	/** 下落流程控制 */
	public void softDropAction() {
		if (canDrop()) {
		tetromino.softDrop();
		} else {
		landIntoWall();
		destoryLines();
		if (isGameOver()) {
		state = GAME_OVER;
		} else {
		tetromino = nextOne;
		nextOne = Tetromino.randomOne();
		}
		}
	}
	private static int[] scoreTable = { 0, 1, 10, 50, 100 };
	// 0 1 2 3 4
	private void destoryLines() {
		int lines = 0;
		for (int row = 0; row < wall.length; row++) {
		if (fullCells(row)) {
		deleteRow(row);
		lines++;
		}
		}
		this.score += scoreTable[lines];
		this.lines += lines;
	}
	private void deleteRow(int row) {
		for (int i = row; i >= 1; i--) {
		System.arraycopy(wall[i - 1], 0, wall[i], 0, COLS);
		}
		Arrays.fill(wall[0], null);
	}
	/**
	* 检查当前行的每个格子, 是否是满的，如果满了则返回 true，否则返回 false
	*/
	private boolean fullCells(int row) {
		Cell[] line = wall[row];
		for (Cell cell : line) {
		if (cell == null) {// 如果有 null 返回 false 否则返回 true
		return false;
		}
		}
		return true;
	}
	private void landIntoWall() {
		Cell[] cells = tetromino.cells;
		for (int i = 0; i < cells.length; i++) {
		Cell cell = cells[i];
		int row = cell.getRow();
		int col = cell.getCol();
		wall[row][col] = cell;
		}
	}
	/** 检查当前的方块是否能够下落, 返回 true 能够下落 */
	private boolean canDrop() {
		Cell[] cells = tetromino.cells;
		for (int i = 0; i < cells.length; i++) {
			Cell cell = cells[i];
			int row = cell.getRow();
			if (row == ROWS - 1) {
			return false;
		}
		}
		for (Cell cell : cells) {// Java 5 以后可以使用
			int row = cell.getRow() + 1;
			int col = cell.getCol();
			if (row >= 0 && row < ROWS && col >= 0 && col <= COLS
			&& wall[row][col] != null) {
			return false;
			}
		}
		return true;
	}
	/**
	* 硬下落流程, 下落到不能下落为止 绑定到 空格(VK_SPACE) 事件上
	* */
	public void hardDropAction() {
		while (canDrop()) {
			tetromino.softDrop();
		}
			landIntoWall();
			destoryLines();
		if (isGameOver()) {
			state = GAME_OVER;
		} else {
			tetromino = nextOne;
			nextOne = Tetromino.randomOne();
		}
	}
	/** 在 Tetris 类中添加 旋转流程控制方法 */
	public void rotateRightAction() {
		tetromino.rotateRight();
		if (outOfBounds() || coincide()) {
		tetromino.rotateLeft();
		}
	}
	/** 检查游戏是否结束 */
		private boolean isGameOver() {
		// 如果下一个方块没有出场位置了, 则游戏结束
		// 就是: 下一个出场方块每个格子行列对应的
		// 墙上位置如果有格子, 就游戏结束
		Cell[] cells = nextOne.cells;
		for (Cell cell : cells) {
			int row = cell.getRow();
			int col = cell.getCol();
		if (wall[row][col] != null) {
			return true;
		}
		}
		return false;
	}
	public static void main(String[] args) {
		JFrame frame = new JFrame();
		// 在加载 Tetris 类的时候, 会执行静态代码块
		// 静态代码块, 装载了图片素材, 为图片对象
		Tetris tetris = new Tetris();
		// 将面板的颜色设置为蓝色，用于测试
		tetris.setBackground(new Color(0x0000ff));
		frame.add(tetris);
		frame.setSize(530, 580);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);// 在显示窗口时候, 会"尽快"的调用 paint() 方法绘制界面
		tetris.action();
	}
}