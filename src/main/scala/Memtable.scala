// Importation de TreeMap pour une collection ordonnée et mutable, et LocalDateTime pour le marquage temporel.
import scala.collection.mutable.TreeMap
import java.time.LocalDateTime

// Le trait Memtable définit l'interface pour un tableau en mémoire. Il doit être combiné avec des implémentations concrètes.
trait Memtable {
  // Méthode pour récupérer une valeur par clé, renvoyant une erreur si la clé n'est pas trouvée.
  def get(key: Key): Either[StoreError, Value]

  // Méthode pour placer une clé et une valeur dans le Memtable, renvoyant une erreur si l'opération échoue.
  def put(key: Key, value: Value): Either[StoreError, Unit]

  // Méthode pour supprimer une clé du Memtable, renvoyant une erreur si la clé n'existe pas.
  def delete(key: Key): Either[StoreError, Unit]

  // Méthode pour scanner tous les enregistrements dans le Memtable.
  def scan(): Either[StoreError, Iterator[Record]]

  // Méthode pour obtenir un itérateur pour tous les enregistrements à partir d'une clé donnée.
  def getFrom(key: Key): Either[StoreError, Iterator[Record]]

  // Méthode pour obtenir un itérateur pour tous les enregistrements qui commencent par un préfixe donné.
  def getPrefix(prefix: String): Either[StoreError, Iterator[Record]]
}

// Implémentation du trait Memtable en utilisant un TreeMap, qui maintient les clés triées.
class MemoryMemtable extends Memtable {
  // Définition d'un TreeMap pour stocker les enregistrements. Il est privé pour ne pas être accessible de l'extérieur.
  private val records = TreeMap.empty[Key, Record]

  // Implémentation de la méthode 'get' qui cherche une clé et retourne la valeur associée, ou une erreur si non trouvée ou marquée comme supprimée.
  override def get(key: Key): Either[StoreError, Value] =
    records.get(key).filterNot(_.deleted).map(_.value).toRight(StoreError.KeyNotFound(key))

  // Implémentation de la méthode 'put' qui ajoute ou met à jour un enregistrement avec la clé et la valeur spécifiées.
  override def put(key: Key, value: Value): Either[StoreError, Unit] = Right {
    records.update(key, Record(key, value, LocalDateTime.now, deleted = false))
  }

  // Implémentation de la méthode 'delete' qui marque un enregistrement comme supprimé.
  override def delete(key: Key): Either[StoreError, Unit] =
    records.get(key) match {
      case Some(record) =>
        records.update(key, record.copy(deleted = true))
        Right(())
      case None => Left(StoreError.KeyNotFound(key))
    }

  // Implémentation de la méthode 'scan' qui retourne un itérateur sur tous les enregistrements non supprimés.
  override def scan(): Either[StoreError, Iterator[Record]] =
    Right(records.valuesIterator.filterNot(_.deleted))

  // Implémentation de la méthode 'getFrom' qui retourne un itérateur commençant à la clé spécifiée, pour les enregistrements non supprimés.
  override def getFrom(key: Key): Either[StoreError, Iterator[Record]] =
    Right(records.from(key).valuesIterator.filterNot(_.deleted))

  // Implémentation de la méthode 'getPrefix' qui retourne un itérateur pour les enregistrements avec le préfixe spécifié, excluant ceux supprimés.
  override def getPrefix(prefix: String): Either[StoreError, Iterator[Record]] =
    Right(records.iterator.filter(_._1.startsWith(prefix)).map(_._2).filterNot(_.deleted))
}