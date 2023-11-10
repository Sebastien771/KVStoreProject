import org.scalatest.funsuite.AnyFunSuite
import java.time.LocalDateTime

class MemoryStoreTest extends AnyFunSuite {
  // Crée une nouvelle instance de MemoryStore, un KV-store en mémoire.
  val commitLogPath = "commit_log.json"
  val memtable = new MemoryMemtable()
  //val store = new MemoryStore(commitLogPath, memtable)
  val memstore = new MemoryStore(commitLogPath, memtable)

  test("MemoryStore.put should store a value by a key") {
    val key = "testKey"
    val record = Record(key, "testValue", LocalDateTime.now, deleted = false)
    memstore.put(key, record.value) match {
      case Right(_) => succeed
      case Left(error) => fail(s"put method failed with error: $error")
    }
    memstore.get(key) match {
      case Right(value) => assert(value == record.value)
      case Left(error) => fail(s"get method failed with error: $error")
    }
  }

  test("MemoryStore.delete should mark a value as deleted by a key") {
    val key = "testKey"
    val record = Record(key, "testValue", LocalDateTime.now, deleted = false)
    memstore.put(key, record.value)
    memstore.delete(key) match {
      case Right(_) => succeed
      case Left(error) => fail(s"delete method failed with error: $error")
    }
    memstore.get(key) match {
      case Right(_) => fail("Value was not deleted")
      case Left(StoreError.KeyNotFound(_)) => succeed // Assuming this is the expected error
      case Left(error) => fail(s"get method after delete failed with error: $error")
    }
  }

  test("MemoryStore.scan should retrieve all values") {
    // Assuming you have a method to clear the store before running this test
    // ...

    // Insert records into the store
    (1 to 5).foreach { i =>
      val key = s"key$i"
      val record = Record(key, s"value$i", LocalDateTime.now, deleted = false)
      memstore.put(key, record.value)
    }

    // Scan the store and convert to a Map
    val scanResult = memstore.scan() match {
      case Right(iterator) => iterator.map(record => record.key -> record).toMap
      case Left(error) => fail(s"scan method failed with error: $error")
    }

    // Assert that the map contains all the inserted records
    (1 to 5).foreach { i =>
      val key = s"key$i"
      assert(scanResult(key).value == s"value$i")
    }
  }

}
