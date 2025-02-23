package monPackage;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.*;

public class ChromeDinosaur extends JPanel implements ActionListener, KeyListener{
	int boardWidth = 750;
	int boardHeight = 250;
	
	// utilisation des images
	Image dinosaurImg;
	Image dinosaurDeadImg;
	Image dinosaurJumpImg;
	Image cactus1Img;
	Image cactus2Img;
	Image cactus3Img;
	Image dinosaurDuckImg;
	
	class Block {
		int x, y, width, height;
		Image img;
		
		Block(int x, int y, int width, int height, Image img) {
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
			this.img = img;
			
		}
	}
	
	// taille et position du dino
	int dinosaurWidth = 80;
	int dinosaurHeight = 90;
	int dinosaurX = 50;
	int dinosaurY = boardHeight - dinosaurHeight;
	
	Block dinosaur;
	
	//le cactus
	int cactus1Width = 34;
	int cactus2Width = 69;
	int cactus3Width = 102;
	
	int cactusHeight = 70;
	int cactusX = 700;
	int cactusY = boardHeight - cactusHeight;
	ArrayList<Block> cactusArray;
	
	//physique du jeu
	int velocityX = -12;
	int velocityY = 0; // vitesse saut du dino (il change pas de position)
	int gravity = 1;
	
	// fonctionnement du game over
	boolean gameOver = false;
	// déclaration de la variable du score
	int score = 0;
	
	// déclaration de la variable du reset (une loop)
	Timer gameLoop;
	// déclaration de la variable d'apparition de cactus
	Timer placeCactusTimer;
	
	public ChromeDinosaur() {
		setPreferredSize(new Dimension(boardWidth, boardHeight));
		setBackground(Color.LIGHT_GRAY);
		setFocusable(true);
		addKeyListener(this);
		
        dinosaurImg = new ImageIcon(getClass().getResource("./img/dino-run.gif")).getImage();
        dinosaurDeadImg = new ImageIcon(getClass().getResource("./img/dino-dead.png")).getImage();
        dinosaurJumpImg = new ImageIcon(getClass().getResource("./img/dino-jump.png")).getImage();
        cactus1Img = new ImageIcon(getClass().getResource("./img/cactus1.png")).getImage();
        cactus2Img = new ImageIcon(getClass().getResource("./img/cactus2.png")).getImage();
        cactus3Img = new ImageIcon(getClass().getResource("./img/cactus3.png")).getImage();
        dinosaurDuckImg = new ImageIcon(getClass().getResource("./img/dino-duck1.png")).getImage();
		
		// apparition du dino
		dinosaur = new Block(dinosaurX, dinosaurY, dinosaurWidth, dinosaurHeight, dinosaurImg);
		// apparition des cactus
		cactusArray = new ArrayList<Block>();
		
		// timer avant refresh
		gameLoop = new Timer(1000/60, this); // 1000/60 = 60 frames par seconde, update
		gameLoop.start();
		
		// timer d'apparition de cactus
		placeCactusTimer = new Timer(1500, new ActionListener() { // toutes les 1.5 secondes un nouveau cactus apparait
			@Override
			public void actionPerformed(ActionEvent e) {
				placeCactus();
			}
		}); 
		placeCactusTimer.start();
	}
	
	void placeCactus() {
		// si game over, on arrête et donc on arrête la logique du placement de cactus
		if (gameOver) {
			return;
		}
		
		double placeCactusChance = Math.random(); // Il nous donne un chiffre entre 0 et 0.999999
		if (placeCactusChance > .90) { // Seulement 10% de nombre au dessus de .90 donc 10% de chance d'avoir cactus 3
			Block cactus = new Block(cactusX, cactusY, cactus3Width, cactusHeight, cactus3Img);
			cactusArray.add(cactus);
		} else if (placeCactusChance > .70){ // Seulement 20% d'avoir le cactus 2 car on prend entre .70 et .90 exclu
			Block cactus = new Block(cactusX, cactusY, cactus2Width, cactusHeight, cactus2Img);
			cactusArray.add(cactus);
		} else if (placeCactusChance > .50) { // Seulement 20% d'avoir le cactus 1
			Block cactus = new Block(cactusX, cactusY, cactus1Width, cactusHeight, cactus1Img);
			cactusArray.add(cactus);
		}
		
		if (cactusArray.size() > 10) {
			cactusArray.remove(0); // enlève le premier cactus de l'array
		}
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		draw(g);
	}
	
	public void draw (Graphics g){
		// L'image du dino
		g.drawImage(dinosaur.img, dinosaur.x, dinosaur.y, dinosaur.width,dinosaur.height, null);
		
		// L'image du cactus
		for (int i = 0; i < cactusArray.size(); i++) {
			Block cactus = cactusArray.get(i);
			g.drawImage(cactus.img, cactus.x, cactus.y, cactus.width, cactus.height, null);
		}
		
		// Affichage du score
		g.setColor(Color.black);
		g.setFont(new Font("Courier", Font.PLAIN, 32));
		if (gameOver) {
			g.drawString("Game Over: " + String.valueOf(score), 10, 35);
		} else {
			g.drawString(String.valueOf(score), 10, 35);
		}
	}
	
	public void move() {
		// soumis à la gravité
		velocityY += gravity;
		// dinosaur saute
		dinosaur.y += velocityY;
		
		// empêche que le dino descendes plus bas que le sol
		if (dinosaur.y > dinosaurY) { 
			dinosaur.y = dinosaurY; 
			velocityY = 0; 
			dinosaur.img = dinosaurImg;
		}
		
		// on ajoute un cactus à l'array 
		for (int i = 0; i < cactusArray.size(); i++) {
			Block cactus = cactusArray.get(i);
			cactus.x += velocityX;
			
			if (collision(dinosaur, cactus)) { 
				gameOver = true;
				dinosaur.img = dinosaurDeadImg;
			}
		}
		
		// Score s'update à chaque frame
		score++;
	}

	boolean collision(Block a, Block b) {
		return a.x < b.x + b.width &&  // le coin haut gauche de a ne touche pas le coin haut droite de b
			   a.x + a.width > b.x && // le coin haut droite de a passe pas le coin haut gauche de b
			   a.y < b.y + b.height && // le coin haut gauche de a ne touche pas le coin bas gauche de b
			   a.y + a.height > b.y; // le coin bas gauche de a passe le coin haut gauche de b
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		// à chaque loop on update la position puis on repaint donc on refresh
		move();
		repaint();
		if (gameOver) {
			placeCactusTimer.stop();
			gameLoop.stop();
		}
	}
	
	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_SPACE || e.getKeyCode() == KeyEvent.VK_UP){ 
			// le saut n'est possible que si le dino touche le sol
			if (dinosaur.y == dinosaurY) {
				velocityY = -17;
				dinosaur.img = dinosaurJumpImg;
			}
		}
		
		if (gameOver) {
			// reset le jeu en remettant les conditions de base
			dinosaur.y = dinosaurY;
			dinosaur.img = dinosaurImg;
			velocityY = 0;
			cactusArray.clear();
			score = 0;
			gameOver = false;
			gameLoop.start();
			placeCactusTimer.start();
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {}


	@Override
	public void keyReleased(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_DOWN) {
			System.out.println("DUCK");
			if (dinosaur.y == dinosaurY) {
				velocityY = +10;
				dinosaur.img = dinosaurDuckImg;
			}
		}
	}
}
