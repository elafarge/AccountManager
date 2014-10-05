/**
 * JPanel permettant l'ajout d'un utilisateur
 */

/**
 * @author Etienne LAFARGE - Ecole Nationale Supérieure des Mines de Paris
 *
 */
package vue;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import modele.Utilisateur;
import modele.Utilisateurs;

public class VueAjouterUtilisateur extends JPanel {
	private static final long serialVersionUID = 4846201652136535566L;
	
	///////////////////////////
	//////// ATTRIBUTS ////////
	///////////////////////////
	
	private Utilisateurs utilisateurs;
	
	private JFrame conteneur;
	
	//Widgets
	private JLabel lAjouter;
	private JLabel lNotice;
	private JButton bAjouter;
	
	
	///////////////////////////
	//////// METHODES /////////
	///////////////////////////
	public VueAjouterUtilisateur(Utilisateurs utilisateurs, JFrame conteneur){
		super(); 
		
		this.setLayout(new GridLayout(3,1));
		
		this.utilisateurs = utilisateurs;
		this.conteneur = conteneur;
		
		this.lAjouter = new JLabel("Ajouter un nouvel utilisateur");
		this.add(lAjouter);
		
		String notice = "";
		FileReader fr;
		try {
			fr = new FileReader(new File("notice.html"));
			int i;
			while((i = fr.read()) != -1){
				notice += (char) i;
			}
			
		} catch (FileNotFoundException e) {
			notice = "Erreur, fichier de notice non trouvé.";
			e.printStackTrace();
		} catch (IOException e) {
			notice = "Erreur lors de la lecture du fichier \"notice.html\".";
			e.printStackTrace();
		}

		this.lNotice = new JLabel(notice);
		this.add(lNotice);
		
		bAjouter = new JButton("Nouvel utilisateur");
		this.add(bAjouter);
		bAjouter.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				@SuppressWarnings("unused")
				DialogAjouterUtilisateur d = new DialogAjouterUtilisateur();
			}
			
		});
		
	}
	
	///////////////////////////////////////////////////////
	//// La boite de dialogue d'ajout d'un utilisateur ////
	///////////////////////////////////////////////////////
	private class DialogAjouterUtilisateur extends JDialog{
		private static final long serialVersionUID = -7664578758834997918L;
		
		JLabel lNom;
		JTextField tfNom;
		JButton bValider;
		
		public DialogAjouterUtilisateur(){
			super(conteneur, "Ajouter un utilisateur", true);
			
			this.getContentPane().setLayout(new FlowLayout());
			
			lNom = new JLabel("Nom de l'utilisateur : ");
			this.getContentPane().add(lNom);
			
			tfNom = new JTextField(12);
			this.getContentPane().add(tfNom);
			
			bValider = new JButton("Ajouter");
			bValider.addActionListener(new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent e) {
					//On ajoute directement l'utilisateur (pas besoin de contrôleur ici, enfin on pourrais mais pour vérifier juste un champ de texte bon...)
					utilisateurs.add(new Utilisateur(tfNom.getText()));
					
					setVisible(false);
				}
				
			});
			this.getContentPane().add(bValider);
			
		    this.setSize(550, 70);
		    this.setLocationRelativeTo(null);
		    this.setResizable(false);
		    this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		    this.setVisible(true);
		}
	}
	
}
