// Import des classes nécessaires pour la gestion des fichiers et des chemins, ainsi que pour les opérations qui peuvent échouer.
import java.nio.file.{Files, Paths, StandardOpenOption}
import java.time.LocalDateTime
import scala.util.{Try, Using}
import ujson._ // ujson est une bibliothèque de JSON pour Scala.

// Déclaration de trait CommitLog qui définit l'interface pour les opérations de journalisation.
trait CommitLog {
  // Méthode pour ajouter un enregistrement au journal.
  def add(record: Record): Try[Unit]
  // Méthode pour lire tous les enregistrements du journal.
  def readAll(): Try[Seq[Record]]
}

// Implémentation de CommitLog qui stocke les enregistrements dans un fichier.
class FileCommitLog(filename: String) extends CommitLog{
  // Obtient le chemin du système de fichiers à partir du nom du fichier.
  private val path = Paths.get(filename)

  // Vérifie si le fichier existe et le crée s'il n'existe pas.
  if (Files.notExists(path)) then (Files.createFile(path))

  // Implémentation de la méthode 'add' qui ajoute un enregistrement au fichier journal.
  override def add(record: Record): Try[Unit] = Try {
    // Convertit l'enregistrement en objet JSON.
    val json = ujson.Obj(
      "key" -> record.key,
      "value" -> record.value,
      "timestamp" -> record.timestamp.toString,
      "deleted" -> record.deleted
    )
    // Convertit l'objet JSON en chaîne de caractères et ajoute un saut de ligne.
    val data = json.toString() + "\n"
    // Écrit la chaîne de caractères dans le fichier journal en mode d'ajout.
    Files.write(path, data.getBytes, StandardOpenOption.APPEND)
  }

  // Implémentation de la méthode 'readAll' qui lit tous les enregistrements du fichier journal.
  override def readAll(): Try[Seq[Record]] = Try {
    // Utilise un BufferedReader pour lire le fichier.
    Using(Files.newBufferedReader(path)) { reader =>
      // Crée un itérateur qui lit continuellement les lignes du fichier jusqu'à ce qu'il atteigne la fin.
      Iterator.continually(reader.readLine())
        .takeWhile(_ != null) // Continue jusqu'à ce qu'une ligne nulle soit rencontrée.
        .map { line =>         // Pour chaque ligne lue, effectue les opérations suivantes :
          // Parse la ligne comme JSON.
          val json = ujson.read(line)
          // Convertit l'objet JSON en un enregistrement Record.
          Record(
            key = json("key").str,
            value = json("value").str,
            timestamp = LocalDateTime.parse(json("timestamp").str),
            deleted = json("deleted").bool
          )
        }.toList // Convertit l'itérateur en liste.
    }.get // Extrait le résultat de l'opération 'Using' ou lance une exception si une erreur survient.
  }
}
