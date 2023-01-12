package no.nav.helse

import kotliquery.TransactionalSession
import kotliquery.queryOf
import kotliquery.sessionOf
import org.intellij.lang.annotations.Language
import java.lang.IllegalStateException
import java.time.LocalDateTime
import java.util.UUID
import javax.sql.DataSource

internal class UtbetalingEndretDao(private val dataSource: DataSource) {

    internal fun finnUtbetalingFor(korrelasjonsId: UUID): Utbetaling? {
        @Language("PostgreSQL")
        val query = "SELECT arbeidsgiver_fagsystemid, person_fagsystemid, opprettet FROM utbetaling WHERE korrelasjon_id = ?"
        return sessionOf(dataSource).use { session ->
            session.transaction { tx ->
                tx.run(queryOf(query, korrelasjonsId).map {
                    Utbetaling(
                        korrelasjonsId,
                        it.string("arbeidsgiver_fagsystemid"),
                        it.string("person_fagsystemid"),
                        it.localDateTime("opprettet"),
                        tx.finnVersjonerFor(korrelasjonsId)
                    )
                }.asSingle)
            }
        }
    }


    private fun TransactionalSession.finnVersjonerFor(korrelasjonsId: UUID): List<Utbetaling.Versjon> {
        @Language("PostgreSQL")
        val query = "SELECT utbetaling_id, type, opprettet FROM utbetalingsversjon WHERE utbetaling_ref = ?"
        return run(
            queryOf(query, korrelasjonsId).map {
                Utbetaling.Versjon(
                    it.uuid("utbetaling_id"),
                    enumValueOf(it.string("type")),
                    it.localDateTime("opprettet")
                )
            }.asList
        )
    }

    internal fun nyUtbetalingFor(
        korrelasjonsId: UUID,
        arbeidsgiverFagsystemId: String,
        personFagsystemId: String,
        opprettet: LocalDateTime
    ): Utbetaling {
        sessionOf(dataSource).use { session ->
            @Language("PostgreSQL")
            val query =
                "INSERT INTO utbetaling(korrelasjon_id, arbeidsgiver_fagsystemid, person_fagsystemid, opprettet) VALUES (?, ?, ?, ?)"
            val rowsAffected = session.run(
                queryOf(
                    query,
                    korrelasjonsId,
                    arbeidsgiverFagsystemId,
                    personFagsystemId,
                    opprettet
                ).asUpdate
            )
            if (rowsAffected <= 0) throw IllegalStateException("Kunne ikke lagre utbetaling med korrelasjonsId=$korrelasjonsId")
        }
        return Utbetaling(korrelasjonsId, arbeidsgiverFagsystemId, personFagsystemId, opprettet)
    }

    internal fun nyVersjonFor(
        korrelasjonsId: UUID,
        utbetalingId: UUID,
        utbetalingstype: Utbetalingstype,
        opprettet: LocalDateTime
    ) {
        sessionOf(dataSource).use { session ->
            @Language("PostgreSQL")
            val query =
                "INSERT INTO utbetalingsversjon(utbetaling_id, utbetaling_ref, type, opprettet) VALUES (?, ?, ?, ?)"
            val rowsAffected =
                session.run(queryOf(query, utbetalingId, korrelasjonsId, utbetalingstype.name, opprettet).asUpdate)
            if (rowsAffected <= 0) throw IllegalStateException("Kunne ikke lagre utbetalingsversjon med korrelasjonsId=$korrelasjonsId, utbetalingId=$utbetalingId")
        }
    }

    fun markerAnnullertFor(korrelasjonsId: UUID) {
        TODO("Not yet implemented")
    }
}