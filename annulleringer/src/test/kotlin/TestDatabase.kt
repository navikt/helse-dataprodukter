import com.github.navikt.tbd_libs.test_support.DatabaseContainers
import javax.sql.DataSource

val databaseContainer = DatabaseContainers.container("dataprodukt-annulleringer", databasePoolSize = 1, walLevelLogical = true)

fun databaseTest(testblokk: (DataSource) -> Unit) {
    val testDataSource = databaseContainer.nyTilkobling()
    try {
        testblokk(testDataSource.ds)
    } finally {
        databaseContainer.droppTilkobling(testDataSource)
    }
}