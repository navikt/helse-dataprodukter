package no.nav.helse

import kotliquery.TransactionalSession
import kotliquery.queryOf
import kotliquery.sessionOf
import org.intellij.lang.annotations.Language
import java.time.LocalDateTime
import java.util.*
import javax.sql.DataSource

class FørstegangsbehandlingDao(private val dataSource: DataSource) {


        internal fun lagreSøknad(personRef: Long, søknad: Søknad, førstegangsbehandling: Boolean): Int = sessionOf(dataSource).use { session ->
            @Language("PostgreSQL")
            val statement = """ 
            INSERT INTO søknad (person_ref, hendelse_id, soknad_id, sykmelding_id, opprettet, fom, tom, arbeid_gjenopptatt, forstegangsbehandling) 
            VALUES (:person_ref, :hendelse_id, :soknad_id, :sykmelding_id, :opprettet, :fom, :tom, :arbeid_gjenopptatt, :forstegangsbehandling) 
            ON CONFLICT (hendelse_id) 
            DO 
            UPDATE SET forstegangsbehandling = søknad.forstegangsbehandling;
        """.trimMargin()
            queryOf(
                statement,
                mapOf(
                    "person_ref" to personRef,
                    "hendelse_id" to søknad.id,
                    "soknad_id" to søknad.søknadId,
                    "sykmelding_id" to søknad.sykmeldingId,
                    "opprettet" to søknad.opprettet,
                    "fom" to søknad.fom,
                    "tom" to søknad.tom,
                    "arbeid_gjenopptatt" to søknad.arbeidGjenopptatt,
                    "forstegangsbehandling" to førstegangsbehandling
                )
            ).asUpdate.runWithSession(session)
        }

        internal fun refFor(fnr: String, orgnummer: String): Long  = sessionOf(dataSource).use { session ->
            @Language("PostgreSQL")
            val statement = """ 
            SELECT id FROM person where fnr = :fnr AND organisasjonsnummer = :organisasjonsnummer;
        """.trimMargin()
            queryOf(
                statement,
                mapOf(
                    "fnr" to fnr,
                    "organisasjonsnummer" to orgnummer,
                )
            ).map { it.long("id") }
                .asSingle
                .runWithSession(session) ?: throw IllegalStateException("Fant ikke ref til fnr, orgnummer i person tabell")

        }

        internal fun lagrePerson(fnr: String, orgnummer: String): Long {
            oppdaterPersonTabell(fnr, orgnummer)
            return refFor(fnr, orgnummer)
        }

        private fun oppdaterPersonTabell(fnr: String, orgnummer: String) = sessionOf(dataSource).use { session ->
            @Language("PostgreSQL")
            val statement = """ 
            INSERT INTO person (fnr, organisasjonsnummer, opprettet) VALUES (:fnr, :organisasjonsnummer, :opprettet) ON CONFLICT DO NOTHING;
        """.trimMargin()
            queryOf(
                statement,
                mapOf(
                    "fnr" to fnr,
                    "organisasjonsnummer" to orgnummer,
                    "opprettet" to LocalDateTime.now()
                )
            ).asUpdate.runWithSession(session)
        }

    internal fun hentSøknader(personRef: Long)  = sessionOf(dataSource).use { session ->
        @Language("PostgreSQL")
        val statement = """ 
            SELECT * FROM søknad
            JOIN person ON person.id = person_ref
            WHERE person_ref = :person_ref
        """.trimMargin()
        queryOf(
            statement,
            mapOf(
                "person_ref" to personRef
            )
        ).map { Søknad(
            it.uuid("hendelse_id"),
            it.uuid("soknad_id"),
            it.uuid("sykmelding_id"),
            it.string("fnr"),
            it.string("organisasjonsnummer"),
            it.localDate("fom"),
            it.localDate("tom"),
            it.localDateOrNull("arbeid_gjenopptatt"),
            it.localDateTime("opprettet"))
        }.asList.runWithSession(session)
    }

    internal fun oppdaterSøknader(personRef: Long, updateMap: List<Pair<UUID, Boolean>>) = sessionOf(dataSource).use { session ->
        session.transaction { transaction ->
            updateMap.forEach { update -> transaction.oppdaterSøknader(personRef, update.first,  update.second) }
        }
    }

    private fun TransactionalSession.oppdaterSøknader(personRef: Long, hendelseId: UUID, førstegangsbehandling: Boolean)  {
        @Language("PostgreSQL")
        val statement = """ 
            UPDATE søknad
            SET forstegangsbehandling = :er_forstegangsbehandling
            WHERE søknad.person_ref = :person_ref AND hendelse_id = :hendelse_id
        """.trimMargin()
        queryOf(
            statement,
            mapOf(
                "person_ref" to personRef,
                "hendelse_id" to hendelseId,
                "er_forstegangsbehandling" to førstegangsbehandling
            )
        ).asUpdate.runWithSession(this)
    }
}


internal fun List<Pair<UUID, Boolean>>.førstegangsbehandlinger() =
    filter { it.second }.map { it.first }

internal fun List<Pair<UUID, Boolean>>.forlengelser() =
    filter { !it.second }.map { it.first }


fun List<UUID>.toSQLValues() = map { it.toString() }
        .reduceIndexed { i, acc, s ->
            if(i == 0 ) { s }
            else {
                "$acc, $s"
            }
}
