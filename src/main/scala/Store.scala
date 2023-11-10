// Importation des classes nécessaires pour la manipulation des dates et des collections en Scala
import java.nio.file.{Files, Paths}
import java.time.LocalDateTime
import scala.collection.mutable.TreeMap
import scala.util.{Failure, Success, Try}



// Key et Value sont des chaînes de caractères
type Key = String
type Value = String

// Définition d'une classe de cas Record qui représente un enregistrement dans le store.
// Chaque enregistrement a une clé, une valeur, un timestamp et un marqueur pour indiquer s'il a été supprimé
case class Record(
                   key: Key, //clé de l'enregistrement
                   value: Value, // valeur associée à la clé
                   timestamp: LocalDateTime, // timestamp qui indique quand l'enregistrement a été créé ou modifié
                   deleted: Boolean // booléen qui indique si l'enregistrement à été supprimé
                 )

// Énumération StoreError définissant les erreurs possibles qui peuvent survenir dans le store
enum StoreError:
  case KeyNotFound(key: Key) // Une erreur indiquant qu'une clé donnée n'a pas été trouvée
  case KeyAlreadyDeleted(key: Key)
  case KeyDeleted(key: Key)
  case LogWriteError(message: String)
// Trait Store décrivant les opérations que doit fournir un store de clés-valeurs
trait Store:
  def get(key: Key): Either[StoreError, Value] // Récupère la valeur associée à une clé
  def put(key: Key, value: Value): Either[StoreError, Unit] // Ajoute ou met à jour une clé avec une nouvelle valeur
  def delete(key: Key): Either[StoreError, Unit] // Supprime (logiquement) une clé du store
  def scan(): Either[StoreError, Iterator[Record]] // Récupère un itérateur sur tous les enregistrements
  def getFrom(key: Key): Either[StoreError, Iterator[Record]] // Récupère un itérateur à partir d'une clé spécifique
  def getPrefix(prefix: String): Either[StoreError, Iterator[Record]] // Récupère un itérateur sur les enregistrements qui ont un préfixe commun


// Implémentation concrète de Store utilisant la mémoire vive comme support de stockage
class MemoryStore(commitLogPath: String, memtable: Memtable) extends Store:

  // Initialisation du store
  init()

  private def init(): Unit = {
    // Vérifier si le fichier de commit log existe
    val path = Paths.get(commitLogPath)
    if (Files.exists(path)) {
      // Charger les données du commit log dans la memtable
      loadCommitLogIntoMemtable()
    } else {
      // Créer un nouveau fichier de commit log
      Files.createFile(path)
      // Initialiser une nouvelle memtable si nécessaire
    }
  }

  private def loadCommitLogIntoMemtable(): Unit = {
    // Implémenter la logique de lecture du commit log et de chargement dans la memtable
    val commitLog = new FileCommitLog(commitLogPath)
    commitLog.readAll() match {
      case Success(records) =>
        records.foreach(record => {
          if (!record.deleted) {
            memtable.put(record.key, record.value)
          }
        })
      case Failure(exception) =>
        throw new IllegalStateException("Failed to load commit log", exception)
    }
  }

  private val commitLog = new FileCommitLog(commitLogPath)

  // Implémentation de la méthode get pour récupérer la valeur associée à une clé
  override def get(key: Key): Either[StoreError, Value] =
    memtable.get(key) // Renvoie la valeur ou une erreur si la clé n'existe pas

  // Implémentation de la méthode put pour ajouter ou mettre à jour une clé avec une valeur
  override def put(key: Key, value: Value): Either[StoreError, Unit] = Right {
    val record = Record(key, value, LocalDateTime.now, deleted = false) // Création d'un nouvel enregistrement
    commitLog.add(record) match {
      case Success(_) =>
        memtable.put(key, value)
        Right(())
      case Failure(key) =>
        Left(StoreError.KeyNotFound("la clé"+key+"est introuvable")) // You might need to add this case to StoreError
    }// Mise à jour du TreeMap avec la nouvelle clé et l'enregistrement
  }

  // Implémentation de la méthode delete pour marquer un enregistrement comme supprimé
  override def delete(key: Key): Either[StoreError, Unit] = {
    memtable.get(key) match {
      case Right(value) =>
        // If the key exists, create a deletion Record and attempt to add it to the commit log
        val deletionRecord = Record(key, null, LocalDateTime.now, deleted = true)
        Try(commitLog.add(deletionRecord)) match {
          case Success(_) =>
            // If the commit log is successfully updated, delete the key from the memtable
            memtable.delete(key) // Assuming delete here only needs the key
          case Failure(exception) =>
            // If there is a failure writing to the commit log, return an error
            Left(StoreError.LogWriteError(exception.getMessage))
        }
      case Left(StoreError.KeyNotFound(_)) =>
        // If the key is not found, return an error indicating it doesn't exist
        Left(StoreError.KeyNotFound(key))
      case Left(error) =>
        // If there is a different error from the memtable, return that error
        Left(error)
    }
  }

  // Implémentation de la méthode scan pour obtenir un itérateur sur tous les enregistrements
  override def scan(): Either[StoreError, Iterator[Record]] =
    memtable.scan() // Retourne un itérateur sur les valeurs du TreeMap

  // Implémentation de la méthode getFrom pour obtenir un itérateur à partir d'une clé spécifique
  override def getFrom(key: Key): Either[StoreError, Iterator[Record]] =
    memtable.getFrom(key) // Retourne un itérateur à partir de la clé spécifiée

  // Implémentation de la méthode getPrefix pour obtenir un itérateur sur les enregistrements avec un préfixe commun
  override def getPrefix(prefix: String): Either[StoreError, Iterator[Record]] =
    memtable.getPrefix(prefix) // Filtre les enregistrements par préfixe
