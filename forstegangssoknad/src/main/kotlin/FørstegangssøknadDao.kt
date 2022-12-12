package no.nav.helse

import kotliquery.queryOf
import kotliquery.sessionOf
import org.intellij.lang.annotations.Language
import java.time.LocalDateTime
import javax.sql.DataSource

class FørstegangssøknadDao(private val dataSource: DataSource) {


    fun lagre(søknad: Søknad): Long? {
        lagrePerson(søknad.fnr, søknad.orgnummer)
        val personOgOrgnummerRef = refFor(søknad.fnr, søknad.orgnummer)
//        lagreSøknad(søknad)
        return personOgOrgnummerRef
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
//
//    private fun lagreSøknad(søknad: Søknad) {
//        @Language("PostgreSQL")
//        val statement = """ INSERT INTO førstegangssøkand (person_ref, hendelse_id, søknad_id, fom, tom, arbeidGjenopptatt ) VALUES (:fnr, :orgnummer) ON CONFLICT DO NOTHING;"""
//        sessionOf(dataSource).run {
//            queryOf(
//                statement,
//                mapOf(
//                    "fnr" to fnr,
//                    "organisasjonsnummer" to orgnummer
//                )
//            )
//        }
//    }

    private fun lagrePerson(fnr: String, orgnummer: String) = sessionOf(dataSource).use { session ->
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

}