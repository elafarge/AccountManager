/**
 * Interface permettant à un Observable d'appeler des objets lorsqu'il a fini de notifier tous les observers. Cela peut être utile si l'un d'eux veut modifier la liste des Observers (en s'ajoutant/supprimant à/de la liste des observers par exemple) lors du update. En effet vu qu'on a un itérateur qui parcourt la liste des observers, toute modification va lancer une ConcurrentModificationException (ou quelquechose dans le genre. Il faut donc attendre la fin de la notification des Observers puis rappeler l'objet qui veut modifier la liste des Observers. C'est exactement ce qui se passe avec Utilisateurs qui réinitialise tous les objets qu'il observe à chaque modification (pour prendre en compte la disparition ou l'apparition d'un élément).
 */

/**
 * @author Etienne LAFARGE - Ecole Nationale Supérieure des Mines de Paris
 *
 */

package outils;

public interface NotifyOverObserver {
	public void onNotifyOver();
}
