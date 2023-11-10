import java.time.LocalDateTime
import scala.io.Source
import java.time.format.DateTimeFormatter
import scala.util.{Failure, Success, Using}
import Utils._


// Déclare un objet singleton QuickTest qui étend App, une façon simple de créer un programme exécutable en Scala.
object QuickTest extends App {



  private val idStore = new MemoryStore("idStoreCommitLog.json", new MemoryMemtable())
  private val clientTimestampStore = new MemoryStore("clientTimestampStoreCommitLog.json", new MemoryMemtable())
  private val productClientTimestampStore = new MemoryStore("productClientTimestampStoreCommitLog.json", new MemoryMemtable())

  private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")

  val inputStream = getClass.getResourceAsStream("/orders.csv")

  Using(Source.fromInputStream(inputStream)) { source =>
    for (line <- source.getLines().drop(1)) {


      val order = parseOrder(line)


      idStore.put(order.get.id, order.get.toString)


      val clientTimestampKey = s"${order.get.clientId}_${order.get.timestamp}"
      clientTimestampStore.put(clientTimestampKey, order.toString)


      val productClientTimestampKey = s"${order.get.product}_${order.get.clientId}_${order.get.timestamp}"
      productClientTimestampStore.put(productClientTimestampKey, order.toString)
    }
  } match {
    case Success(_) => println("Stores populated successfully")
    case Failure(e) => e.printStackTrace()
  }


}