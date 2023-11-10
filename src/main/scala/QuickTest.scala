import java.time.LocalDateTime
import scala.io.Source
import java.time.format.DateTimeFormatter
import scala.util.{Failure, Success, Using}

case class Order(
                  id: String, // L'identifiant unique de la commande
                  clientId: String, // L'identifiant du client qui a passé la commande
                  timestamp: LocalDateTime, // L'horodatage de quand la commande a été passée
                  product: String, // Le produit commandé
                  price: Double // Le prix de la commande
                )
// Déclare un objet singleton QuickTest qui étend App, une façon simple de créer un programme exécutable en Scala.
object QuickTest extends App {

  

  private val idStore = new MemoryStore("idStoreCommitLog.json", new MemoryMemtable())
  private val clientTimestampStore = new MemoryStore("clientTimestampStoreCommitLog.json", new MemoryMemtable())
  private val productClientTimestampStore = new MemoryStore("productClientTimestampStoreCommitLog.json", new MemoryMemtable())

  private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")

  val inputStream = getClass.getResourceAsStream("/orders.csv")
  
  Using(Source.fromInputStream(inputStream)) { source =>
    for (line <- source.getLines().drop(1)) { 
      val Array(id, client, timestampStr, product, priceStr) = line.split(",").map(_.trim)
      val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
      val dateTime = LocalDateTime.parse(timestampStr, formatter)
      //val timestamp = LocalDateTime.parse(timestampStr, dateTimeFormatter)
      val price = priceStr.toDouble

      val order = Order(id, client, dateTime, product, price)

      
      idStore.put(order.id, order.toString)

      
      val clientTimestampKey = s"${order.clientId}_${order.timestamp}"
      clientTimestampStore.put(clientTimestampKey, order.toString)

      
      val productClientTimestampKey = s"${order.product}_${order.clientId}_${order.timestamp}"
      productClientTimestampStore.put(productClientTimestampKey, order.toString)
    }
  } match {
    case Success(_) => println("Stores populated successfully")
    case Failure(e) => e.printStackTrace()
  }


}