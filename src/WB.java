import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JFrame;

public class WB extends JFrame implements Runnable, KeyListener {
	private BufferedImage bi = null;
	private ArrayList pMsList = null; // 플레이어 미사일 리스트
	private ArrayList eMsList = null; // 적 미사이 리스트
	private ArrayList enList = null;// 필드에 나와있는 적기 리스트

	final double playerSpeed = 6.5;// ?? 모든 클래스에 final을 적용시키는 방법은 없을까?

	Rectangle box = null;

	int tCount;
	int eCount;
	private boolean start = false;
	private boolean left, right, down, up, fire, fusion, change;// ?? private
																// 왜먹임?
	private int w = 956, h = 778;

	Player p = new Player();

	public WB() {
		// ///////////////////////////////////////////////////////
		// ///////////////// MAIN SETTING CODE ///////////////////
		// ///////////////////////////////////////////////////////
		bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		pMsList = new ArrayList();
		eMsList = new ArrayList();
		enList = new ArrayList();

		this.addKeyListener(this);
		this.setSize(w, h);
		this.setTitle("Shooting Game");
		this.setResizable(false);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setVisible(true);
	}

	public void run() {
		bgsound bg = new bgsound();
		bg.get_Bgsound();

		try {
			while (true) {
				if (bg.play == false) {
					bg.bgm_on();
				}
				Thread.sleep((long) 16);

				if (start) {
					tCount++; // 프레임 증가

					playerChangeChk(); // 변신 체크
					playerMove(); // 무브체크
					playerFire(); // 발사 체크
									// 플레이어 버튼 이벤트 전부 클리어
					pMsMove();// 내미사일 움직임

					stage(tCount);// 저장한 패턴에 따라 적생성
									// 적 고유패턴에 의해 움직임(미사일 발사 포함)
									// stage에서는 명령만 내린다
					enemyMove(); // enemyMove 에서는 예약된 명령을 일괄적으로 수행한다

					eMsMove();// 적 미사일 움직임
					crashChk();// 충돌검사

				} else if (!start) {
					bg.bgm_off();
					firstSet();
					tCount = 0;
				}

				draw();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void pMsMove() {
		for (int i = 0; i < pMsList.size(); i++) {
			PMs pm = (PMs) pMsList.get(i);
			pm.move();

		}

	}

	private void eMsMove() {
		for (int i = 0; i < eMsList.size(); i++) {
			EMs em = (EMs) eMsList.get(i);
			Enemy en = null;
			boolean find = false;
			switch (em.type) {
			case 1:
				NMs nm;
				nm = (NMs) em;
				nm.move();
				break;

			case 2:
				LMslr lmlr;
				lmlr = (LMslr) em;
				for (int j = 0; j < enList.size(); j++) {
					en = (Enemy) enList.get(j);
					if (en.number == lmlr.number) {
						lmlr.move(en.y + (en.h / 2));
						find = true;
					}
				}
				if (find == false) {
					lmlr.move();
				}
				break;
			case 3:
				LMsud lmud;
				lmud = (LMsud) em;
				for (int j = 0; j < enList.size(); j++) {
					en = (Enemy) enList.get(j);
					if (en.number == lmud.number) {
						lmud.move(en.x + (en.w / 2));
						find = true;
					}
				}

				if (find == false) {
					lmud.move();
				}
				break;
			}

		}
	}

	private void enemyMove() {
		for (int i = 0; i < enList.size(); i++) {
			Enemy en = (Enemy) enList.get(i);
			BEnemy ben;
			if (en.cmdCnt == en.CmdList.size()) {
				enList.remove(i);
			} else {
				int cmdNum = (int) en.CmdList.get(en.cmdCnt);

				if (cmdNum > 10000) {
					en.stop(cmdNum % 10000);
				} else if (cmdNum > 500) {
					en.setSpeed(cmdNum % 500);
				} else {
					switch (cmdNum) {
					case 1:
						en.moveToXY(128, 150);
						break;
					case 2:
						en.moveToXY(378, 150);
						break;
					case 3:
						en.moveToXY(628, 150);
						break;
					case 4:
						en.moveToXY(128, 400);
						break;
					case 5:
						en.moveToXY(378, 400);
						break;
					case 6:
						en.moveToXY(628, 400);
						break;
					case 7:
						en.moveToXY(128, 650);
						break;
					case 8:
						en.moveToXY(378, 650);
						break;
					case 9:
						en.moveToXY(628, 650);
						break;
					case 11:
						en.moveLD(128, 400);
						break;
					case 12:
						en.moveRU(378, 150);
						break;
					case 13:
						en.moveLU(378, 150);
						break;
					case 14:
						en.moveRD(628, 400);
						break;
					case 15:
						en.moveRU(628, 400);
						break;
					case 16:
						en.moveLD(378, 650);
						break;
					case 17:
						en.moveRD(378, 650);
						break;
					case 18:
						en.moveLU(128, 400);
						break;
					case 21:
						en.moveLDR(128, 400, false);
						break;
					case 22:
						en.moveRUR(378, 150, true);
						break;
					case 23:
						en.moveLUR(378, 150, true);
						break;
					case 24:
						en.moveRDR(628, 400, false);
						break;
					case 25:
						en.moveRUR(628, 400, false);
						break;
					case 26:
						en.moveLDR(378, 650, true);
						break;
					case 27:
						en.moveRDR(378, 650, true);
						break;
					case 28:
						en.moveLUR(128, 400, false);
						break;
					case 31:
						en.fireStart(true);
						break;
					case 32:
						en.fireStart(false);
						break;

					case 33:
						ben = (BEnemy) en;
						ben.roatateSet(true);
						break;
					case 34:
						ben = (BEnemy) en;
						ben.roatateSet(false);
						break;
					case 101:
						en.moveToXY(628, 25 - (h / 2));
						break;
					case 102:
						en.moveToXY(753 + (w / 2), 150);
						break;
					case 103:
						en.moveToXY(753 + (w / 2), 400);
						break;
					case 104:
						en.moveToXY(753 + (w / 2), 650);
						break;
					case 105:
						en.moveToXY(628, 775 + (h / 2));
						break;
					case 106:
						en.moveToXY(378, 775 + (h / 2));
						break;
					case 107:
						en.moveToXY(128, 775 + (h / 2));
						break;
					case 108:
						en.moveToXY(3 - (w / 2), 650);
						break;
					case 109:
						en.moveToXY(3 - (w / 2), 400);
						break;
					case 110:
						en.moveToXY(3 - (w / 2), 150);
						break;
					case 111:
						en.moveToXY(128, 25 - (h / 2));
						break;
					case 112:
						en.moveToXY(378, 25 - (h / 2));
						break;

					}
				}

				switch (en.type) {
				case 1:
					BEnemy bE;
					bE = (BEnemy) en;
					bE.fire(eMsList);
					bE.rotate();
					break;
				case 2:
					CEnemy cE;
					cE = (CEnemy) en;
					cE.fire(eMsList, p);
					break;
				}

			}

		}

	}

	public void playerMove() {
		// ///////////// Key Control Code //////////////////
		// 캐릭터의 이동에 관련한 코드
		double diag = Math.cos(Math.toRadians(45)) * playerSpeed;

		if (right == true && up == true) {
			if (w - 203 >= p.x + p.pw && 25 <= p.y) {
				p.x += diag;
				p.y -= diag;
			}
			if (p.x + p.pw > w - 203)
				p.x = w - p.pw - 203;
			if (p.y <= 25)
				p.y = 25;

		} else if (left == true && up == true) {
			if (3 <= p.x && 25 <= p.y) {
				p.x -= diag;
				p.y -= diag;
			}
			if (p.x < 3)
				p.x = 3;
			if (p.y <= 25)
				p.y = 25;

		} else if (right == true && down == true) {
			if (w - 203 >= p.x + p.pw && 775 >= p.y + p.ph) {
				p.x += diag;
				p.y += diag;
			}
			if (p.x + p.pw > w - 203)
				p.x = w - p.pw - 203;
			if (p.y + p.ph > 775)
				p.y = 775 - p.ph;

		} else if (left == true && down == true) {
			if (3 <= p.x && 775 >= p.y + p.ph) {
				p.x -= diag;
				p.y += diag;
			}
			if (p.x < 3)
				p.x = 3;
			if (p.y + p.ph > 775)
				p.y = 775 - p.ph;

		} else {

			if (left) {
				if (3 <= p.x)
					p.x -= playerSpeed;
				if (p.x < 3)
					p.x = 3;
			}
			if (right) {
				if (w - 203 >= p.x + p.pw)
					p.x += playerSpeed;
				if (p.x + p.pw > w - 203)
					p.x = w - p.pw - 203;
			}
			if (up) {
				if (25 <= p.y)
					p.y -= playerSpeed;
				if (p.y <= 25)
					p.y = 25;
			}
			if (down) {
				if (775 >= p.y + p.ph)
					p.y += playerSpeed;
				if (p.y + p.ph > 775)
					p.y = 775 - p.ph;
			}
		}
	}

	public void playerFire() {
		if (tCount % 1.5 == 0) {

			if (fire) {

				if (p.wb == true) {
					PMs m;
					m = new PMs(p.x + (p.pw / 2) - 8, p.y, true);
					pMsList.add(m);

					m = new PMs(p.x + (p.pw / 2), p.y, true);
					pMsList.add(m);

					m = new PMs(p.x + (p.pw / 2) + 8, p.y, true);
					pMsList.add(m);

				}

				if (p.wb == false) {
					PMs m;
					m = new PMs(p.x + (p.pw / 2) - 8, p.y, false);
					pMsList.add(m);

					m = new PMs(p.x + (p.pw / 2), p.y, false);
					pMsList.add(m);

					m = new PMs(p.x + (p.pw / 2) + 8, p.y, false);
					pMsList.add(m);
				}
			}
		}
	}

	public void playerChangeChk() {

		if (change) {
			if (p.wb == true) {
				p.wb = false;
				change = false;
			} else if (p.wb == false) {
				p.wb = true;
				change = false;
			}
		}

	}

	// GAME OVER CHECK
	public void chkGameover() {
		if (p.white == 0 || p.black == 0) {
			start = false;
	
		}
	}

	// 초기세팅
	// 미완성
	public void firstSet() {
		p.score = 0;
		p.white = 500;
		p.black = 500;
		p.wb = true;
		p.x = 355;
		p.y = 373;
	}

	public void crashChk() {
		// ///////////Crash Check CODE/////////////////
		// 충돌 판정을 확인하는 용도

		Graphics g = this.getGraphics();
		Polygon pg = null;
		// i는 플레이어가 발사한 총알의 개체 넘버 (몇번쨰로 쏜 총알이냐)
		// j는 적기의 개체 넘버 (몇번쨰로 생성된 적기냐)

		for (int i = 0; i < pMsList.size(); i++) {
			PMs m = (PMs) pMsList.get(i);

			for (int j = 0; j < enList.size(); j++) {
				try {
					Enemy e = (Enemy) enList.get(j);

					// xpoint, ypoint배열에 각각 사각형의 꼭지점위치를 넣는다.
					// x,y는 시작지점, w,h는 시작지점부터의 크기
					int[] xpoints = { (int) Math.round(m.x),
							((int) m.x + m.pmw), ((int) m.x + m.pmw), (int) m.x };
					int[] ypoints = { (int) m.y, (int) m.y,
							((int) m.y + m.pmh), ((int) m.y + m.pmh) };
					// P에 미사일의 위치 정보를 넣는다.
					pg = new Polygon(xpoints, ypoints, 4);

					if (pg.intersects((double) e.x, (double) e.y, (double) e.w,
							(double) e.h)) {
						pMsList.remove(i);
						if (e.wb != m.wb)
							e.hp -= m.damage;
						if (e.hp == 0){
							p.score += e.score;
							enList.remove(j);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (m.x > w || m.x < 0 || m.y > h || m.y < 0) {
				pMsList.remove(i);
			}
		}

		// 플레이어기체와 적미사일의 충돌을 감지하는것
		for (int i = 0; i < eMsList.size(); i++) {
			EMs e = (EMs) eMsList.get(i);
			int[] xpoints = { (int) p.x + 14, ((int) p.x + p.pw - 14),
					((int) p.x + p.pw - 14), (int) p.x + 14 };
			int[] ypoints = { (int) p.y + 22, (int) p.y + 22,
					((int) p.y + p.ph) - 5, ((int) p.y + p.ph) - 5 };
			pg = new Polygon(xpoints, ypoints, 4);

			if (pg.intersects((double) e.x, (double) e.y, (double) e.emw,
					(double) e.emh)) {

				eMsList.remove(i);

				if (e.wb == true) {
					if (p.wb == true) {

					} else {
						p.black -= e.damage;
					}

				} else {
					if (p.wb == true) {
						p.white -= e.damage;
					} else {

					}
				}
				chkGameover(); // 피격후 게임오버 체크

			}

			if (e.x > w || e.x < 0 || e.y > h || e.y < 0) {
				eMsList.remove(i);
			}
		}

	}

	public void keyPressed(KeyEvent ke) {
		switch (ke.getKeyCode()) {
		case KeyEvent.VK_LEFT:
			left = true;
			break;
		case KeyEvent.VK_RIGHT:
			right = true;
			break;
		case KeyEvent.VK_UP:
			up = true;
			break;
		case KeyEvent.VK_DOWN:
			down = true;
			break;
		case KeyEvent.VK_A:
			fire = true;
			break;
		case KeyEvent.VK_S:
			change = true;
			break;
		case KeyEvent.VK_SPACE:
			fusion = true;
			break;
		case KeyEvent.VK_ENTER:
			start = true;
			break;
		case KeyEvent.VK_ESCAPE:
			start = false;
			break;
		}
	}

	public void keyReleased(KeyEvent ke) {
		switch (ke.getKeyCode()) {
		case KeyEvent.VK_LEFT:
			left = false;
			break;
		case KeyEvent.VK_RIGHT:
			right = false;
			break;
		case KeyEvent.VK_UP:
			up = false;
			break;
		case KeyEvent.VK_DOWN:
			down = false;
			break;
		case KeyEvent.VK_A:
			fire = false;
			break;
		}
	}

	public void keyTyped(KeyEvent ke) {

	}

	public void draw() throws IOException {
		// ///////////////////////////////////////////////////////////////////
		// ////////////////////////DROW CODE//////////////////////////////////
		// ///////////////////////////////////////////////////////////////////
		int min;
		// ----------get image data--------------------
		Toolkit tk = Toolkit.getDefaultToolkit();
		Image backGroundImg = tk.getImage("GameSource/picture/background.png"); // 배경이미지
		Image backGroundImg2 = tk
				.getImage("GameSource/picture/background2.png");
		Image gameoverImg = tk.getImage(""); // 게임오버
												// 로고
		Image characterImg1 = tk
				.getImage("GameSource/picture/player_white.png");
		Image characterImg2 = tk
				.getImage("GameSource/picture/player_black.png");

		// 미완성 원래는 터지는 모습이나 변신하는 모습 들어갈 예정
		Image characterImg3 = tk.getImage("GameSource/picture/player.png");
		Image characterImg4 = tk.getImage("GameSource/picture/player.png");
		Image characterImg5 = tk.getImage("GameSource/picture/player.png");
		Image characterImg6 = tk.getImage("GameSource/picture/player.png");

		// 이미지
		Image whiteBulletImg = tk.getImage("GameSource/picture/shot_white.png"); // 총알
		Image blackBulletImg = tk.getImage("GameSource/picture/shot_black.png"); // 미완성
		Image wNms = tk.getImage("GameSource/picture/wNms.png"); // 적 총알
		Image bNms = tk.getImage("GameSource/picture/bNms.png"); // 적
		Image lmlw = tk.getImage("GameSource/picture/lmlw.png"); // 적 총알
		Image lmuw = tk.getImage("GameSource/picture/lmuw.png"); // 적 총알
		Image lmlb = tk.getImage("GameSource/picture/lmlb.png"); // 적 총알
		Image lmub = tk.getImage("GameSource/picture/lmub.png"); // 적 총알
		// 총알
		// 추가해야함

		// 이미지
		Image b4lb = tk.getImage("GameSource/picture/b4lb.png"); // 적기 이미지
		Image b4lw = tk.getImage("GameSource/picture/b4lw.png"); // 적기 이미지
		Image bulb = tk.getImage("GameSource/picture/bulb.png"); // 적기 이미지
		Image bulw = tk.getImage("GameSource/picture/bulw.png"); // 적기 이미지
		Image bllb = tk.getImage("GameSource/picture/bllb.png"); // 적기 이미지
		Image bllw = tk.getImage("GameSource/picture/bllw.png"); // 적기 이미지

		BufferedImage b4nb = ImageIO.read(new File(
				"GameSource/picture/b4nb.png"));
		BufferedImage b4nw = ImageIO.read(new File(
				"GameSource/picture/b4nw.png"));
		BufferedImage bunb = ImageIO.read(new File(
				"GameSource/picture/bunb.png"));
		BufferedImage bunw = ImageIO.read(new File(
				"GameSource/picture/bunw.png"));
		BufferedImage blnb = ImageIO.read(new File(
				"GameSource/picture/blnb.png"));
		BufferedImage blnw = ImageIO.read(new File(
				"GameSource/picture/blnw.png"));

		Image cen1w = tk.getImage("GameSource/picture/cen1w.png"); // 적기 이미지
		Image cen2w = tk.getImage("GameSource/picture/cen2w.png"); // 적기 이미지
		Image cen3w = tk.getImage("GameSource/picture/cen3w.png"); // 적기 이미지
		Image cen1b = tk.getImage("GameSource/picture/cen1b.png"); // 적기 이미지
		Image cen2b = tk.getImage("GameSource/picture/cen2b.png"); // 적기 이미지
		Image cen3b = tk.getImage("GameSource/picture/cen3b.png"); // 적기 이미지

		BufferedImage f1 = ImageIO.read(new File("GameSource/picture/f1.png"));
		BufferedImage f2 = ImageIO.read(new File("GameSource/picture/f2.png"));
		BufferedImage f3 = ImageIO.read(new File("GameSource/picture/f3.png"));

		Image white_bar = tk.getImage("GameSource/picture/white.png");
		Image black_bar = tk.getImage("GameSource/picture/black.png");
		Image empty_barl = tk.getImage("GameSource/picture/emptyl.png");
		Image empty_barr = tk.getImage("GameSource/picture/emptyr.png");

		Image rogo = tk.getImage("GameSource/picture/rogo.png");
		Image blackboard = tk.getImage("GameSource/picture/blackboard.png"); // 적기 이미지
		// bi (아마도 게임화면)의 그래픽부분을 gs변수가 담당하게 함
		Graphics2D gs = (Graphics2D) bi.getGraphics();
		AffineTransform tx;
		AffineTransformOp op;
		double rotationRequired;
		// ------Draw Background------
		gs.drawImage(backGroundImg, 3, 25, this);// bgy 가 Y


		// ------cliping------
		gs.drawImage(blackboard, 753, 25, this);// bgy 가 Y
		gs.drawString("SCORE : " + p.score, 790, 80);
		System.out.println(p.score);
		
		box = new Rectangle(3, 25, 750, 750);
		gs.setClip(box);
		// ------Draw Gameover------
		if (!start) {
			gs.drawImage(gameoverImg, 55, 250, this);
		}

		// ------Draw Player---------

		if (p.wb == true) {
			gs.drawImage(characterImg1, (int) p.x, (int) p.y, this);
		} else {
			gs.drawImage(characterImg2, (int) p.x, (int) p.y, this);
		}

		// ------Draw Bullit---------
		for (int i = 0; i < pMsList.size(); i++) {
			PMs pm = (PMs) pMsList.get(i);
			if (pm.wb == true) {
				gs.drawImage(whiteBulletImg, (int) pm.x, (int) pm.y, this);
			} else {
				gs.drawImage(blackBulletImg, (int) pm.x, (int) pm.y, this);
			}

		}

		for (int i = 0; i < eMsList.size(); i++) {
			EMs em = (EMs) eMsList.get(i);
			switch (em.type) {
			case 1:
				if (em.wb == true) {
					gs.drawImage(wNms, (int) em.x, (int) em.y, this);
				} else {
					gs.drawImage(bNms, (int) em.x, (int) em.y, this);
				}
				break;
			case 2:
				if (em.wb == true) {
					gs.drawImage(lmlw, (int) em.x, (int) em.y, this);
				} else {
					gs.drawImage(lmlb, (int) em.x, (int) em.y, this);
				}
				break;
			case 3:
				if (em.wb == true) {
					gs.drawImage(lmuw, (int) em.x, (int) em.y, this);
				} else {
					gs.drawImage(lmub, (int) em.x, (int) em.y, this);
				}
				break;
			}

		}

		// ------Draw Enemy---------
		// 현재 카운트 되어있는 적기 수만큼 그려냄
		for (int i = 0; i < enList.size(); i++) {
			Enemy e = (Enemy) enList.get(i);
			switch (e.type) {
			case 1:
				switch (e.fireType) {
				case 1:
					if (e.wb == true) {

						rotationRequired = Math.toRadians(e.rotateX);
						tx = AffineTransform.getRotateInstance(
								rotationRequired, 30, 30);
						op = new AffineTransformOp(tx,
								AffineTransformOp.TYPE_BILINEAR);

						gs.drawImage(op.filter((BufferedImage) b4nw, null),
								(int) e.x, (int) e.y, this);
					} else {
						rotationRequired = Math.toRadians(e.rotateX);
						tx = AffineTransform.getRotateInstance(
								rotationRequired, 30, 30);
						op = new AffineTransformOp(tx,
								AffineTransformOp.TYPE_BILINEAR);

						gs.drawImage(op.filter(b4nb, null), (int) e.x,
								(int) e.y, this);
					}
					break;
				case 2:
					if (e.wb == true) {
						rotationRequired = Math.toRadians(e.rotateX);
						tx = AffineTransform.getRotateInstance(
								rotationRequired, 30, 30);
						op = new AffineTransformOp(tx,
								AffineTransformOp.TYPE_BILINEAR);

						gs.drawImage(op.filter((BufferedImage) blnw, null),
								(int) e.x, (int) e.y, this);
					} else {
						rotationRequired = Math.toRadians(e.rotateX);
						tx = AffineTransform.getRotateInstance(
								rotationRequired, 30, 30);
						op = new AffineTransformOp(tx,
								AffineTransformOp.TYPE_BILINEAR);

						gs.drawImage(op.filter((BufferedImage) blnb, null),
								(int) e.x, (int) e.y, this);
					}
					break;
				case 3:
					if (e.wb == true) {
						rotationRequired = Math.toRadians(e.rotateX);
						tx = AffineTransform.getRotateInstance(
								rotationRequired, 30, 30);
						op = new AffineTransformOp(tx,
								AffineTransformOp.TYPE_BILINEAR);

						gs.drawImage(op.filter((BufferedImage) bunw, null),
								(int) e.x, (int) e.y, this);

					} else {
						rotationRequired = Math.toRadians(e.rotateX);
						tx = AffineTransform.getRotateInstance(
								rotationRequired, 30, 30);
						op = new AffineTransformOp(tx,
								AffineTransformOp.TYPE_BILINEAR);

						gs.drawImage(op.filter((BufferedImage) bunb, null),
								(int) e.x, (int) e.y, this);
					}
					break;
				case 4:
					if (e.wb == true) {
						gs.drawImage(b4lw, (int) e.x, (int) e.y, this);
					} else {
						gs.drawImage(b4lb, (int) e.x, (int) e.y, this);
					}
					break;
				case 5:
					if (e.wb == true) {
						gs.drawImage(bulw, (int) e.x, (int) e.y, this);
					} else {
						gs.drawImage(bulb, (int) e.x, (int) e.y, this);
					}
					break;
				case 6:
					if (e.wb == true) {
						gs.drawImage(bllw, (int) e.x, (int) e.y, this);
					} else {
						gs.drawImage(bllb, (int) e.x, (int) e.y, this);
					}
					break;
				}

				break;

			case 2:

				switch (e.fireType) {
				case 1:

					tx = AffineTransform.getRotateInstance(e.rotateR, 30, 30);
					op = new AffineTransformOp(tx,
							AffineTransformOp.TYPE_BILINEAR);
					gs.drawImage(op.filter(f1, null), (int) e.x, (int) e.y,
							this);
					break;
				case 2:
					tx = AffineTransform.getRotateInstance(e.rotateR, 30, 30);
					op = new AffineTransformOp(tx,
							AffineTransformOp.TYPE_BILINEAR);
					gs.drawImage(op.filter(f2, null), (int) e.x, (int) e.y,
							this);
					break;
				case 3:
					tx = AffineTransform.getRotateInstance(e.rotateR, 30, 30);
					op = new AffineTransformOp(tx,
							AffineTransformOp.TYPE_BILINEAR);
					gs.drawImage(op.filter(f3, null), (int) e.x, (int) e.y,
							this);
					break;
				}

				if (e.wb == true) {

					switch (e.level) {
					case 1:
						gs.drawImage(cen1w, (int) e.x, (int) e.y, this);
						break;
					case 2:
						gs.drawImage(cen2w, (int) e.x, (int) e.y, this);
						break;
					case 3:
						gs.drawImage(cen3w, (int) e.x, (int) e.y, this);
						break;
					}

				} else {
					switch (e.level) {
					case 1:
						gs.drawImage(cen1b, (int) e.x, (int) e.y, this);
						break;
					case 2:
						gs.drawImage(cen2b, (int) e.x, (int) e.y, this);
						break;
					case 3:
						gs.drawImage(cen3b, (int) e.x, (int) e.y, this);
						break;
					}
				}

			}

		}

		box = new Rectangle(753, 0, 200, 1000);
		gs.setClip(box);
		gs.drawImage(rogo, 753, 500, this);

		gs.setClip(box);
		gs.drawImage(white_bar, 753, 300, this);
		gs.drawImage(black_bar, 853, 300, this);

		min = (500 - p.white) / 5 * 2;
		box = new Rectangle(753, 300, 200, min);
		gs.setClip(box);
		gs.drawImage(empty_barl, 753, 300, this);

		min = (500 - p.black) / 5 * 2;
		box = new Rectangle(853, 300, 200, min);
		gs.setClip(box);
		gs.drawImage(empty_barr, 853, 300, this);

		// --???--
		Graphics ge = this.getGraphics();
		ge.drawImage(bi, 0, 0, w, h, this);

	}

	void stage(int time) {
		Enemy en;
		switch (time) {
		case 300:
			en = new BEnemy(1, eCount++, 628, 25, 2, false, 3);
			en.CmdList.add(3);
			en.CmdList.add(10430);
			en.CmdList.add(33);
			en.CmdList.add(10320);
			en.CmdList.add(31);
			en.CmdList.add(10720);
			en.CmdList.add(32);
			en.CmdList.add(102);

			enList.add(en);

			en = new BEnemy(1, eCount++, 753, 650, 2, true, 2);
			en.CmdList.add(9);
			en.CmdList.add(10430);
			en.CmdList.add(33);
			en.CmdList.add(10320);
			en.CmdList.add(31);
			en.CmdList.add(10720);
			en.CmdList.add(32);
			en.CmdList.add(105);

			enList.add(en);

			en = new BEnemy(1, eCount++, 128, 775, 2, false, 3);
			en.CmdList.add(7);
			en.CmdList.add(10430);
			en.CmdList.add(33);
			en.CmdList.add(10320);
			en.CmdList.add(31);
			en.CmdList.add(10720);
			en.CmdList.add(32);
			en.CmdList.add(108);

			enList.add(en);

			en = new BEnemy(1, eCount++, 3, 150, 2, true, 2);

			en.CmdList.add(1);
			en.CmdList.add(10430);
			en.CmdList.add(33);
			en.CmdList.add(10320);
			en.CmdList.add(31);
			en.CmdList.add(10720);
			en.CmdList.add(32);
			en.CmdList.add(111);

			enList.add(en);

			break;

		case 600:

			en = new BEnemy(1, eCount++, 752, 400, 2, false, 5);

			en.CmdList.add(6);
			en.CmdList.add(10450);
			en.CmdList.add(31);
			en.CmdList.add(13);
			en.CmdList.add(11);
			en.CmdList.add(17);
			en.CmdList.add(15);
			en.CmdList.add(103);

			enList.add(en);

			en = new BEnemy(1, eCount++, 378, 775, 2, true, 6);

			en.CmdList.add(8);
			en.CmdList.add(10450);
			en.CmdList.add(31);
			en.CmdList.add(15);
			en.CmdList.add(13);
			en.CmdList.add(11);
			en.CmdList.add(17);
			en.CmdList.add(106);

			enList.add(en);

			en = new BEnemy(1, eCount++, 3, 400, 2, false, 5);

			en.CmdList.add(4);
			en.CmdList.add(10450);
			en.CmdList.add(31);
			en.CmdList.add(17);
			en.CmdList.add(15);
			en.CmdList.add(13);
			en.CmdList.add(11);
			en.CmdList.add(109);

			enList.add(en);

			en = new BEnemy(1, eCount++, 378, 25, 2, true, 6);

			en.CmdList.add(2);
			en.CmdList.add(10450);
			en.CmdList.add(31);
			en.CmdList.add(11);
			en.CmdList.add(17);
			en.CmdList.add(15);
			en.CmdList.add(13);
			en.CmdList.add(31);
			en.CmdList.add(112);

			enList.add(en);

			break;

		case 1900:

			en = new BEnemy(1, eCount++, 128, 25, 5, true, 6);

			en.CmdList.add(31);
			en.CmdList.add(107);
			enList.add(en);

			break;

		case 1950:

			en = new BEnemy(1, eCount++, 628, 25, 5, false, 6);

			en.CmdList.add(31);
			en.CmdList.add(105);
			enList.add(en);

			break;
		case 2000:

			en = new BEnemy(1, eCount++, 128, 25, 5, true, 6);

			en.CmdList.add(31);
			en.CmdList.add(107);
			enList.add(en);

			break;

		case 2050:

			en = new BEnemy(1, eCount++, 628, 25, 5, false, 6);

			en.CmdList.add(31);
			en.CmdList.add(105);
			enList.add(en);

			en = new CEnemy(1, eCount++, 378, 775, 3.5, true, 1);
			en.CmdList.add(8);
			en.CmdList.add(25);
			en.CmdList.add(23);
			en.CmdList.add(21);
			en.CmdList.add(27);
			en.CmdList.add(31);
			en.CmdList.add(25);
			en.CmdList.add(32);
			en.CmdList.add(23);
			en.CmdList.add(31);
			en.CmdList.add(21);
			en.CmdList.add(32);
			en.CmdList.add(27);
			en.CmdList.add(106);
			enList.add(en);

			break;

		case 2070:
			en = new CEnemy(2, eCount++, 378, 775, 3.5, true, 2);
			en.CmdList.add(8);
			en.CmdList.add(25);
			en.CmdList.add(23);
			en.CmdList.add(21);
			en.CmdList.add(27);
			en.CmdList.add(31);
			en.CmdList.add(25);
			en.CmdList.add(32);
			en.CmdList.add(23);
			en.CmdList.add(31);
			en.CmdList.add(21);
			en.CmdList.add(32);
			en.CmdList.add(27);
			en.CmdList.add(106);
			enList.add(en);
			break;

		case 2090:
			en = new CEnemy(3, eCount++, 378, 775, 3.5, true, 1);
			en.CmdList.add(8);
			en.CmdList.add(25);
			en.CmdList.add(23);
			en.CmdList.add(21);
			en.CmdList.add(27);
			en.CmdList.add(31);
			en.CmdList.add(25);
			en.CmdList.add(32);
			en.CmdList.add(23);
			en.CmdList.add(31);
			en.CmdList.add(21);
			en.CmdList.add(32);
			en.CmdList.add(27);
			en.CmdList.add(106);
			enList.add(en);
			break;

		case 2100:

			en = new BEnemy(1, eCount++, 128, 25, 5, true, 6);

			en.CmdList.add(31);
			en.CmdList.add(107);
			enList.add(en);

			break;

		case 2110:
			en = new CEnemy(2, eCount++, 378, 775, 3.5, true, 2);
			en.CmdList.add(8);
			en.CmdList.add(25);
			en.CmdList.add(23);
			en.CmdList.add(21);
			en.CmdList.add(27);
			en.CmdList.add(31);
			en.CmdList.add(25);
			en.CmdList.add(32);
			en.CmdList.add(23);
			en.CmdList.add(31);
			en.CmdList.add(21);
			en.CmdList.add(32);
			en.CmdList.add(27);
			en.CmdList.add(106);
			enList.add(en);
			break;

		case 2130:
			en = new CEnemy(1, eCount++, 378, 775, 3.5, true, 1);
			en.CmdList.add(8);
			en.CmdList.add(25);
			en.CmdList.add(23);
			en.CmdList.add(21);
			en.CmdList.add(27);
			en.CmdList.add(31);
			en.CmdList.add(25);
			en.CmdList.add(32);
			en.CmdList.add(23);
			en.CmdList.add(31);
			en.CmdList.add(21);
			en.CmdList.add(32);
			en.CmdList.add(27);
			en.CmdList.add(106);
			enList.add(en);
			break;

		case 2150:

			en = new BEnemy(1, eCount++, 628, 25, 5, false, 6);

			en.CmdList.add(31);
			en.CmdList.add(105);
			enList.add(en);

			break;

		case 2175:

			en = new CEnemy(1, eCount++, 378, 775, 3.5, false, 1);
			en.CmdList.add(8);
			en.CmdList.add(25);
			en.CmdList.add(23);
			en.CmdList.add(21);
			en.CmdList.add(27);
			en.CmdList.add(31);
			en.CmdList.add(25);
			en.CmdList.add(32);
			en.CmdList.add(23);
			en.CmdList.add(31);
			en.CmdList.add(21);
			en.CmdList.add(32);
			en.CmdList.add(27);
			en.CmdList.add(106);
			enList.add(en);

			break;

		case 2195:
			en = new CEnemy(2, eCount++, 378, 775, 3.5, false, 2);
			en.CmdList.add(8);
			en.CmdList.add(25);
			en.CmdList.add(23);
			en.CmdList.add(21);
			en.CmdList.add(27);
			en.CmdList.add(31);
			en.CmdList.add(25);
			en.CmdList.add(32);
			en.CmdList.add(23);
			en.CmdList.add(31);
			en.CmdList.add(21);
			en.CmdList.add(32);
			en.CmdList.add(27);
			en.CmdList.add(106);
			enList.add(en);
			break;

		case 2200:

			en = new BEnemy(1, eCount++, 128, 25, 5, true, 6);

			en.CmdList.add(31);
			en.CmdList.add(107);
			enList.add(en);

			break;

		case 2215:
			en = new CEnemy(3, eCount++, 378, 775, 3.5, false, 1);
			en.CmdList.add(8);
			en.CmdList.add(25);
			en.CmdList.add(23);
			en.CmdList.add(21);
			en.CmdList.add(27);
			en.CmdList.add(31);
			en.CmdList.add(25);
			en.CmdList.add(32);
			en.CmdList.add(23);
			en.CmdList.add(31);
			en.CmdList.add(21);
			en.CmdList.add(32);
			en.CmdList.add(27);
			en.CmdList.add(106);
			enList.add(en);
			break;

		case 2235:
			en = new CEnemy(2, eCount++, 378, 775, 3.5, false, 2);
			en.CmdList.add(8);
			en.CmdList.add(25);
			en.CmdList.add(23);
			en.CmdList.add(21);
			en.CmdList.add(27);
			en.CmdList.add(31);
			en.CmdList.add(25);
			en.CmdList.add(32);
			en.CmdList.add(23);
			en.CmdList.add(31);
			en.CmdList.add(21);
			en.CmdList.add(32);
			en.CmdList.add(27);
			en.CmdList.add(106);
			enList.add(en);
			break;

		case 2250:

			en = new BEnemy(1, eCount++, 753, 150, 5, true, 5);

			en.CmdList.add(31);
			en.CmdList.add(110);
			enList.add(en);

			break;

		case 2255:
			en = new CEnemy(1, eCount++, 378, 775, 3.5, false, 1);
			en.CmdList.add(8);
			en.CmdList.add(25);
			en.CmdList.add(23);
			en.CmdList.add(21);
			en.CmdList.add(27);
			en.CmdList.add(31);
			en.CmdList.add(25);
			en.CmdList.add(32);
			en.CmdList.add(23);
			en.CmdList.add(31);
			en.CmdList.add(21);
			en.CmdList.add(32);
			en.CmdList.add(27);
			en.CmdList.add(106);
			enList.add(en);
			break;

		case 2300:

			en = new BEnemy(1, eCount++, 753, 150, 5, false, 5);

			en.CmdList.add(31);
			en.CmdList.add(110);
			enList.add(en);
			break;

		case 2305:
			en = new CEnemy(1, eCount++, 378, 775, 3.5, true, 1);
			en.CmdList.add(8);
			en.CmdList.add(25);
			en.CmdList.add(23);
			en.CmdList.add(21);
			en.CmdList.add(27);
			en.CmdList.add(31);
			en.CmdList.add(25);
			en.CmdList.add(32);
			en.CmdList.add(23);
			en.CmdList.add(31);
			en.CmdList.add(21);
			en.CmdList.add(32);
			en.CmdList.add(27);
			en.CmdList.add(106);
			enList.add(en);
			break;

		case 2325:
			en = new CEnemy(1, eCount++, 378, 775, 3.5, false, 1);
			en.CmdList.add(8);
			en.CmdList.add(25);
			en.CmdList.add(23);
			en.CmdList.add(21);
			en.CmdList.add(27);
			en.CmdList.add(31);
			en.CmdList.add(25);
			en.CmdList.add(32);
			en.CmdList.add(23);
			en.CmdList.add(31);
			en.CmdList.add(21);
			en.CmdList.add(32);
			en.CmdList.add(27);
			en.CmdList.add(106);
			enList.add(en);
			break;

		case 2345:
			en = new CEnemy(2, eCount++, 378, 775, 3.5, true, 2);
			en.CmdList.add(8);
			en.CmdList.add(25);
			en.CmdList.add(23);
			en.CmdList.add(21);
			en.CmdList.add(27);
			en.CmdList.add(31);
			en.CmdList.add(25);
			en.CmdList.add(32);
			en.CmdList.add(23);
			en.CmdList.add(31);
			en.CmdList.add(21);
			en.CmdList.add(32);
			en.CmdList.add(27);
			en.CmdList.add(106);
			enList.add(en);
			break;

		case 2350:

			en = new BEnemy(1, eCount++, 753, 150, 5, true, 5);

			en.CmdList.add(31);
			en.CmdList.add(110);
			enList.add(en);

			break;

		case 2365:
			en = new CEnemy(2, eCount++, 378, 775, 3.5, false, 2);
			en.CmdList.add(8);
			en.CmdList.add(25);
			en.CmdList.add(23);
			en.CmdList.add(21);
			en.CmdList.add(27);
			en.CmdList.add(31);
			en.CmdList.add(25);
			en.CmdList.add(32);
			en.CmdList.add(23);
			en.CmdList.add(31);
			en.CmdList.add(21);
			en.CmdList.add(32);
			en.CmdList.add(27);
			en.CmdList.add(106);
			enList.add(en);
			break;

		case 2385:
			en = new CEnemy(1, eCount++, 378, 775, 3.5, true, 1);
			en.CmdList.add(8);
			en.CmdList.add(25);
			en.CmdList.add(23);
			en.CmdList.add(21);
			en.CmdList.add(27);
			en.CmdList.add(31);
			en.CmdList.add(25);
			en.CmdList.add(32);
			en.CmdList.add(23);
			en.CmdList.add(31);
			en.CmdList.add(21);
			en.CmdList.add(32);
			en.CmdList.add(27);
			en.CmdList.add(106);
			enList.add(en);
			break;

		case 2400:

			en = new BEnemy(1, eCount++, 753, 150, 5, false, 5);

			en.CmdList.add(31);
			en.CmdList.add(110);
			enList.add(en);

			break;

		case 2405:
			en = new CEnemy(1, eCount++, 378, 775, 3.5, false, 1);
			en.CmdList.add(8);
			en.CmdList.add(25);
			en.CmdList.add(23);
			en.CmdList.add(21);
			en.CmdList.add(27);
			en.CmdList.add(31);
			en.CmdList.add(25);
			en.CmdList.add(32);
			en.CmdList.add(23);
			en.CmdList.add(31);
			en.CmdList.add(21);
			en.CmdList.add(32);
			en.CmdList.add(27);
			en.CmdList.add(106);
			enList.add(en);
			break;

		case 2450:

			en = new BEnemy(1, eCount++, 753, 150, 5, true, 5);

			en.CmdList.add(31);
			en.CmdList.add(110);
			enList.add(en);

			break;

		case 2500:

			en = new BEnemy(1, eCount++, 753, 150, 5, false, 5);

			en.CmdList.add(31);
			en.CmdList.add(110);
			enList.add(en);

			break;

		case 2550:

			en = new BEnemy(1, eCount++, 753, 150, 5, true, 5);

			en.CmdList.add(31);
			en.CmdList.add(110);
			enList.add(en);

			en = new CEnemy(3, eCount++, 753, 150, 5, true, 1);
			en.CmdList.add(3);
			en.CmdList.add(31);
			en.CmdList.add(10200);
			en.CmdList.add(102);
			enList.add(en);

			en = new CEnemy(3, eCount++, 3, 150, 5, true, 1);
			en.CmdList.add(1);
			en.CmdList.add(31);
			en.CmdList.add(10200);
			en.CmdList.add(110);
			enList.add(en);

			en = new CEnemy(3, eCount++, 753, 650, 5, false, 1);
			en.CmdList.add(9);
			en.CmdList.add(31);
			en.CmdList.add(10200);
			en.CmdList.add(104);
			enList.add(en);

			en = new CEnemy(3, eCount++, 3, 650, 5, false, 1);
			en.CmdList.add(7);
			en.CmdList.add(31);
			en.CmdList.add(10200);
			en.CmdList.add(108);
			enList.add(en);

			break;

		case 2600:

			en = new BEnemy(1, eCount++, 753, 150, 5, false, 5);
			en.CmdList.add(31);
			en.CmdList.add(110);
			enList.add(en);

			break;

		case 2800:

			en = new BEnemy(1, eCount++, 378, 25, 3, false, 4);

			en.CmdList.add(5);
			en.CmdList.add(10050);
			en.CmdList.add(31);
			en.CmdList.add(4);
			en.CmdList.add(22);
			en.CmdList.add(24);
			en.CmdList.add(26);
			en.CmdList.add(28);
			en.CmdList.add(22);
			en.CmdList.add(24);
			en.CmdList.add(26);
			en.CmdList.add(28);
			en.CmdList.add(22);
			en.CmdList.add(24);
			en.CmdList.add(26);
			en.CmdList.add(28);
			en.CmdList.add(22);
			en.CmdList.add(24);
			en.CmdList.add(26);
			en.CmdList.add(28);
			en.CmdList.add(22);
			en.CmdList.add(24);
			en.CmdList.add(26);
			en.CmdList.add(106);

			enList.add(en);

			break;

		case 2950:

			en = new CEnemy(1, eCount++, 753, 150, 3, true, 1);
			en.CmdList.add(3);
			en.CmdList.add(31);
			en.CmdList.add(1);
			en.CmdList.add(7);
			en.CmdList.add(9);
			en.CmdList.add(3);
			en.CmdList.add(32);
			en.CmdList.add(102);
			enList.add(en);

			break;

		case 2970:

			en = new CEnemy(1, eCount++, 753, 150, 3, true, 1);
			en.CmdList.add(3);
			en.CmdList.add(31);
			en.CmdList.add(1);
			en.CmdList.add(7);
			en.CmdList.add(9);
			en.CmdList.add(3);
			en.CmdList.add(32);
			en.CmdList.add(102);
			enList.add(en);

			break;
		case 2990:

			en = new CEnemy(1, eCount++, 753, 150, 3, true, 1);
			en.CmdList.add(3);
			en.CmdList.add(31);
			en.CmdList.add(1);
			en.CmdList.add(7);
			en.CmdList.add(9);
			en.CmdList.add(3);
			en.CmdList.add(32);
			en.CmdList.add(102);
			enList.add(en);

			break;
		case 3010:

			en = new CEnemy(1, eCount++, 753, 150, 3, true, 1);
			en.CmdList.add(3);
			en.CmdList.add(31);
			en.CmdList.add(1);
			en.CmdList.add(7);
			en.CmdList.add(9);
			en.CmdList.add(3);
			en.CmdList.add(32);
			en.CmdList.add(102);
			enList.add(en);

			break;
		case 3030:

			en = new CEnemy(1, eCount++, 753, 150, 3, true, 1);
			en.CmdList.add(3);
			en.CmdList.add(31);
			en.CmdList.add(1);
			en.CmdList.add(7);
			en.CmdList.add(9);
			en.CmdList.add(3);
			en.CmdList.add(32);
			en.CmdList.add(102);
			enList.add(en);

			break;
		case 3050:

			en = new CEnemy(1, eCount++, 753, 150, 3, true, 1);
			en.CmdList.add(3);
			en.CmdList.add(31);
			en.CmdList.add(1);
			en.CmdList.add(7);
			en.CmdList.add(9);
			en.CmdList.add(3);
			en.CmdList.add(32);
			en.CmdList.add(102);
			enList.add(en);

			break;

		case 3600:

			en = new CEnemy(1, eCount++, 3, 150, 3.5, true, 2);
			en.CmdList.add(1);
			en.CmdList.add(31);
			en.CmdList.add(2);
			en.CmdList.add(11);
			en.CmdList.add(6);
			en.CmdList.add(32);
			en.CmdList.add(103);
			enList.add(en);

			break;

		case 3625:

			en = new CEnemy(1, eCount++, 3, 650, 3.5, false, 2);
			en.CmdList.add(7);
			en.CmdList.add(31);
			en.CmdList.add(8);
			en.CmdList.add(18);
			en.CmdList.add(6);
			en.CmdList.add(32);
			en.CmdList.add(103);
			enList.add(en);

			break;
		case 3650:

			en = new CEnemy(1, eCount++, 3, 150, 3.5, true, 2);
			en.CmdList.add(1);
			en.CmdList.add(31);
			en.CmdList.add(2);
			en.CmdList.add(11);
			en.CmdList.add(6);
			en.CmdList.add(32);
			en.CmdList.add(103);
			enList.add(en);

			break;

		case 3675:

			en = new CEnemy(1, eCount++, 3, 650, 3.5, false, 2);
			en.CmdList.add(7);
			en.CmdList.add(31);
			en.CmdList.add(8);
			en.CmdList.add(18);
			en.CmdList.add(6);
			en.CmdList.add(32);
			en.CmdList.add(103);
			enList.add(en);

			break;
		case 3700:

			en = new CEnemy(1, eCount++, 3, 150, 3.5, true, 2);
			en.CmdList.add(1);
			en.CmdList.add(31);
			en.CmdList.add(2);
			en.CmdList.add(11);
			en.CmdList.add(6);
			en.CmdList.add(32);
			en.CmdList.add(103);
			enList.add(en);

			break;

		case 3725:

			en = new CEnemy(1, eCount++, 3, 650, 3.5, false, 2);
			en.CmdList.add(7);
			en.CmdList.add(31);
			en.CmdList.add(8);
			en.CmdList.add(18);
			en.CmdList.add(6);
			en.CmdList.add(32);
			en.CmdList.add(103);
			enList.add(en);

			break;

		case 3750:

			en = new CEnemy(1, eCount++, 3, 150, 3.5, true, 2);
			en.CmdList.add(1);
			en.CmdList.add(31);
			en.CmdList.add(2);
			en.CmdList.add(11);
			en.CmdList.add(6);
			en.CmdList.add(32);
			en.CmdList.add(103);
			enList.add(en);

			break;

		case 4100:

			en = new CEnemy(2, eCount++, 3, 150, 4, true, 2);
			en.CmdList.add(1);
			en.CmdList.add(31);
			en.CmdList.add(100130);
			en.CmdList.add(32);
			en.CmdList.add(110);
			enList.add(en);

			en = new CEnemy(2, eCount++, 3, 400, 4, true, 2);
			en.CmdList.add(4);
			en.CmdList.add(31);
			en.CmdList.add(100130);
			en.CmdList.add(32);
			en.CmdList.add(109);
			enList.add(en);

			break;

		case 4140:

			en = new CEnemy(2, eCount++, 378, 25, 4, false, 2);
			en.CmdList.add(2);
			en.CmdList.add(31);
			en.CmdList.add(100130);
			en.CmdList.add(32);
			en.CmdList.add(112);
			enList.add(en);

			en = new CEnemy(2, eCount++, 628, 25, 4, false, 2);
			en.CmdList.add(3);
			en.CmdList.add(31);
			en.CmdList.add(100130);
			en.CmdList.add(32);
			en.CmdList.add(101);
			enList.add(en);

			break;

		case 4180:

			en = new CEnemy(2, eCount++, 753, 400, 4, true, 2);
			en.CmdList.add(6);
			en.CmdList.add(31);
			en.CmdList.add(100130);
			en.CmdList.add(32);
			en.CmdList.add(103);
			enList.add(en);

			en = new CEnemy(2, eCount++, 753, 650, 4, true, 2);
			en.CmdList.add(9);
			en.CmdList.add(31);
			en.CmdList.add(100130);
			en.CmdList.add(32);
			en.CmdList.add(104);
			enList.add(en);

			break;

		case 4220:

			en = new CEnemy(2, eCount++, 128, 775, 4, false, 2);
			en.CmdList.add(7);
			en.CmdList.add(31);
			en.CmdList.add(100130);
			en.CmdList.add(32);
			en.CmdList.add(107);
			enList.add(en);

			en = new CEnemy(2, eCount++, 378, 775, 4, false, 2);
			en.CmdList.add(8);
			en.CmdList.add(31);
			en.CmdList.add(100130);
			en.CmdList.add(32);
			en.CmdList.add(106);
			enList.add(en);

			break;

		

		case 4441:
			en = new BEnemy(1, eCount++, 378, 25, 3, true, 4);
			en.CmdList.add(5);
			en.CmdList.add(10050);
			en.CmdList.add(31);
			en.CmdList.add(4);
			en.CmdList.add(22);
			en.CmdList.add(24);
			en.CmdList.add(26);
			en.CmdList.add(28);
			en.CmdList.add(22);

			en.CmdList.add(112);

			enList.add(en);

			break;
			
		case 4485:
			en = new CEnemy(3, eCount++, 3, 150, 4, true, 3);
			en.CmdList.add(1);
			en.CmdList.add(10041);
			en.CmdList.add(31);
			en.CmdList.add(10041);
			en.CmdList.add(32);
			en.CmdList.add(111);
			enList.add(en);

			en = new CEnemy(3, eCount++, 628, 25, 4, false, 3);
			en.CmdList.add(3);
			en.CmdList.add(10041);
			en.CmdList.add(31);
			en.CmdList.add(10041);
			en.CmdList.add(32);
			en.CmdList.add(102);
			enList.add(en);

			en = new CEnemy(3, eCount++, 753, 650, 4, true, 3);
			en.CmdList.add(9);
			en.CmdList.add(10041);
			en.CmdList.add(31);
			en.CmdList.add(10041);
			en.CmdList.add(32);
			en.CmdList.add(105);
			enList.add(en);

			en = new CEnemy(3, eCount++, 128, 775, 4, false, 3);
			en.CmdList.add(7);
			en.CmdList.add(10041);
			en.CmdList.add(31);
			en.CmdList.add(10041);
			en.CmdList.add(32);
			en.CmdList.add(108);
			enList.add(en);

			break;
			
		case 4615:
			en = new CEnemy(3, eCount++, 3, 150, 4, true, 2);
			en.CmdList.add(1);
			en.CmdList.add(31);
			en.CmdList.add(10041);
			en.CmdList.add(32);
			en.CmdList.add(111);
			enList.add(en);

			en = new CEnemy(3, eCount++, 628, 25, 4, false, 2);
			en.CmdList.add(3);
			en.CmdList.add(31);
			en.CmdList.add(10041);
			en.CmdList.add(32);
			en.CmdList.add(102);
			enList.add(en);

			en = new CEnemy(3, eCount++, 753, 650, 4, true, 2);
			en.CmdList.add(9);
			en.CmdList.add(31);
			en.CmdList.add(10041);
			en.CmdList.add(32);
			en.CmdList.add(105);
			enList.add(en);

			en = new CEnemy(3, eCount++, 128, 775, 4, false, 2);
			en.CmdList.add(7);
			en.CmdList.add(31);
			en.CmdList.add(10041);
			en.CmdList.add(32);
			en.CmdList.add(108);
			enList.add(en);

			break;
			
		case 4715:
			en = new CEnemy(3, eCount++, 3, 150, 4, true, 2);
			en.CmdList.add(1);
			en.CmdList.add(31);
			en.CmdList.add(10041);
			en.CmdList.add(32);
			en.CmdList.add(111);
			enList.add(en);

			en = new CEnemy(3, eCount++, 628, 25, 4, false, 2);
			en.CmdList.add(3);
			en.CmdList.add(31);
			en.CmdList.add(10041);
			en.CmdList.add(32);
			en.CmdList.add(102);
			enList.add(en);

			en = new CEnemy(3, eCount++, 753, 650, 4, true, 2);
			en.CmdList.add(9);
			en.CmdList.add(31);
			en.CmdList.add(10041);
			en.CmdList.add(32);
			en.CmdList.add(105);
			enList.add(en);

			en = new CEnemy(3, eCount++, 128, 775, 4, false, 2);
			en.CmdList.add(7);
			en.CmdList.add(31);
			en.CmdList.add(10041);
			en.CmdList.add(32);
			en.CmdList.add(108);
			enList.add(en);

			break;
			
		case 4795:
			en = new CEnemy(3, eCount++, 3, 150, 4, true, 2);
			en.CmdList.add(1);
			en.CmdList.add(31);
			en.CmdList.add(10041);
			en.CmdList.add(32);
			en.CmdList.add(111);
			enList.add(en);

			en = new CEnemy(3, eCount++, 628, 25, 4, false, 2);
			en.CmdList.add(3);
			en.CmdList.add(31);
			en.CmdList.add(10041);
			en.CmdList.add(32);
			en.CmdList.add(102);
			enList.add(en);

			en = new CEnemy(3, eCount++, 753, 650, 4, true, 2);
			en.CmdList.add(9);
			en.CmdList.add(31);
			en.CmdList.add(10041);
			en.CmdList.add(32);
			en.CmdList.add(105);
			enList.add(en);

			en = new CEnemy(3, eCount++, 128, 775, 4, false, 2);
			en.CmdList.add(7);
			en.CmdList.add(31);
			en.CmdList.add(10041);
			en.CmdList.add(32);
			en.CmdList.add(108);
			enList.add(en);

			break;
			
		case 4815:
			en = new CEnemy(3, eCount++, 3, 400, 4, true, 1);
			en.CmdList.add(4);
			en.CmdList.add(31);
			en.CmdList.add(10050);
			en.CmdList.add(32);
			en.CmdList.add(109);
			enList.add(en);

			en = new CEnemy(3, eCount++, 378, 25, 4, false, 1);
			en.CmdList.add(2);
			en.CmdList.add(31);
			en.CmdList.add(10050);
			en.CmdList.add(32);
			en.CmdList.add(112);
			enList.add(en);

			en = new CEnemy(3, eCount++, 753, 400, 4, true, 1);
			en.CmdList.add(6);
			en.CmdList.add(31);
			en.CmdList.add(10050);
			en.CmdList.add(32);
			en.CmdList.add(103);
			enList.add(en);

			en = new CEnemy(3, eCount++, 378, 775, 4, false, 1);
			en.CmdList.add(8);
			en.CmdList.add(31);
			en.CmdList.add(10050);
			en.CmdList.add(32);
			en.CmdList.add(106);
			enList.add(en);

			break;
			
			
		case 4865:
			en = new CEnemy(3, eCount++, 3, 150, 4, true, 3);
			en.CmdList.add(1);
			en.CmdList.add(10041);
			en.CmdList.add(31);
			en.CmdList.add(10041);
			en.CmdList.add(32);
			en.CmdList.add(111);
			enList.add(en);

			en = new CEnemy(3, eCount++, 628, 25, 4, false, 3);
			en.CmdList.add(3);
			en.CmdList.add(10041);
			en.CmdList.add(31);
			en.CmdList.add(10041);
			en.CmdList.add(32);
			en.CmdList.add(102);
			enList.add(en);

			en = new CEnemy(3, eCount++, 753, 650, 4, true, 3);
			en.CmdList.add(9);
			en.CmdList.add(10041);
			en.CmdList.add(31);
			en.CmdList.add(10041);
			en.CmdList.add(32);
			en.CmdList.add(105);
			enList.add(en);

			en = new CEnemy(3, eCount++, 128, 775, 4, false, 3);
			en.CmdList.add(7);
			en.CmdList.add(10041);
			en.CmdList.add(31);
			en.CmdList.add(10041);
			en.CmdList.add(32);
			en.CmdList.add(108);
			enList.add(en);

			break;
			
		case 4995:
			en = new CEnemy(3, eCount++, 3, 150, 4, true, 2);
			en.CmdList.add(1);
			en.CmdList.add(31);
			en.CmdList.add(10041);
			en.CmdList.add(32);
			en.CmdList.add(111);
			enList.add(en);

			en = new CEnemy(3, eCount++, 628, 25, 4, false, 2);
			en.CmdList.add(3);
			en.CmdList.add(31);
			en.CmdList.add(10041);
			en.CmdList.add(32);
			en.CmdList.add(102);
			enList.add(en);

			en = new CEnemy(3, eCount++, 753, 650, 4, true, 2);
			en.CmdList.add(9);
			en.CmdList.add(31);
			en.CmdList.add(10041);
			en.CmdList.add(32);
			en.CmdList.add(105);
			enList.add(en);

			en = new CEnemy(3, eCount++, 128, 775, 4, false, 2);
			en.CmdList.add(7);
			en.CmdList.add(31);
			en.CmdList.add(10041);
			en.CmdList.add(32);
			en.CmdList.add(108);
			enList.add(en);

			break;
			
		case 5095:
			en = new CEnemy(3, eCount++, 3, 150, 4, true, 2);
			en.CmdList.add(1);
			en.CmdList.add(31);
			en.CmdList.add(10041);
			en.CmdList.add(32);
			en.CmdList.add(111);
			enList.add(en);

			en = new CEnemy(3, eCount++, 628, 25, 4, false, 2);
			en.CmdList.add(3);
			en.CmdList.add(31);
			en.CmdList.add(10041);
			en.CmdList.add(32);
			en.CmdList.add(102);
			enList.add(en);

			en = new CEnemy(3, eCount++, 753, 650, 4, true, 2);
			en.CmdList.add(9);
			en.CmdList.add(31);
			en.CmdList.add(10041);
			en.CmdList.add(32);
			en.CmdList.add(105);
			enList.add(en);

			en = new CEnemy(3, eCount++, 128, 775, 4, false, 2);
			en.CmdList.add(7);
			en.CmdList.add(31);
			en.CmdList.add(10041);
			en.CmdList.add(32);
			en.CmdList.add(108);
			enList.add(en);

			break;
			
		case 5175:
			en = new CEnemy(3, eCount++, 3, 150, 4, true, 2);
			en.CmdList.add(1);
			en.CmdList.add(31);
			en.CmdList.add(10041);
			en.CmdList.add(32);
			en.CmdList.add(111);
			enList.add(en);

			en = new CEnemy(3, eCount++, 628, 25, 4, false, 2);
			en.CmdList.add(3);
			en.CmdList.add(31);
			en.CmdList.add(10041);
			en.CmdList.add(32);
			en.CmdList.add(102);
			enList.add(en);

			en = new CEnemy(3, eCount++, 753, 650, 4, true, 2);
			en.CmdList.add(9);
			en.CmdList.add(31);
			en.CmdList.add(10041);
			en.CmdList.add(32);
			en.CmdList.add(105);
			enList.add(en);

			en = new CEnemy(3, eCount++, 128, 775, 4, false, 2);
			en.CmdList.add(7);
			en.CmdList.add(31);
			en.CmdList.add(10041);
			en.CmdList.add(32);
			en.CmdList.add(108);
			enList.add(en);

			break;
			
		case 5195:
			en = new CEnemy(3, eCount++, 3, 400, 4, true, 1);
			en.CmdList.add(4);
			en.CmdList.add(31);
			en.CmdList.add(10050);
			en.CmdList.add(32);
			en.CmdList.add(109);
			enList.add(en);

			en = new CEnemy(3, eCount++, 378, 25, 4, false, 1);
			en.CmdList.add(2);
			en.CmdList.add(31);
			en.CmdList.add(10050);
			en.CmdList.add(32);
			en.CmdList.add(112);
			enList.add(en);

			en = new CEnemy(3, eCount++, 753, 400, 4, true, 1);
			en.CmdList.add(6);
			en.CmdList.add(31);
			en.CmdList.add(10050);
			en.CmdList.add(32);
			en.CmdList.add(103);
			enList.add(en);

			en = new CEnemy(3, eCount++, 378, 775, 4, false, 1);
			en.CmdList.add(8);
			en.CmdList.add(31);
			en.CmdList.add(10050);
			en.CmdList.add(32);
			en.CmdList.add(106);
			enList.add(en);

			break;
		}

	}

	public static void main(String[] args) {
		Thread t = new Thread(new WB());
		t.start();
	}
}

class Player {
	int score;
	double x;
	double y;
	int pw = 50;
	int ph = 55;
	int white;
	int black;
	int fusionTime;
	boolean wb;

}

class PMs {
	final double pMsSpeed = 10;

	double x;
	double y;
	int pmw = 7;
	int pmh = 13;
	int damage = 1;
	boolean wb;

	public PMs(double x, double y, boolean wb) {
		this.x = x - (pmw / 2);
		this.y = y - (pmh / 2);
		this.wb = wb;
	}

	public void move() {
		y -= pMsSpeed;
	}

}

class EMs {
	int type;
	double x;
	double y;
	int emw;
	int emh;
	int damage;
	boolean wb;
	double speed;
	double rotateX;
	int number;

	EMs(int number, double x, double y, double speed, boolean wb, double rotateX) {
		this.x = x - (emw / 2);
		this.y = y - (emh / 2);
		this.wb = wb;
		this.speed = speed;
		this.rotateX = Math.toRadians(rotateX);
		this.number = number;
	}

	public void move() {

	}

}

class NMs extends EMs {

	NMs(int number, double x, double y, double speed, boolean wb, double rotateX) {
		super(number, x, y, speed, wb, rotateX);

		type = 1;
		emw = 12;// 미완성 노말탄 크기, 데미지 수정
		emh = 12;
		damage = 10;
		this.number = number;
		this.x = x - (emw / 2);
		this.y = y - (emh / 2);
	}

	public void move() {
		x = x + Math.cos(rotateX) * speed;
		y = y + Math.sin(rotateX) * speed;
	}

}

class LMslr extends EMs {
	boolean left;
	double lastY;

	LMslr(int number, double x, double y, double speed, boolean wb,
			double rotateX, boolean left) {
		super(number, x, y, speed, wb, rotateX);
		type = 2;
		emw = 34;// 미완성 레이져탄 크기 데미지 수정
		emh = 10;
		damage = 8;
		this.number = number;
		this.left = left;
	}

	public void move() {
		if (left == true) {
			x = x - speed;
			this.y = lastY;
		} else {
			x = x + speed;
			this.y = lastY;
		}

	}

	public void move(double y) {
		if (left == true) {
			x = x - speed;
			this.y = y - (emh / 2);
		} else {
			x = x + speed;
			this.y = y - (emh / 2);
		}
		lastY = y;
	}

}

class LMsud extends EMs {
	boolean up;
	double lastX;

	LMsud(int number, double x, double y, double speed, boolean wb,
			double rotateX, boolean up) {
		super(number, x, y, speed, wb, rotateX);
		type = 3;
		emw = 10;// 미완성 레이져탄 크기 데미지 수정
		emh = 34;
		damage = 8;
		this.number = number;
		this.up = up;
	}

	public void move() {
		if (up == true) {
			y = y - speed;
			this.x = lastX;
		} else {
			y = y + speed;
			this.x = lastX;
		}

	}

	public void move(double x) {

		if (up == true) {

			y = y - speed;
			this.x = x - (emw / 2);

		} else {

			y = y + speed;
			this.x = x - (emw / 2);

		}
		lastX = x;

	}

}

class Enemy {
	double x;
	double y;
	int w = 60;
	int h = 60;
	int hp = 20;
	double speed;
	double diag;
	double rad90;
	double radx;
	double radi;
	boolean wb; // white == true
	int type;
	int fireType;
	boolean fire;
	double bSpeed;
	int fireCnt;
	int fireTerm;
	int number;
	int level;
	int timer;
	double rotateX;
	double rotateR;
	int score;

	ArrayList CmdList = new ArrayList();
	// fire cmd list???
	int cmdCnt;

	final double rad45 = Math.toRadians(45);

	Enemy(int level, int number, double x, double y, double speed, boolean wb) {
		this.level = level;
		this.number = number;
		this.x = x - (w / 2);
		this.y = y - (h / 2);
		this.speed = speed;
		diag = Math.cos(Math.toRadians(45)) * speed;
		radi = 65 / (250 / speed);
		this.wb = wb;
	}

	public void setSpeed(int speed) {
		this.speed = speed;
		cmdCnt++;
	}

	public void fireStart(boolean fire) {
		this.fire = fire;
		cmdCnt++;
	}

	public void stop(int time) {
		if (timer < time) {
			timer++;
		} else if (timer == time) {
			timer = 0;
			cmdCnt++;
		}
	}

	public void moveToXY(int X, int Y) {
		if (x == X - (w / 2)) {
			if (y < Y - (h / 2)) {
				y += speed;
				if (y > Y - (h / 2)) {
					y = Y - (h / 2);
					cmdCnt++;
				}
			} else if (y > Y - (h / 2)) {
				y -= speed;
				if (y < Y - (h / 2)) {
					y = Y - (h / 2);
					cmdCnt++;
				}
			} else if (y == Y - (h / 2)) {
				cmdCnt++;
			}
		} else if (y == Y - (h / 2)) {
			if (x < X - (w / 2)) {
				x += speed;
				if (x > X - (w / 2)) {
					x = X - (w / 2);
					cmdCnt++;
				}
			} else if (x > X - (w / 2)) {
				x -= speed;
				if (x < X - (w / 2)) {
					x = X - (w / 2);
					cmdCnt++;
				}
			} else if (x == X - (w / 2)) {
				cmdCnt++;
			}
		}
	}

	public void moveRU(int X, int Y) {
		if (x < X - (w / 2)) {
			x += diag;
			y -= diag;
		} else if (x >= X - (w / 2)) {
			x = X - (w / 2);
			y = Y - (h / 2);
			cmdCnt++;
		}
	}

	public void moveLU(int X, int Y) {
		if (x > X - (w / 2)) {
			x -= diag;
			y -= diag;
		} else if (x <= X - (w / 2)) {
			x = X - (w / 2);
			y = Y - (h / 2);
			cmdCnt++;
		}
	}

	public void moveRD(int X, int Y) {
		if (x < X - (w / 2)) {
			x += diag;
			y += diag;
		} else if (x >= X - (w / 2)) {
			x = X - (w / 2);
			y = Y - (h / 2);
			cmdCnt++;
		}
	}

	public void moveLD(int X, int Y) {
		if (x > X - (w / 2)) {
			x -= diag;
			y += diag;
		} else if (x <= X - (w / 2)) {
			x = X - (w / 2);
			y = Y - (h / 2);
			cmdCnt++;
		}
	}

	public void moveRUR(int X, int Y, boolean inside) {
		if (rad90 < 90) {
			rad90 = rad90 + radi;
			radx = Math.toRadians(rad90);
			if (inside == true) {
				x = (378 - Math.cos(radx) * 250) - (w / 2);
				y = (400 - Math.sin(radx) * 250) - (h / 2);
			} else {
				x = (378 + Math.sin(radx) * 250) - (w / 2);
				y = (400 + Math.cos(radx) * 250) - (h / 2);
			}
		} else if (rad90 >= 90) {
			x = X - (w / 2);
			y = Y - (h / 2);
			rad90 = 0;// ?? enmove메소드 case문 부분에 넣을까 싶다
			cmdCnt++;
		}

	}

	public void moveLUR(int X, int Y, boolean inside) {
		if (rad90 < 90) {
			rad90 = rad90 + radi;
			radx = Math.toRadians(rad90);
			if (inside == true) {
				x = (378 + Math.cos(radx) * 250) - (w / 2);
				y = (400 - Math.sin(radx) * 250) - (w / 2);
			} else {
				x = (378 - Math.sin(radx) * 250) - (w / 2);
				y = (400 + Math.cos(radx) * 250) - (w / 2);
			}
		} else if (rad90 >= 90) {
			x = X - (w / 2);
			y = Y - (h / 2);
			rad90 = 0;
			cmdCnt++;
		}
	}

	public void moveRDR(int X, int Y, boolean inside) {
		if (rad90 < 90) {
			rad90 = rad90 + radi;
			radx = Math.toRadians(rad90);
			if (inside == true) {
				x = (378 - Math.cos(radx) * 250) - (w / 2);
				y = (400 + Math.sin(radx) * 250) - (w / 2);
			} else {
				x = (378 + Math.sin(radx) * 250) - (w / 2);
				y = (400 - Math.cos(radx) * 250) - (w / 2);
			}
		} else if (rad90 >= 90) {
			x = X - (w / 2);
			y = Y - (h / 2);
			rad90 = 0;
			cmdCnt++;
		}
	}

	public void moveLDR(int X, int Y, boolean inside) {

		if (rad90 < 90) {
			rad90 = rad90 + radi;
			radx = Math.toRadians(rad90);
			if (inside == true) {
				x = (378 + Math.cos(radx) * 250) - (w / 2);
				y = (400 + Math.sin(radx) * 250) - (w / 2);
			} else {
				x = (378 - Math.sin(radx) * 250) - (w / 2);
				y = (400 - Math.cos(radx) * 250) - (w / 2);
			}
		} else if (rad90 >= 90) {
			x = X - (w / 2);
			y = Y - (h / 2);
			rad90 = 0;
			cmdCnt++;
		}
	}

}

class BEnemy extends Enemy {
	boolean rotate = false;
	double rotSpeed = 1;

	BEnemy(int level, int number, double x, double y, double speed, boolean wb,
			int fireType) {
		super(level, number, x, y, speed, wb);
		type = 1;
		this.fireType = fireType;// 미완성 if문이나 case문으로 각 fireType별 미사일 발사속도,발사 주기
		hp = 10000000;
		// 기본값 설정

		switch (fireType) {
		case 1:
			
			fireTerm = 2;
			bSpeed = 4;
			break;
		case 2:
			fireTerm = 2;
			bSpeed = 4;
			break;
		case 3:
			fireTerm = 2;
			bSpeed = 4;
			break;
		case 4:
			fireTerm = 1;
			bSpeed = 18;
			break;
		case 5:
			fireTerm = 1;
			bSpeed = 18;
			break;
		case 6:
			fireTerm = 1;
			bSpeed = 18;
			break;
		}
	}

	BEnemy(int level, int number, double x, double y, double speed, boolean wb,
			int fireType, int fireTerm, int bSpeed) {
		super(level, number, x, y, speed, wb);
		type = 1;
		this.fireType = fireType;
		this.fireTerm = fireTerm;
		this.bSpeed = bSpeed;
		hp = 10000000;

	}

	BEnemy(int level, int number, double x, double y, double speed, boolean wb,
			int fireType, double rotSpeed, boolean rotate) {
		super(level, number, x, y, speed, wb);
		type = 1;
		this.fireType = fireType;
		this.rotSpeed = rotSpeed;
		this.rotate = rotate;
		hp = 10000000;

		switch (fireType) {
		case 1:
			fireTerm = 2;
			bSpeed = 3;
			break;
		case 2:
			fireTerm = 2;
			bSpeed = 3;
			break;
		case 3:
			fireTerm = 2;
			bSpeed = 3;
			break;
		case 4:
			fireTerm = 1;
			bSpeed = 18;
			break;
		case 5:
			fireTerm = 1;
			bSpeed = 18;
			break;
		case 6:
			fireTerm = 1;
			bSpeed = 18;
			break;
		}
	}

	BEnemy(int level, int number, double x, double y, double speed, boolean wb,
			int fireType, int fireTerm, int bSpeed, double rotSpeed,
			boolean rotate) {
		super(level, number, x, y, speed, wb);
		type = 1;
		this.fireType = fireType;
		this.rotSpeed = rotSpeed;
		this.rotate = rotate;
		this.fireTerm = fireTerm;
		this.bSpeed = bSpeed;
		hp = 10000000;
	}

	void rotate() {
		if (rotate == true) {
			rotateX += rotSpeed;
			if (rotateX > 360) {
				rotateX %= 360;
			}
		}
	}

	void roatateSet(boolean rotate) {
		this.rotate = rotate;
		cmdCnt++;
	}

	void fire(ArrayList msl) {
		if (fire == true) {
			switch (fireType) {
			case 1:
				fire1(msl);
				break;
			case 2:
				fire2(msl);
				break;
			case 3:
				fire3(msl);
				break;

			case 4:
				fire4(msl);
				break;
			case 5:
				fire5(msl);
				break;
			case 6:
				fire6(msl);
				break;
			}
		}
	}

	void fire1(ArrayList msl) {
		if (fireCnt < fireTerm) {
			fireCnt++;
		} else {
			NMs m;
			double i;
			double j;

			i = rotateX;
			j = Math.toRadians(i);
			m = new NMs(number, x + (w / 2) + Math.cos(j) * (w / 2), y
					+ (h / 2) + Math.sin(j) * (h / 2), bSpeed, wb, i);
			msl.add(m);

			i = (rotateX + 90) % 360;
			j = Math.toRadians(i);
			m = new NMs(number, x + (w / 2) + Math.cos(j) * (w / 2), y
					+ (h / 2) + Math.sin(j) * (h / 2), bSpeed, wb, i);
			msl.add(m);

			i = (rotateX + 180) % 360;
			j = Math.toRadians(i);
			m = new NMs(number, x + (w / 2) + Math.cos(j) * (w / 2), y
					+ (h / 2) + Math.sin(j) * (h / 2), bSpeed, wb, i);
			msl.add(m);

			i = (rotateX + 270) % 360;
			j = Math.toRadians(i);
			m = new NMs(number, x + (w / 2) + Math.cos(j) * (w / 2), y
					+ (h / 2) + Math.sin(j) * (h / 2), bSpeed, wb, i);
			msl.add(m);

			fireCnt = 0;
		}

	}

	void fire2(ArrayList msl) {
		if (fireCnt < fireTerm) {
			fireCnt++;
		} else {
			NMs m;
			double i;
			double j;

			i = rotateX;
			j = Math.toRadians(i);
			m = new NMs(number, x + (w / 2) + Math.cos(j) * (w / 2), y
					+ (h / 2) + Math.sin(j) * (h / 2), bSpeed, wb, i);
			msl.add(m);

			i = (rotateX + 180) % 360;
			j = Math.toRadians(i);
			m = new NMs(number, x + (w / 2) + Math.cos(j) * (w / 2), y
					+ (h / 2) + Math.sin(j) * (h / 2), bSpeed, wb, i);
			msl.add(m);

			fireCnt = 0;
		}
	}

	void fire3(ArrayList msl) {
		if (fireCnt < fireTerm) {
			fireCnt++;
		} else {
			NMs m;
			double i;
			double j;

			i = (rotateX + 90) % 360;
			j = Math.toRadians(i);
			m = new NMs(number, x + (w / 2) + Math.cos(j) * (w / 2), y
					+ (h / 2) + Math.sin(j) * (h / 2), bSpeed, wb, i);
			msl.add(m);

			i = (rotateX + 270) % 360;
			j = Math.toRadians(i);
			m = new NMs(number, x + (w / 2) + Math.cos(j) * (w / 2), y
					+ (h / 2) + Math.sin(j) * (h / 2), bSpeed, wb, i);
			msl.add(m);

			fireCnt = 0;
		}
	}

	void fire4(ArrayList msl) {
		if (fireCnt < fireTerm) {
			fireCnt++;
		} else {
			EMs m;
			double i;
			double j;
			i = rotateX;
			j = Math.toRadians(i);
			m = new LMsud(number, x + (w / 2) + Math.cos(j) * (w / 2), y
					+ (h / 2) + Math.sin(j) * (h / 2), bSpeed, wb, i, true);
			msl.add(m);

			i = (rotateX + 90) % 360;
			j = Math.toRadians(i);
			m = new LMslr(number, x + (w / 2) + Math.cos(j) * (w / 2), y
					+ (h / 2) + Math.sin(j) * (h / 2), bSpeed, wb, i, true);
			msl.add(m);

			i = (rotateX + 180) % 360;
			j = Math.toRadians(i);
			m = new LMsud(number, x + (w / 2) + Math.cos(j) * (w / 2), y
					+ (h / 2) + Math.sin(j) * (h / 2), bSpeed, wb, i, false);
			msl.add(m);

			i = (rotateX + 270) % 360;
			j = Math.toRadians(i);
			m = new LMslr(number, x + (w / 2) + Math.cos(j) * (w / 2), y
					+ (h / 2) + Math.sin(j) * (h / 2), bSpeed, wb, i, false);
			msl.add(m);

			fireCnt = 0;

			/*
			 * i = rotateX; j = Math.toRadians(i); m = new LMsud(number, x + (w
			 * / 2) + Math.cos(j) * (w / 2), y + (h / 2) + Math.sin(j) * (h /
			 * 2), bSpeed, wb, i,true); msl.add(m);
			 * 
			 * i = (rotateX + 90) % 360; j = Math.toRadians(i); m = new
			 * LMslr(number, x + (w / 2) + Math.cos(j) * (w / 2), y + (h / 2) +
			 * Math.sin(j) * (h / 2), bSpeed, wb, i,true); msl.add(m);
			 * 
			 * i = (rotateX + 180) % 360; j = Math.toRadians(i); m = new
			 * LMsud(number, x + (w / 2) + Math.cos(j) * (w / 2), y + (h / 2) +
			 * Math.sin(j) * (h / 2), bSpeed, wb, i,true); msl.add(m);
			 * 
			 * i = (rotateX + 270) % 360; j = Math.toRadians(i); m = new
			 * LMslr(number, x + (w / 2) + Math.cos(j) * (w / 2), y + (h / 2) +
			 * Math.sin(j) * (h / 2), bSpeed, wb, i,true); msl.add(m);
			 * 
			 * fireCnt = 0;
			 */
		}
	}

	void fire5(ArrayList msl) {
		if (fireCnt < fireTerm) {
			fireCnt++;
		} else {
			EMs m;
			double i;
			double j;

			i = (rotateX + 90) % 360;
			j = Math.toRadians(i);
			m = new LMsud(number, x + (w / 2) + Math.cos(j) * (w / 2), y
					+ (h / 2) + Math.sin(j) * (h / 2), bSpeed, wb, i, true);
			msl.add(m);

			i = (rotateX + 270) % 360;
			j = Math.toRadians(i);
			m = new LMsud(number, x + (w / 2) + Math.cos(j) * (w / 2), y
					+ (h / 2) + Math.sin(j) * (h / 2), bSpeed, wb, i, false);
			msl.add(m);

			fireCnt = 0;
		}
	}

	void fire6(ArrayList msl) {
		if (fireCnt < fireTerm) {
			fireCnt++;
		} else {
			EMs m;
			double i;
			double j;

			i = rotateX;
			j = Math.toRadians(i);
			m = new LMslr(number, x + (w / 2) + Math.cos(j) * (w / 2), y
					+ (h / 2) + Math.sin(j) * (h / 2), bSpeed, wb, i, true);
			msl.add(m);

			i = (rotateX + 180) % 360;
			j = Math.toRadians(i);
			m = new LMslr(number, x + (w / 2) + Math.cos(j) * (w / 2), y
					+ (h / 2) + Math.sin(j) * (h / 2), bSpeed, wb, i, false);
			msl.add(m);

			fireCnt = 0;
		}
	}
}

class CEnemy extends Enemy {

	CEnemy(int level, int number, double x, double y, double speed, boolean wb,
			int fireType) {
		super(level, number, x, y, speed, wb);
		type = 2;
		this.fireType = fireType;// 미완성 각 fireType별로 발사주기와 총알 속도 기본값 설정
		switch (fireType) {
		case 1:
			switch (level) {
			case 1:
				score = 100;
				hp = 20;
				fireTerm = 60;
				bSpeed = 4;
				break;
			case 2:
				score = 200;
				hp = 25;
				fireTerm = 50;
				bSpeed = 4;
				break;
			case 3:
				score = 300;
				hp = 35;
				fireTerm = 40;
				bSpeed = 4;
				break;
			}
			break;
		case 2:
			switch (level) {
			case 1:
				score = 100;
				hp = 20;
				fireTerm = 70;
				bSpeed = 3;
				break;
			case 2:
				score = 200;
				hp = 25;
				fireTerm = 60;
				bSpeed = 4;
				break;
			case 3:
				score = 300;
				hp = 35;
				fireTerm = 40;
				bSpeed = 5;
				break;
			}

			break;
		case 3:

			switch (level) {
			case 1:
				score = 100;
				hp = 20;
				fireTerm = 60;
				bSpeed = 3;
				break;
			case 2:
				score = 200;
				hp = 25;
				fireTerm = 40;
				bSpeed = 4;
				break;
			case 3:
				score = 300;
				hp = 35;
				fireTerm = 40;
				bSpeed = 4;
				break;
			}

			break;
		}
	}

	CEnemy(int level, int number, double x, double y, double speed, boolean wb,
			int fireType, int fireTerm, int bSpeed) {
		super(level, number, x, y, speed, wb);
		type = 2;
		this.fireType = fireType;
		this.fireTerm = fireTerm;
		this.bSpeed = bSpeed;
	}

	void fire(ArrayList msl, Player p) {
		if (fire == true) {
			switch (fireType) {
			case 1:
				fire1(msl, p);
				break;
			case 2:
				fire2(msl, p);
				break;
			case 3:
				fire3(msl);
				break;

			}

		}
	}

	void fire1(ArrayList msl, Player p) {

		if (fireCnt < fireTerm) {
			fireCnt++;

		} else if (fireCnt < fireTerm + 15) {
			if (((fireTerm + 15) - fireCnt) % 3 == 0) {

				EMs m;
				rotateX = (Math.atan2((p.y + (p.ph / 2)) - (y + (h / 2)),
						(p.x + (p.pw / 2)) - (x + (w / 2))) * (180 / Math.PI));

				rotateR = Math.toRadians(rotateX);
				m = new NMs(number, x + (w / 2) + Math.cos(rotateR) * (w / 2),
						y + (h / 2) + Math.sin(rotateR) * (h / 2), bSpeed, wb,
						rotateX);
				msl.add(m);
			}
			fireCnt++;
			if (fireCnt == fireTerm + 15)
				fireCnt = 0;
		}

	}

	void fire2(ArrayList msl, Player p) {
		if (fireCnt < fireTerm) {
			fireCnt++;
		} else {
			EMs m;
			double rSpeed;
			for (int i = 0; i < 12; i++) {
				rotateX = (Math.atan2((p.y + (p.ph / 2)) - (y + (h / 2)),
						(p.x + (p.pw / 2)) - (x + (w / 2))) * (180 / Math.PI));
				rotateX = rotateX - 7 + (Math.random() * 15);
				rotateR = Math.toRadians(rotateX);
				rSpeed = bSpeed * (((Math.random() * 30) + 70) / 100);
				m = new NMs(number, x + (w / 2) + Math.cos(rotateR) * (w / 2),
						y + (h / 2) + Math.sin(rotateR) * (h / 2), rSpeed, wb,
						rotateX);
				msl.add(m);
			}
			fireCnt = 0;
		}

	}

	void fire3(ArrayList msl) {

		if (fireCnt < fireTerm) {
			fireCnt++;
		} else {
			EMs m;

			for (int i = 0; i < 360; i = i + 15) {
				double j = Math.toRadians(i);
				m = new NMs(number, x + (w / 2) + Math.cos(j) * (w / 2), y
						+ (h / 2) + Math.sin(j) * (h / 2), bSpeed, wb, i);
				msl.add(m);
			}

			fireCnt = 0;
		}

	}

}

class bgsound {

	Boolean play = false;
	String bgFile = "GameSource/music/bgm.wav";
	Clip clip;
	Line.Info linfo = new Line.Info(Clip.class);
	Line line;

	// 배경 사운드
	public void get_Bgsound() {
		File soundFile = new File(bgFile);
		try {
			line = AudioSystem.getLine(linfo);
		} catch (LineUnavailableException e2) {
			e2.printStackTrace();
		}
		clip = (Clip) line;
		AudioInputStream ais = null;
		try {
			ais = AudioSystem.getAudioInputStream(soundFile);
		} catch (UnsupportedAudioFileException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		try {
			clip.open(ais);
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void bgm_on() {
		clip.setFramePosition(0);
		clip.start();
		play = true;
	}

	public void bgm_off() {
		clip.stop();
		play = false;
	}
}