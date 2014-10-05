/**
 * Une boite de dialogue permettant d'éditer une liste de String (Ajout/Suppression d'éléments et réordonnancement). Les listes éditées seront les listes de moyen de paiement, de motifs de dépense et de crédit.
 */

/**
 * @author Etienne LAFARGE - Ecole Nationale Supérieure des Mines de Paris
 *
 */
package vue;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

public class DialogEditStringList extends JDialog implements ActionListener {
	private static final long serialVersionUID = 1440283208391179610L;
	
	///////////////////////////
	//////// ATTRIBUTS ////////
	///////////////////////////
	
	private Vector<String> liste;
	
	//Widgets
	JTextField tfNouveau;
	JList<String> lListe;
	JButton bAdd, bUp, bDown, bDel, bValider;
	
	///////////////////this.debit = d;this.l////////
	//////// METHODES /////////
	///////////////////////////

	public DialogEditStringList(JFrame conteneur, List<String> liste, String titre, boolean modal){
		super(conteneur, titre, modal);
		
		this.liste = new Vector<String>(liste); //On passe la liste en ArrayList, pour ce qu'on veut faire dessus c'est le plus pratique
		
		this.setSize(200, 300);
		this.setLocationRelativeTo(null);
		this.setResizable(false);
		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		this.build();
	}

	private void build() {
		this.getContentPane().removeAll();
		
		this.getContentPane().setLayout(new BorderLayout());
		
		JPanel pCenter = new JPanel();
		pCenter.setLayout(new BoxLayout(pCenter, BoxLayout.PAGE_AXIS));
		
		JPanel pTop = new JPanel();
		pTop.setLayout(new FlowLayout());
		
		JPanel pBottom = new JPanel();
		pBottom.setLayout(new FlowLayout());
		
		this.lListe = new JList<String>(this.liste);
		pTop.add(new JScrollPane(this.lListe));
		
		JPanel pBoutons = new JPanel();
		pBoutons.setLayout(new BoxLayout(pBoutons, BoxLayout.PAGE_AXIS));
		
		this.bUp = new JButton(">");
		this.bUp.addActionListener(this);
		pBoutons.add(this.bUp);
		
		this.bDown = new JButton("<");
		this.bDown.addActionListener(this);
		pBoutons.add(this.bDown);
		
		this.bDel = new JButton("x");
		this.bDel.addActionListener(this);
		pBoutons.add(this.bDel);
		
		pTop.add(pBoutons);
		
		pCenter.add(pTop);
		
		this.tfNouveau = new JTextField(12);
		pBottom.add(tfNouveau);
		
		this.bAdd = new JButton("+");
		this.bAdd.addActionListener(this);
		pBottom.add(this.bAdd);
		
		pCenter.add(pBottom);
		
		this.getContentPane().add(pCenter ,BorderLayout.CENTER);
		
		
		this.bValider = new JButton("Terminé");
		this.bValider.addActionListener(this);
		this.getContentPane().add(this.bValider, BorderLayout.SOUTH);
		
		this.setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == this.bDel){
			if(lListe.getSelectedIndex() != -1){
				liste.remove(lListe.getSelectedIndex());
				this.build();
			}
		}else if(e.getSource() == this.bUp){
			if(lListe.getSelectedIndex() != -1 && lListe.getSelectedIndex() > 0){
				int i = lListe.getSelectedIndex();
				String tmp = liste.get(i);
				liste.set(i, liste.get(i-1));
				liste.set(i-1, tmp);
				this.build();
				lListe.setSelectedIndex(i-1);
			}
		}else if(e.getSource() == this.bDown){
			if(lListe.getSelectedIndex() != -1 && lListe.getSelectedIndex() < liste.size() - 1){
				int i = lListe.getSelectedIndex();
				String tmp = liste.get(i);
				liste.set(i, liste.get(i+1));
				liste.set(i+1, tmp);
				this.build();
				lListe.setSelectedIndex(i+1);
			}
		}else if(e.getSource() == this.bAdd){
			if(!tfNouveau.getText().isEmpty()){
				liste.add(tfNouveau.getText());
				this.build();
				lListe.setSelectedIndex(liste.size() - 1);
				tfNouveau.setText("");
			}
		}else if(e.getSource() == this.bValider){
			this.setVisible(false);
		}
		
	}
	
	public List<String> resultat(){
		return this.liste;
	}
	
}
