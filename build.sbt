name := "KVStoreProject"

version := "0.1"

scalaVersion := "3.3.1" // ou la version que vous utilisez

// Dépendances de votre projet
libraryDependencies ++= Seq(
  "com.lihaoyi" %% "upickle" % "1.4.0", // pour le JSON avec uPickle
  "org.scala-lang.modules" %% "scala-parser-combinators" % "1.1.2", // si nécessaire pour le parsing
  // Ajoutez ici d'autres dépendances dont vous pourriez avoir besoin
  
)

// Options de compilation supplémentaires
scalacOptions ++= Seq(
  "-unchecked",
  "-deprecation",
  "-encoding", "utf8"
)

// (Optionnel) Options pour l'exécution des tests
testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-oD")

// (Optionnel) Ajouter des plugins sbt pour des fonctionnalités supplémentaires comme sbt-assembly pour le packaging
