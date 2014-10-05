/**
 * Classe décrivant la fenêtre pincipale (on onglet par utilisateur, un bouton "+" pour ajouter un utilisateur et le Panel correspondant à l'utilisateur sélectionné si il existe ou un Panel d'Info sinon)
 */

/**
 * @author Etienne LAFARGE- Ecole Nationale Supérieure des Mines de Paris
 *
 */
package vue;

import java.util.Iterator;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import outils.Observer;

import modele.Utilisateur;
import modele.Utilisateurs;

public class FenetrePrincipale extends JFrame implements Observer {
	private static final long serialVersionUID = -1107523636858835792L;
	
	///////////////////////////
	//////// ATTRIBUTS ////////
	///////////////////////////
	private Utilisateurs usrs;
	
	//Widgets
	JTabbedPane onglets;
	
	
	///////////////////////////
	//////// METHODES /////////
	///////////////////////////
	public FenetrePrincipale(){
		super();
		
		this.usrs = new Utilisateurs();
		
		this.usrs.addObserver(this);
		
		this.build();
		
		this.setTitle("Suivi de Dépenses");
	    this.setSize(800, 600);
	    this.setLocationRelativeTo(null);
	    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);    
		this.setVisible(true);
	}
	
	/**
	 * Redessine l'interface graphique. Appelée à la construction et lors de l'ajout/suppression d'un utilisateur
	 */
	private void build(){
		//On ajoute les onglets correspondant à chaque utilisateur
		this.onglets = new JTabbedPane();
		
		Iterator<Utilisateur> i = this.usrs.getUtilisateurs().listIterator();
		while(i.hasNext()){
			Utilisateur user =  i.next();
			this.onglets.add(user.getNom(), new VueUtilisateur(user, this.usrs, this) );
		}
		
		//Puis celui pour ajouter un nouvel utilisateur
		this.onglets.add("", new VueAjouterUtilisateur(this.usrs, this));
		this.onglets.setIconAt(onglets.getTabCount() - 1, new ImageIcon("nouveau.png"));
		
		this.setContentPane(this.onglets);
	}
	
	/**
	 * Méthode appelée lors d'un ajout/suppression d'utilisateur
	 */
	@Override
	public void update() {
		this.build();
		this.validate();
	}
	
	///////////////////////////
	////////// MAIN ///////////
	///////////////////////////
	
	/**
	 * Le point d'entrée du programme
	 * @param args : non pris en compte ici
	 */
	public static void main(String[] args) {
		@SuppressWarnings("unused")
		FenetrePrincipale f = new FenetrePrincipale();
	}

}
