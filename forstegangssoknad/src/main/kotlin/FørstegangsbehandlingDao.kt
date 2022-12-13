package no.nav.helse

import kotliquery.queryOf
import kotliquery.sessionOf
import org.intellij.lang.annotations.Language
import java.time.LocalDateTime
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
                    "soknad_id" to søknad.søkandsId,
                    "sykmelding_id" to søknad.sykemeldingId,
                    "opprettet" to søknad.opprettet,
                    "fom" to søknad.fom,
                    "tom" to søknad.tom,
                    "arbeid_gjenopptatt" to søknad.arbeidGjenopptatt,
                    "forstegangsbehandling" to førstegangsbehandling
                )
            ).asUpdate.runWithSession(session)
        }

        private fun refFor(fnr: String, orgnummer: String): Long?  = sessionOf(dataSource).use { session ->
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
                .runWithSession(session)

        }

        internal fun lagrePerson(fnr: String, orgnummer: String): Long {
            oppdaterPersonTabell(fnr, orgnummer)
            return refFor(fnr, orgnummer) ?: throw IllegalStateException("Fant ikke ref til fnr, orgnummer i person tabell")
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

    fun hentSøknader(personRef: Long, fnr: String, orgnummer: String)  = sessionOf(dataSource).use { session ->
        @Language("PostgreSQL")
        val statement = """ 
            SELECT * FROM søknad WHERE person_ref = :person_ref
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
            fnr,
            orgnummer,
            it.localDate("fom"),
            it.localDate("tom"),
            it.localDateOrNull("arbeid_gjenopptatt"),
            it.localDateTime("opprettet"))
        }.asList.runWithSession(session)
    }

}
