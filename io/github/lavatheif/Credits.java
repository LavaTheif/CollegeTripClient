/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.lavatheif;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;
import javax.swing.JFrame;

/**
 *
 * @author cf32047
 */
public class Credits extends Canvas {
    JFrame frame;
    static boolean left = false, right = false, space = false;
    Thread run;
    
    public Credits(){
        frame = new JFrame("Credits");
        frame.setVisible(true);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setAutoRequestFocus(true);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.add(this);
        addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }
            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode() == e.VK_LEFT)
                    left = true;
                else if(e.getKeyCode() == e.VK_RIGHT)
                    right = true;
                else if(e.getKeyCode() == e.VK_SPACE)
                    space = true;
                
            }
            @Override
            public void keyReleased(KeyEvent e) {
                if(e.getKeyCode() == e.VK_LEFT)
                    left = false;
                else if(e.getKeyCode() == e.VK_RIGHT)
                    right = false;
                else if(e.getKeyCode() == e.VK_SPACE)
                    space = false;
            }
        });
        run = new Thread(new Runnable() {
            int x = 400;
            ArrayList<String> bullets = new ArrayList<String>();
            ArrayList<String> black = new ArrayList<String>();
            int dir = 1, offs = 0, tick = 0;
            
            @Override
            public void run() {
                createBufferStrategy(3);
                while(true){
                    if(!frame.isDisplayable())
                        break;
                    BufferedImage bi = renderImage();
                    renderScreen(bi);
                    doCalculations(bi);
                }
            }
            
            private void doCalculations(BufferedImage bi){
                tick++;
                if(tick%10==0){
                    offs += dir;
                    if(offs > 150 || offs < -150)
                        dir *= -1;
                }

                if(left)
                    x--;
                if(right)
                    x++;
                if(x>800-20)
                    x=800-20;
                if(x<0)
                    x=0;

                if(space){
                    space = false;
                    bullets.add((x+10)+"|"+(550));
                }
//                    ArrayList<Integer> set = new ArrayList<Integer>();
                ArrayList<String> next = new ArrayList<>();
                bullets.forEach((dat) -> {
                    int i = Integer.parseInt(dat.split("\\|")[0]);
                    int y = Integer.parseInt(dat.split("\\|")[1]);
                    y--;

                    int col = -16777216;
                    if(y-6 >= 0)
                        col = bi.getRGB(i, y-6);
//                    System.out.println(col);
                    //Check if colliding with words
                    //by getting col of pixel at y-6

                    if(col != -16777216){
//                            set.add(i);
//                            black.put(i, y-6);
                    for(int j = -3; j <= 3; j++){
                        for(int k = -3; k <= 3; k++){
                            if(i+j-offs < 0)
                                continue;
                            if(y-6+k < 0)
                                continue;
                            black.add((i-offs+j)+"|"+(y-6+k));
                        }
                    }
                    }else
                        next.add((i)+"|"+(y));
                });
                bullets = next;
//                    for(int i : set)
//                        bullets.remove(i);
            }
            
            private BufferedImage renderImage() {
                BufferedImage bi = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);
                Graphics g = bi.createGraphics();
                g.setColor(Color.BLACK);
                g.fillRect(0, 0, 800, 600);

                g.setColor(Color.BLUE);
                g.setFont(new Font("cambria", 0, 50));
                g.drawString("Created by:", 250+offs, 50);
                g.drawString("Charlie Ferris", 250+offs, 100);
                g.drawString("Jack Barron", 250+offs, 150);

                g.setFont(new Font("cambria", 0, 25));
                g.drawString("Supervisor: Nilesh Jogoo", 250+offs, 250);

                g.setColor(Color.BLACK);
                for(String dat : black){
                    int i = Integer.parseInt(dat.split("\\|")[0]);
                    int y = Integer.parseInt(dat.split("\\|")[1]);
                    g.drawRect(i+offs, y, 1, 1);
                }
//                black.re

                g.setColor(Color.RED);
                g.fillRect(x, 550, 20, 10);

                g.setColor(Color.YELLOW);
                for(String dat : bullets){
                    int i = Integer.parseInt(dat.split("\\|")[0]);
                    int y = Integer.parseInt(dat.split("\\|")[1]);
                    g.fillRect(i, y, 2, 5);
                }
                g.dispose();
                
                return bi;
            }

            private void renderScreen(BufferedImage bi) {
                BufferStrategy bs = getBufferStrategy();
                Graphics g = bs.getDrawGraphics();
                g.drawImage(bi, 0, 0, null);
                bs.show();
            }
        });
        run.start();
    }
}
