/*
 * KeysPerSecond: An open source input statistics displayer.
 * Copyright (C) 2017  Roan Hofland (roan@roanh.dev).  All rights reserved.
 * GitHub Repository: https://github.com/RoanH/KeysPerSecond
 *
 * KeysPerSecond is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * KeysPerSecond is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package dev.roanh.kps.ui.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;

import dev.roanh.kps.CommandKeys;
import dev.roanh.kps.KeyInformation;
import dev.roanh.kps.Main;
import dev.roanh.util.Dialog;

/**
 * Logic for the key setup dialog.
 * @author Roan
 */
public class KeysDialog{

	/**
	 * Shows the key configuration dialog
	 */
	public static final void configureKeys(){
		List<KeyInformation> copy = new ArrayList<KeyInformation>(Main.config.keyinfo);
		boolean[] visibleState = new boolean[copy.size()];
		String[] nameState = new String[copy.size()];
		int autoIndex = KeyInformation.autoIndex;
		for(int i = 0; i < copy.size(); i++){
			visibleState[i] = copy.get(i).visible;
			nameState[i] = copy.get(i).name;
		}
		
		JPanel keyform = new JPanel(new BorderLayout());
		keyform.add(new JLabel("Currently added keys (you can edit these fields):"), BorderLayout.PAGE_START);
		JTable keys = new JTable();
		KeysModel model = new KeysModel();
		keys.setModel(model);
		keys.setDragEnabled(false);
		JScrollPane pane = new JScrollPane(keys);
		pane.setPreferredSize(new Dimension((int)keyform.getPreferredSize().getWidth() + 50, 120));
		keyform.add(pane, BorderLayout.CENTER);
		JButton newkey = new JButton("Add Key");
		newkey.addActionListener(e->showAddKeyDialog(model));
		JButton newmouse = new JButton("Add Mouse Button");
		newmouse.addActionListener(e->showAddMouseButtonDialog(model));
		JPanel nbuttons = new JPanel(new GridLayout(1, 2, 2, 0));
		JButton settenkeyless = new JButton("Tenkeyless key Setting");
		settenkeyless.addActionListener(e->setTenkeylessButtonDialog(model));
		JPanel sbutton = new JPanel(new GridLayout(2, 1));
		nbuttons.add(newkey, BorderLayout.LINE_START);
		nbuttons.add(newmouse, BorderLayout.LINE_END);
		sbutton.add(nbuttons, BorderLayout.PAGE_START);
		sbutton.add(settenkeyless, BorderLayout.PAGE_END);
		keyform.add(sbutton, BorderLayout.PAGE_END);
		
		if(!Dialog.showSaveDialog(keyform, true)){
			for(int i = 0; i < copy.size(); i++){
				copy.get(i).visible = visibleState[i];
				copy.get(i).setName(nameState[i]);
			}
			KeyInformation.autoIndex = autoIndex;
			Main.config.keyinfo = copy;
		}
	}
	
	/**
	 * Shows the dialog for adding an new mouse button.
	 * @param model The model for the table listing all the keys.
	 */
	private static void showAddMouseButtonDialog(KeysModel model){
		JPanel addform = new JPanel(new BorderLayout());
		addform.add(new JLabel("Select the mouse buttons to add:"), BorderLayout.PAGE_START);

		JPanel buttons = new JPanel(new GridLayout(5, 1, 2, 0));
		String[] names = new String[]{"M1", "M2", "M3", "M4", "M5"};
		JCheckBox[] boxes = new JCheckBox[]{
			new JCheckBox(names[0] + " (left click)"), 
			new JCheckBox(names[1] + " (right click)"), 
			new JCheckBox(names[2] + " (mouse wheel)"), 
			new JCheckBox(names[3]), 
			new JCheckBox(names[4])
		};

		for(JCheckBox box : boxes){
			buttons.add(box);
		}

		addform.add(buttons, BorderLayout.CENTER);

		if(Dialog.showSaveDialog(addform)){
			for(int i = 0; i < boxes.length; i++){
				if(boxes[i].isSelected()){
					KeyInformation key = new KeyInformation(names[i], -(i + 1), false, false, false, true);
					if(Main.config.keyinfo.contains(key)){
						KeyInformation.autoIndex -= 2;
						Dialog.showMessageDialog("The " + names[i] + " button was already added before.\nIt was not added again.");
					}else{
						Main.config.keyinfo.add(key);
					}
				}
			}
			model.fireTableDataChanged();
		}
	}
	
	/**
	 * Shows the dialog for adding an key.
	 * @param model The model for the table listing all the keys.
	 */
	private static void showAddKeyDialog(KeysModel model){
		JPanel form = new JPanel(new GridLayout(Main.config.enableModifiers ? 4 : 1, 1));
		JLabel txt = new JLabel("Press a key and click 'Save' to add it.");
		form.add(txt);
		JCheckBox ctrl = new JCheckBox();
		JCheckBox alt = new JCheckBox();
		JCheckBox shift = new JCheckBox();
		if(Main.config.enableModifiers){
			JPanel a = new JPanel(new BorderLayout());
			JPanel c = new JPanel(new BorderLayout());
			JPanel s = new JPanel(new BorderLayout());
			c.add(ctrl, BorderLayout.LINE_START);
			c.add(new JLabel("Ctrl"), BorderLayout.CENTER);
			a.add(alt, BorderLayout.LINE_START);
			a.add(new JLabel("Alt"), BorderLayout.CENTER);
			s.add(shift, BorderLayout.LINE_START);
			s.add(new JLabel("Shift"), BorderLayout.CENTER);
			form.add(c);
			form.add(a);
			form.add(s);
		}
		if(Dialog.showSaveDialog(form)){
			if(Main.lastevent == null){
				Dialog.showMessageDialog("No key pressed!");
				return;
			}
			KeyInformation info = new KeyInformation(NativeKeyEvent.getKeyText(Main.lastevent.getKeyCode()), Main.lastevent.getKeyCode(), (alt.isSelected() || CommandKeys.isAltDown) && Main.config.enableModifiers, (ctrl.isSelected() || CommandKeys.isCtrlDown) && Main.config.enableModifiers, (shift.isSelected() || CommandKeys.isShiftDown) && Main.config.enableModifiers, false);
			int n = (CommandKeys.hasAlt(info.keycode) ? 1 : 0) + (CommandKeys.hasCtrl(info.keycode) ? 1 : 0) + (CommandKeys.hasShift(info.keycode) ? 1 : 0);
			if(Dialog.showConfirmDialog("Add the " + info.getModifierString() + info.name.substring(n) + " key?")){
				if(Main.config.keyinfo.contains(info)){
					KeyInformation.autoIndex -= 2;
					Dialog.showMessageDialog("That key was already added before.\nIt was not added again.");
				}else{
					Main.config.keyinfo.add(info);
				}
			}
			model.fireTableDataChanged();
		}
	}
	
	/**
	 * Setting defautl Tenkeyless key setting
	 * @author Richsea
	 */
	private static void setTenkeylessButtonDialog(KeysModel model)
	{
		
	}
	
	/**
	 * Table model that displays all configured keys.
	 * @author Roan
	 */
	private static class KeysModel extends DefaultTableModel{
		/**
		 * Serial ID
		 */
		private static final long serialVersionUID = -5510962859479828507L;

		@Override
		public int getRowCount(){
			return Main.config.keyinfo.size();
		}

		@Override
		public int getColumnCount(){
			return 3;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex){
			switch(columnIndex){
			case 0:
				return Main.config.keyinfo.get(rowIndex).name;
			case 1:
				return Main.config.keyinfo.get(rowIndex).visible;
			case 2:
				return false;
			default:
				return null;
			}
		}

		@Override
		public String getColumnName(int col){
			switch(col){
			case 0:
				return "Key";
			case 1:
				return "Visible";
			case 2:
				return "Remove";
			default:
				return null;
			}
		}

		@Override
		public Class<?> getColumnClass(int columnIndex){
			if(columnIndex == 1 || columnIndex == 2){
				return Boolean.class;
			}
			return super.getColumnClass(columnIndex);
		}

		@Override
		public boolean isCellEditable(int row, int col){
			return true;
		}

		@Override
		public void setValueAt(Object value, int row, int col){
			switch(col){
			case 0:
				Main.config.keyinfo.get(row).setName((String)value);
				break;
			case 1:
				Main.config.keyinfo.get(row).visible = (boolean)value;
				break;
			case 2:
				if((boolean)value == true){
					Main.keys.remove(Main.config.keyinfo.get(row).keycode);
					Main.config.keyinfo.remove(row);
					this.fireTableDataChanged();
				}
				break;
			}
		}
	}
}
