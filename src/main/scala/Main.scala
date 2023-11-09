// Importation des bibliothèques nécessaires pour la manipulation de fichiers et la gestion des erreurs.
import scala.io.Source
import scala.util.{Using, Try, Success, Failure}
import java.time.LocalDateTime

// Déclaration d'une classe de cas `Order` qui représente une commande avec des champs pour l'ID, le client, le timestamp, le produit, et le prix.
case class Order(
                  id: String,                  // L'identifiant unique de la commande
                  clientId: String,            // L'identifiant du client qui a passé la commande
                  timestamp: LocalDateTime,    // L'horodatage de quand la commande a été passée
                  product: String,             // Le produit commandé
                  price: Double                // Le prix de la commande
                )

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

// Fonction qui crée et remplit trois stores différents à partir des données d'un fichier CSV et les retourne.
def createAndFillStores(filename: String): Try[(MemoryStore, MemoryStore, MemoryStore)] = Try {
  val stream = getClass.getResourceAsStream(filename) // Obtient le fichier CSV comme un flux d'entrée.
  require(stream != null, s"Le fichier '$filename' n'a pas été trouvé.") // Vérifie que le fichier existe.

  // Utilisation de la ressource de fichier et fermeture automatique à la fin du bloc.
  Using(Source.fromInputStream(stream)) { file =>
    val lines: Iterator[String] = file.getLines() // Lit les lignes du fichier CSV.
    val orders: Iterator[Order] = for (line <- lines.drop(1)) yield line2order(line) // Convertit chaque ligne en un objet Order, en ignorant la première ligne.

    // Crée trois instances de MemoryStore pour différents types de clés.
    val idStore = new MemoryStore
    val clientTimestampStore = new MemoryStore
    val productClientTimestampStore = new MemoryStore

    // Remplit chaque store avec des données en fonction de la clé choisie.
    orders.foreach { order =>
      idStore.put(order.id, order.product) // Stocke le produit en utilisant l'ID de la commande comme clé.
      clientTimestampStore.put(s"${order.clientId}_${order.timestamp}", order.product) // Utilise une combinaison de l'ID client et du timestamp comme clé.
      productClientTimestampStore.put(s"${order.product}_${order.clientId}_${order.timestamp}", order.product) // Utilise une combinaison du produit, de l'ID client et du timestamp comme clé.
    }

    // Retourne un tuple contenant les trois stores remplis.
    (idStore, clientTimestampStore, productClientTimestampStore)
  }.get // Récupère le résultat de l'utilisation ou lance une exception si une erreur survient.
}

// Fonction pour imprimer le contenu d'un MemoryStore sous forme de paires clé:valeur.
def printStoreContents(store: MemoryStore, storeName: String): Unit = {
  println(s"Contents of $storeName:") // Affiche le nom du store.
  // Effectue un scan du store et imprime chaque enregistrement ou une erreur en cas d'échec.
  store.scan() match {
    case Right(records) => records.foreach(record => println(s"${record.key}:${record.value}")) // Imprime chaque paire clé:valeur.
    case Left(error) => println(s"Error scanning $storeName: $error") // Imprime l'erreur de scan.
  }
}

// Fonction principale de l'application qui est exécutée au lancement.
@main
def order_analytics_run(): Unit = {
  val filename = "/orders.csv" // Définit le chemin du fichier CSV.
  // Crée et remplit les stores, puis imprime leur contenu si réussi, ou affiche une erreur si échec.
  createAndFillStores(filename) match {
    case Success((idStore, clientTimestampStore, productClientTimestampStore)) =>
      println("Les stores ont été créés avec succès.") // Message de réussite.
      // Imprime le contenu de chaque store.
      printStoreContents(idStore, "ID Store")
      printStoreContents(clientTimestampStore, "Client Timestamp Store")
      printStoreContents(productClientTimestampStore, "Product Client Timestamp Store")
    case Failure(e) =>
      println(s"Erreur lors de la création des stores: ${e.getMessage}") // Imprime le message d'erreur si la création a échoué.
  }
}