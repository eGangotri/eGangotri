import org.junit.Test

/**
 * Created by user on 11/19/15.
 */
class StringSubstitutionTest {

    @Test
    void configureNotFoundInSpanish() {
        def stringSubstitution = new StringSubstitutionClient().createStringSubstitution( ["notFoundText":"extraviado"])
        println ">>>" + stringSubstitution.parse()
        assert stringSubstitution.parse().contains("extraviado")
    }


    @Test
    void configureDifferentPrefix1() {
        def stringSubstitution = new StringSubstitutionClient().createStringSubstitution( ["prefix1":"#",
                input: 'BienVednido #{name}. #{greeting} It\'s nice to meet you, #{name}. #{xyz}'
        ])
        println ">" + stringSubstitution.parse()
        assert stringSubstitution.parse().contains("Bob")
    }

}
