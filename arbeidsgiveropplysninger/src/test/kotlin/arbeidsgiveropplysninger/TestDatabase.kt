package no.nav.helse.arbeidsgiveropplysninger

import com.github.navikt.tbd_libs.test_support.CleanupStrategy
import com.github.navikt.tbd_libs.test_support.DatabaseContainers
import javax.sql.DataSource

val databaseContainer = DatabaseContainers.container("dataprodukt-arbeidsgiveropplysninger", CleanupStrategy.tables("arbeidsgiveropplysninger_korrigert, inntektsmelding_aktivitet, inntektsmelding_haandtert, inntektsmelding_registrert"), databasePoolSize = 1, walLevelLogical = true)

fun databaseTest(testblokk: (DataSource) -> Unit) {
    val testDataSource = databaseContainer.nyTilkobling()
    try {
        testblokk(testDataSource.ds)
    } finally {
        databaseContainer.droppTilkobling(testDataSource)
    }
}
