// Déclare un objet singleton QuickTest qui étend App, une façon simple de créer un programme exécutable en Scala.
object QuickTest extends App {

  // Crée une nouvelle instance de MemoryStore, un KV-store en mémoire.
  val store = new MemoryStore()

  // Affiche un message indiquant qu'une opération de mise en place (put) va être effectuée.
  println("Ajout de key1 -> value1")
  // Tente d'ajouter une paire clé-valeur ("key1" -> "value1") au store et imprime le résultat.
  store.put("key1", "value1") match {
    case Right(_) => println("Ajout réussi.")
    case Left(error) => println(s"Erreur lors de l'ajout : $error")
  }

  // Répète la même opération pour une seconde paire clé-valeur.
  println("Ajout de key2 -> value2")
  store.put("key2", "value2") match {
    case Right(_) => println("Ajout réussi.")
    case Left(error) => println(s"Erreur lors de l'ajout : $error")
  }

  // Répète la même opération pour une troisième paire clé-valeur.
  println("Ajout de key3 -> value3")
  store.put("key3", "value3") match {
    case Right(_) => println("Ajout réussi.")
    case Left(error) => println(s"Erreur lors de l'ajout : $error")
  }

  // Affiche un message indiquant qu'une opération de scan va être effectuée.
  println("Scan de tous les éléments :")
  // Tente de scanner tous les éléments du store et imprime le résultat.
  store.scan() match {
    case Right(records) => records.foreach(record => println(s"${record.key} -> ${record.value}"))
    case Left(error) => println(s"Erreur lors du scan : $error")
  }

  // Définit une clé de départ pour la récupération et affiche un message correspondant.
  val startKey = "key2"
  println(s"Get from '$startKey':")
  // Tente de récupérer tous les enregistrements à partir d'une clé spécifique et imprime le résultat.
  store.getFrom(startKey) match {
    case Right(records) => records.foreach(record => println(s"${record.key} -> ${record.value}"))
    case Left(error) => println(s"Erreur lors de getFrom : $error")
  }

  // Définit un préfixe pour la récupération et affiche un message correspondant.
  val prefix = "key1"
  println(s"Get avec le préfixe '$prefix':")
  // Tente de récupérer tous les enregistrements qui commencent par un préfixe spécifique et imprime le résultat.
  store.getPrefix(prefix) match {
    case Right(records) => records.foreach(record => println(s"${record.key} -> ${record.value}"))
    case Left(error) => println(s"Erreur lors de getPrefix : $error")
  }

  // Affiche un message indiquant qu'une opération de suppression va être effectuée.
  println("Suppression de key1")
  // Tente de supprimer une paire clé-valeur et imprime le résultat.
  store.delete("key1") match {
    case Right(_) => println("Suppression réussie.")
    case Left(error) => println(s"Erreur lors de la suppression : $error")
  }

  // Tente de récupérer la valeur pour la clé supprimée et imprime le résultat.
  println(s"Récupération de key1 après suppression : ${store.get("key1")}")

  // Répète les opérations de suppression et de récupération pour une seconde et une troisième clé.
  println("Suppression de key2")
  store.delete("key2") match {
    case Right(_) => println("Suppression réussie.")
    case Left(error) => println(s"Erreur lors de la suppression : $error")
  }
  println(s"Récupération de key2 après suppression : ${store.get("key2")}")

  println("Suppression de key3")
  store.delete("key3") match {
    case Right(_) => println("Suppression réussie.")
    case Left(error) => println(s"Erreur lors de la suppression : $error")
  }
  println(s"Récupération de key3 après suppression : ${store.get("key3")}")
}