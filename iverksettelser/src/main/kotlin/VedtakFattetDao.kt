package no.nav.helse

import kotliquery.TransactionalSession
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
            session.transaction { tx ->
                tx.run(queryOf(query, vedtaksperiodeId).map {
                    Vedtak(
                        vedtaksperiodeId = it.uuid("vedtaksperiode_id"),
                        meldingId = it.uuid("melding_id"),
                        utbetalingId = it.uuidOrNull("utbetaling_id"),
                        korrelasjonsId = it.uuidOrNull("korrelasjon_id"),
                        fattetTidspunkt = it.localDateTime("fattet_tidspunkt"),
                        hendelser = tx.finnHendelserFor(vedtaksperiodeId)
                    )
                }.asSingle)
            }
        }
    }

    private fun TransactionalSession.finnHendelserFor(vedtaksperiodeId: UUID): Set<UUID> {
        @Language("PostgreSQL")
        val query = "SELECT hendelse_id FROM hendelse WHERE vedtaksperiode_id = ?"
        return run(queryOf(query, vedtaksperiodeId).map { it.uuid("hendelse_id") }.asList).toSet()
    }

    internal fun fjernVedtakFor(vedtaksperiodeId: UUID) {
        @Language("PostgreSQL")
        val query = "DELETE FROM vedtak_fattet WHERE vedtaksperiode_id = ?"
        sessionOf(dataSource).use { session ->
            session.transaction { tx ->
                tx.run(queryOf(query, vedtaksperiodeId).asUpdate)
                tx.fjernHendelserFor(vedtaksperiodeId)
            }
        }
    }

    private fun TransactionalSession.fjernHendelserFor(vedtaksperiodeId: UUID) {
        @Language("PostgreSQL")
        val query = "DELETE FROM hendelse WHERE vedtaksperiode_id = ?"
        run(queryOf(query, vedtaksperiodeId).asUpdate)
    }

    internal fun lagre(meldingId: UUID, vedtaksperiodeId: UUID, utbetalingId: UUID?, korrelasjonsId: UUID?, fattetTidspunkt: LocalDateTime, hendelser: Set<UUID>) {
        @Language("PostgreSQL")
        val query =
            """
                INSERT INTO vedtak_fattet(vedtaksperiode_id, melding_id, utbetaling_id, korrelasjon_id, fattet_tidspunkt) 
                VALUES (:vedtaksperiodeId, :meldingId, :utbetalingId, :korrelasjonsId, :fattetTidspunkt)
            """
        sessionOf(dataSource).use { session ->
            session.transaction { tx ->
                tx.run(
                    queryOf(
                        query,
                        mapOf(
                            "meldingId" to meldingId,
                            "vedtaksperiodeId" to vedtaksperiodeId,
                            "utbetalingId" to utbetalingId,
                            "korrelasjonsId" to korrelasjonsId,
                            "fattetTidspunkt" to fattetTidspunkt,
                        )
                    ).asExecute
                )
                hendelser.forEach {
                    tx.lagre(it, vedtaksperiodeId)
                }
            }

        }
    }

    private fun TransactionalSession.lagre(hendelseId: UUID, vedtaksperiodeId: UUID) {
        @Language("PostgreSQL")
        val query =
            "INSERT INTO hendelse(hendelse_id, vedtaksperiode_id) VALUES (:hendelseId, :vedtaksperiodeId) ON CONFLICT DO NOTHING"
        this.run(
            queryOf(
                query,
                mapOf(
                    "hendelseId" to hendelseId,
                    "vedtaksperiodeId" to vedtaksperiodeId
                )
            ).asUpdate
        )
    }
}
