//import des packages nécessaire pour la manipulation des dates et des exceptions
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.util.Try

//définition d'un objet scala, 'Utils' qui contiendra des fonctions utilitaires
object Utils {

  // Fonction parseOrder: Prend une ligne de texte CSV et tente de la parser en un objet Order.
  // La fonction renvoie un Try[Order], qui sera un Success[Order] si le parsing est réussi,
  // ou un Failure si une exception est levée pendant le processus.
  def parseOrder(line: String): Try[Order] = Try {
    val parts = line.split(',') //Découpage de la ligne csv en ses composants
    Order(
      id = parts(0), //id de la commande
      clientId = parts(1), //id du client
      timestamp = LocalDateTime.parse(parts(2), DateTimeFormatter.ISO_DATE_TIME), //Timestamp qui es parsé en un LocalDateTime
      product = parts(3), //Nom du produit
      price = parts(4).toDouble // Prix converti en double
    )
  }

  // Fonction orderToRecord: Convertit un objet Order en un objet Record.
  // Prend un objet Order et un indicateur booléen pour le statut 'deleted'.
  // Retourne un objet Record qui peut être utilisé pour peupler le Store.
  def orderToRecord(order: Order, deleted: Boolean = false): Record = {
    Record(
      key = order.id, //id de la commande
      value = order.product, // Nom du produit (peut être changé selon les besoin)
      timestamp = order.timestamp, // Date
      deleted = deleted // indique si l'enregistrement doit être marqué comme supprimé
    )
  }

  // Ajoutez ici d'autres fonctions utilitaires que vous pourriez avoir besoin
}



// Assurez-vous que la classe Record est définie quelque part dans votre projet
// soit dans ce fichier, soit dans un fichier séparé qui est importé ici.
