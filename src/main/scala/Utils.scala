//import des packages nécessaire pour la manipulation des dates et des exceptions
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.util.Try

//définition d'un objet scala, 'Utils' qui contiendra des fonctions utilitaires
object Utils {
  // Déclaration d'une classe de cas `Order` qui représente une commande avec des champs pour l'ID, le client, le timestamp, le produit, et le prix.
  case class Order(
                    id: String, // L'identifiant unique de la commande
                    clientId: String, // L'identifiant du client qui a passé la commande
                    timestamp: LocalDateTime, // L'horodatage de quand la commande a été passée
                    product: String, // Le produit commandé
                    price: Double // Le prix de la commande
                  )
  // Déclare un objet s
  // Fonction qui transforme une ligne de texte CSV en un objet Order.
  def line2order(line: String): Order = {
    val fields = line.split(",") // Divise la ligne en plusieurs champs en utilisant la virgule comme séparateur.
    Order(                        // Crée un nouvel objet Order avec les champs obtenus.
      id = fields(0),
      clientId = fields(1),
      timestamp = LocalDateTime.parse(fields(2)), // Convertit le champ de texte en un LocalDateTime.
      product = fields(3),
      price = fields(4).toDouble  // Convertit le champ de texte du prix en un Double.
    )
  }
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

  
}



// Assurez-vous que la classe Record est définie quelque part dans votre projet
// soit dans ce fichier, soit dans un fichier séparé qui est importé ici.
