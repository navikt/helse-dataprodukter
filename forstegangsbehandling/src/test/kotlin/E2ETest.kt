import com.github.navikt.tbd_libs.rapids_and_rivers.test_support.TestRapid
import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.helse.FørstegangsbehandlingDao
import no.nav.helse.SøknadMediator
import no.nav.helse.januar
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.*
import javax.sql.DataSource

class E2ETest {

    @Test
    fun `Person ender opp i databasen`() = e2e {
        SøknadMediator(rapid, dao)
        rapid.sendTestMessage(testSøknad(fom = "2022-10-01", tom = "2022-10-31"))
        val result = dao.refFor("27845899830", "805824352")
        assertEquals(1L, result)
    }
    @Test
    fun `Førstegangsbehandling ender i databasen`() = e2e {
        SøknadMediator(rapid, dao)
        rapid.sendTestMessage(testSøknad(fom = "2022-10-01", tom = "2022-10-31"))
        val ref = dao.refFor("27845899830", "805824352")
        val søknader = dao.hentSøknader(ref)
        assertEquals(1, søknader.size)
    }

    @Test
    fun `Ignorerer lagrede søknader som har tom mindre enn fom`() = e2e {
        SøknadMediator(rapid, dao)
        val søknadIdForTomFørFom = UUID.randomUUID()
        rapid.sendTestMessage(testSøknad(søknadIdForTomFørFom, 31.januar(2023).toString(), 30.januar(2023).toString()))

        val søknadId = UUID.randomUUID()
        rapid.sendTestMessage(testSøknad(søknadId = søknadId, "2023-02-01", "2023-02-28"))

        //siste søknad teller som førstegangsbehandling fordi første søknad ikke er gyldig
        assertEquals(true, erFørstegangsbehandling(søknadId))
        assertEquals(false, erFørstegangsbehandling(søknadIdForTomFørFom))
    }

    @Test
    fun `Ignorerer lagrede søknader som har arbeidGjenopptatt mindre enn fom`() = e2e {
        SøknadMediator(rapid, dao)
        val søknadIdForArbeidGjenopptattFørFom = UUID.randomUUID()
        rapid.sendTestMessage(testSøknad(søknadIdForArbeidGjenopptattFørFom, 2.januar(2023).toString(), 31.januar(2023).toString(), 1.januar(2023)))

        val søknadId = UUID.randomUUID()
        rapid.sendTestMessage(testSøknad(søknadId = søknadId, "2023-02-01", "2023-02-28"))

        //siste søknad teller som førstegangsbehandling fordi første søknad ikke er gyldig
        assertEquals(false, erFørstegangsbehandling(søknadIdForArbeidGjenopptattFørFom))
        assertEquals(true, erFørstegangsbehandling(søknadId))
    }

    private fun e2e(testblokk: E2ETestContext.() -> Unit) {
        val rapid = TestRapid()
        databaseTest { ds ->
            testblokk(E2ETestContext(rapid, this, ds))
        }
    }

    data class E2ETestContext(
        val rapid: TestRapid,
        val dao: FørstegangsbehandlingDao,
        val dataSource: DataSource
    )

    fun E2ETestContext.erFørstegangsbehandling(søknadId: UUID): Boolean? {
        @Language("PostgreSQL")
        val query = "SELECT forstegangsbehandling FROM soknad WHERE soknad_id = ?"
        return sessionOf(dataSource).use {
            it.run(queryOf(query, søknadId).map { it.boolean("forstegangsbehandling") }.asSingle)
        }
    }

//    @Test
//    fun `Førstegangsbehandling endring registres i database`() {
//        SøknadMediator(rapid, dao)
//        val t1 = testSøknad("2022-10-01", "2022-10-31")
//        val t2 = testSøknad("2022-10-01", "2022-10-31")
//        val t3 = testSøknad("2022-10-01", "2022-10-31")
//
//        rapid.sendTestMessage(t1)
//        rapid.sendTestMessage(t2)
//        rapid.sendTestMessage(t3)
//
//    }

}

@Language("JSON")
private fun testSøknad(søknadId: UUID = UUID.randomUUID(), fom: String, tom: String, arbeidGjenopptatt: LocalDate? = null) = """
    {
      "id": "$søknadId",
      "type": "ARBEIDSTAKERE",
      "status": "SENDT",
      "fnr": "27845899830",
      "sykmeldingId": "b14ff260-ace6-4625-8fa7-860a6f54c706",
      "arbeidsgiver": {
        "navn": "Vegansk Slakteri",
        "orgnummer": "805824352"
      },
      "arbeidssituasjon": "ARBEIDSTAKER",
      "korrigerer": null,
      "korrigertAv": null,
      "soktUtenlandsopphold": null,
      "arbeidsgiverForskutterer": null,
      "fom": "$fom",
      "tom": "$tom",
      "dodsdato": null,
      "startSyketilfelle": "2022-08-01",
      "arbeidGjenopptatt": ${arbeidGjenopptatt?.let { """"$it"""" }},
      "sykmeldingSkrevet": "2022-10-01T02:00:00",
      "opprettet": "2022-12-09T14:55:17.106944",
      "opprinneligSendt": null,
      "sendtNav": "2022-12-09T14:56:50.945681886",
      "sendtArbeidsgiver": "2022-12-09T14:56:50.945681886",
      "egenmeldinger": [],
      "fravarForSykmeldingen": [],
      "papirsykmeldinger": [],
      "fravar": [],
      "andreInntektskilder": [],
      "soknadsperioder": [
        {
          "fom": "2022-10-01",
          "tom": "2022-10-31",
          "sykmeldingsgrad": 80,
          "faktiskGrad": null,
          "avtaltTimer": null,
          "faktiskTimer": null,
          "sykmeldingstype": "GRADERT",
          "grad": 80
        }
      ],
      "sporsmal": [],
      "avsendertype": "BRUKER",
      "ettersending": false,
      "mottaker": "ARBEIDSGIVER_OG_NAV",
      "egenmeldtSykmelding": false,
      "arbeidUtenforNorge": null,
      "harRedusertVenteperiode": null,
      "behandlingsdager": null,
      "permitteringer": [],
      "merknaderFraSykmelding": null,
      "merknader": null,
      "sendTilGosys": null,
      "@id": "${UUID.randomUUID()}",
      "@opprettet": "2022-12-09T14:56:50.945681886",
      "@event_name": "sendt_søknad_nav"
    }
""".trimIndent()
