import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.*;

public class Animation extends JPanel implements MouseInputListener, ActionListener{
  	private ArrayList<Particle> particles;
	private JFrame frame;
	Timer timer;
	Coord target;
	Coord targetSize;
	Random rand;
	double updateFreq = 60.0/1000.0; // frequency to update the animation (framerate)

	int numParticles;

	// initial values for window size
	int INITW = 500;
	int INITV = 600;


	// SUB-PANELS
	JPanel menuContain; // panel for menu component organization
	JPanel menuPanel; // panel for main components

	// COMPONENTS
	JButton b_pause; // button for pause/resume toggle
	JButton b_load; // button for showing open file dialog
	JButton b_save; // button for showing save file dialog
	JButton b_quit; // button for exiting the program
	JLabel l_numPart; // label for the number of particles
	JLabel l_variationF; // label for the force variation
	JLabel l_variationS; // label for the speed variation
	JLabel l_speed; // label for the speed setting
	JLabel l_force; // label for the force setting
	JLabel l_seek; // label for the seek setting
	JLabel l_wander; // label for the wander setting
	JLabel l_orbit; // label for the orbit setting
	JSpinner i_numPart; // spinner for setting number of particles
	JSlider s_variationF; // slider for setting force variation
	JSlider s_variationS; // slider for setting speed variation
	JSlider s_speed; // slider for the speed setting
	JSlider s_force; // slider for the speed setting
	JCheckBox c_seek; // check box for seek setting
	JCheckBox c_wander; // check box for wander setting
	JCheckBox c_orbit; // check box for orbit setting
	


	/**
	 * Constructor for the animation class
	 * @param numParticles the number of particles to animate
	 **/
	public Animation() {
	  	numParticles = 10;

		// container for the menu so we can place it at the top
		menuContain = new JPanel();
		menuContain.setLayout(new BorderLayout());

		// menu panel
		menuPanel = new JPanel();
		menuPanel.setLayout(new GridBagLayout());
		// common constraints
		GridBagConstraints con = new GridBagConstraints();
		con.insets = new Insets(5,5,5,5); // give the buttons some breathing room
		con.gridwidth = 2;
		Dimension bSize = new Dimension(90, 25);

		// make tooltips appear instantly
		ToolTipManager.sharedInstance().setInitialDelay(0);


		// BUTTON 1 - pause/resume
		b_pause = new JButton("Pause");
		con.gridx = 0;
		con.gridy = 0;
		b_pause.setPreferredSize(bSize);
		b_pause.addActionListener(new ActionListener() {
		 	public void actionPerformed(ActionEvent e) {
		 		pause();
				if (timer.isRunning()) {
					b_pause.setText("Pause");
				} else {
					b_pause.setText("Resume");
				}
			}
		});
		// add to panel
		menuPanel.add(b_pause, con);

		// BUTTON 2 - load a state from file
		b_load = new JButton("Load");
		con.gridx = 2;
		con.gridy = 2;
		b_load.setPreferredSize(bSize);
		b_load.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				boolean oldv = timer.isRunning();
				if (timer.isRunning()) {
					b_pause.doClick();
				}
				System.out.println("Loading file");//DEBUG
				if (!load() && oldv) {
					b_pause.doClick();
				}
				repaint();
			}
		});
		// add to panel
		menuPanel.add(b_load, con);

		// BUTTON 3 - save state to a file
		b_save = new JButton("Save");
		con.gridx = 0;
		con.gridy = 2;
		b_save.setPreferredSize(bSize);
		b_save.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				boolean oldv = timer.isRunning();
				if (timer.isRunning()) {	
					b_pause.doClick();
				}
				System.out.println("Saving file");//DEBUG
				if (!save() && oldv) {
					b_pause.doClick();
				}
			}
		});
		// add to panel
		menuPanel.add(b_save, con);

		// INPUT 1 - number of particles
		// label
		l_numPart = new JLabel("# particles");
		con.gridx = 0;
		con.gridy = 3;
		// spinner
		menuPanel.add(l_numPart, con);
		SpinnerModel model = new SpinnerNumberModel(numParticles, 1, 2000, 1);
		i_numPart = new JSpinner(model);
		con.gridx = 2;
		con.gridy = 3;
		i_numPart.setPreferredSize(bSize);
		i_numPart.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				int curVal = (Integer)i_numPart.getValue();
				while (particles.size() < curVal) {
					addParticle();
				}
				while (particles.size() > curVal) {
					particles.remove(particles.get(particles.size()-1));
				}
				repaint();
			}
		});
		// add to panel
		menuPanel.add(i_numPart, con);

		JPanel properties = new JPanel();
		properties.setLayout(new GridLayout(8,1));
		// SLIDER 1 - max force variation (imprecision)
		l_variationF = new JLabel("Force variation");
		s_variationF = new JSlider(0, 100);
		s_variationF.setToolTipText(String.valueOf(s_variationF.getValue()));
		s_variationF.setPreferredSize(new Dimension((int)bSize.getWidth()*2, (int)bSize.getHeight()));
		s_variationF.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				for (Particle p : particles) {
					p.impForce = s_variationF.getValue();
					p.setForceVariation();
				}
				s_variationF.setToolTipText(String.valueOf(s_variationF.getValue()));
//				System.out.println("ForceVar "+s_variationF.getValue());//DEBUG
			}		
		});
		properties.add(l_variationF, con);
		properties.add(s_variationF, con);
		// SLIDER 2 - max speed variation (imprecision)
		l_variationS = new JLabel("Speed variation");
		s_variationS = new JSlider(0, 100);
		s_variationS.setToolTipText(String.valueOf(s_variationS.getValue()));
		s_variationS.setPreferredSize(new Dimension((int)bSize.getWidth()*2, (int)bSize.getHeight()));
		s_variationS.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				for (Particle p : particles) {
					p.impSpeed = s_variationS.getValue();
					p.setSpeedVariation();
				}
				s_variationS.setToolTipText(String.valueOf(s_variationS.getValue()));
//				System.out.println("SpeedVar "+s_variationS.getValue());//DEBUG
			}		
		});
		properties.add(l_variationS, con);
		properties.add(s_variationS, con);
		// SLIDER 3 - max speed
		l_speed = new JLabel("Speed");
		s_speed = new JSlider(0, 300);
		s_speed.setToolTipText(String.valueOf(s_speed.getValue()));
		s_speed.setPreferredSize(new Dimension((int)bSize.getWidth()*2, (int)bSize.getHeight()));
		s_speed.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				for (Particle p : particles) {
					p.setInitSpeed(s_speed.getValue());
				}
				s_speed.setToolTipText(String.valueOf(s_speed.getValue()));
			}		
		});
		properties.add(l_speed, con);
		properties.add(s_speed, con);
		// SLIDER 4 - max force
		l_force = new JLabel("Force");
		s_force = new JSlider(0, 300);
		s_force.setToolTipText(String.valueOf(s_force.getValue()));
		s_force.setPreferredSize(new Dimension((int)bSize.getWidth()*2, (int)bSize.getHeight()));
		s_force.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				for (Particle p : particles) {
					p.setInitForce(s_force.getValue());
				}
				s_force.setToolTipText(String.valueOf(s_force.getValue()));
			}		
		});
		properties.add(l_force, con);
		properties.add(s_force, con);
		// adding stuff to panel
		con.gridx = 0;
		con.gridy = 5;
		con.gridwidth = 4;
		menuPanel.add(properties, con);
		
		// CHECKBOX 1 - wandering particles
		l_wander = new JLabel("Wander");
		con.gridx = 0;
		con.gridy = 6;
		menuPanel.add(l_wander, con);
		c_wander = new JCheckBox(null, null, false);
		c_wander.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (c_wander.isSelected()) {
					c_orbit.setSelected(false);
				}
				for (Particle p : particles) {
					p.wander = c_wander.isSelected();
				}
			}
		});
		// add to panel
		con.gridx = 2;
		con.gridy = 6;
		menuPanel.add(c_wander, con);
		
		// CHECKBOX 2 - orbiting particles
		l_orbit = new JLabel("Orbit");
		con.gridx = 0;
		con.gridy = 7;
		menuPanel.add(l_orbit, con);
		c_orbit = new JCheckBox(null, null, false);
		c_orbit.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (c_orbit.isSelected()) {
					c_wander.setSelected(false);
				}
				for (Particle p : particles) {
					p.orbit = c_orbit.isSelected();
				}
			}
		});
		// add to panel
		con.gridx = 2;
		con.gridy = 7;
		menuPanel.add(c_orbit, con);

		// CHECKBOX 3 - seek/flee
		l_seek = new JLabel("Seek");
		con.gridx = 0;
		con.gridy = 8;
		menuPanel.add(l_seek, con);
		c_seek = new JCheckBox(null, null, true);
		c_seek.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				for (Particle p : particles) {
					p.seek = c_seek.isSelected();
				}
			}		
		});
		// add to panel
		con.gridx = 2;
		con.gridy = 8;
		menuPanel.add(c_seek, con);

		// CHECKBOX 4 - show color panel?
		// add to panel

		// Color chooser


		// BUTTON quit
		JPanel menuGrid = new JPanel();
		menuGrid.setLayout(new GridBagLayout());
		JButton b_quit = new JButton("Quit");
		b_quit.setPreferredSize(bSize);
		b_quit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("Quitting...");//DEBUG
				System.exit(0);
			}
		});
		con.gridx = 0;
		con.gridy = 0;
		// add to panel
		menuGrid.add(b_quit, con);
		menuContain.add(menuGrid, BorderLayout.SOUTH);

		/*
		// BUTTON prefs
		JButton b_prefs = new JButton("Prefs");
		b_prefs.setPreferredSize(bSize);
		b_prefs.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("Editing preferences...");//DEBUG
			}
		});
		con.gridx = 2;
		con.gridy = 0;
		// add to panel
		menuGrid.add(b_prefs, con);
		*/

		// things for window
		frame = new JFrame("Particle Animation");
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(INITW, INITV); // set default size
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH); // make fullscreen
		
		frame.setLayout(new BorderLayout());
		frame.getContentPane().add(this);
		menuContain.add(menuPanel, BorderLayout.NORTH);
		frame.getContentPane().add(menuContain, BorderLayout.WEST);
		

		// objects in scene
		target = new Coord(this.getWidth()/2, this.getHeight()/2);
		targetSize = new Coord(20, 20);
		particles = new ArrayList<Particle>();


		// stuff to set particles to random position
		rand = new Random();

		// setting color for the particles
		Color colTemp = new Color(254,0,0);
		for (int i = 0; i < numParticles; i++) {
			addParticle();
		}

		// required stuff
		timer = new Timer(17, this);
		//timer.start();
		addMouseListener(this);
		addMouseMotionListener(this);
	}

	// updates when timer ticks
	public void actionPerformed(ActionEvent e) {
		for (Particle p : particles) {
			p.move(target);
			p.update(updateFreq);
			p.inBounds(this.getWidth()-1, this.getHeight()-1);
		}

		repaint();
	}

	// action when mouse is clicked
	public void mouseClicked(MouseEvent e) {
	  	// move target to click position
		target.set(e.getX(), e.getY());
	}

	/**
	 * Pauses the simulation by stopping the timer
	 **/
	public void pause() {
		if (timer.isRunning()) {
			timer.stop();
		} else {
			timer.start();
		}
	}

	/**
	 * Calls fileOpen with "save" configuration
	 * @return true if file saved, false if operation cancelled
	 **/
	public boolean save() {
	  	return fileOpen(false);
	}

	/**
	 * Cals fileOpen with "load" configuration
	 * @return true if file loaded, false if operation cancelled
	 **/
	public boolean load() {
	  	return fileOpen(true);
	}

	/**
	 * Handles opening a file and saving or loading information
	 * @param isLoad is this a load operation?
	 * @return true if loaded/saved, false if operation cancelled
	 **/
	public boolean fileOpen(boolean isLoad) {
	  	// default save directory
	  	File directory = new File("./saves/");
		// make sure it exists
	  	if (!directory.exists()) {
			directory.mkdir();
		}
		// start file chooser in default directory
		JFileChooser fc = new JFileChooser(directory);
		// filter only animation state files
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Animation states", "anim");
		fc.setFileFilter(filter);
		int returnVal = -1;
	  	if (isLoad) {
	  		returnVal = fc.showOpenDialog(null);
		} else {
			returnVal = fc.showSaveDialog(null);
		}
		File file = fc.getSelectedFile();
		// if operation is confirmed
		if (returnVal == 0) {
		  	// make sure the file extension is appended
			if (!file.getName().endsWith(".anim")) {
				file = new File(fc.getSelectedFile()+".anim");
			}
			// try to open file and operate
			try {
			  	if (isLoad) {
				  	// remove particles before loading
				  	particles.clear();
				  	Scanner in = new Scanner(file);
					// for the entire file
					while (in.hasNextLine()) {
						Particle p = new Particle();
						String line = in.nextLine();
						// check for header
						if (line.contains("PARTICLE")) {
							line = in.nextLine();
						}
						// load position
						String[] ls = line.split(" ");
						p.position = new Coord(Double.parseDouble(ls[0]), Double.parseDouble(ls[1]));

						// load force
						line = in.nextLine();
						ls = line.split(" ");
						p.force = new Coord(Double.parseDouble(ls[0]), Double.parseDouble(ls[1]));

						// load acceleration
						line = in.nextLine();
						ls = line.split(" ");
						p.acceleration = new Coord(Double.parseDouble(ls[0]), Double.parseDouble(ls[1]));

						// load velocity
						line = in.nextLine();
						ls = line.split(" ");
						p.velocity = new Coord(Double.parseDouble(ls[0]), Double.parseDouble(ls[1]));

						// load color
						line = in.nextLine();
						ls = line.split(" ");
						p.color = new Color(Integer.parseInt(ls[0]), 
							 Integer.parseInt(ls[1]), 
							 Integer.parseInt(ls[2]), 
							 Integer.parseInt(ls[3]));

						// load size
						p.size = Integer.parseInt(in.nextLine());

						// load maxForce
						p.maxForce = Double.parseDouble(in.nextLine());

						// load maxSpeed
						p.maxSpeed = Double.parseDouble(in.nextLine());

						// load impForce
						p.impForce = Double.parseDouble(in.nextLine());

						// load impSpeed
						p.impSpeed = Double.parseDouble(in.nextLine());

						// load initForce
						p.initForce = Double.parseDouble(in.nextLine());

						// load initSpeed
						p.initSpeed = Double.parseDouble(in.nextLine());

						// load movement behavior
						p.seek = Boolean.parseBoolean(in.nextLine());

						// load wander behavior
						p.wander = Boolean.parseBoolean(in.nextLine());

						// load orbit behavior
						p.orbit = Boolean.parseBoolean(in.nextLine());

						// add particle to arraylist
					  	particles.add(p);
					}
					System.out.println("Finished loading "+file);
					numParticles = particles.size();
					i_numPart.setValue(numParticles);
					Particle p = particles.get(0);
					s_variationF.setValue((int)p.impForce);
					s_variationS.setValue((int)p.impSpeed);
					s_force.setValue((int)p.initForce);
					s_speed.setValue((int)p.initSpeed);
					c_seek.setSelected(p.seek);
					c_wander.setSelected(p.wander);
					c_orbit.setSelected(p.orbit);
				} else {
					BufferedWriter br = new BufferedWriter(new FileWriter(file, false));
					// write all particle info to file
					for(Particle p : particles) {
					  	br.write(p.toString());
					}
					System.out.println("Finished saving "+file);
					br.close();
				}

			} catch(Exception e) {
				System.out.println("\tProblem accessing file "+e.getMessage());
				e.printStackTrace();
			}
			return true;
		}
		return false;
	}

	/**
	 * Adds a particle to the arraylist with proper parameters
	 **/
	public void addParticle() {
		Color colTemp = new Color(254,0,0);
		int limW = this.getWidth();
		int limH = this.getHeight();
		Particle npart = new Particle((int)(rand.nextDouble()*limW)+1, (int)(rand.nextDouble()*limH)+1);
		int r = colTemp.getRed();
		int g = colTemp.getGreen();
		int b = colTemp.getBlue();
		r = rand.nextInt(255);
		g = rand.nextInt(255);
		b = rand.nextInt(255);
		colTemp = new Color(r, g, b);
		npart.setColor(new Color(r, g, b, 200));
		npart.seek = c_seek.isSelected();
		npart.wander = c_wander.isSelected();
		npart.orbit = c_orbit.isSelected();
		npart.impForce = s_variationF.getValue();
		npart.impSpeed = s_variationS.getValue();
		npart.maxForce = s_force.getValue();
		npart.maxSpeed = s_speed.getValue();
		particles.add(npart);
	}

	// painting things
	public void paintComponent(Graphics og) {
	  	//Background
		og.setColor(Color.BLACK);
		og.fillRect(0,0,this.getWidth(),this.getHeight());

		//Target
		og.setColor(Color.WHITE);
		og.fillRect((int)(target.x - targetSize.x/2), (int)(target.y - targetSize.y/2), (int)targetSize.x, (int)targetSize.y);

		//Particles
		for (Particle p : particles) {
			og.setColor(p.color);
			og.fillOval((int)p.position.x-(p.size/2), (int)p.position.y-(p.size/2), p.size, p.size);
		}
	}

	// garbage I don't need
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mouseMoved(MouseEvent e) {}
	public void mousePressed(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}

	// update when mouse is held and moved
	public void mouseDragged(MouseEvent e) {
	  	target.set(e.getPoint().x, e.getPoint().y);
		repaint();
	}
	

	public static void main(String[] args){
	  	int numPart;
		Animation anim = new Animation();
	}
}
