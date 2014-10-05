/**
 * JPanel décrivant un utilisateur (affichage de la liste de ses comptes sur la droite, solde, boutons débit et crédit et liste des comptes sur la gauche puis panel correspondant au compte sur la gauche).
 */

/**
 * @author Etienne LAFARGE - Ecole Nationale Supérieure des Mines de Paris
 *
 */
package vue;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import outils.Observer;

import modele.Compte;
import modele.Utilisateur;
import modele.Utilisateurs;

public class VueUtilisateur extends JPanel implements Observer {
	private static final long serialVersionUID = 5359232070066061840L;
	
	///////////////////////////
	//////// ATTRIBUTS ////////
	///////////////////////////
	
	private Utilisateurs utilisateurs;
	private Utilisateur utilisateur;
	
	private JFrame conteneur;
	
	//Widgets
	private JButton bSupprimer, bAjouterCompte, bSupprimerCompte;
	private JLabel lSolde, lComptes;
	private JPanel pListeComptes, pTotal, pCompte;
	private JList<String> liListeComptes;
	private VueCompte[] vCompte;
	private CardLayout compteSwapper;
	private JSplitPane pSplit;
	
	///////////////////////////
	//////// METHODES /////////
	///////////////////////////
	public VueUtilisateur(Utilisateur u, Utilisateurs us, JFrame cont){
		super();
		
		
		this.conteneur = cont;
		
		this.utilisateur = u;
		
		this.utilisateur.addObserver(this); //Un changement de nom d'utilisateur ? un ajout de compte ? on met tout le panel à jour (pour des changements plus minimes et surtout plus fréquents, on mettra a jour uniquement les composants concernés)
		
		this.utilisateurs = us;
		
		this.build();
		
		
	}

	private void build(){
		
		this.removeAll();
		
		this.setLayout(new BorderLayout());
		
		//Le panel du Top
		this.pTotal = new JPanel();
		this.pTotal.setLayout(new FlowLayout() );
		this.lSolde = new JLabel("Solde Total : " + new DecimalFormat("0.00").format(this.utilisateur.soldeTotal()) + "€");
		this.pTotal.add(lSolde);
		this.bSupprimer = new JButton("Supprimer utilisateur");
		this.pTotal.add(bSupprimer);
		this.bSupprimer.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) { 
				int option = JOptionPane.showConfirmDialog(null, 
				"Voulez-vous vraiment supprimer cet utilisateur (cette opération est irréversible, tous les comptes seront effacés) ?", 
				"Supprimer un utilisateur", 
				JOptionPane.YES_NO_OPTION, 
				JOptionPane.QUESTION_MESSAGE);
				
				if(option == JOptionPane.OK_OPTION){
					utilisateurs.remove(utilisateur);
				}
			}
		});
		this.add(pTotal, BorderLayout.NORTH);
		
		
		//Le panel de gauche (et au passage celui de droite tant qu'à faire!)
		this.pListeComptes = new JPanel();
		this.pListeComptes.setLayout(new BorderLayout() );
		
		this.pCompte = new JPanel();
		this.compteSwapper = new CardLayout();
		this.pCompte.setLayout(this.compteSwapper);
		
		
		this.lComptes = new JLabel("Comptes");
		this.pListeComptes.add(this.lComptes, BorderLayout.NORTH);
		
		Vector <String> sComptes = new Vector<String>();
		vCompte = new VueCompte[this.utilisateur.getComptes().size() + 1];
		
		sComptes.add("<Tous>");
		this.vCompte[0] = new VueCompte(this.utilisateur.getCompteGlobal(), this.conteneur, this.utilisateur, true);
		this.pCompte.add(vCompte[0], "0");
		
		Iterator<Compte> i = this.utilisateur.getComptes().listIterator();
		int j=1;
		while(i.hasNext()){
			Compte c = i.next();
			sComptes.add(c.getNom());
			this.vCompte[(j)] = new VueCompte(c, this.conteneur, this.utilisateur);
			this.pCompte.add(vCompte[j], Integer.toString(j++)); //Et vive la post-incrémentation pour gagner une ligne de code!
		}
		
		this.liListeComptes = new JList<String>(sComptes);
		this.pListeComptes.add(this.liListeComptes, BorderLayout.CENTER);
		this.liListeComptes.addListSelectionListener(new ListSelectionListener(){
			@Override
			public void valueChanged(ListSelectionEvent arg0) {
				int i = liListeComptes.getSelectedIndex();
				compteSwapper.show(pCompte, Integer.toString(i));
			}
		});
		this.liListeComptes.setSelectedIndex(0);
		
		JPanel pSouthWest = new JPanel();
		pSouthWest.setLayout(new GridLayout(2,1));
		
		this.bAjouterCompte = new JButton("Ajouter");
		pSouthWest.add(this.bAjouterCompte);
		this.bAjouterCompte.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				@SuppressWarnings("unused")
				DialogAjouterCompte d = new DialogAjouterCompte();
			}
		});

		this.bSupprimerCompte = new JButton("Supprimer");
		pSouthWest.add(this.bSupprimerCompte);
		this.bSupprimerCompte.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(liListeComptes.getSelectedIndex() > 0){
					int option = JOptionPane.showConfirmDialog(null, 
						"Voulez-vous vraiment supprimer le compte \""+utilisateur.getComptes().get(liListeComptes.getSelectedIndex() - 1).getNom()+"\" de  "+utilisateur.getNom()+"  (cette opération est irréversible) ?", 
						"Supprimer un compte", 
						JOptionPane.YES_NO_OPTION, 
						JOptionPane.QUESTION_MESSAGE);
						
					if(option == JOptionPane.OK_OPTION){
						utilisateur.supprimerCompte(utilisateur.getComptes().get(liListeComptes.getSelectedIndex() - 1));
					}else{
						//TODO : afficher un message d'erreur
					}
				}
			}
			
		});
		
		this.pListeComptes.add(pSouthWest, BorderLayout.SOUTH);
		
		this.pSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, this.pListeComptes, this.pCompte);
		this.add(pSplit, BorderLayout.CENTER);		
	}
	
	@Override
	public void update() {
		this.build();
		this.validate();
	}
	
	///////////////////////////////////////////////////////
	/////// La boite de dialogue d'ajout d'un compte //////
	///////////////////////////////////////////////////////
	private class DialogAjouterCompte extends JDialog{
		private static final long serialVersionUID = 1982686379926898823L;
			
		JLabel lNom, lSolde;
		JTextField tfNom;
		JFormattedTextField tfSolde;
		JButton bValider;
		
		public DialogAjouterCompte(){
			super(conteneur, "Ajouter un compte à l'utilisateur \"" + utilisateur.getNom() + "\" ", false);
			
			this.getContentPane().setLayout(new FlowLayout());
			
			lNom = new JLabel("Nom du compte : ");
			this.getContentPane().add(lNom);
			
			tfNom = new JTextField(12);
			this.getContentPane().add(tfNom);
			
			lSolde = new JLabel("   Solde initial : ");
			this.getContentPane().add(lSolde);
			
			tfSolde = new JFormattedTextField(NumberFormat.getNumberInstance());
			tfSolde.setColumns(8);
			this.getContentPane().add(tfSolde);	
			this.getContentPane().add(new JLabel("€   "));
			
			bValider = new JButton("Ajouter");
			bValider.addActionListener(new ActionListener(){
			
			@Override
			public void actionPerformed(ActionEvent e) {
					//On ajoute directement l'utilisateur (pas besoin de contrôleur ici, enfin on pourrais mais pour vérifier juste un champ de texte bon...)
					utilisateur.ajouterCompte(new Compte(tfNom.getText(), (!tfSolde.getText().equals("") && !tfSolde.getText().equals(" ")) ? Double.parseDouble(tfSolde.getText()) : 0.0 ));
					
					setVisible(false);
				}
			
			});
			this.getContentPane().add(bValider);
			
			this.setSize(700, 70);
			this.setLocationRelativeTo(null);
			this.setResizable(false);
			this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			this.setVisible(true);
		}
	}
	
}
