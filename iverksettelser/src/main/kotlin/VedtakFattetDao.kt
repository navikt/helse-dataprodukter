package no.nav.helse

import kotliquery.queryOf
import kotliquery.sessionOf
import org.intellij.lang.annotations.Language
import java.time.LocalDateTime
import java.util.UUID
import javax.sql.DataSource

class VedtakFattetDao(private val dataSource: DataSource) {
    internal fun finnVedtakFor(vedtaksperiodeId: UUID): Vedtak? {
        @Language("PostgreSQL")
        val query = "SELECT * FROM vedtak_fattet WHERE vedtaksperiode_id = ?"
        return sessionOf(dataSource, strict = true).use { session ->
            session.run(queryOf(query, vedtaksperiodeId).map {
                Vedtak(
                    vedtaksperiodeId = it.uuid("vedtaksperiode_id"),
                    hendelseId = it.uuid("hendelse_id"),
                    utbetalingId = it.uuidOrNull("utbetaling_id"),
                    korrelasjonsId = it.uuidOrNull("korrelasjon_id"),
                    fattetTidspunkt = it.localDateTime("fattet_tidspunkt")
                )
            }.asSingle)
        }
    }

    internal fun fjernVedtakFor(vedtaksperiodeId: UUID) {
        @Language("PostgreSQL")
        val query = "DELETE FROM vedtak_fattet WHERE vedtaksperiode_id = ?"
        sessionOf(dataSource).use { session ->
            session.run(queryOf(query, vedtaksperiodeId).asUpdate)
        }
    }

    internal fun lagre(hendelseId: UUID, vedtaksperiodeId: UUID, utbetalingId: UUID?, korrelasjonsId: UUID?, fattetTidspunkt: LocalDateTime) {
        @Language("PostgreSQL")
        val query =
            """
                INSERT INTO vedtak_fattet(vedtaksperiode_id, hendelse_id, utbetaling_id, korrelasjon_id, fattet_tidspunkt) 
                VALUES (:vedtaksperiodeId, :hendelseId, :utbetalingId, :korrelasjonsId, :fattetTidspunkt)
            """
        sessionOf(dataSource).use { session ->
            session.run(queryOf(query, mapOf(
                "hendelseId" to hendelseId,
                "vedtaksperiodeId" to vedtaksperiodeId,
                "utbetalingId" to utbetalingId,
                "korrelasjonsId" to korrelasjonsId,
                "fattetTidspunkt" to fattetTidspunkt,
            )).asExecute)
        }
    }
}
