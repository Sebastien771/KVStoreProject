// Importation des classes nécessaires pour la manipulation des dates et des collections en Scala
import java.time.LocalDateTime
import scala.collection.mutable.TreeMap

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

// Trait Store décrivant les opérations que doit fournir un store de clés-valeurs
trait Store:
  def get(key: Key): Either[StoreError, Value] // Récupère la valeur associée à une clé
  def put(key: Key, value: Value): Either[StoreError, Unit] // Ajoute ou met à jour une clé avec une nouvelle valeur
  def delete(key: Key): Either[StoreError, Unit] // Supprime (logiquement) une clé du store
  def scan(): Either[StoreError, Iterator[Record]] // Récupère un itérateur sur tous les enregistrements
  def getFrom(key: Key): Either[StoreError, Iterator[Record]] // Récupère un itérateur à partir d'une clé spécifique
  def getPrefix(prefix: String): Either[StoreError, Iterator[Record]] // Récupère un itérateur sur les enregistrements qui ont un préfixe commun


// Implémentation concrète de Store utilisant la mémoire vive comme support de stockage
class MemoryStore extends Store:
  private val data: TreeMap[Key, Record] = TreeMap.empty // Un TreeMap pour stocker les enregistrements. Les clés y sont triées.


  // Implémentation de la méthode get pour récupérer la valeur associée à une clé
  override def get(key: Key): Either[StoreError, Value] =
    data.get(key).toRight(StoreError.KeyNotFound(key)).map(_.value) // Renvoie la valeur ou une erreur si la clé n'existe pas

  // Implémentation de la méthode put pour ajouter ou mettre à jour une clé avec une valeur
  override def put(key: Key, value: Value): Either[StoreError, Unit] = Right {
    val record = Record(key, value, LocalDateTime.now, deleted = false) // Création d'un nouvel enregistrement
    data.update(key, record) // Mise à jour du TreeMap avec la nouvelle clé et l'enregistrement
  }

  // Implémentation de la méthode delete pour marquer un enregistrement comme supprimé
  override def delete(key: Key): Either[StoreError, Unit] =
    data.get(key) match
      case Some(record) => Right(data.update(key, record.copy(deleted = true))) // Marque l'enregistrement comme supprimé
      case None => Left(StoreError.KeyNotFound(key)) // Renvoie une erreur si la clé n'existe pas

  // Implémentation de la méthode scan pour obtenir un itérateur sur tous les enregistrements
  override def scan(): Either[StoreError, Iterator[Record]] =
    Right(data.values.iterator) // Retourne un itérateur sur les valeurs du TreeMap

  // Implémentation de la méthode getFrom pour obtenir un itérateur à partir d'une clé spécifique
  override def getFrom(key: Key): Either[StoreError, Iterator[Record]] =
    Right(data.from(key).values.iterator) // Retourne un itérateur à partir de la clé spécifiée

  // Implémentation de la méthode getPrefix pour obtenir un itérateur sur les enregistrements avec un préfixe commun
  override def getPrefix(prefix: String): Either[StoreError, Iterator[Record]] =
    Right(data.iterator.filter(_._1.startsWith(prefix)).map(_._2)) // Filtre les enregistrements par préfixe
