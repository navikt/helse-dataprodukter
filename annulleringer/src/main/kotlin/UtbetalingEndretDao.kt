package no.nav.helse

import java.time.LocalDateTime
import java.util.UUID
import javax.sql.DataSource

internal class UtbetalingEndretDao(private val dataSource: DataSource) {

    internal fun finnUtbetalingFor(korrelasjonsId: UUID): Utbetaling? {
        TODO()
    }

    internal fun opprettFor(korrelasjonsId: UUID, utbetalingId: UUID, utbetalingstype: Utbetalingstype, opprettet: LocalDateTime): Utbetaling {
        TODO()
    }

    fun markerAnnullertFor(korrelasjonsId: UUID) {
        TODO("Not yet implemented")
    }
}