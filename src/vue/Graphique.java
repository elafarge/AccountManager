/**
 * Cette classe dessine un graphique basé sur les données qu'elle reçoit. C'est très probablement l'une des classes les plus bas niveau de ce programme, et très certainement aussi la plus intéressante à écrire. C'est elle qui a demandé le plus de réflexion, de dessin et de prise de têtes.
 * On dessine d'abord chaque "intervalle" (par exemple chaque mois) dans un JPanel spécifique puis on assemble le tout en mettant les JPanel côte à côte tout en essayant d'occupper le maxium d'espace en hauteur (la largeur du graphe est déterminé par le nombre d'intervalles * le nombre de postes à afficher) le tout étant afficher (au niveau de VueGraphe) dans un JScrollPane horizontal pour gérer les graphes "trop" larges
 */

/**
 * @author Etienne LAFARGE - Ecole Nationale Supérieure des Mines de Paris
 *
 */
package vue;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.JComponent;
import javax.swing.JPanel;

public class Graphique extends JPanel {
	private static final long serialVersionUID = -682146150985823586L;

	private static final int BARRE_WIDTH = 20; //Constante fixant la taille des barres de l'histogramme
	
	//La palette de couleurs pour l'histogramme (choisie avec minutie comme vous pourrez ne pas le remarquer)
	private static final Color[] colors = {new Color(153, 204, 204), new Color(178, 153, 204), 
		new Color(204, 153, 153), new Color(178, 204, 153), new Color(153, 153, 102), new Color(103, 153, 128), 
		new Color(103, 103, 153), new Color(153, 103, 128)};
	
	private TreeMap<Date, TreeMap<String, Double> > donnees;
	
	private double montantMaxi;
	private int nbPostes;
	private Set<String> postes;
	
	public Graphique(TreeMap<Date, TreeMap<String, Double> > donnees){
		this.donnees = donnees;
		this.setLayout(new FlowLayout());
		
		this.montantMaxi = 0;
		
		this.nbPostes = 0;
		
		Iterator<Entry<Date, TreeMap<String, Double>>> i = this.donnees.entrySet().iterator();
		while(i.hasNext()){
			Entry<Date, TreeMap<String, Double>> e = i.next();
			Iterator<Entry<String, Double>> j = e.getValue().entrySet().iterator();
			while(j.hasNext()){
				Double d = j.next().getValue();
				if(d.doubleValue() > this.montantMaxi)
					montantMaxi = d.doubleValue();
			}
			if(nbPostes==0){ //On compte le nombre de postes pour bien afficher la légende et on enregistre les noms des postes pour la même raison
				nbPostes = e.getValue().size();
				this.postes = e.getValue().keySet();
			}
		}
		
		//Détermination du montant maximum
		
		SimpleDateFormat f = new SimpleDateFormat("dd/MM/yyyy");
		
		i = this.donnees.entrySet().iterator();
		while(i.hasNext()){
			Entry<Date, TreeMap<String, Double>> e = i.next();
			this.add(new Element(f.format(e.getKey()), e.getValue()));
		}
		
	}
	
	@Override
	public void paintComponent(Graphics g1){
		
		super.paintComponent(g1);
		
		Graphics2D g = (Graphics2D) g1;
		
		//On dessine la légende
		int cursor = 20;
		Iterator<String> k = postes.iterator();
		int j = 0;
		while(k.hasNext()){
			g.setColor(colors[j%8]);
			g.fillRect(cursor, this.getHeight() - 28, 20, 15);
			g.setColor(Color.BLACK);
			g.drawRect(cursor, this.getHeight() - 28, 20, 15);
			cursor += 25;
			String s = k.next();
			g.drawString(s, cursor , this.getHeight() - 15);
			cursor += g.getFontMetrics().getStringBounds(s, g).getWidth() + 10;
			j++;
		}
	}
	
	/**
	 * Dessine un élément du graphique (pour un intervalle donné)
	 * @author Virginie FAURE - Ecole Nationale Supérieure des Mines de Paris
	 *
	 */
	private class Element extends JComponent{
		private static final long serialVersionUID = 3507736892093770391L;
		
		String date;
		TreeMap<String, Double> data;
		
		public Element(String d, TreeMap<String, Double> dt){
			this.date = d;
			this.data = dt;
			System.out.println(this.data);
		}
		
		@Override //NOTE PERSO : nos chaines de dates font, dans la police courante 71 px de largeur
		public void paintComponent(Graphics g1){
			Graphics2D g = (Graphics2D) g1; // Au cas ou l'envie nous prenne de faire des dégradés dans les histogrammes
			
			g.drawLine(5, this.getHeight() - 55, this.getWidth() - 5, this.getHeight() - 55); //La ligne de base
			
			g.drawString(this.date, this.getWidth()/2-35, this.getHeight() - 38); //La date en dessous
			
			g.setFont(g.getFont().deriveFont(Font.PLAIN, 8));
			
			//Allez c'est parti! On dessine les barres
			Iterator<Entry<String, Double>> i = this.data.entrySet().iterator();
			int j = 0;
			while(i.hasNext()){
				Entry<String, Double> e = i.next();
				g.setColor(colors[j%8]); 
				g.fillRect(10+j*BARRE_WIDTH, 25 + barHeight() -(int) Math.floor(e.getValue()/montantMaxi*this.barHeight()), BARRE_WIDTH, (int) Math.floor(e.getValue()/montantMaxi*this.barHeight()) );
				g.setColor(Color.BLACK);
				g.drawRect(10+j*BARRE_WIDTH, 25 + barHeight() -(int) Math.floor(e.getValue()/montantMaxi*this.barHeight()), BARRE_WIDTH, (int) Math.floor(e.getValue()/montantMaxi*this.barHeight()) );
				g.setColor(colors[j%8]); 
				String s = Integer.toString( (int) e.getValue().doubleValue());
				g.drawString( s, 20+(j)*BARRE_WIDTH - (int) g.getFontMetrics().getStringBounds(s, g).getWidth()/2 , 20 + barHeight() -(int) Math.floor(e.getValue()/montantMaxi*this.barHeight()));
				j++;
			} 
			
		}
		
		@Override
		public Dimension getPreferredSize(){
			return new Dimension(this.width(), 380);
		}
		
		@Override
		public Dimension getMaximumSize(){
			return new Dimension(this.width(), 380);
		}
		
		@Override
		public Dimension getMinimumSize(){
			return new Dimension(this.width(), 380);
		}
		
		public int width(){
			return Math.max(data.size()*BARRE_WIDTH + 20, 90); // la largueur des barres + le padding + 5px de marge entre la première barre horizontale et le début de la ligne du bas et idem avec la dernière barre soit un total de 10
		}
		
		public int barHeight(){
			return this.getHeight() - 80; //Les 50px correpondant à 25 de padding + la String du montant en haut et 25 en bas pour du padding et le texte AJOUT : + 30px en bas pour la légende :(
		}
		
	}
	
	
	
}
