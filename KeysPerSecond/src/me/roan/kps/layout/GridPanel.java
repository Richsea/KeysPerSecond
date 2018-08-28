package me.roan.kps.layout;

import java.awt.Graphics;

import javax.swing.JPanel;

import me.roan.kps.Main;
import me.roan.kps.SizeManager;

public class GridPanel extends JPanel{
	/**
	 * Serial ID
	 */
	private static final long serialVersionUID = 5878892347456014988L;
	
	@Override
	public void paintComponent(Graphics g){
		g.setColor(Main.config.getBackgroundColor());
		g.fillRect(0, 0, this.getWidth(), this.getHeight());
		g.setColor(Main.config.getForegroundColor());
		for(int i = SizeManager.cellSize; i < this.getWidth(); i += SizeManager.cellSize){
			g.drawLine(i, 0, i, this.getHeight());
			g.drawLine(i - 1, 0, i - 1, this.getHeight());
		}
		for(int i = SizeManager.cellSize; i < this.getHeight(); i += SizeManager.cellSize){
			g.drawLine(0, i, this.getWidth(), i);
			g.drawLine(0, i - 1, this.getWidth(), i -1);
		}
	}
}
